# Flutter (iOS) port of the Hawary user app: kickoff prompt

This file is the prompt that starts the Flutter project. Everything between **BEGIN PROMPT** and **END PROMPT** is for the agent; the part above it is for you.

## Before you paste it (for you)

1. **Install:** Flutter SDK (stable), Firebase CLI and FlutterFire CLI (`dart pub global activate flutterfire_cli`), Android Studio with an emulator. Keep the Figma connector enabled in Claude Code.
2. **iOS needs macOS.** Windows cannot build or run iOS apps. Until you have a Mac (or a cloud Mac CI such as Codemagic or GitHub Actions macOS runners), the agent builds and checks the Flutter app on the Android emulator, which is enough for screen-by-screen parity. iOS-only items stay marked "unverified" until they can be checked on iOS.
3. **Create an empty folder** for the new project, for example `C:\Users\aliha\FlutterProjects\hawary_app`.
4. **Start Claude Code in that folder with access to this repo:**
   `claude --add-dir "C:\Users\aliha\AndroidStudioProjects\HassanAlHawary"`
5. Paste everything between the markers. The agent copies it to `docs/port-brief.md` and imports it from the new `CLAUDE.md`, so later sessions pick up where the last one stopped. In a new session, just say "continue with the next stage".
6. The agent stops after every stage and waits for your go-ahead.

---- BEGIN PROMPT ----

# Mission

Build a **Flutter app that is a 1:1 copy of the existing Android user app** ("Dr. Hassan Al-Hawary", Arabic, RTL). The Flutter app will ship on **iOS** (iPhone and iPad); the Android app stays Kotlin/Compose. The admin app (`:admin`) stays Android-only and is **out of scope**.

"Identical" means the same screens, layouts, measurements, colours, fonts, icons, Arabic strings, animations, states (loading, empty, error, offline), navigation and behaviour, on phone and tablet (Compact, Medium, Expanded). The only differences allowed are the iOS requirements in section 6, and only after the user approves each one.

While building, write a **Flutter study course** in `study-notes/` (section 9) for a senior Android engineer who knows only basic Flutter. The lessons explain the code you write, stage by stage.

## 0. Project settings

| Setting | Value |
|---|---|
| Android source repo (read-only) | `C:\Users\aliha\AndroidStudioProjects\HassanAlHawary` (branch `master`) |
| Flutter project (this folder) | the current working directory |
| Dart package name | `hawary_app` (confirm in Stage 0) |
| iOS bundle id, display name, minimum iOS | ask in Stage 0 (suggest bundle id `app.netlify.devalihassan`, same as the Android applicationId) |
| Android applicationId of the Flutter dev build | a separate id so it installs next to the real app, e.g. `app.netlify.devalihassan.flutter` (ask) |
| Figma file | **Hawary App — Design System**, file key `gyiLOI3lrVYKrmwJbJ4qKv` |
| Firebase | the same project the Android app uses; add an iOS app (and the Flutter dev Android app) with `flutterfire configure` |

**Never modify the Android repo.** Read it only. Never print or commit secrets (Algolia keys, Google client ids); they live in the Android repo's `local.properties` and in a gitignored env file here.

## 1. Sources of truth

1. **The Android app's current code** (`master`) for behaviour, data, Arabic strings, states and every visual detail the code implements. The README screenshots in the Android repo are **outdated** (they show login/register screens that no longer exist); ignore them.
2. **Figma** for exact measurements, tokens and text styles. 1 Figma px = 1 dp = 1 Flutter logical pixel; never rescale. The phone frames sit in sections named "provisional colors", and the Android code went through several UI passes after them, so **colour values come from `BrandColors.kt`** (Figma variable names equal `BrandPalette` property names).
3. **The running Android app** (screenshots from the emulator) as the final visual check.
4. **Design notes in the Android repo** `docs/plans/`: `tablet-design.md` (tablet rules, tokens, frame ids), `tablet-design-log.md`, `motion-pass.md`, `ui-pass-2.md`, `overnight-ui-pass.md`, `home-redesign.md`, `welcome-screen-redesign.md`, `share-audio-video.md`, `share-video-redesign.md`. Read them before the matching stage.

**If Figma and the Android app disagree, stop and ask the user which one wins.** Record the answer in `docs/decisions.md`.

How to read Figma: load the `/figma-design-to-code` skill before the first `get_design_context` call. For each frame call `get_design_context` and `get_screenshot` (Light, then Dark), and `get_variable_defs` for tokens. Screenshot URLs expire: download them right away to `docs/figma/<screen>-<class>-<theme>.png`.

## 2. Scope: the user app

