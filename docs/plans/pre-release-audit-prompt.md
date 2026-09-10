# Pre-release audit prompt — HassanAlHawary (v1.0.5 share feature)

Paste everything below the line into Claude Code, from the repo root, on the branch that
contains the share feature.

---

You are performing a **pre-release audit** of an Android app that is already live on Google
Play. A new feature has been added and I am about to ship an update. I have manually tested
the feature on one physical device in a **debug** build and it worked. Your job is to find
everything that manual debug testing on one device cannot find, before I upload the AAB.

## Ground rules

1. **Report first, do not refactor.** Do not change code until I approve. The only thing you
   may run without asking is read-only analysis and Gradle verification tasks
   (`lint`, `assembleRelease`, `bundleRelease`, unit tests, `dependencies`).
2. Every finding must be **anchored to a real file and line** that you have actually read.
   Do not report generic Android best-practice advice that is not grounded in this codebase.
   If you suspect something but cannot confirm it from the code, mark it `UNVERIFIED` and say
   exactly what you would need to check.
3. Rank findings by **probability × blast radius**, i.e. "will this crash for real users or
   get the release rejected", not by how tidy the code is.
4. Do not open `**/build/`, `.gradle/`, `.idea/`, or `.kotlin/` — those are generated.
5. Never print the contents of `local.properties` or any API key into your output. If a
   secret leaks somewhere it should not, name the file and line only.

## The app

- Published on Google Play, package `app.netlify.devalihassan`.
- Multi-module: `:app`, `:admin`, `:data`, `:core:{core-domain,core-network,core-database,core-ui,core-player}`,
  and `:feature:{splash-screen,onboarding,auth,home,profile,search,study,article,audio,video,image,about-dr-hassan,share}`.
- Kotlin, Jetpack Compose, MVVM + clean architecture, Hilt, Room, Retrofit/Ktor, Firebase
  (Auth, Firestore, Storage, Realtime DB, Messaging), Media3/ExoPlayer, Paging 3, Coil.
- minSdk 24, compileSdk 36, targetSdk 36, JVM 11, Media3 1.8.0.
- **Arabic / RTL is the primary language.** `android:supportsRtl="true"`, a `locales_config.xml`
  is declared.
- Currently `versionCode = 5`, `versionName = "1.0.4"` in `app/build.gradle.kts`.

## What is new in this release

A share feature in the new module **`:feature:feature-share`**
(`feature/feature-share/src/main/java/com/example/feature/share/`):

- `domain/` — `ShareCardContent`, `ShareClip`, `ShareExportState`
- `engine/` — `AudioClipExtractor`, `WaveformAnalyzer`, `WaveformOverlay`, `CardDrawing`,
  `ShareCardSpec`, `TextCardSpec`, `ShareCardBitmapRenderer`, `TextCardBitmapRenderer`,
  `ShareFileStore`, `ShareVideoExporter`
- `presentation/` — `SharePreviewScreen/ViewModel/UiState`,
  `TextCardPreviewScreen/ViewModel/UiState`, `components/`
- `di/ShareModule.kt`

Two user-facing flows:

- **Share an article** → renders a branded image card and shares it.
- **Share audio** → extracts a clip, renders a 9:16 branded card with a real-PCM waveform,
  and encodes an MP4 (1080×1920, 30 fps, H.264 ~2.5 Mbps, AAC 128k) with **Media3 Transformer**,
  then shares it via FileProvider (`${applicationId}.provider`, cache path `cacheDir/share/`).

Read `docs/plans/share-audio-video.md` first — it is the spec for this feature and records
the decisions and two known traps (do not delete the exported MP4 in `onDispose`; Arabic drawn
on Canvas must use `StaticLayout` with `TextDirectionHeuristics.RTL`, never `canvas.drawText`).
Verify the code actually honours that spec; where it deviates, say so.

---

## Phase 1 — Release-build integrity (highest priority)

