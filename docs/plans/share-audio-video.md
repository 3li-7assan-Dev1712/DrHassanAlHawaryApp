# Share Audio as Branded Video — Implementation Spec

**Feature:** From an audio lecture, generate a 60-second branded MP4 on-device, preview it, and share it to WhatsApp / Telegram / anything else. Temporary file, deleted after use.

**Repo:** `HassanAlHawary` · Kotlin · Compose · Hilt · Media3 1.8.0 · minSdk 24 / compileSdk 36 · Arabic-first RTL

---

## 0. Kickoff prompt for Claude Code

> Paste this into Claude Code in Android Studio. It points at this file for the detail.

```
Implement the "share audio as branded video" feature described in
docs/plans/share-audio-video.md. Read that file first, in full.

Before writing any code, read these existing files so you match the project's
conventions exactly — do not guess at names, colors, or patterns:

  core/core-ui/src/main/java/com/example/core/ui/theme/Color.kt
  core/core-ui/src/main/java/com/example/core/ui/theme/Type.kt
  core/core-ui/src/main/java/com/example/core/ui/theme/Font.kt
  core/core-ui/src/main/java/com/example/core/ui/navigation/Routes.kt
  feature/feature-audio/src/main/java/com/example/feature/audio/presentation/detail/AudioDetailScreen.kt
  feature/feature-audio/src/main/java/com/example/feature/audio/presentation/detail/AudioDetailViewModel.kt
  feature/feature-audio/src/main/java/com/example/feature/audio/domain/model/Audio.kt
  feature/feature-audio/src/main/java/com/example/feature/audio/player/AudioPlayerManagerImpl.kt
  core/core-player/src/main/java/com/example/core/player/PlaybackService.kt
  core/core-player/src/main/java/com/example/core/player/di/PlayerModule.kt
  app/src/main/java/com/example/hassanalhawary/HiltApplication.kt
  app/src/main/java/com/example/hassanalhawary/ui/navigation/  (the NavHost)
  app/src/main/AndroidManifest.xml
  app/src/main/res/xml/provider_paths.xml
  gradle/libs.versions.toml

Then work through the phases in the spec IN ORDER. After each phase:
  ./gradlew :feature:feature-share:assembleDebug   (and :app:assembleDebug at the end)
Do not start the next phase until the current one compiles.

Rules:
- Do not refactor anything outside the scope of this feature.
- Do not add any third-party dependency beyond the androidx.media3 artifacts
  listed in the spec. No FFmpeg.
- Every user-visible string goes in strings.xml (values-ar + values-en). The
  burned-in branding line is always Arabic regardless of app locale.
- Arabic text drawn onto Canvas must use StaticLayout with RTL text direction,
  never canvas.drawText.
- Stop and ask me if any assumption in the spec turns out not to match the code.
```

---

## 1. Decisions already made

| Question | Decision |
|---|---|
| Which 60 seconds | Trim window, **defaults to the current playback position**, user can drag it |
| Aspect ratio | **9:16, 1080×1920** |
| Motion | **Waveform synced to the real audio** (PCM amplitude envelope) |
| Wait handling | **Live Compose preview opens instantly; MP4 encodes in the background** |
| Duration | 60s default, **30s chip offered** (WhatsApp Status caps video at 30s) |
| Generation | 100% on-device, Media3 Transformer. No server, no FFmpeg |
| Lifetime | `cacheDir`, FileProvider, swept aggressively — see §7 |

---

## 2. Why Media3 Transformer

The project already ships Media3 1.8.0 for playback, so the transformer/effect
artifacts are a version-aligned, zero-risk addition. The alternatives are worse:

- **FFmpeg (ffmpeg-kit)** — retired by its maintainer in Jan 2025, adds 30–50 MB
  to the APK, and drags GPL/LGPL licensing questions into a published app. No.
- **Raw MediaCodec + MediaMuxer** — works, but you hand-write the GL surface
  pipeline, the AAC re-encode, and the A/V timestamp interleave. That's a week
  of work and a long tail of device-specific bugs that Transformer already fixed.

Transformer gives us image-to-video input, a GL overlay pipeline with per-frame
bitmaps, and audio muxing, in roughly 150 lines.

### Dependencies to add

