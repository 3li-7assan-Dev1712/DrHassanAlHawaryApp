# Pre-Release Audit — Share Audio/Article Feature (`app.netlify.devalihassan`, targeting versionCode 5 → next)

Scope: `feature/feature-share` (new module) and every file it touches (`app/proguard-rules.pro`, manifest, `provider_paths.xml`, `HiltApplication.kt`, `Routes.kt`, `MainActivity.kt` nav graph, `feature-audio`'s `AudioDetailScreen`, `feature-article`'s share-selection screen). All findings below are anchored to files I actually read and, where marked, commands I actually ran against this checkout.

---

## 1. Verdict

**Ship-with-fixes.** The two user-facing flows (share audio as video, share article excerpt as image) are well-built and mostly honour their own spec — the Arabic/RTL handling, the FileProvider wiring, and the file-lifetime policy are all implemented correctly and match `docs/plans/share-audio-video.md` closely. Separately, the three new Media3 artifacts (`media3-transformer`, `media3-effect`, `media3-muxer`) ship **no ProGuard consumer rules** in 1.8.0, and neither this module nor the app add any — that is exactly the kind of gap that survives `assembleRelease`/`bundleRelease` (both succeed) and a debug‑build smoke test, and only shows up when a real user's device hits the encoder path in the shrunk build. Either add defensive keep rules or verify the release‑minified build's export path on a device before you upload.

> **Update:** §2.2's `./gradlew build` failure and its underlying `MediaMuxer` flag bug are now fixed (see the note at the end of §2.2) — `./gradlew :app:lintRelease :feature:feature-share:lintRelease testDebugUnitTest` runs clean as of this update. §2.1 (missing Transformer/Effect/Muxer consumer rules) and §2.3 (unsigned artifact / signing confirmation) are unchanged and still need your attention before upload.

---

## 2. Blockers

### 2.1 `media3-transformer` / `media3-effect` / `media3-muxer` ship zero consumer ProGuard rules in 1.8.0 — nothing in this project compensates

**Files:** `feature/feature-share/consumer-rules.pro` (0 bytes), `feature/feature-share/proguard-rules.pro` (comment only, line 1), `app/proguard-rules.pro` (no `com.example.feature.share.**` or `androidx.media3.transformer/effect/muxer` entries anywhere in its 71 lines).

**What I checked:** I unzipped the actual AARs resolved by this build out of the Gradle cache and grepped for `proguard.txt`:

```
media3-exoplayer-1.8.0.aar  → contains proguard.txt (4805 bytes)
media3-transformer-1.8.0.aar → no proguard/consumer rules file
media3-effect-1.8.0.aar      → no proguard/consumer rules file
media3-muxer-1.8.0.aar       → no proguard/consumer rules file
```

`media3-exoplayer` (already used elsewhere in the app for normal playback) ships its own consumer rules, which is why existing playback is safe under R8. The three artifacts this release specifically adds do not, and this module's own `consumer-rules.pro` is empty.

**Why your debug test didn't catch it:** debug builds have `isMinifyEnabled = false`; nothing gets stripped or renamed. `:app:assembleRelease` and `:app:bundleRelease` both build successfully with the code as-is (I ran both — see §6), because R8 has no way to know it's breaking something reflection depends on; it just silently removes/renames what it thinks is dead code. If Transformer/Effect/Muxer reflectively resolve any internal class by name (their own factory/codec-selection machinery, which I could not fully trace without the AOSP source), that only breaks when you actually run the shrunk APK's export path on a device — never during a build, and never in a debug build.

**What I could not verify:** I cannot run the actual release‑minified APK's video-export flow from here, so I cannot confirm whether Transformer/Effect/Muxer actually break without rules, or whether their 1.8.0 internals happen to be reflection-free. This is a gap, not a proven crash.