Module code lives under `<module>/src/main/java/com/example/...` (the `:app` module's package is `app.netlify.devalihassan` but its folder is `app/src/main/java/com/example/hassanalhawary/`). Find files with Glob; the paths below are shortened.

### 2.1 Screens

| # | Screen | Android source | Figma phone (Light / Dark) | Figma tablet: Expanded L / D · Medium L / D |
|---|---|---|---|---|
| 1 | Splash (system splash + exit animation) | `feature-splash-screen` `SplashScreen.kt`, `MainActivity.onCreate` | `25:1037` / — | `61:3803` / `61:3807` · `64:6787` / `64:6791` |
| 2 | Welcome / sign-in (Google) | `feature-auth` `auth/AuthScreen.kt`, `components/*` | `25:1040` / — | `61:3812` / `61:3825` · `64:6796` / `64:6812` |
| 3 | Onboarding (3 pages) | `feature-onboarding` `OnboardingScreen.kt` | `25:1058` / — | `61:3845` / `61:3859` · `64:6832` / `64:6851` |
| 4 | Update, optional and required | `core-ui` `components/ForceUpdateScreen.kt` | `36:1354` / `36:1379`, `36:1404` / `36:1427` | `61:3881` / `61:3895`, `61:3910` / `61:3922` · `64:6873` / `64:6887`, `64:6902` / `64:6914` |
| 5 | Home | `feature-home` `HomeScreen.kt`, `components/*` | `5:2` / `5:274` | `56:192` / `56:414` · `63:2756` / `63:2969` |
| 6 | Notifications | `feature-home` `notifications/*` | — | — |
| 7 | Articles list | `feature-article` `list/ArticleListScreen.kt` | `8:338` / `10:935` | `53:2` / `53:170` · `63:2412` / `63:2490` |
| 8 | Reader | `feature-article` `detail/ArticleDetailScreen.kt` | `9:522` / `10:1157` | inside Articles (Expanded) · `63:2569` / `63:2662` |
| 9 | Article passage selection | `feature-article` `share/ArticleShareSelectionScreen.kt` | — | — |
| 10 | Quote card preview | `feature-share` `presentation/TextCardPreviewScreen.kt`, `engine/QuoteCard*` | — | — |
| 11 | Audio categories | `feature-audio` `category/AudioCategoryScreen.kt` | `8:407` / `10:978` | `57:498` / `57:870` · `63:3174` / `63:3310` |
| 12 | Audio list (Fatwas etc.) | `feature-audio` `list/AudioListScreen.kt` | `25:1083` / — | inside Player (Expanded) · `63:3447` / `63:3556` |
| 13 | Player | `feature-audio` `detail/AudioDetailScreen.kt` | `5:147` / `5:389` | `57:646` / `57:1015` · `63:3658` / `63:3719` |
| 14 | Audio clip share preview (dialog on tablet) | `feature-share` `presentation/SharePreviewScreen.kt`, `engine/*` | `10:684` / `10:1379` | `59:1158` / `59:1626` · `64:4477` / `64:4726` |
| 15 | Video categories (phone only; tablet uses pills) | `feature-video` `category/VideoCategoryScreen.kt` | — | — |
| 16 | Videos grid | `feature-video` `list/VideoScreen.kt` | `25:1188` / — | `60:1403` / `60:1809` · `64:3744` / `64:3936` |
| 17 | Video player (YouTube) | `feature-video` `detail/VideoPlayerScreen.kt` | — | — |
| 18 | Designs grid | `feature-image` `list/ImageGroupsScreen.kt` | `25:1258` / — | `60:1597` / `60:1960` · `64:4089` / `64:4306` |
| 19 | Designs viewer (immersive) | `feature-image` `detail/ImageScreen.kt` | `26:1226` / — | `60:1782` / `60:2086` · `64:4976` / `64:5003` |
| 20 | Search | `feature-search` `SearchScreen.kt`, `SearchViewModel.kt` | `8:534` / `10:1079` | `59:939` / `59:1412` · `64:3376` / `64:3466` |
| 21 | Profile | `feature-profile` `profile/ProfileScreen.kt` | `9:578` / `10:1202` | `59:1054` / `59:1522` · `64:3557` / `64:3650` |
| 22 | About the app | `feature-profile` `about_app/*` | — | — |
| 23 | Share the app (QR) | `feature-profile` `share_app/ShareAppScreen.kt` | — | — |
| 24 | Support | `feature-profile` `support/SupportScreen.kt` | — | — |
| 25 | Privacy, Terms, Licenses | `feature-profile` `components/LegalTextScreen.kt` + `app/src/main/assets/*.md` | — | — |
| 26 | About the Sheikh | `feature-about-dr-hassan` `AboutDrHassanScreen.kt`, data in `core-domain` `module/DoctorProfile.kt` | `26:1255` / — | `61:1998` / `61:2273` · `64:6378` / `64:6446` |
| 27 | Fasaloo (فاسألوا) | `app` `ui/q_a/QAScreen.kt` | `26:1386` / — | `61:2118` / `61:2373` · `64:6544` / `64:6603` |
| 28 | Institute (coming soon, guest, not a channel member) | `feature-study` `StudyScreen.kt`, `dashboard/InstituteComingSoonContent.kt`, `GuestContent.kt`, `NotChannelMemberContent.kt` | `26:1338` / — | `61:2211` / `61:2458` · `64:6686` / `64:6733` |

Where Figma has no frame (— above), the Android code is the only reference: read it closely and screenshot the running app. Where a phone frame has no Dark version, build Dark from the tokens and compare with the Android app in dark mode.

### 2.2 Shell, behaviour and services (all must match)

- **Navigation graph:** every route in `MainActivity.kt` (`MainAppContent`'s `NavHost`) and `ui/navigation/Routes.kt`, with the same arguments and back behaviour.
- **Tabs (Compact):** bottom bar with Home (الرئيسية), Search (بحث), Institute (المعهد), Profile (حسابي), shown only on the four tab roots (`BottomNavItem.kt`, `BottomNavigationBar.kt`).
- **Tablet (Medium 600–839, Expanded ≥ 840):** follow `docs/plans/tablet-design.md` of the Android repo exactly: navigation rail on every main screen except the designs viewer, window margins, list 400 + gap 24 + detail panes, `EmptyDetail`, selected-item style, grids at fixed cell widths, share preview as a dialog. Code: `core-ui` `theme/AdaptiveLayout.kt`, `components/AdaptivePanes.kt`, `AppNavigationRail.kt`, `EmptyDetail.kt`, `SelectedItemStyle.kt`, and `MainActivity.kt` (selection kept per route). iPads in Split View and Stage Manager produce any width, so use the same width breakpoints.
- **Startup gate** (`MainActivity`, `MainActivityViewModel`): splash stays until theme, onboarding flag and auth state are ready → update check (`config/app_config`: `minVersionCode` = forced, `latestVersionCode` = optional, dismissible; observed only while signed in) → onboarding if not completed → sign-in if signed out → main app.
- **Locale:** Arabic forced on every device (`LocaleForce.kt`), RTL everywhere.
- **Theme:** Auto (follow system), Light, Dark; persisted; animated theme switch (`AnimatedTheme.kt`); status-bar icon colour follows the app's theme, not the system's.
- **Motion** (`theme/Motion.kt`, `MotionModifiers.kt`, `SharedElements.kt`): fade-through between tabs (out 90 ms, in 210 ms, scale 0.92), shared-axis X (30 dp, 300 ms, mirrored in RTL) for everything else, emphasized easings, container transforms card → screen for article, audio and design, and the reduced-motion variants (plain 100 ms fade).
- **Data:** Firestore collections `articles`, `audios`, `videos`, `image_groups` (+ subcollection `images`), `config/app_config`, `students`. Ordering by `publishDate` desc, cursor paging with `startAfter`, incremental sync by `updatedAt > lastSync`, soft delete by `isDeleted` (older docs use `deleted`, and older audio docs keep the category in `type`; see `toAudioDtoSafe` in `AudioFirestoreSource.kt`). Local cache in Room (`core-database`) so content shows offline. Read the DTOs in `core-network` `data_firebase/model/` and mirror the field names exactly.
- **Preferences** (`LocalDataStore.kt`): `onboarding_completed`, `dark_theme_enabled`, `theme_follow_system`, `reader_font_step`, `recent_searches`, `playback_speed`, plus the last-sync timestamps.
- **Offline:** network status stream; "offline / back online" messages on Home; cached content when offline.
- **Audio:** background playback with lock-screen controls, play/pause, seek, skip forward/back, speed cycle `1, 1.25, 1.5, 2, 0.75` persisted, download for offline with progress and cancel (file `${audioId}_${urlHash}.mp3` in app storage), switch to the local file after download. Source: `AudioDetailViewModel.kt`, `core-player`, `data/FileDownloader.kt`.
- **Share:** article passage → branded quote image; audio clip (60 s, or 30 s chip) → 9:16 1080×1920 MP4 with a waveform synced to the real audio, then the system share sheet. Read `share-audio-video.md` and `share-video-redesign.md` in full.
- **Search:** Algolia index `content_index`, facet filter `isDeleted:false` plus a type filter, per-type counts on the chips, 300 ms debounce, recent searches (max 8), the "too short" rule and whole-word Arabic highlighting from `ArabicSearchText`. Filters: الكل, مقالات, صوتيات, فيديوهات, صور.
- **Push:** FCM topics `all_users` (always when signed in), `student_broadcasts` and the student's `batch` (when `isCourseMember`). Every received message is stored locally for the Notifications screen; Home shows the unread badge. Notification permission is requested at startup.
- **Text utilities to port 1:1, together with their unit tests:** everything in `core-domain` `domain/text/` (ArabicDates, ArabicNumerals, ArabicSearchText, ArticleText, ArticleTextCleaner, AudioTitleCleaner, BidiText, CalendarDates, DatedEntry, DesignTitle, InlineBold, QuoteSelection, ShareTitleParser, TextSanitizer), `domain/media/ClipWindow.kt`, `core-ui` `util/HijriCalendar.kt`, and `FixedCategories` in `module/ContentCategory.kt`. The existing tests (`*/src/test/**`) are the spec: port every test case, including `NoTatweelInStringsTest`.
- **Strings:** copy every Arabic string verbatim from the `values/` and `values-ar/` `strings.xml` files of the modules in scope. Never retype or translate them.

### 2.3 Out of scope (keep it out unless the user says otherwise)

- The admin app and every admin use case (upload, edit, delete, admins, channels management).
- The institute dashboard behind the "coming soon" card (levels journey map, playlists, lessons, quizzes, leaderboard) and its background sync in `HiltApplication`. Port only the states the app shows today.
- The Telegram verification flow: unfinished on Android (`TelegramVerificationActivity` does nothing). Register the URL scheme only if the user asks.
- The Maintenance screen: in Figma but not in the Android app (`maintenanceMode` is never read). Do not build it unless the user decides to add it to both apps.

## 3. Technology mapping

Look up the current stable version of every package on pub.dev before adding it, check that it is maintained and supports iOS, and record the choice and version in `docs/decisions.md`. Never guess versions. Prefer fewer dependencies.

| Need | Android today | Flutter (default choice; verify) |
|---|---|---|
| DI + screen state | Hilt + ViewModel + StateFlow | `flutter_riverpod` (Notifier / AsyncNotifier; providers as DI) |
| Navigation | Navigation Compose | `go_router` with `StatefulShellRoute` for the tabs |
| Immutable models and UI state | data classes, sealed interfaces | Dart records and sealed classes; `freezed` where `copyWith` and equality help |
| Firebase | Firebase BoM | `firebase_core`, `firebase_auth`, `cloud_firestore`, `firebase_messaging` (add `cloud_functions` only if the user app really calls a function) |
| Google sign-in | Credential Manager | `google_sign_in` (+ `sign_in_with_apple`, section 6) |
| Local database | Room | `drift` |
| Preferences | DataStore | `shared_preferences` (async API) |
| Audio | Media3 ExoPlayer + MediaSessionService | `just_audio` + `audio_service` + `audio_session` |
| Downloads | OkHttp | `dio` + `path_provider` |
| YouTube | android-youtube-player | `youtube_player_iframe` (or the best maintained option) |
| Images | Coil | `cached_network_image` |
| Lottie | lottie-compose | `lottie` (reuse `core-ui/src/main/res/raw/loading_animation.json`) |
| Search | Algolia InstantSearch | the official Algolia Dart client or `algolia_helper_flutter` |
| Markdown (legal screens) | compose-markdown | a maintained Markdown renderer (`flutter_markdown` is discontinued: pick its maintained successor) |
| Share sheet, links | Intents, FileProvider | `share_plus`, `url_launcher` |
| Connectivity | ConnectivityManager | `connectivity_plus` |
| App version | PackageManager | `package_info_plus` |
| Icons | Tabler outline vector drawables | the same Tabler icons as SVG (stroke 2) with `flutter_svg`. Convert the existing drawables mechanically so the geometry is identical |
| Window size classes | material3-adaptive | `MediaQuery.sizeOf` + your own `LayoutTokens` (same breakpoints) |
| Clip → video export, waveform | Media3 Transformer, MediaCodec | **native Swift (AVFoundation)** behind a typed **Pigeon** API |
| Native splash | core-splashscreen | `flutter_native_splash` + a Flutter exit animation |
| Tests | JUnit, Compose UI tests | `flutter_test` (unit, widget, golden), `mocktail`, `integration_test` |

## 4. Architecture and layout

Feature-first, mirroring the Android modules, in one package (no melos; explain the trade-off in the notes):

```
lib/
  main.dart, app.dart, bootstrap.dart
  core/
    design_system/   tokens (colors, typography, spacing, radius, motion, layout), theme, icons, components/
    navigation/      router, routes, shell (bottom bar, rail, window margin), transitions
    data/            firebase sources, drift database, preferences, connectivity, downloader
    domain/          models, repository interfaces, text utilities (ported)
    player/          audio handler
  features/
    splash, onboarding, auth, home, notifications, articles, audio, video, designs,
    search, profile, about_sheikh, fasaloo, institute, share
      each: data/ domain/ presentation/ (screens, widgets, controllers)
ios/Runner/          Swift: ShareVideoExporter, WaveformAnalyzer behind Pigeon
pigeons/             Pigeon API definitions
test/  integration_test/  study-notes/  docs/
```

Rules:
- Name things after their Android counterparts (`BrandPalette`, `AdaptiveLayoutTokens`, `AppTopBar`, `ArticleItem`, `AudioCard` ...), so the two codebases can be compared file by file.
- Build reusable widgets once in `core/design_system/components` or the feature's `widgets/`, the way the Android app does, and reuse them in tablet layouts instead of rebuilding them.
- Screens read colours only from the theme extension (`context.brand.<token>`), sizes only from tokens or named constants next to the screen (with the Figma node id in a comment). **No hex values or magic numbers in screens.**
- Use `start`/`end` (`EdgeInsetsDirectional`, `AlignmentDirectional`), never left/right.

## 5. Design system rules

- **Colours:** `BrandPalette` as a `ThemeExtension` with `light` and `dark` instances and exactly the fields and values of `BrandColors.kt` (`background`, `surface`, `surfaceMuted`, `divider`, `textPrimary`, `textSecondary`, `textMuted`, `accent`, `accentStrong`, `accentContainer`, `onAccentContainer`, `accentText`, `gold`, `goldSoft`, `goldText`, `goldStroke`, `onGold`, `success`, `successContainer`, `onSuccessContainer`, `danger`, `fasalooTeal`, `viewerBackground`, `onViewerBackground`, `isDark`), plus the fixed dark `BrandTokens` and the share-frame colours. Map the Material `ColorScheme` the way `core-ui` `theme/Theme.kt` does.
- **Typography:** bundle the Cairo TTFs from `core/core-ui/src/main/res/font/`. Port `CairoTypography` (`theme/Type.kt`) and every inline `TextStyle` the screens use. Compose `lineHeight` in sp becomes Flutter `height = lineHeight / fontSize`; check `leadingDistribution` against the Android screenshot. `letterSpacing` 0.5 sp = 0.5 logical px.
- **Spacing and radius:** Figma `space/4,8,12,16,20,24,32`, `radius/9,10,12,14,16,20,full`.
- **Layout tokens:** port `AdaptiveLayoutTokens` (Compact, Medium, Expanded) with the same values and an inherited provider; Compact is the default.
- **Motion:** port `Motion.kt` constants and curves as Flutter `Duration`s and `Cubic`s; reduced motion follows `MediaQuery.disableAnimationsOf` (iOS "Reduce Motion").
- **Illustrations:** the light and dark variants in `core-ui/src/main/res/drawable` (`*_illu.xml`, `*_illu_dark.xml`, `network_error*`, `journey_illu*`) converted to SVG; raster images (`dr_hassan_photo.jpg`, `institute_logo.jpg`, `new_logo.JPG`, ...) copied as is.

## 6. iOS requirements and open decisions (ask the user; never decide alone)

Ask these in Stage 0 with `AskUserQuestion` (at most four per call, recommended option first) and record the answers in `docs/decisions.md`:

1. **Sign in with Apple.** App Store guideline 4.8: an app whose only login is a third-party service (Google) must also offer an equivalent privacy-focused login, which in practice means Sign in with Apple. Figma has no design for it: propose a button that matches the Google button and get approval. Account deletion must also revoke the Apple token.
2. **Version gate on iOS.** `config/app_config` holds Android `versionCode`s and a Play Store `updateUrl`. Propose separate iOS fields (e.g. `iosMinBuild`, `iosLatestBuild`, `iosUpdateUrl`) that the user sets by hand in the Firestore console (the admin app does not know them), and decide the behaviour while they are missing.
3. **Store links.** Profile's rate and share actions and the bundled QR (`qr_play_store.png`) point to Google Play. The App Store id exists only after the App Store Connect record is created.
4. **Legal texts.** `licenses.md` lists Android libraries and must list the Flutter packages instead; check `privacy.md` and `terms.md` for Android or Google Play wording. Propose the changes; the user approves the text.
5. **Platform feel.** Keep the Android visuals and transitions, but decide: iOS edge-swipe back, scroll overscroll (iOS bounce or Android stretch), text-selection handles. Recommendation: identical visuals, native iOS gestures.
6. **Identifiers.** Bundle id, display name, minimum iOS version (at least what the Firebase plugins require), Apple Developer team, APNs key for push.
7. **Building iOS.** Mac or cloud CI (Codemagic / GitHub Actions macOS runner) and when.
8. **Notifications received while the app is killed.** On iOS, Dart code does not run for notification messages when the app is terminated, so they would not reach the in-app list. Options: a Notification Service Extension that writes to a shared App Group store, or accept the gap.
9. **Clip export on the Flutter Android dev build.** iOS gets the Swift exporter. On Android, either a stub ("not available in this build") or a port of the existing Kotlin Media3 exporter behind the same Pigeon API so it can be checked on the emulator.
10. **State management.** Confirm Riverpod (recommended: one tool for DI and state, close to Hilt + ViewModel, testable with overrides) or choose Bloc.

Platform setup you handle without asking: `UIBackgroundModes` audio and remote-notification, push capability, Arabic as `CFBundleDevelopmentRegion` and in `CFBundleLocalizations` (so system dialogs appear in Arabic), Info.plist usage strings (Arabic), privacy manifest (`PrivacyInfo.xcprivacy`), the Google Sign-In URL scheme, app icon and launch screen from the Android assets.

## 7. How to work: every screen, every stage

1. **Read** the Android code for the screen and every component it uses, its ViewModel and UI state, and its strings. List its states and interactions in `docs/parity-matrix.md`.
2. **Get Figma:** phone Light and Dark, then tablet Expanded and Medium; save the PNGs to `docs/figma/`.
3. **Screenshot the Android app** in the same state:
   - Phone: an AVD of 1080×2400 at 480 dpi (= 360×800 dp, the Figma phone size).
   - Tablet: the Pixel Tablet AVD (1280×800 dp landscape, 800×1280 dp portrait).
   - Install from the Android repo with `./gradlew :app:installDebug`; capture with `adb exec-out screencap -p > docs/android/<screen>-<class>-<theme>.png`.
4. **Build** the Flutter screen from the shared widgets, for Compact, Medium and Expanded at once.
5. **Verify** (all required before the screen counts as done):
   - **Golden tests** at 360×800, 800×1280 and 1280×800 in Light and Dark, with the Cairo font loaded in `flutter_test_config.dart` (the default test font renders boxes), fed with fixture data. Generate goldens on one OS only, because rendering differs between Windows and macOS.
   - **Dimension tests** for the measured numbers (rail 80, list pane 400, gap 24, margins, card sizes).
   - **Side-by-side check:** Figma PNG, Android screenshot and Flutter screenshot of the same state. Fix every visible difference; list anything you cannot match, with the reason.
   - **Behaviour check** against the parity matrix: loading, empty, error, offline, success, navigation, persisted state, rotation and resize (selection and scroll survive), RTL.
   - `flutter analyze` clean, all tests green.
6. **Report and stop** (section 10).

## 8. Stages

One commit per stage, or per screen inside a stage. Each stage lists the lessons it adds (section 9.4). The code and the lessons that explain it go in the same commit.

- **Stage 0 · Discovery (no app code).** Read the Android repo (`settings.gradle.kts`, `gradle/libs.versions.toml`, the manifest, `MainActivity.kt`, `core-ui` theme and components, every feature listed in 2.1, `core-domain`, `core-network`, `core-database`, `data`) and the design notes. Confirm the Figma frame ids with `get_metadata`. Create `docs/port-brief.md` (this prompt, verbatim), `CLAUDE.md` (imports `@docs/port-brief.md`, `@docs/decisions.md`, `@docs/progress-log.md`, plus a five-line summary of the working rules), `docs/parity-matrix.md` (one row per screen, state, component, behaviour, preference key, Firestore field and string group, each with the Android source, Flutter target and status), `docs/decisions.md`, `docs/progress-log.md`. Ask the section 6 questions. Write the `study-notes/` skeleton and the orientation lessons.
- **Stage 1 · Project setup.** `flutter create` (iOS and Android), lints, folder layout, env files via `--dart-define-from-file` (gitignored, with a committed `env/example.json`), fonts, assets, SVG icons and illustrations converted, Firebase via `flutterfire configure` (the user runs `firebase login` with `! firebase login`), a CI skeleton. Runs on the Android emulator.
- **Stage 2 · Design system.** Tokens, theme (light/dark), typography, icons, illustrations, every shared component from `core-ui` and the feature `components/` folders (AppTopBar, ArticleItem, ArticleCard, AudioCard, AudioListRow, CategoryGridTile, CategoryRow, SearchBar, FilterPill, VideoCard, DesignTile, SeekBar, TransportRow, ProfileRow, buttons, tabs, shimmer, Lottie loading, EmptyDetail, AppNavigationRail, pane surface, selected-item style ...). A debug-only **Gallery** route showing every component in Light and Dark, RTL. Golden tests for each component.
- **Stage 3 · Shell and navigation.** Every route from section 2.2 (screens as placeholders), tabs with preserved back stacks, bottom bar on Compact, rail on Medium and Expanded, window margins, transitions with reduced motion, startup gate, forced Arabic, theme mode persistence and animated switch, status-bar style, permission request timing.
- **Stage 4 · Data layer.** Models, Firestore sources with tolerant parsing, the Drift database mirroring the Room entities the user app uses (articles, audios, videos, image groups and images, notifications, student), preferences, repositories with the same paging and sync rules, connectivity, auth repository, the ported text utilities and their tests.
- **Stage 5 · First-run and system screens.** Splash, Welcome / sign-in (Google, and Apple if approved), Onboarding, Update (optional and required).
- **Stage 6 · Home and Notifications.** Header, announcement carousel, category tiles, latest articles and audios, unread badge, offline messages.
- **Stage 7 · Articles.** List with paging, Reader (font-size step, reading progress), two-pane on Expanded, passage selection, quote card image and share.
- **Stage 8 · Audio.** Categories, lists, Player (background playback, lock-screen controls, speeds, seek, downloads), two-pane on Expanded.
- **Stage 9 · Audio clip share.** Clip selector (60 s / 30 s), live preview, waveform, the Pigeon API and the Swift AVFoundation exporter, share sheet, temp-file cleanup (the sweep on start, as in `ShareFileStore`).
- **Stage 10 · Videos.** Categories (phone), filter pills (tablet), grid at the designed cell width, YouTube player.
- **Stage 11 · Designs.** Grid, immersive viewer (pinch zoom, thumbnails, counter, share).
- **Stage 12 · Search.** Algolia, filters with counts, recent searches, highlighting, detail pane on Expanded.
- **Stage 13 · Profile.** Account card, appearance (Auto / Light / Dark), settings, About the app, Share the app, Support, legal texts, sign out, delete account with confirmation.
- **Stage 14 · About the Sheikh, Fasaloo, Institute.** Including the Telegram link and the institute states.
- **Stage 15 · Push and deep links.** FCM on iOS (APNs), topics, local storage of received messages, opening the app from a notification.
- **Stage 16 · Polish and parity audit.** Container transforms, motion review, accessibility (VoiceOver labels, Dynamic Type up to 200%, 48 dp / 44 pt touch targets, focus order), performance (DevTools, no jank on scroll), full golden suite, walk the whole parity matrix.
- **Stage 17 · iOS release readiness.** Signing, capabilities, privacy manifest, App Store privacy answers, screenshots, TestFlight via the chosen CI, App Store review checklist. Nothing is uploaded without the user's explicit go-ahead.

## 9. Study notes

### 9.1 Reader and goal
A **senior Android engineer** (Kotlin, Compose, Hilt, Coroutines/Flow, Room, Media3) who knows basic Flutter. Goal: from zero to hero in Flutter fast, by learning from this exact app. Skip general programming; anchor every concept to its Android equivalent and explain where Flutter thinks differently.

### 9.2 Folder layout

```
study-notes/
  README.md                 roadmap, how to study, level descriptions, full lesson index with status checkboxes
  00-orientation/
  01-beginner/              can build static screens
  02-intermediate/          can build a data-driven feature end to end
  03-advanced/              platform integration, background work, custom rendering, performance
  04-expert/                shipping on iOS, CI/CD, internals, architecture at scale
  cheatsheets/
    kotlin-to-dart.md
    compose-to-flutter-widgets.md
    android-libraries-to-pub-packages.md
    flutter-cli-and-tooling.md
    debugging-recipes.md
  glossary.md
```

File names: `L07-theming-and-theme-extensions.md`. The number is the **reading order** across the whole course, which can differ from the order in which lessons are written.

### 9.3 Lesson template (every lesson uses these headings)

```
# L07 · Theming: ThemeData and ThemeExtension
Level: Beginner · Reading time: ~35 min · Written in: Stage 2 (commit <hash>)

## Why this matters in our app
## Coming from Android            (a mapping table: Compose/Kotlin ↔ Flutter/Dart)
## Core concepts                  (explanations with short snippets)
## In our codebase                (real excerpts, max ~40 lines each, with file paths)
## Pitfalls for Android developers
## Check yourself                 (3–5 questions, answers inside <details>)
## Practice                       (one small exercise on this app's code)
## Go deeper                      (official docs only: dart.dev, docs.flutter.dev, pub.dev, Apple, Firebase)
```

### 9.4 Depth and style
- Medium detail: each lesson reads in 20–45 minutes (about 1,500–3,000 words). Longer than that: split it. Shorter than 800 words: merge it.
- Tables and short code over long prose. Mermaid diagrams where they help (widget trees, data flow, navigation).
- Code excerpts come from this repo and must compile. When a later stage changes quoted code, update the lesson in the same commit.
- Explain the *why* (trade-offs, what we chose and what we did not), not only the *how*.
- Keep `README.md`'s index, status checkboxes and the cheatsheets current after every stage.
- Write the notes in English; Arabic appears only inside app examples.

### 9.5 Curriculum (adjust as you go; keep the README in sync)

| # | Lesson | Level | Written in stage |
|---|---|---|---|
| L00 | How to use these notes, roadmap and study plan | Orientation | 0 |
| L01 | The Android → Flutter mental map | Orientation | 0 |
| L02 | Dart for Kotlin developers (null safety, classes, records, sealed, extensions, mixins) | Beginner | 1 |
| L03 | How Flutter works: widgets, elements, render objects, hot reload | Beginner | 1 |
| L04 | Project anatomy, pubspec, lints and the CLI | Beginner | 1 |
| L05 | Widgets and composition: Stateless, Stateful, keys | Beginner | 2 |
| L06 | Layout: constraints go down, sizes go up | Beginner | 2 |
| L07 | Theming: ThemeData, ThemeExtension, light and dark | Beginner | 2 |
| L08 | Text, fonts, Arabic and RTL | Beginner | 2 |
| L09 | Assets: images, SVG icons, Lottie | Beginner | 2 |
| L10 | Widget tests and golden tests | Beginner | 2 |
| L11 | Lists and scrolling: ListView, GridView, slivers | Beginner | 6 |
| L12 | Navigation with go_router (vs Navigation Compose) | Intermediate | 3 |
| L13 | State management with Riverpod (vs Hilt + ViewModel + StateFlow) | Intermediate | 3 |
| L14 | Adaptive layouts: window classes, rail, two panes, iPad | Intermediate | 3 |
| L15 | Async Dart: Future and Stream (vs coroutines and Flow) | Intermediate | 4 |
| L16 | Firebase in Flutter: setup, Auth, Firestore | Intermediate | 4 |
| L17 | Local persistence: preferences and Drift (vs DataStore and Room) | Intermediate | 4 |
| L18 | Architecture: feature-first, repositories, DI with providers | Intermediate | 4 |
| L19 | Code generation: build_runner, freezed, drift | Intermediate | 4 |
| L20 | Unit testing and mocking | Intermediate | 4 |
| L21 | App startup: native splash, async init, gating | Intermediate | 5 |
| L22 | Authentication: Google and Sign in with Apple | Intermediate | 5 |
| L23 | Carousels, PageView and implicit animations | Intermediate | 6 |
| L24 | Streams in the UI: connectivity, live data, snackbars | Intermediate | 6 |
| L25 | Pagination with a local cache | Intermediate | 7 |
| L26 | Rich text, selection and the reading experience | Intermediate | 7 |
| L27 | Search: debounce, request state, Algolia | Intermediate | 12 |
| L28 | Settings, dialogs, url_launcher and share_plus | Intermediate | 13 |
| L29 | Background audio with just_audio and audio_service | Advanced | 8 |
| L30 | Downloads: files, progress, cancellation | Advanced | 8 |
| L31 | CustomPainter and rendering widgets to images | Advanced | 7 |
| L32 | Platform channels and Pigeon | Advanced | 9 |
| L33 | Swift and AVFoundation for Android developers | Advanced | 9 |
| L34 | Isolates and heavy work off the UI thread | Advanced | 9 |
| L35 | Platform views: embedding the YouTube player | Advanced | 10 |
| L36 | Gestures, InteractiveViewer and Hero | Advanced | 11 |
| L37 | Push notifications on iOS (APNs and FCM) | Advanced | 15 |
| L38 | Deep links and URL schemes | Advanced | 15 |
| L39 | Animations in depth and reduced motion | Advanced | 16 |
| L40 | Performance and DevTools | Advanced | 16 |
| L41 | Accessibility: Semantics, VoiceOver, text scaling | Advanced | 16 |
| L42 | iOS for Android developers: Xcode, Info.plist, capabilities, signing | Expert | 17 |
| L43 | Flavors, environments and secrets | Expert | 17 |
| L44 | CI/CD: building iOS without a Mac, TestFlight | Expert | 17 |
| L45 | App Store review: privacy manifest, account deletion, login rules | Expert | 17 |
| L46 | Flutter internals: BuildContext, element lifecycle, custom RenderObjects | Expert | 17 |
| L47 | Scaling the codebase: packages and modularization (vs Gradle modules) | Expert | 17 |

## 10. Working rules

- Work **one stage at a time**. At the end of each stage: `flutter analyze`, all tests, update `docs/parity-matrix.md` and `docs/progress-log.md`, write the stage's lessons, commit, then **stop and report**:
  - what was built, with the verification evidence (golden and screenshot paths);
  - every deviation from the Android app or Figma, and why;
  - Figma node ids used;
  - lessons written;
  - open questions and the next stage.
  Wait for the user's go-ahead before the next stage.
- Git: `git init` in Stage 0; one commit per stage or screen with a clear message. Never push, create remotes, upload builds or publish anything without asking.
- Do not guess. When the Android code, Figma and this brief do not answer something, ask.
- Never change the Android repo, Firestore data, Firebase settings or Algolia indexes. Changes there are the user's to make: say exactly what is needed.
- When this brief turns out to be wrong about the Android code, the code wins: fix `docs/port-brief.md` and tell the user.

## 11. Start now

Do **Stage 0 only**: read, map, create the docs, the study-notes skeleton and the orientation lessons (L00, L01), ask the section 6 questions, then report and stop.

---- END PROMPT ----