`gradle/libs.versions.toml` (reuse the existing `media3 = "1.8.0"` version ref):

```toml
media3-transformer = { group = "androidx.media3", name = "media3-transformer", version.ref = "media3" }
media3-effect      = { group = "androidx.media3", name = "media3-effect",      version.ref = "media3" }
media3-common      = { group = "androidx.media3", name = "media3-common",      version.ref = "media3" }
media3-muxer       = { group = "androidx.media3", name = "media3-muxer",       version.ref = "media3" }
```

`ImmutableList` (Guava) arrives transitively with `media3-common`. If it doesn't
resolve, use `ImmutableList.of(...)` via an explicit `com.google.guava:guava`
entry rather than swapping in a Kotlin `listOf` — the overlay API wants Guava's type.

---

## 3. Module layout

Create **`:feature:feature-share`** — a new Android library module, registered in
`settings.gradle.kts`, namespace `com.example.feature.share`, following the exact
shape of `feature/feature-audio/build.gradle.kts` (minSdk 24, compileSdk 36,
JVM 11, hilt + ksp + compose plugins).

Why one module and not two: the engine and the screen are useless apart, and the
project already has 17 modules. `feature-video` / `feature-image` can depend on
this same module later — the engine takes a generic `ShareCardContent`, not an
`Audio`, precisely so that stays true.

```
feature/feature-share/src/main/java/com/example/feature/share/
├── di/ShareModule.kt
├── domain/
│   ├── ShareCardContent.kt        // title, subtitle, category, background source
│   ├── ShareClip.kt               // local file + startMs + durationMs
│   └── ShareExportState.kt        // Idle | Preparing | Encoding(progress) | Ready(uri) | Failed(reason)
├── engine/
│   ├── AudioClipExtractor.kt      // remote/local audio → small local clip file
│   ├── WaveformAnalyzer.kt        // clip file → FloatArray amplitude envelope
│   ├── ShareCardSpec.kt           // ★ single source of truth for the design
│   ├── ShareCardBitmapRenderer.kt // spec → 1080×1920 base bitmap (Canvas)
│   ├── WaveformOverlay.kt         // BitmapOverlay subclass, animated
│   ├── ShareVideoExporter.kt      // Transformer wrapper
│   └── ShareFileStore.kt          // cacheDir paths, FileProvider uris, sweeping
└── presentation/
    ├── SharePreviewScreen.kt
    ├── SharePreviewViewModel.kt
    ├── SharePreviewUiState.kt
    └── components/
        ├── ShareCardPreview.kt    // ★ Compose rendering of the SAME spec
        ├── TrimTimeline.kt
        └── ShareActionBar.kt
```

`feature/feature-audio/build.gradle.kts` gains
`implementation(project(":feature:feature-share"))`.

---

## 4. The pipeline

```
tap ⤳ share
   │
   ├─▶ navigate to SharePreview/{audioId}?startMs=<current position>
   │
   ├─▶ [instant]  ShareCardPreview renders in Compose + ExoPlayer starts the audio
   │              → user sees the finished design in < 300 ms
   │
   ├─▶ [~1.5 s]   AudioClipExtractor  : remote/downloaded audio → cacheDir/share/clip_<id>.m4a
   ├─▶ [~0.8 s]   WaveformAnalyzer    : clip → FloatArray(durationSec × 30) of RMS values
   ├─▶ [~0.1 s]   ShareCardBitmapRenderer : spec → base_<id>.png (1080×1920)
   └─▶ [8–18 s]   ShareVideoExporter  : Transformer → share_<id>.mp4
                                        Share button unlocks
```

Everything after the first step runs in a `viewModelScope` coroutine on
`Dispatchers.Default`, reporting into a `StateFlow<ShareExportState>`. Rotation
does not restart the export.

### 4.1 AudioClipExtractor

Goal: end up with a **small local file containing exactly the chosen window** —
never download a 40-minute lecture to share one minute of it.

1. If the audio already exists locally (check the app's download folder / ExoPlayer
   `SimpleCache` — read `PlayerModule.kt` to find out whether a cache is configured),
   use that file.