**What to check / proposed fix:**
1. Install the already-built `app/build/outputs/apk/release/app-release-unsigned.apk` (or a locally re-signed copy) on a real device and run the full share-audio-as-video flow end to end — this is the one manual step that would settle this definitively.
2. As a defensive measure regardless, add to `feature/feature-share/consumer-rules.pro`:
   ```proguard
   -keep class androidx.media3.transformer.** { *; }
   -keep class androidx.media3.effect.** { *; }
   -keep class androidx.media3.muxer.** { *; }
   ```
   This is broader than ideal (it will keep more than strictly necessary), but it's the safe interim move until you've confirmed the minimal set that's actually required.

### 2.2 `./gradlew build` fails today — `feature-share:lintRelease` errors, and one of the errors is a real bug

**Command run:** `./gradlew :app:lintRelease :feature:feature-share:lintRelease :app:processReleaseManifest testDebugUnitTest` → `BUILD FAILED in 2m 59s`, `Execution failed for task ':feature:feature-share:lintRelease'. Lint found errors in the project; aborting build.` Full report: `feature/feature-share/build/reports/lint-results-release.html` (20 errors, 7 warnings).

Your CI workflow (`.github/workflows/android-ci.yml:36`) runs exactly `./gradlew build`, which invokes every module's `lintRelease` as part of `check`. This branch will fail CI the moment it's pushed/PR'd, unless CI is somehow configured to ignore it (I found no `lint { abortOnError = false }` anywhere in the repo). Note: `:app:lintRelease` run **standalone** succeeds with zero findings — the app module's own code/manifest is clean; the failure is entirely scoped to the new `feature-share` module, and a full multi-module build/CI run is what surfaces it.

**The one real bug among the 20 errors** — `feature/feature-share/src/main/java/com/example/feature/share/engine/AudioClipExtractor.kt:149`:
```kotlin
bufferInfo.set(0, sampleSize, relativeTimeUs, extractor.sampleFlags)
```
`MediaExtractor.sampleFlags` returns `MediaExtractor.SAMPLE_FLAG_*` constants (`SAMPLE_FLAG_SYNC = 1`, `SAMPLE_FLAG_ENCRYPTED = 2`, `SAMPLE_FLAG_PARTIAL_FRAME = 4`). `MediaCodec.BufferInfo.set()`'s `flags` parameter expects `MediaCodec.BUFFER_FLAG_*` constants (`BUFFER_FLAG_KEY_FRAME = 1`, `BUFFER_FLAG_CODEC_CONFIG = 2`, `BUFFER_FLAG_END_OF_STREAM = 4`, `BUFFER_FLAG_PARTIAL_FRAME = 8`, `BUFFER_FLAG_DECODE_ONLY = 32`). These are two different namespaces that Lint's `WrongConstant` check correctly flags as incompatible.

**Concrete failure scenario:** bit value `1` (sync/keyframe) happens to line up in both namespaces, so that case is harmless by coincidence. But if `MediaExtractor` ever reports `SAMPLE_FLAG_PARTIAL_FRAME` (value `4`) for a sample — which is exactly the kind of thing that can happen right at a seek boundary, and this feature deliberately seeks to an arbitrary mid-track `startMs` for every clip — `MediaMuxer.writeSampleData` receives that sample tagged as `BUFFER_FLAG_END_OF_STREAM` (also value `4`) instead. Depending on how `MediaMuxer` treats an EOS flag mid-stream, this can truncate the muxed `.m4a` clip early or write a malformed sample table, producing a shortened/corrupt AAC clip — silently, with no exception thrown, only surfacing as "the shared video's audio cuts off early" for whatever user happens to trim a clip that lands on a partial-frame boundary. This is not reachable from a normal manual test unless you happen to trim at exactly the wrong spot.

**Fix:** derive the muxer flag explicitly instead of passing the extractor's raw flags through:
```kotlin
val muxerFlags = if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
    MediaCodec.BUFFER_FLAG_KEY_FRAME
} else 0
bufferInfo.set(0, sampleSize, relativeTimeUs, muxerFlags)
```