I tested a **debug** build. The release build is minified (`isMinifyEnabled = true`,
`isShrinkResources = true` in `app/build.gradle.kts`) and I have not exercised the new feature
under R8. This is the single most likely source of a "works on my phone, crashes in production"
bug. Check specifically:

1. `app/proguard-rules.pro` keeps `com.example.domain.module.**`, `data_firebase.model.**`,
   `feature.home/article/study.domain.model.**`, `data_local.**` — but **nothing under
   `com.example.feature.share.**`**. Determine whether any share class needs a keep rule:
   anything reflected over, serialized, used as a navigation argument, deserialized by
   Firestore/Gson/kotlinx-serialization, or referenced by name.
2. `feature/feature-share/consumer-rules.pro` is **empty (0 bytes)** and `proguard-rules.pro`
   in that module is a comment only. Media3 Transformer, Effect and Muxer pull in codec and
   effect classes that are instantiated reflectively. Confirm whether Media3 ships its own
   consumer rules for `transformer`/`effect`/`muxer` in 1.8.0, or whether keep rules are
   required here. Same question for OkHttp, Coil and Hilt in this module.
3. Actually run `./gradlew :app:assembleRelease` and `./gradlew :app:bundleRelease`. Report
   any failure verbatim. If a keystore or signing config is missing, say so — do not invent one.
4. Run `./gradlew :app:lintRelease` (and `lintRelease` on `:feature:feature-share`) and triage
   **every** error and warning that touches the new module or the manifest. Ignore noise in
   untouched modules unless it is an error.
5. Check `isShrinkResources = true` against the new brand assets in `core-ui`
   (`dr_hassan_image.png`, `institute_logo.jpg`, the Cairo font family). If any of them is
   referenced only dynamically (by name, via `getIdentifier`, or from a spec class rather than
   `R.drawable.*`), resource shrinking will strip it and the card will render blank in release.
   This is exactly the kind of bug a debug build hides.

## Phase 2 — Crash, ANR and OOM risk in the share pipeline

Play's Android vitals thresholds (crash rate and ANR rate) can get an app demoted in search
and, if bad enough, flagged. Audit the new code for:

- **Main-thread work.** Bitmap rendering at 1080×1920, `StaticLayout` measurement, PCM decode
  in `WaveformAnalyzer`, MediaCodec/MediaExtractor use in `AudioClipExtractor`, file I/O in
  `ShareFileStore` — confirm each runs off the main dispatcher and that the dispatcher is
  injected, not hard-coded, where it matters.
- **OOM.** `WaveformAnalyzer` is the largest file in the module (~12 KB). Check what happens
  with a 90-minute lecture: is the whole PCM buffer held in memory, or streamed? Check bitmap
  allocation, `Bitmap.Config`, and whether bitmaps are recycled or leaked across recompositions.
  Check for unbounded `ByteArray`/`ShortArray` growth.
- **Resource leaks.** `MediaExtractor`, `MediaCodec`, `MediaMuxer`, `Transformer`, ExoPlayer
  instances, `InputStream`s, OkHttp `Response` bodies — every one must be released/closed on
  every path including cancellation and exception. Check `finally` blocks and
  `DisposableEffect`/`onCleared`.
- **Coroutine lifecycle.** Export runs 8–18 seconds. What happens if the user rotates, presses
  back, or backgrounds the app mid-export? Look for work leaking past `viewModelScope`,
  `GlobalScope`, missing cancellation, and `Transformer.Listener` callbacks firing after the
  ViewModel is cleared.
- **Encoder availability.** minSdk is 24. Not every device supports H.264 encoding at
  1080×1920. Check whether `ShareVideoExporter` handles `ExportException`
  (`ERROR_CODE_ENCODING_FORMAT_UNSUPPORTED` / `ERROR_CODE_ENCODER_INIT_FAILED`), and whether
  there is a fallback resolution/bitrate or at minimum a user-visible error instead of a crash.
  Also check for a hard `Build.VERSION.SDK_INT` assumption that only holds on modern devices.