2. Otherwise use `MediaExtractor` + `MediaMuxer` directly against the remote URL:
   `setDataSource(url)` → `selectTrack(audioTrack)` → `seekTo(startUs, SEEK_TO_CLOSEST_SYNC)`
   → copy samples until `endUs` → write to `cacheDir/share/clip_<id>.m4a`.
   `MediaExtractor` issues HTTP range requests, so this pulls ~1 MB, not 40 MB.
   This is a **remux, not a re-encode** — it takes ~1 second.
3. If `MediaExtractor` cannot handle the URL (some CDNs, some codecs), fall back to
   an OkHttp download of the whole file with a determinate progress bar, then remux
   locally. Surface a distinct UI state for this path so the user knows why it's slow.

The clip file is then used for **three** things: the ExoPlayer preview, the waveform
analysis, and the export audio track. One artifact, perfect A/V agreement.

### 4.2 WaveformAnalyzer

`MediaExtractor` + `MediaCodec` decode the clip to PCM, accumulate RMS into
`durationSec × 30` buckets (one per output video frame), normalise to 0..1 with a
mild gamma (≈0.6) so quiet speech still moves the bars, and apply a 3-tap smoothing
pass so the animation doesn't strobe.

For a 60-second clip this is well under a second. Returns `FloatArray`.

**Fallback:** if decoding fails for any reason, return a deterministic synthetic
envelope (a seeded pseudo-random walk) and carry on. A slightly-off waveform is
infinitely better than a failed share.

### 4.3 ShareCardSpec — the anti-drift trick

The single most important structural decision in this feature.

The design exists twice: once as Compose (the live preview) and once as Canvas
(the burned-in frame). If those two drift, the user shares something that isn't
what they previewed — the worst possible bug for a sharing feature.

So both read from one immutable spec expressed in **normalised 0..1 coordinates**:

```kotlin
data class ShareCardSpec(
    val aspect: Float = 1080f / 1920f,
    val scrim: List<Pair<Float, Long>>,       // stop → ARGB
    val logo: Rect01,                          // top, centred
    val instituteLine: TextBlock01,            // "معهد الشيخ حسن الهواري الفقهي"
    val title: TextBlock01,                    // audio title, max 3 lines, auto-shrink
    val category: TextBlock01,
    val waveform: Rect01,                      // the animated strip
    val brandLine: TextBlock01,                // "تم الإنشاء بواسطة تطبيق الشيخ د. حسن الهواري"
    val accentRule: Rect01,
    val safeTop: Float = 0.09f,                // clear of status-bar UI
    val safeBottom: Float = 0.07f,             // clear of WhatsApp Status controls
)
```

`ShareCardBitmapRenderer` multiplies by 1080/1920. `ShareCardPreview` multiplies by
the Compose card size. Neither hardcodes a pixel. Colors and fonts come from
`core-ui`'s `Color.kt` / `Font.kt` — read them, don't invent a palette.

### 4.4 The design itself

Layered bottom-to-top on 1080×1920:

1. **Background** — `dr_hassan_image.png` from `core-ui`, centre-cropped to 9:16.
2. **Scrim** — vertical gradient, transparent at ~35% height down to ~85% opaque
   brand-dark at the bottom, plus a soft top scrim behind the logo. This is what
   makes the text readable over any photo; do not skip it.
3. **Top** — `institute_logo.jpg` clipped to a circle with a 3 px accent ring, and
   under it "معهد الشيخ حسن الهواري الفقهي" in Cairo SemiBold.
4. **Lower third** — the audio title in Cairo Bold, RTL, up to 3 lines, auto-shrinking
   from 62 sp down to 44 sp until it fits; below it the category in Cairo Regular at
   70% opacity.
5. **Waveform strip** — full-width, ~360 px tall, sitting above the brand line.
   ~48 rounded bars, mirrored around a centre line, brand accent colour, min height
   6 px so it never looks dead during silence.
6. **Brand line** — a 2 px accent rule, then a small app icon and
   **"تم الإنشاء بواسطة تطبيق الشيخ د. حسن الهواري"** in Cairo SemiBold, 34 sp,
   90% white. Anchored inside `safeBottom` so WhatsApp Status controls don't cover it.