**The other 19 errors** are all `UnsafeOptInUsageError` (Lint's dedicated check for AndroidX's `@RequiresOptIn`-style annotations — note this is *not* enforced by the Kotlin compiler, which is why `compileReleaseKotlin`/`compileDebugKotlin` both succeed while lint fails):
- `SharePreviewScreen.kt:61,95,96,97,98,99` — the composable references `SharePreviewViewModel` (marked `@UnstableApi` at `SharePreviewViewModel.kt:45`) and its methods without `@OptIn(UnstableApi::class)` on the composable itself.
- `WaveformOverlay.kt:29,29,40,40,41,42,46,47,49,64,64,85,86` — the whole class extends Media3-effect's `@UnstableApi`-marked `BitmapOverlay` without opting in at the class declaration (contrast with `ShareVideoExporter.kt:44`, which correctly has `@UnstableApi` on its class).

Fix: add `@OptIn(androidx.media3.common.util.UnstableApi::class)` to the `SharePreviewScreen` composable and to the `WaveformOverlay` class declaration.

**7 warnings** (not blocking, listed for completeness): `ShareCardPreview.kt:215` modifier factory should be a `Modifier` extension; `GenerationOverlay.kt:43` modifier param placement; `ShareFileStore.kt:70` suggests `StorageManager#getAllocatableBytes` over `cacheDir.usableSpace`; `CardDrawing.kt:126`, `ShareCardBitmapRenderer.kt:27`, `TextCardBitmapRenderer.kt:27`, `WaveformOverlay.kt:34` suggest KTX helpers (`Canvas.withTranslation`, `createBitmap`).