- **Disk space.** A 60 s export is ~19 MB. What happens when the device is full or
  `cacheDir` is evicted by the OS mid-export?
- **Missing null/empty guards.** Zero-length audio, a clip window at the very end of the track,
  an article with an empty or extremely long title, missing remote asset, no network during
  the OkHttp fallback download.

## Phase 3 — Sharing, FileProvider and inter-app behaviour

- `app/src/main/res/xml/provider_paths.xml` now declares `<cache-path name="share_videos" path="share/"/>`.
  Verify the directory `ShareFileStore` actually writes to matches that path exactly (a
  mismatch throws `IllegalArgumentException: Failed to find configured root` at share time —
  this fails on *some* paths only, so a single manual test may have missed it).
- Confirm the share `Intent` sets `FLAG_GRANT_READ_URI_PERMISSION`, a correct `type`
  (`video/mp4` / `image/*` — not `*/*`), `EXTRA_STREAM` as a content URI (never `file://`,
  which throws `FileUriExposedException`), and uses `Intent.createChooser`.
- Check the file-lifetime policy against the spec: intermediates deleted immediately, the
  shared MP4 **kept** (not deleted in `onDispose`), files older than 10 minutes swept on screen
  entry, `cacheDir/share/` wiped in `HiltApplication.onCreate`. Confirm the sweep cannot delete
  a file that a target app is still reading, and that the wipe on cold start cannot race an
  in-flight share.
- If the code resolves or targets specific apps (WhatsApp, Telegram, Instagram) by package
  name, Android 11+ package visibility requires a `<queries>` element — there is none in the
  manifest today. Confirm whether one is needed.
- Check whether anything was added that writes outside app-private storage or requests
  `READ_MEDIA_*` / `WRITE_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE`. It should not be —
  flag it loudly if it is, because those trigger Play policy declarations.

## Phase 4 — Manifest, permissions and Play policy

Diff the manifest and permission set against what is currently live and check:

- No new permission was added silently by a library merge. Run
  `./gradlew :app:processReleaseManifest` and read the merged manifest — Media3 Transformer,
  Coil and OkHttp can all contribute. Report the **final merged** permission list.
- `TelegramVerificationActivity` is `android:exported="true"` with **no intent filter**. Confirm
  whether that is intentional; an exported activity with no filter is an unnecessary attack
  surface and lint flags it.
- `PlaybackService` is `exported="true"` with `foregroundServiceType="mediaPlayback"`. On
  targetSdk 34+ every foreground service start must be attributable to an allowed reason;
  confirm nothing in the new export path starts a foreground service from the background.
- `android:allowBackup="true"` with `backup_rules.xml` / `data_extraction_rules.xml` — verify
  the new cache/share directory is excluded from backup and from device-transfer extraction.
- targetSdk 36 obligations: predictive back, edge-to-edge enforcement, 16 KB page size for any
  native library (Media3 ships `.so` files — confirm 1.8.0 is 16 KB-aligned, this is a hard
  Play requirement now), and photo-picker/media permission changes.
- Play Console paperwork this change may trigger: **Data safety** form (does the new feature
  change what is collected or shared?), ad-ID declaration, and whether generated/shared content
  needs anything in the content rating. Tell me what to update, do not guess that nothing changed.
- Confirm no debug-only code ships: `Log` calls leaking user data or tokens, `StrictMode`
  penalties, `BuildConfig.DEBUG` branches that are inverted, leftover test endpoints,
  `android:debuggable`, or a `usesCleartextTraffic` allowance.

## Phase 5 — Arabic / RTL correctness

- Every string drawn onto a Canvas in `CardDrawing`, `ShareCardSpec`, `TextCardSpec`,
  `WaveformOverlay` must go through `StaticLayout` with `TextDirectionHeuristics.RTL`, never
  `canvas.drawText`. Flag any violation with the line.