**Arabic on Canvas:** use `StaticLayout.Builder` with
`setTextDirection(TextDirectionHeuristics.RTL)` and `setAlignment(ALIGN_CENTER)`,
and load the typeface with `ResourcesCompat.getFont(context, R.font.cairo_bold)`.
`canvas.drawText` will mangle multi-line Arabic shaping. This is non-negotiable.

### 4.5 ShareVideoExporter

```kotlin
val imageItem = MediaItem.Builder()
    .setUri(baseBitmapFileUri)          // the 1080×1920 PNG from 4.3
    .setImageDurationMs(clipDurationMs)
    .build()

val videoItem = EditedMediaItem.Builder(imageItem)
    .setFrameRate(30)
    .setEffects(
        Effects(
            /* audioProcessors = */ emptyList(),
            /* videoEffects   = */ listOf(
                Presentation.createForWidthAndHeight(
                    1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT
                ),
                OverlayEffect(ImmutableList.of(waveformOverlay)),
            ),
        )
    )
    .build()

val audioItem = EditedMediaItem.Builder(MediaItem.fromUri(clipFileUri)).build()

val composition = Composition.Builder(
    EditedMediaItemSequence.Builder(videoItem).build(),
    EditedMediaItemSequence.Builder(audioItem).build(),
).build()

Transformer.Builder(context)
    .setVideoMimeType(MimeTypes.VIDEO_H264)
    .setAudioMimeType(MimeTypes.AUDIO_AAC)
    .setEncoderFactory(
        DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder().setBitrate(2_500_000).build()
            )
            .build()
    )
    .addListener(listener)
    .build()
    .start(composition, outputPath)
```

**API drift warning.** Media3's editing APIs move between minor versions. In some
1.x releases the sequence factories are `EditedMediaItemSequence.withAudioAndVideoFrom(list)`
/ `.withAudioFrom(list)` rather than `Builder(...)`, and `Composition.Builder` may take
a `List<EditedMediaItemSequence>`. Use whichever form actually compiles against 1.8.0 in
this project — check the decompiled sources in Android Studio rather than trusting the
snippet above. The *shape* (one video sequence + one audio sequence → Composition) is stable.

**Progress:** poll `transformer.getProgress(progressHolder)` every 400 ms on the main
handler; that's the number the Share button shows. All `Transformer` calls must happen
on one thread — keep it on the main thread and hop to `Dispatchers.Default` only for
the extract/analyse/render steps.

**Bitrate note.** 1080×1920 at 2.5 Mbps for 60 s ≈ 19 MB. That's comfortably under
WhatsApp's attachment limit and well inside Telegram's. The content is near-static, so
2.5 Mbps looks pristine; going higher only makes the file harder to send.

### 4.6 WaveformOverlay

```kotlin
class WaveformOverlay(
    private val envelope: FloatArray,
    private val settings: StaticOverlaySettings,
    private val spec: ShareCardSpec,
) : BitmapOverlay() {

    private val buffers = Array(2) { Bitmap.createBitmap(W, H, ARGB_8888) }
    private var index = 0

    override fun getBitmap(presentationTimeUs: Long): Bitmap { … }
    override fun getOverlaySettings(presentationTimeUs: Long) = settings
}
```

Only the strip is redrawn — roughly 1080×360, not the full frame. Draw the bars for
`frame = presentationTimeUs * 30 / 1_000_000`, taking a window of the envelope centred
on that frame so the bars scroll rather than flicker.

**Buffer gotcha.** `BitmapOverlay` caches the uploaded GL texture and re-uploads only
when the `Bitmap` reference *or* its `generationId` changes. Mutating one bitmap does
bump `generationId`, so a single buffer *should* work — but double-buffering two
bitmaps (alternate on every call, as above) removes the question entirely and costs
one extra 1.5 MB allocation. Do it. If you ever see a frozen waveform in the exported
file, this is the first place to look.

Position the strip with `StaticOverlaySettings.Builder().setOverlayFrameAnchor(...)`
/ `.setBackgroundFrameAnchor(...)` so it lands exactly where `spec.waveform` says.

---

## 5. The preview screen