**Fix applied (this update):** `AudioClipExtractor.kt:149` now derives the muxer flag explicitly instead of passing `extractor.sampleFlags` straight through:
```kotlin
val muxerFlags = if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
    MediaCodec.BUFFER_FLAG_KEY_FRAME
} else {
    0
}
bufferInfo.set(0, sampleSize, relativeTimeUs, muxerFlags)
```
`@OptIn(UnstableApi::class)` doesn't work for Media3's `@UnstableApi` (it isn't a `kotlin.RequiresOptIn` marker — confirmed by a compiler warning: `'@OptIn' has no effect`), so the fix instead annotates the declarations directly with `@UnstableApi`, matching the existing convention in `ShareVideoExporter.kt`: `SharePreviewScreen` (`SharePreviewScreen.kt:58`), the `WaveformOverlay` class (`WaveformOverlay.kt:25`), and — since marking `SharePreviewScreen` `@UnstableApi` propagates the opt-in requirement to its own callers — `MainActivity.MainAppContent`'s call site was covered by annotating `MainActivity.onCreate` (`MainActivity.kt:89`), where the `NavHost`'s `SharePreviewScreen(...)` composable call lives inside the `setContent { }` lambda.

Verified: `:feature:feature-share:lintRelease` → 0 errors, 7 warnings (was 20 errors, 7 warnings). `:app:lintRelease` → 0 errors. `./gradlew :app:lintRelease :feature:feature-share:lintRelease testDebugUnitTest` → **BUILD SUCCESSFUL**. The 7 remaining warnings are the same pre-existing style suggestions listed above (Modifier factory conventions, `UsableSpace`/KTX hints) — not blocking.

### 2.3 The AAB/APK this build produces are completely unsigned — cannot be uploaded to Play as-is

**Command run:** `./gradlew :app:bundleRelease` → `BUILD SUCCESSFUL`, output `app/build/outputs/bundle/release/app-release.aab` (16 MB). I inspected the zip directly (`unzip -l`) and confirmed there is no `META-INF/MANIFEST.MF`, no `.RSA`/`.SF`/`.DSA` anywhere in the archive — it carries no jar signature at all. `app/build/outputs/apk/release/app-release-unsigned.apk` is named accurately.

**Why:** there is no `signingConfig` anywhere in `app/build.gradle.kts` (I read the full file), no keystore file in the repo, and `.github/workflows/android-ci.yml` has no signing secrets — it only runs `./gradlew build`, which never touches signing. This is not new to this release; it's the project's permanent state. I'm surfacing it because it's the literal precondition for the upload you're about to do.

**What I could not verify:** whatever process you actually use to sign the AAB before uploading to Play (Android Studio's "Generate Signed Bundle" wizard with a local keystore is the likely candidate, since none of this is tracked in git) is outside this repo, so I cannot confirm it still points at the same upload key / Play App Signing identity as the live `versionCode 5` release. **You need to confirm this yourself** — sign this AAB the same way you signed the last one, and if you're not certain which keystore that was, check Play Console → Setup → App signing before uploading anything.

### 2.4 `versionCode`/`versionName` are unchanged from the live release — this upload will be rejected as-is

**File:** `app/build.gradle.kts:21-22`:
```kotlin
versionCode = 5
versionName = "1.0.4"
```
Confirmed against the merged release manifest (`android:versionCode="5" android:versionName="1.0.4"`) — these are exactly the values already live on Play per your description, and Play Console rejects an upload whose `versionCode` isn't strictly greater than the currently-published one.

**Exact edit:**
```kotlin
versionCode = 6
versionName = "1.1.0"   // or "1.0.5" — your call: this is a real feature addition, not just a patch, so a minor bump reads more accurately, but the versionCode bump is the only part Play actually enforces
```

---

## 3. Should fix before release

### 3.1 The live preview and the burned-in video deliberately show different designs — direct violation of the spec's own "anti-drift" rule

**File:** `feature/feature-share/src/main/java/com/example/feature/share/presentation/components/ShareCardPreview.kt:69-73`, comment (present in the code, not something I'm inferring):
> "The live on-screen preview is deliberately simpler than the actual burned-in video: just background + circle logo + title + waveform. The institute line, category, accent rule and Arabic brand line still render into the exported mp4 via `ShareCardBitmapRenderer` — this screen alone omits them to avoid the text stack overflowing the small preview card."

`docs/plans/share-audio-video.md` §4.3 calls this exact scenario "the worst possible bug for a sharing feature" and built the whole normalised-coordinate `ShareCardSpec` system specifically to prevent it. The code compiles both renderers against the same spec object, but `ShareCardPreview` (Compose) skips rendering four of the eight design elements that `ShareCardBitmapRenderer` (Canvas, burned into the actual shared MP4) does render. Contrast with the article/text-card flow, where `TextCardPreview.kt:30-34`'s own comment explicitly notes it renders the *full* design 1:1 for exactly this reason.

**User-visible symptom:** every audio share shows the user a preview missing the institute name, category label, accent rule, and the Arabic "تم الإنشاء بواسطة..." credit line — then the video that actually gets sent to WhatsApp/Telegram has all four of those elements added. Debug testing on one device wouldn't catch this unless you specifically compared the live preview against the exported file side-by-side (which is exactly what the spec's own Phase 4 gate calls for and which appears not to have happened).

**Fix:** either render the full spec in `ShareCardPreview` (the comment's stated concern — text overflowing a small card — is precisely what `drawCardText`'s auto-shrink-then-ellipsize logic in `CardDrawing.kt` already solves for the Canvas renderer; the same approach can apply to the Compose `ShareText` composable), or, if the simplified preview is a deliberate final decision, say so and update the spec doc so this isn't flagged again next audit.

### 3.2 Branded background photo and institute logo are entirely unused — the shared card never shows the promised background image

**Files:** `SharePreviewViewModel.kt:116-117`, `TextCardPreviewViewModel.kt:51-52`:
```kotlin
background = ShareBackgroundSource.Gradient,
logoResId = R.drawable.dr_hassan_image,
```
Spec §4.4 calls for `dr_hassan_image.png` (the Sheikh's photo) as the **centre-cropped background**, and `institute_logo.jpg` clipped to a circle as the **top logo**. The actual code uses a flat two-color gradient as the background (no photo at all) and reuses the Sheikh's photo as the small circular "logo" — a repurposing, not what the spec describes. I confirmed `institute_logo.jpg` (`core/core-ui/src/main/res/drawable/institute_logo.jpg`) is referenced nowhere else in the entire repo (`grep -rn institute_logo --include=*.kt` → 0 hits in Kotlin source); it's dead weight in the APK either way.

**Why it matters:** this isn't a crash, but it's a visible design regression from the documented intent — the branded card looks noticeably different (flat gradient instead of a photo) from what was speced and presumably approved. Worth a deliberate yes/no from you before shipping, not a silent scope-creep.

### 3.3 Export bitrate/frame-rate deviate from spec — output files are ~80% larger than documented

**File:** `feature/feature-share/src/main/java/com/example/feature/share/engine/ShareVideoExporter.kt:150,154` — `VIDEO_FRAME_RATE = 20` (spec: 30), `VIDEO_BITRATE_BPS = 4_500_000` (spec: 2,500,000). The code has inline comments explaining the deliberate tradeoff (lower fps to cut encode time, higher bitrate to reduce banding), so this reads as an intentional change, not an oversight — but it changes the numbers the spec documented as a hard budget: a 60s clip is now ≈34 MB instead of the ≈19 MB spec'd (4.5 Mbps × 60s / 8 ≈ 33.75 MB video + ~1 MB AAC audio). Still comfortably under WhatsApp/Telegram chat limits, but worth confirming you're fine with nearly double the upload size on a slow connection, since §9's whole point was to land under a specific budget.

### 3.4 No timeout/watchdog on the primary clip-extraction path; a cross-thread `MediaExtractor.release()` elsewhere is a plausible native-crash source

**File:** `AudioClipExtractor.kt:32-56` (`extractClip`/`remux`) has no timeout at all around `extractor.setDataSource(sourcePath)` or the sample-copy loop. A stalled network read against a slow/misbehaving CDN (the exact "no network during the OkHttp fallback download" edge case Phase 2 asks about) blocks the coroutine on `Dispatchers.IO` indefinitely — cancelling the coroutine doesn't interrupt a blocking native `MediaExtractor` call, so the ViewModel would sit in `Preparing`/`isExtracting = true` forever with no error shown and no retry offered.

Contrast with `WaveformAnalyzer.kt:57-68`'s `decodeWithWatchdog`, which *does* have a timeout — but its mechanism is itself a risk: it force-closes the `MediaExtractor` from a **different coroutine** (the watchdog) while the decode call may still be executing on the original thread. `MediaExtractor` is not documented as safe for concurrent access from two threads; calling `release()` while another thread is mid-native-call inside the same instance is a plausible source of a native (non-catchable, non-`try/catch`-able) crash rather than the clean fallback-to-synthetic-envelope the code intends. I could not confirm this actually crashes without a device test under an artificially stalled connection — flagging as a real risk pattern, not a proven bug.

**What to check:** test both paths with the network throttled to near-zero / a connection that accepts the TCP handshake but never sends data, for both a cached and an uncached track, and confirm you get the "clean error" behaviour §8 promises rather than a hang or a crash.

### 3.5 Zero test coverage for the two files with the most silent-corruption risk

**Confirmed via build output:** `./gradlew testDebugUnitTest` shows `feature:feature-share:compileDebugUnitTestKotlin NO-SOURCE` and `feature:feature-share:testDebugUnitTest NO-SOURCE` — there is no `src/test` directory in this module at all (not even the boilerplate stub every other module has). `WaveformAnalyzer`'s bucket/gamma/smoothing math and `AudioClipExtractor`'s clip-window clamping math (`startUs`/`endUs` computation in `remux()`) are exactly the kind of pure-function logic that's cheap to unit test and, per §3.4/§2.2 above, already has at least one live bug plus a class of edge cases (track shorter than window, `startMs + window > duration`, zero-length clip) that a handful of parameterised tests would pin down cheaply.

---

## 4. Nice to have / follow-up

- **Brand color duplicated as a literal in 4 places** (`CardDrawing.kt:22-23` `CARD_ACCENT_COLOR`/`GRADIENT_*_COLOR`, `ShareCardPreview.kt:60`, `WaveformOverlay.kt:28`, `TrimTimeline.kt:43`) instead of importing `core-ui`'s `Color.kt` (`primaryLight`/`backgroundDark`). Values match today (verified) but will silently drift if the brand palette ever changes.
- **`formatDuration` reimplemented a 4th time** (`SharePreviewScreen.kt:337-341`) — the app already has 3 near-identical copies in `feature-audio`, `feature-home`, `feature-study`. Harmless, just avoidable duplication.
- **Three independent, unconfigured `OkHttpClient()` instances app-wide**: `data/FileDownloader.kt:22`, `feature-share/di/ShareModule.kt:21`, plus whatever Ktor's own engine spins up in `feature-search`. No shared connection pool, timeouts, or cache between them. Not a correctness bug, just wasted sockets/threads.
- **No baseline profile coverage for the new screens.** Confirmed by grepping the merged 7211-line `baseline-prof.txt` generated during `assembleRelease`: 0 lines reference `feature/share` or `feature.share`. The profile that does exist comes entirely from dependencies (Compose, AndroidX), not a project-authored `app/src/main/baseline-prof.txt` (none exists). `SharePreviewScreen`/`TextCardPreviewScreen` will be JIT-cold on first open — minor, but easy to improve later with `androidx.benchmark`'s baseline profile generator.
- **`institute_logo.jpg` is now fully dead** (see §3.2) — remove it or wire it in, either way it shouldn't sit unused.
- **Pre-existing, not part of this diff, but surfaced by the dependency check you asked for:** `feature-search` hardcodes three Ktor HTTP engines at `2.0.1` directly in its `build.gradle.kts:108-110` (bypassing the version catalog's unused `ktor = "3.3.3"` entry), and the actual resolved `releaseRuntimeClasspath` shows `com.algolia:instantsearch-android:3.3.1` transitively pulling `ktor-client-okhttp`/`ktor-client-core:2.2.4`, which Gradle's conflict resolution then substitutes in place of the `2.0.1` those three engines were built against — so `ktor-client-android:2.0.1` and `ktor-client-cio:2.0.1` end up linked against a `ktor-client-core` two minor versions newer than they shipped with. Feature-search's own comment (`build.gradle.kts:105-107`) already flags the "drop two of the three engines" tech debt; this is a second, sharper reason to actually do it. Out of scope for this release (feature-share doesn't touch feature-search), noting it since it came out of the dependency-tree check you asked for.

---

## 5. Verified clean

Things I specifically checked and found correct, so you know what the audit actually covered:

- **Arabic/RTL on Canvas.** Every text block in `ShareCardBitmapRenderer` and `TextCardBitmapRenderer` goes through `CardDrawing.kt`'s `drawCardText` → `StaticLayout.Builder` with `.setTextDirection(TextDirectionHeuristics.RTL)` and `.setAlignment(ALIGN_CENTER)`, loading typefaces via `ResourcesCompat.getFont`. Never `canvas.drawText` for real text content. (One narrow exception: `TextCardBitmapRenderer.kt:60` uses `canvas.drawText` for a single decorative `"”"` glyph — not user-facing multi-line Arabic text, so it doesn't hit the shaping bug the spec warns about.)
- **Burned-in brand line stays Arabic regardless of app locale.** `R.string.share_brand_line` is defined identically in `core-ui/src/main/res/values/strings.xml:99` and `values-ar/strings.xml:72`, with **no** `values-en` override — confirmed by direct grep. So the English locale falls through to the same Arabic string, exactly as spec'd.
- **No hardcoded UI strings.** Every user-facing string in the module resolves through `stringResource`/`context.getString`, and every `share_*` key exists in both `values-ar` and `values-en` with real translations (not just English left untranslated).
- **`provider_paths.xml` matches `ShareFileStore` exactly.** `<cache-path name="share_videos" path="share/" />` (`app/src/main/res/xml/provider_paths.xml:6-8`) matches `ShareFileStore.rootDir() = File(context.cacheDir, "share")` (`ShareFileStore.kt:20`) byte-for-byte — no `IllegalArgumentException: Failed to find configured root` risk.
- **Share intents are correct.** `EXTRA_STREAM` is always a `content://` URI via `FileProvider.getUriForFile`, `FLAG_GRANT_READ_URI_PERMISSION` is set, MIME types are explicit (`video/mp4`, `image/png`, `image/*` is never used — I checked), `Intent.createChooser` is used everywhere, and `SharePreviewScreen.kt:80-88` has an explicit `resolveActivity` fallback to a plain-text share per spec §8.
- **File lifetime matches spec §7 exactly.** Intermediates (`clip_*`, `base_*`) deleted unconditionally in `SharePreviewViewModel.onCleared()`; the exported `.mp4`/`.png` deleted only if `!hasBeenShared`; `sweepStale(10 min)` runs on every preview-screen open (`init` block); `HiltApplication.onCreate:73-75` calls `shareFileStore.sweepAll()` on `Dispatchers.IO`, matching the documented "any handoff is long finished by cold start" reasoning.
- **No `<queries>` element needed, and none was added.** Nothing in the share flow resolves or targets a specific package (WhatsApp/Telegram) by name — every intent goes through a generic `Intent.createChooser`. Confirmed by grepping for `setPackage`/known package names across `feature-share` and `app/src/main` — zero hits.
- **No new dangerous permissions.** I read the actual merged release manifest (`app/build/intermediates/merged_manifest/release/.../AndroidManifest.xml`, produced by a real `processReleaseManifest` run) line by line. Every `<uses-permission>` traces to Firebase/GMS/WorkManager/credentials, none to Media3, OkHttp, or Coil. No `READ_MEDIA_*`, `WRITE_EXTERNAL_STORAGE`, or `MANAGE_EXTERNAL_STORAGE` anywhere.
- **No debug leftovers.** No `Log.*` calls anywhere in `feature-share` (grepped). No `usesCleartextTraffic`, `android:debuggable`, or `networkSecurityConfig` overrides anywhere in the manifest tree — secure platform defaults apply on targetSdk 36.
- **Rotation/back/backgrounding mid-export.** `SharePreviewViewModel.onCleared()` explicitly cancels `extractionJob`, calls `videoExporter.cancel()`, and cleans up files. Rotation does not clear the ViewModel, so export continues (`viewModelScope` + a `Handler` on the main `Looper`, independent of Activity lifecycle) — matches spec exactly.
- **Release build integrity.** `./gradlew :app:assembleRelease` and `:app:bundleRelease` both complete with `BUILD SUCCESSFUL` against the code as it stands today. R8's `mapping.txt` **is** produced (`app/build/outputs/mapping/release/mapping.txt`, 99.5 MB) — remember to upload it to Play Console for readable production stack traces.
- **Resource shrinking is safe for this module's assets.** `dr_hassan_image.png` and all `cairo_*` font files are referenced exclusively via direct `R.drawable.*`/`R.font.*` constants in `feature-share`, never `getIdentifier()` or any string-based lookup — confirmed by grep. Nothing here will get silently stripped by `isShrinkResources = true`.
- **Dependency versions are consistent.** Across the full `releaseRuntimeClasspath` (`./gradlew :app:dependencies`), Media3 resolves to `1.8.0` everywhere with no conflicts, Kotlin stdlib resolves to `2.2.21` everywhere, Compose is governed consistently by `compose-bom:2025.07.00`, OkHttp resolves to `4.12.0` everywhere, and Guava resolves to `33.3.1-android` everywhere with the standard `listenablefuture` conflict-avoidance shim correctly in place. Adding the Transformer/Effect/Muxer artifacts introduced no version skew.
- **`versionCode`/`versionName`** confirmed still `5`/`"1.0.4"` in the actual merged manifest output — matches what's already live, per your description.

---

## 6. Commands run and their outcomes

| Command | Outcome |
|---|---|
| `./gradlew --version` | Gradle 8.13, AGP `8.11.1`, Kotlin 2.0.21 (repo's Kotlin plugin), Java 17 launcher |
| `./gradlew :app:assembleRelease` | **BUILD SUCCESSFUL** in 3m 41s → `app/build/outputs/apk/release/app-release-unsigned.apk` |
| `./gradlew :app:bundleRelease` | **BUILD SUCCESSFUL** in 2m 55s → `app/build/outputs/bundle/release/app-release.aab` (16 MB, confirmed unsigned by zip inspection) |
| `./gradlew :app:lintRelease :feature:feature-share:lintRelease :app:processReleaseManifest testDebugUnitTest` | **BUILD FAILED** in 2m 59s — `feature:feature-share:lintRelease` errored (20 errors, 7 warnings); all `testDebugUnitTest` tasks that did run passed (all boilerplate stubs, several `NO-SOURCE`) |
| `./gradlew :app:lintRelease --continue` (standalone) | **BUILD SUCCESSFUL** in 16s, zero findings — confirms the failure above is scoped entirely to the new module |
| `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` | Completed; full tree inspected for Media3/Kotlin/Compose/OkHttp/Guava/Ktor version conflicts (see §3/§4/§5) |
| Manual: unzip `media3-{transformer,effect,muxer,exoplayer}-1.8.0.aar` from the Gradle cache, grep for `proguard.txt` | Only `media3-exoplayer` ships one; the three new artifacts don't (§2.1) |
| Manual: unzip `app-release.aab`, check for `META-INF/MANIFEST.MF`/`.RSA`/`.SF` | None found — confirmed unsigned (§2.3) |
| Read merged release manifest (`app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml`) | Full permission list enumerated (§5) |
| Read `feature/feature-share/build/intermediates/lint_intermediate_text_report/release/lintReportRelease/lint-results-release.txt` | Full 20-error/7-warning breakdown (§2.2) |
| Grep `merged_art_profile/release/.../baseline-prof.txt` (7211 lines) for `feature.share`/`feature/share` | 0 matches (§4) |
| Grep for `institute_logo`, `Log\.`, `setPackage`, hardcoded English/Arabic strings, `usesCleartextTraffic`/`debuggable`/`networkSecurityConfig` across `feature-share` and manifest tree | See §5 for each result |

---

## Questions I could not answer from the code alone

1. **Does the release-minified build's video export actually work?** (§2.1) — install the built (or a locally re-signed) release APK on a device and run the full audio-share flow; this is the single most important manual check left. While you're at it, throttle the network to near-zero and confirm the extraction/waveform paths fail cleanly rather than hanging or crashing (§3.4).
2. **Is this AAB going to be signed with the same upload key / Play App Signing identity as the live `versionCode 5` release?** (§2.3) — I cannot see how you currently sign for upload; confirm the keystore/process yourself before uploading.
3. **Is the simplified live preview (§3.1) and the gradient-instead-of-photo background / repurposed logo (§3.2) an intentional final design decision, or did the implementation drift from what you approved?** Only you can say which one is "correct" here.
4. **Is the larger export file size (§3.3, ~34 MB vs. the spec'd ~19 MB for 60s) acceptable to you** given it's still under WhatsApp/Telegram's limits but doubles upload time on a slow connection?
5. **Audio-focus behaviour**: opening `SharePreviewScreen` while a lecture is playing via the shared `PlaybackService` — neither player configures `AudioAttributes`/focus handling explicitly, so I can't confirm from source whether Media3's 1.8.0 default behaviour makes them fight or hand off cleanly. Test on a device: play a lecture, open its Share screen, tap play on the clip scrubber, confirm exactly one stream plays and the lecture's notification doesn't end up orphaned.
6. **`TelegramVerificationActivity`** (`exported="true"`, no intent-filter) is pre-existing and unrelated to this release, but you specifically asked about it: confirm whether that's intentional, since an exported activity with no filter is unnecessary attack surface and Lint will flag it.
7. **Play Console paperwork** — Data safety form, ad-ID declaration, content rating: this feature reads/writes only app-private cache storage and shares via the standard system chooser; from the code alone it doesn't look like it changes what's collected or shared with third parties, but this is a form only you can review/submit — don't take "the code looks fine" as clearance here.
8. **True AAB size delta vs. the live Play release** — I only have this session's freshly-built 16 MB AAB; there's no previous release artifact in this checkout to diff against. Check the App Bundle Explorer in Play Console after upload, or if you still have the previous `.aab` locally, I can diff them directly.
9. **Staged rollout** — given the encoder path (§2.1, §2.4) is unverified on real device diversity, a staged rollout is worth considering; that's a judgment call for you once §2.1 is resolved one way or the other.