- Check for hard-coded Latin-assumption layout: `LayoutDirection`, `start`/`end` vs
  `left`/`right`, text truncation and ellipsis on Arabic, digit shaping (Arabic-Indic vs ASCII)
  for durations and timestamps.
- Check for hard-coded user-facing strings in the new module that should be in
  `strings.xml` — the app is localized, and an English string in an Arabic UI is a visible bug.
- Long Arabic titles: does the card layout overflow, clip, or overlap the branding?

## Phase 6 — Whole-app regression surface

The new module is wired into `:app` and reuses `core-ui`. Check that the addition did not
break anything that already shipped:

- `:feature:feature-share` depends on `media3-exoplayer` for a *private* preview player. The
  app also runs a shared `PlaybackService` / MediaSession via `:core:core-player`. Verify the
  two players cannot fight over audio focus, and that opening the share screen while a lecture
  is playing does the right thing (and that the notification/media session is not orphaned).
- Hilt graph: `ShareModule` bindings, scopes (`@Singleton` vs `@ViewModelScoped`), and any
  duplicate binding with an existing module. A Hilt duplicate-binding error is a compile
  failure, but a wrong *scope* is a silent leak.
- Navigation: new routes/arguments, deep-link collisions, back-stack behaviour, and what
  happens on process death (`SavedStateHandle`) mid-export.
- Dependency hygiene: `gradle/libs.versions.toml` pulls **Ktor 3.3.3 with three engines**
  (okhttp + android + cio) in `feature-search`, plus OkHttp 4.12.0 separately. Only one engine
  is needed; the extras inflate the AAB and multiply the `META-INF` duplicates that the
  `packaging { excludes }` block in `app/build.gradle.kts` already has to work around. Report
  the size impact and which engines are actually referenced in code.
- Run `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` and flag version
  conflicts, especially anything forcing a different Media3, Kotlin stdlib, or Compose version
  than intended. Note the existing `resolutionStrategy` that rewrites `com.intellij:annotations`.
- Run all unit tests (`./gradlew testDebugUnitTest`). Report which modules have **no tests at
  all** for logic that can silently produce a corrupt file — `WaveformAnalyzer` bucket math and
  `AudioClipExtractor` clip-window math are the two worth testing.

## Phase 7 — Release checklist

- `versionCode` is still **5** and `versionName` **"1.0.4"** — the values already on Play.
  This upload will be rejected until they are bumped. Tell me the exact edit.
- Confirm the release is signed with the same key / Play App Signing as the live version.
- AAB size delta vs the current release, and whether the Media3 Transformer/Effect/Muxer
  addition pushed past any threshold I should care about.
- Confirm the R8 `mapping.txt` will be produced and remind me to upload it to Play so
  production stack traces are readable.
- Check whether a `baseline-prof.txt` exists; if not, note that the new Compose screens will
  be JIT-cold on first open.
- Note anything in the Play Console I need to touch: release notes (Arabic + English),
  Data safety, target API declaration, and whether a staged rollout is advisable given the new
  encoder path runs on hardware I cannot test.

---

## Output format

Produce a single markdown report at `docs/reports/pre-release-audit.md` **and** summarise it in
your reply. Structure it as:

1. **Verdict** — one paragraph: ship, ship-with-fixes, or do-not-ship, and why.
2. **Blockers** — will crash users, break the release build, or get rejected. For each:
   `file:line`, what breaks, the exact user-visible symptom, why my debug test on one device
   did not catch it, and the proposed fix as a diff.
3. **Should fix before release** — same format.
4. **Nice to have / follow-up** — brief.
5. **Verified clean** — the things you specifically checked and found correct. I want to know
   what the audit actually covered, not just what it found.
6. **Commands you ran and their outcomes.**

At the end, list any question you could not answer from the code alone and tell me exactly what
to check on a device or in the Play Console.

Begin with Phase 1. Work through the phases in order, but read the whole of
`feature/feature-share/` before reporting anything.
