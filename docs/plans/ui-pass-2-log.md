# UI pass 2: log

Spec: `docs/plans/ui-pass-2.md` (plus sections A–D of `docs/plans/overnight-ui-pass.md`).
Branch: `ui/pass-2`, created from HEAD. `ui/overnight-polish` was already contained in HEAD: the snapshot commit `f1874df` sits on top of it.

## Checklist

- [x] Phase 0: Setup and inventory: Done
- [x] Phase 1: Quick fixes: Done
- [x] Phase 2: Audio player: Done
- [x] Phase 3: Share-as-video preview: Done
- [x] Phase 4: Category screens: Done (no counts available)
- [x] Phase 5: Splash and onboarding: Done
- [x] Phase 6: About and Share-app: Done (no what's-new row)
- [x] Phase 7: Empty and error states: Done (no downloads state exists)
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
- D5.1: Illustration night variants are `_dark` drawables picked by the app theme rather than `drawable-night` (the app's theme preference isn't the system night mode). The splash itself does follow system night mode: `values-night` is the only option before any app code runs.
- D6.1: The QR check says `docs/assets/qr_play_store.png`, but Ali added the image as `docs/plans/qr_play_store.png` (there's no `docs/assets/`). It's clearly the file meant for this feature and `applicationId` is `app.netlify.devalihassan`, so I used it: it's copied to `core-ui/res/drawable-nodpi/qr_play_store.png`. I couldn't decode the QR here (no QR library installed), so **scanning it is on the device checklist**. If it doesn't open the Play Store listing, delete the button (one `if`) or replace the PNG.

## Phase notes

### Phase 1: Done
- 1.1 Gray bar: there are no View-based scroll containers in the app (no ScrollView, NestedScrollView, RecyclerView or WebView, and no `scrollbars` attribute), and Compose scroll containers never draw scrollbars, so there's nothing to disable. Pass 1 removed the root Scaffold's gray `surfaceVariant` strip and the gray top-bar gradients; that change is harmless and stays. If a thin bar still shows at the left edge, it isn't drawn by the app's views. The most likely source is the system's back-gesture edge indicator. It's on the device checklist.
- 1.2 "تقييم التطبيق" now opens `market://details?id=<packageName>`, falling back to `https://play.google.com/store/apps/details?id=<packageName>`. Nothing referenced the Rate screen and route except that row, so I deleted them: `RateAppScreen.kt`, `ProfileRoute.Rate`, `ProfileDestinations.RATE` and the `composable(RATE)` entry in `MainActivity`. The `rate_illu` drawable stays (it's used in Phase 7). No in-app review API.
- 1.3 Onboarding spelling fixed in `feature-onboarding/res/values/strings.xml`: أحصل→احصل (2×), إنقطع إتصالك→انقطع اتصالك, ابدا التدرج→ابدأ التدرج, ملفات ال pdf→ملفات PDF.
- 1.4 Skip interval: the player already skipped 10 s (`onRewind(10)`/`onForward(10)`). The "١٠" labels come with the new transport icons in Phase 2.
- 1.5 New `ArabicNumerals.formatMediaTime` (tested: ٠:٢٠، ٣:٠٩، ٢٢:٠٠، ١:٠٥:٣٠). It's used by the player's elapsed/total times, the share preview's times and the share video frame's times (the frame's "dots" were Arabic-Indic zeros from the old zero-padded format).

### Phase 2: Done
- **What the download is for:** it's the user's own "save for offline" download (`DownloadAudioUseCase`, started only by the download button). Playback never waited for it: the player already streamed `audioUrl` and switched to the local file when it existed (`switchToLocalPlayback`). The share flow doesn't wait on it either. So the fix was purely UI: the full-screen dimmed overlay is gone, and an inline card appears under the title while downloading: Tabler download icon, "جارٍ تحميل الدرس", the percentage "٤٥٪" in Arabic-Indic digits, a 3dp `accentStrong` bar, and (x). The (x) calls the new `onCancelDownload()`, which cancels the download coroutine (`downloadJob`). An already-downloaded file skips the download (the button is disabled and shows "محفوظ"). No cache layer was added.
- Top bar: back arrow only (`AppTopBar` with an empty title), so the title isn't shown twice. The top-bar share icon is gone.
- Photo: the shared `SheikhPhoto` (200dp, one 2dp `accent` ring, `clip(CircleShape)` + Crop). The old 280dp gradient-ring surface with shadow and `dr_hassan_image` (the one with the cream corner) is gone.
- Under the photo: a category chip (`accentContainer`) from `FixedCategories.AUDIO_CATEGORIES` when `category` (the id) matches, then the title via `ShareTitleParser.parse` + `AudioTitleCleaner`. A title with a date (sermons) gets a gold `accentText` date line from `ArabicNumerals.formatDateLine`. Then "الشيخ د. حسن الهواري" in `textSecondary`.
- Seek bar: M3 `Slider` with a custom 16dp round thumb, and a track with `drawStopIndicator = null` and `thumbTrackGapSize = 0.dp`. That removes both the end dot and the M3 bar thumb. It's forced LTR, with elapsed on the left and total on the right (`formatMediaTime`).
- Transport (forced LTR): Tabler `rotate` (counter-clockwise) with "١٠", a 76dp `accentStrong` play/pause circle, and Tabler `rotate-clockwise` with "١٠". Skips are 10 s.
- Action row with three labeled 52dp circles:
  - "السرعة" shows "١×"/"١٫٢٥×"…
  - "تحميل": download icon → circular progress with "٪" → green check with the label "محفوظ".
  - "مشاركة" opens the existing share-as-video route.
- Speed cycles 1 → 1.25 → 1.5 → 2 → 0.75 → 1 via `MediaController.setPlaybackSpeed`. It's stored under the NEW DataStore key `playback_speed` (float, via `DataStoreRepository.playbackSpeed()`) and applied whenever the controller connects, so later playback uses it too.
- The description card was kept (it only shows when a description exists).

### Phase 3: Done
- **Quote removed** (video share only; the article quote-image feature is untouched): the `QuoteField`, `onQuoteChanged`, `ShareCardContent.quote`, `ShareFramePainter.drawQuote`, `ShareFrameLayout.QUOTE_*`, the strings `share_quote_label` / `share_quote_hint` / `share_text_char_count` (values, values-ar, values-en), and the androidTest fixtures that passed a quote. The article strings `share_quote_subtitle` / `share_quote_source_label` / `share_quote_page_of` / `share_quote_multi_hint` stay. The frame's centre band now shows the sheikh's circular photo (`dr_hassan_photo`, 300px, 5px gold ring) through a new shared `BrandFramePainter.drawCirclePhoto`, which the header logo also uses now.
- **One screen, no scrolling:** `BoxWithConstraints`. The preview is 40% of the height (9:16, 16dp corners). The share button is pinned at the bottom: "مشاركة الفيديو", and while generating it shows a small progress ring + "جارٍ التجهيز ٤٥٪" inside the same button. The full-screen `GenerationOverlay` is no longer used here; the article flow still uses it.
- **One clip selector** (`components/ClipSelector.kt`, replacing the slider, the "من … إلى …" text and `TrimTimeline`, which is deleted):
  - chips ١٥ ث / ٣٠ ث / ٦٠ ث. The default stays 60 s (`DEFAULT_WINDOW_MS`), and chips longer than the track are hidden.
  - a thin overview bar of the whole lecture with the selection in `accentStrong`.
  - a zoomed waveform strip showing 2× the clip, centred on it, with the window outlined and a handle at each end. Dragging inside the window moves it; dragging near a handle resizes that side (min 5 s). The visible range is frozen during a drag.
  - "−٥ ث" / "+٥ ث" nudges (new VM `onNudge`) and the LTR-isolated range "٤:٣٢ – ٥:٣٢".
  - a 44dp gold play button with "استمع للمقطع قبل المشاركة". The preview card's own waveform fills in sync (it already used `playbackPositionMs`). The separate slider is gone.
  - Everything is forced LTR (rule A.4).
- **Video frame:**
  - Times are 52px (was 34) and use `formatMediaTime` ("٠:٢٠"), so no more "dots" from zero-padded Arabic-Indic zeros.
  - The waveform is laid out left to right: played bars fill from the left, elapsed time on the left, total on the right.
  - The speaker name in the video header is "الشيخ د. حسن الهواري" (new string `share_video_speaker_name`). The article quote images keep their own header name unchanged.
  - Under the "حمّل التطبيق" pill there's a small LTR text link `dr-alhawary.com`. The only Play Store link in the code is the long `play.google.com/store/apps/details?id=…`, not a short link. The video footer moved up (`VIDEO_DIVIDER_Y`/`VIDEO_FOOTER_TOP`) so the link fits above `SAFE_BOTTOM`. The quote images' footer is unchanged (same default positions, no link).
  - The animated overlay region was extended to cover the larger times.
- The share androidTests still compile (`compileDebugAndroidTestKotlin`). Their direction checks are left/right-agnostic.

### Phase 4: Done (no count lines: counts aren't loaded on these screens)
- One shared definition, `core-ui/components/CategoryList.kt` → `ContentCategories`: الكل (`layout-grid`), فتاوى (`messages`), دروس علمية (`books`), خطب الجمعة والعيدين (`building-mosque`), محاضرات (`microphone`), تلاوات (`book`), in that order. The ids match the stored ones (`all`, `fatawah`, `scientific_lessons`, `khotab`, `lectures`, `telawat`). `ContentCategories.resolve` maps whatever the VM loaded onto it. An unknown category goes at the end with a generic icon (Tabler `music`) and its own title. New Tabler icons: layout-grid, messages, books, building-mosque, microphone, book (+ music as the generic one).
- Both screens use the new shared `CategoryScreenContent`: an `AppTopBar` with a start-aligned title "الصوتيات" / "الفيديوهات" ("تصنيفات" dropped) and a back arrow on `background`, then one `surface` card of compact rows. Each row has a 40dp `accentContainer` circle with the icon in `onAccentContainer`, the name (14sp, weight 500), a chevron-left and hairline dividers. The gradient `CategoryGridTile` grid is no longer used (the file is left in place, unused). Both themes use the palette's surface (white in light, #2C2C2A in dark).
- Audio gained "الكل". It opens `audio_list_screen` with no query args. `categoryId`/`categoryTitle` are nullable route args already, the DAO filters with `:categoryId IS NULL OR …`, and the list title falls back to "الصوتيات". No architecture change.
- Count lines: skipped. No counts are available on these screens without new queries.

### Phase 5: Done
**Splash**
- No vector logo exists, only `app/res/drawable-nodpi/app_splash.png` (1024², transparent outside a dark circle, but with a faint alpha ≤ 39 box around it and faint box edges baked into the dark fill). I regenerated it with PIL. Everything outside the circle is now alpha 0 (the box and the stray arc are gone), the dark fill is repainted flat (removes the faint vertical box edges), and the circle is re-centred and resized to 64% of the canvas, which fits inside the splash icon mask's central 192/288 circle. There's no icon background colour and no shadow.
- Splash background per theme: `values/colors.xml` `splash_bg` = #F4EEE5 (the light app background) and a new `values-night/colors.xml` `splash_bg` = #1A1512 (the dark app background).
- `core-splashscreen` is already a dependency and `Theme.SplashScreen` sets `windowSplashScreenBackground`/`AnimatedIcon` for every API level, so no separate `values-v31` theme was needed (it would duplicate the same values).
- The unused `splash_screen` composable route had an artificial `delay(3000L)`. It's removed; the activity and route are kept. `MainActivity` keeps the real readiness gate (`setKeepOnScreenCondition { !appReady }`).

**Onboarding**
- 3 pages on `background`: person at computer (`study_boy`) "كل علم الشيخ في مكان واحد"; computer and server (`network_error`) "انقطع اتصالك بالإنترنت؟"; person and globe (`share_app_illu`) "شارك ما ينفع", with the spec's body texts. Buttons: "التالي" / "لنبدأ" (`accentStrong`) and "تخطي", directly on the page background (the tonal-elevation bottom bar is gone). The illustration box is a fixed 240dp, `ContentScale.Fit`, bottom-aligned (new shared `IllustrationBox`).
- The journey, document (summary), person-at-computer and computer-and-server vectors moved from `feature-onboarding/res` to `core-ui/res` so the Phase 7 empty states in other modules can use them.
- Brand colours: the accent #fcdfa6 → #FAC775 in the vectors (no background rects exist, so they're already transparent). Dark variants are `<name>_dark.xml` with the spec's mapping, **chosen by the app's own theme** (`Brand.colors.isDark`) via the `Illustration` enum, instead of a `drawable-night` folder, which follows the *system* night mode and would show light art in the app's dark mode (and vice versa). Colours found per vector:
- `study_boy`: #e6e6e6 ×13 (light neutral), #ff000000 ×10 (dark ink), #fcdfa6 ×7 (accent), #ccc ×5 (mid gray), #090814 ×5 (dark ink), #2f2e41 ×5 (dark ink), #ed9da0 ×4 (skin), #fff ×3 (white/near-white), #d6d6e3 ×2 (light neutral), #3f3d56 ×2 (dark ink), #f2f2f2 ×1 (white/near-white), #00000000 ×1 (transparent), #d7d7d7 ×1 (light neutral)
- `network_error`: #fcdfa6 ×10 (accent), #f2f2f2 ×8 (white/near-white), #e6e6e6 ×6 (light neutral), #090814 ×2 (dark ink), #2f2e41 ×2 (dark ink), #ff000000 ×1 (dark ink)
- `journey_illu`: #ed9da0 ×7 (skin), #090814 ×5 (dark ink), #3f3d56 ×3 (dark ink), #e6e6e6 ×3 (light neutral), #fcdfa6 ×2 (accent), #f2f2f2 ×1 (white/near-white), #fff ×1 (white/near-white)
- `summary_illu`: #fcdfa6 ×13 (accent), #090814 ×7 (dark ink), #d6d6e3 ×6 (light neutral), #fff ×4 (white/near-white), #f2f2f2 ×2 (white/near-white)
- `share_app_illu`: #fcdfa6 ×44 (accent), #ff000000 ×10 (dark ink), #fff ×9 (white/near-white), #ed9da0 ×5 (skin), #090814 ×4 (dark ink), #d6d6e3 ×4 (light neutral), #fffeff ×3 (white/near-white), #2f2e43 ×2 (dark ink), #e2e3e4 ×2 (light neutral), #e6e6e6 ×1 (light neutral), #f2f2f2 ×1 (white/near-white), #ff6363 ×1 (skin), #c8c8c8 ×1 (mid gray)
- `about_app_illu`: #090814 ×18 (dark ink), #fcdfa6 ×5 (accent), #fff ×5 (white/near-white), #ed9da0 ×4 (skin), #d6d6e3 ×3 (light neutral), #e6e6e6 ×3 (light neutral), #3f3d56 ×2 (dark ink), #f2f2f2 ×1 (white/near-white), #ff000000 ×1 (dark ink), #e6e8ec ×1 (light neutral)
- `rate_illu`: #090814 ×38 (dark ink), #e6e6e6 ×13 (light neutral), #ccc ×8 (mid gray), #ed9da0 ×6 (skin), #fcdfa6 ×5 (accent), #cacaca ×2 (mid gray), #fff ×1 (white/near-white), #2f2e41 ×1 (dark ink)

### Phase 6: Done ("ما الجديد" omitted: no release notes in the project)
**About ("عن التطبيق")**
- Ali's illustration (`about_app_illu`, gold #FAC775 + a `_dark` variant) at the top, capped at 210dp wide / 160dp tall. Then a 28dp logo circle (`admin_logo_app`, thin `accent` ring) next to "تطبيق الشيخ د. حسن الهواري". This replaces the old `appName` label, which presented the sheikh's name as the app name.
- Then "الإصدار <versionName>" with Latin digits. It comes from the existing `AppInfoProvider` (PackageManager `versionName`, which equals `BuildConfig.VERSION_NAME`; `BuildConfig` of `:app` isn't visible from `:feature-profile`).
- A description card with the spec's text.
- Two rows:
  - "الموقع الرسمي" with the LTR subtitle `dr-alhawary.com`, which opens `https://www.dr-alhawary.com`.
  - "تواصل معنا", which uses the same target as Profile's "الدعم والتواصل" (`ProfileDestinations.SUPPORT`, passed in from `MainActivity` via a new `onContact` parameter; no new route).
- "ما الجديد في هذا الإصدار": omitted. There are no release notes in the project (the only assets are licenses/privacy/terms).

**Share app ("مشاركة التطبيق")**
- Ali's illustration (`share_app_illu` + `_dark`), max 170dp wide. Heading "شارك التطبيق لينتفع به غيرك".
- Message preview card: the label "نص المشاركة", the message text, and the Play Store link, LTR-isolated.
- "مشاركة الرابط" (primary, `accentStrong`) opens the system share sheet through the existing `ShareAppUseCase` (ACTION_SEND text/plain) with the message + link.
- "نسخ الرابط" copies the link and shows the Snackbar "تم نسخ الرابط".
- "رمز QR" opens a `ModalBottomSheet` with "امسح الرمز لتحميل التطبيق", the QR at 200dp on white (both themes), and "أظهره لمن حولك في المسجد أو الدرس". The button only shows when `packageName == app.netlify.devalihassan`.

### Phase 7: Done (only existing states; no downloads screen exists)
- A new shared `EmptyState(illustration, title, body)` in `core-ui/components/Illustration.kt`, built on the Phase 5 fixed-height `IllustrationBox` and the `_dark` variants.
- Search, no results: the document illustration, "لا توجد نتائج", "جرّب كلمة أخرى".
- Search, error: the computer-and-server illustration, "تعذّر الاتصال بالإنترنت". The screen has no retry action, so there's no button.
- Designs list, load error: the computer-and-server illustration, "تعذّر الاتصال بالإنترنت". No existing retry action, so no button (I didn't wire paging's `retry()`: that would be new UI logic).
- Institute "منصة المعهد قريبًا" card: the journey illustration (160dp, ≤ 180dp) replaces the school-icon circle.
- "No downloads yet" (whiteboard): skipped. The app has no downloads screen or state to put it in. The whiteboard (`rate_illu` + `_dark`) stays available through `Illustration.Whiteboard`.
- The other empty lists (audio "no audios", videos "no videos", designs "no groups") aren't connection/search/download states in the spec's list, so they were left as they are.