```
┌───────────────────────────────┐
│  ✕                            │   close
│                               │
│      ┌───────────────┐        │
│      │               │        │
│      │  9:16 card    │        │   ShareCardPreview — the live design,
│      │  rounded 24dp │        │   waveform driven by the ExoPlayer position
│      │               │        │
│      └───────────────┘        │
│         ▶  0:12 / 1:00        │   play/pause + scrubber for the clip
│                               │
│  ▁▂▅█▅▂▁▃▆█▇▄▂▁▂▄▆█▅▂▁▂▃▅▇▆▃  │   TrimTimeline: whole-track waveform,
│      └────[ window ]────┘     │   draggable 60s window
│      من 12:30 إلى 13:30        │
│                               │
│      ( 30 ثانية )  ( 60 ثانية )│   duration chips
│                               │
│  ┌─────────────────────────┐  │
│  │   مشاركة   ▸  (72%)     │  │   primary; shows progress until Ready
│  └─────────────────────────┘  │
└───────────────────────────────┘
```

Behaviour:

- Opens with `startMs` = current playback position, clamped so the window fits inside
  the track. If the track is shorter than the window, the clip is the whole track.
- The preview card animates from the moment the screen opens. **No spinner-first
  screen** — that's the entire point of the chosen approach.
- Moving the trim handle **cancels the in-flight export**, re-extracts, and restarts —
  debounced 1.5 s after the handle stops moving, so dragging doesn't thrash the encoder.
- The Share button is enabled but shows inline progress while encoding. Tapping it
  before the MP4 is ready sets a "share when ready" flag and fires the chooser the
  instant `onCompleted` lands. Never block the user's tap.
- On failure: an inline error card with a retry action, in Arabic, plus a
  "شارك الرابط بدلاً من ذلك" fallback that shares the plain audio link. A share feature
  should never dead-end.
- Enter/exit with the app's existing screen transitions (see `core-ui/animation`).

**Shared-element polish (optional, nice):** animate the audio artwork on the detail
screen into the preview card. Compose's shared-element transition API makes this cheap
and it makes the feature feel built-in rather than bolted on.

---

## 6. Sharing

```kotlin
val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

val intent = Intent(Intent.ACTION_SEND).apply {
    type = "video/mp4"
    putExtra(Intent.EXTRA_STREAM, uri)
    putExtra(Intent.EXTRA_TEXT, "$title\n$appStoreLink")
    clipData = ClipData.newUri(context.contentResolver, title, uri)   // needed by some targets
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_video_chooser)))
```

Note that WhatsApp ignores `EXTRA_TEXT` when a stream is attached. That's fine — the
branding is burned into the pixels, which is exactly why we're doing this.

**`provider_paths.xml` must be extended.** It currently only declares
`<files-path name="internal_files" path="." />`. Add:

```xml
<cache-path name="share_videos" path="share/" />
```

Without this the chooser opens and every target fails with a permission error.

---

## 7. File lifetime — do NOT do the naive thing

The obvious implementation — delete the MP4 in `onDispose` of the preview screen — is
**broken**, and it will look like it works on your device. After `startActivity`, the
receiving app reads the content URI *asynchronously*, often after our screen is already
gone. Delete too early and WhatsApp shows "file not found", intermittently, only on
slower phones.

The policy:

- Everything lives in `context.cacheDir/share/`.
- On leaving the preview: **cancel** the Transformer, and delete the intermediate
  artifacts (`clip_*.m4a`, `base_*.png`) immediately. Those are never handed out.
- The exported `share_*.mp4`: delete immediately **only if it was never shared**.
  If the chooser was opened, leave it.
- Sweep on entry: every time the preview screen opens, delete every file in
  `share/` older than 10 minutes.
- Sweep on launch: in `HiltApplication.onCreate`, on `Dispatchers.IO`, delete
  everything in `share/`. By then any handoff is long finished.

Net effect: at most one stale ~19 MB file exists, for at most one app session, in a
directory Android itself will evict under storage pressure. That satisfies "temporary,
deleted when the preview disappears" without the race.

---

## 8. Edge cases — handle every one

