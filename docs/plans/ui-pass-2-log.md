# UI pass 2: log

Spec: `docs/plans/ui-pass-2.md` (plus sections A–D of `docs/plans/overnight-ui-pass.md`).
Branch: `ui/pass-2`, created from HEAD. `ui/overnight-polish` was already contained in HEAD: the snapshot commit `f1874df` sits on top of it.

## Checklist

- [x] Phase 0: Setup and inventory: Done
- [x] Phase 1: Quick fixes: Done
- [ ] Phase 2: Audio player
- [ ] Phase 3: Share-as-video preview
- [ ] Phase 4: Category screens
- [ ] Phase 5: Splash and onboarding
- [ ] Phase 6: About and Share-app
- [ ] Phase 7: Empty and error states
- [ ] Phase 8: Final report

## Phase 0: inventory

- **Baseline:** `assembleDebug testDebugUnitTest` + `:core:core-domain:test` passed (gate script stops Gradle daemons first, see pass-1 D2.1).
- **Audio player:** `feature-audio/.../detail/AudioDetailScreen.kt` + `AudioDetailViewModel.kt`. The full-screen "جار تحميل ملفات الدرس…" overlay is shown while `downloadProgress` is in 1..99. That progress comes from `DownloadAudioUseCase`, started only by the user's **download (save offline) button** (`onDownloadClicked`). Playback never waits for it: `onPlayPauseToggle`/`listenToController` already stream `audioUrl` (the local file is used only once `isDownloaded`), and `switchToLocalPlayback` swaps to the file when the download finishes. The share flow doesn't depend on it either (the share preview has its own download progress).
- Skip buttons already call `onRewind(10)` / `onForward(10)`, but the icons are the generic `round_backword_icon`/`round_forward_icon` with no number.
- **Share preview:** `feature-share/.../presentation/SharePreviewScreen.kt` (+ VM, UiState). The frame is drawn by `engine/ShareFramePainter` + `BrandFramePainter` from the `ShareFrameLayout` spec, and the preview (`components/ShareCardPreview`) reuses the painter. The optional quote exists: `ShareCardContent.quote`, `ShareFramePainter.drawQuote`, `QuoteField`, `onQuoteChanged`, `ShareFrameLayout.QUOTE_*`, strings `share_quote_label/hint`, `share_text_char_count`, and androidTest fixtures. The trim UI is a `Slider` (`PlaybackScrubber`) + "من … إلى …" text + `TrimTimeline` (full-track waveform). The time labels are `ArabicNumerals.formatDuration` → "٠٠:٢٠": Arabic-Indic zeros are dots, which is why they "render as dots".
- **Categories:** `FixedCategories` in `core-domain/.../module/ContentCategory.kt` (ids fatawah, scientific_lessons, khotab, lectures, telawat; the same list for audio and video). The screens `AudioCategoryScreen` / `VideoCategoryScreen` are 2-column `CategoryGridTile` grids with PNG/XML icons. Video adds a synthetic "الكل" (`ALL_VIDEO_CATEGORIES_ID`). The audio list DAO already supports `categoryId = null` (`:categoryId IS NULL OR …`), and the route arg is nullable.
- **Splash:** `core-splashscreen` IS a dependency (`installSplashScreen()` in `MainActivity`). The theme `Theme.HassanAlHawary.Splash` is in `app/res/values/themes.xml` (and a duplicate in `values-en`), with `windowSplashScreenBackground=@color/splash_bg` (#342C2B) and `windowSplashScreenAnimatedIcon=@drawable/app_splash` (a 1024² RGBA PNG). There's no `values-v31` and no `values-night`. The logo PNG is transparent, but it has a faint box (alpha ≤ 39) outside the dark circle. There's no vector logo. The `feature-splash-screen` composable route exists but isn't the start destination. The only wait is `setKeepOnScreenCondition { !appReady }` (a real readiness gate, not an artificial delay).
- **Onboarding:** `feature-onboarding/.../OnboardingScreen.kt`. 4 pages: study_boy, journey_illu, network_error, summary_illu (all vector XML). The "person and globe" is `core-ui/drawable/share_app_illu.xml`. The whiteboard is `core-ui/drawable/rate_illu.xml`. Colors are listed in Phase 5.
- **About / Rate / Share:** `feature-profile/.../about_app/AboutAppScreen.kt` (about_app_illu), `share_app/ShareAppScreen.kt` (share_app_illu), `rate_app/RateAppScreen.kt` (rate_illu). Profile rows call `onNavigate(ProfileRoute.About/Share/Rate)`, and `MainActivity` maps them to `ProfileDestinations.*`. The RATE route/screen is referenced only by that mapping.
- **Existing empty/error states:** audio list (no audios), videos (no videos), designs (load error + empty), search (no results, error), article reader (error), institute coming-soon card. None has a retry action. There's no downloads screen.
- **View-based scroll containers:** none. `grep ScrollView|RecyclerView|WebView|scrollbars` over app/feature/core sources finds nothing. Compose `verticalScroll`/`Lazy*` never draw scrollbars.
- **QR:** `docs/assets/qr_play_store.png` does NOT exist. Ali put `qr_play_store.png` in `docs/plans/` instead (see Phase 6). `applicationId` = `app.netlify.devalihassan` (read from `app/build.gradle.kts`).

## Decisions

## Phase notes

### Phase 1: Done
- 1.1 Gray bar: there are no View-based scroll containers in the app (no ScrollView, NestedScrollView, RecyclerView or WebView, and no `scrollbars` attribute), and Compose scroll containers never draw scrollbars, so there's nothing to disable. Pass 1 removed the root Scaffold's gray `surfaceVariant` strip and the gray top-bar gradients; that change is harmless and stays. If a thin bar still shows at the left edge, it isn't drawn by the app's views. The most likely source is the system's back-gesture edge indicator. It's on the device checklist.
- 1.2 "تقييم التطبيق" now opens `market://details?id=<packageName>`, falling back to `https://play.google.com/store/apps/details?id=<packageName>`. Nothing referenced the Rate screen and route except that row, so I deleted them: `RateAppScreen.kt`, `ProfileRoute.Rate`, `ProfileDestinations.RATE` and the `composable(RATE)` entry in `MainActivity`. The `rate_illu` drawable stays (it's used in Phase 7). No in-app review API.
- 1.3 Onboarding spelling fixed in `feature-onboarding/res/values/strings.xml`: أحصل→احصل (2×), إنقطع إتصالك→انقطع اتصالك, ابدا التدرج→ابدأ التدرج, ملفات ال pdf→ملفات PDF.
- 1.4 Skip interval: the player already skipped 10 s (`onRewind(10)`/`onForward(10)`). The "١٠" labels come with the new transport icons in Phase 2.
- 1.5 New `ArabicNumerals.formatMediaTime` (tested: ٠:٢٠، ٣:٠٩، ٢٢:٠٠، ١:٠٥:٣٠). It's used by the player's elapsed/total times, the share preview's times and the share video frame's times (the frame's "dots" were Arabic-Indic zeros from the old zero-padded format).