| Case | Behaviour |
|---|---|
| Audio shorter than the window | Clip = whole track. Never pad with silence |
| `startMs + window > duration` | Clamp `startMs` back so the window fits |
| Track shorter than ~5 s | Hide the share action entirely |
| No network and not cached | Error state with retry + "share the link instead" |
| `ExportException` `ERROR_CODE_ENCODING_FORMAT_UNSUPPORTED` | Retry once at 720×1280 / 2.0 Mbps before failing. Some API 24–26 devices can't do 1080×1920 |
| `cacheDir.usableSpace < 150 MB` | Refuse up front with a clear message, don't fail mid-encode |
| Rotation / config change | Export lives in the ViewModel; it continues |
| App backgrounded mid-export | Keep going. Cancel only on ViewModel clear |
| Back pressed mid-export | `transformer.cancel()`, clean up, no toast |
| Very long Arabic title | Auto-shrink to 3 lines, then ellipsize with `…` |
| Title mixed Arabic + English/Latin | `StaticLayout` bidi handles it; verify visually |
| Empty/null title | Fall back to the category name, then to the institute name |
| App locale = English | UI strings follow the locale; **the burned-in branding line stays Arabic** |
| Rapid double-tap on share | Single-flight guard in the ViewModel |
| No app can handle the intent | `resolveActivity` check, fall back to a plain-text share |

---

## 9. Performance targets

| Stage | Budget |
|---|---|
| Tap → preview visible | < 300 ms |
| Clip extraction (cached / remote) | < 0.3 s / < 2 s |
| Waveform analysis | < 0.8 s |
| Base bitmap render | < 150 ms |
| Export, 60 s @ 1080×1920, mid-range device | 8–18 s |
| Peak additional heap | < 60 MB |

Memory rules: never hold more than the two overlay buffers plus the base bitmap;
recycle the base bitmap after Transformer finishes; decode `dr_hassan_image.png`
with an `inSampleSize` that lands near 1080 px wide rather than loading it full-size.

---

## 10. Phases

Each phase must compile before the next begins.

**Phase 1 — Skeleton.** Module + Gradle + `settings.gradle.kts` + Hilt module +
route in `Routes.kt` + NavHost entry + share icon in `AudioDetailScreen` that
navigates with the current position. Screen is a stub. *Gate: tapping share opens
an empty screen and back works.*

**Phase 2 — Clip + envelope.** `AudioClipExtractor`, `WaveformAnalyzer`,
`ShareFileStore`. No UI yet — log the clip path, duration, and the first 20 envelope
values. *Gate: correct values for a cached track and a remote track.*

**Phase 3 — The design, once.** `ShareCardSpec` + `ShareCardPreview` (Compose) +
ExoPlayer playback of the clip + the trim timeline. *Gate: the preview screen looks
finished and plays, with no export at all.*

**Phase 4 — Burn it in.** `ShareCardBitmapRenderer` + `WaveformOverlay` +
`ShareVideoExporter`. *Gate: an MP4 lands in `cacheDir/share/` and, opened in a
video player, is visually identical to the Compose preview. Compare screenshots
side by side — this is the acceptance test for the whole feature.*

**Phase 5 — Share + lifetime.** FileProvider path, chooser intent, the §7 cleanup
policy, share-when-ready. *Gate: video arrives intact in WhatsApp and Telegram.*

**Phase 6 — Polish.** 30/60 chips, error + retry + link fallback, loading states,
Arabic strings in `values-ar`/`values-en`, accessibility labels, the low-res retry
path, and the optional shared-element transition.

**Phase 7 — QA.** §11.

---

## 11. QA checklist

- Devices: at least one Samsung, one Xiaomi, one Pixel; API 24, 29, 34, 36.
- Share into: WhatsApp chat, WhatsApp Status, Telegram chat, Telegram Stories,
  Gmail, Google Drive, Files, Nearby Share.
- Confirm the branding line is fully visible in WhatsApp Status (its controls
  overlay the bottom ~7%).
- Confirm the waveform actually moves *with the voice* — play the exported file
  next to the app.
- Rotate mid-export; background mid-export; back mid-export; kill the app mid-export.
- Drag the trim handle rapidly; confirm exactly one export survives.
- Airplane mode with a cached track (should work) and an uncached one (clean error).
- Fill the device to < 150 MB free and confirm the guard fires.
- Run once in English locale, confirm branding is still Arabic.
- Check the APK size delta (expect roughly +1–2 MB for the media3 artifacts).
- Confirm `cacheDir/share/` is empty after a fresh app launch.
