# Overnight UI pass: log

Spec: `docs/plans/overnight-ui-pass.md`. Branch: `ui/overnight-polish` (from `a4833e9` + the spec snapshot commit).

## Checklist

- [x] Phase 0: Setup and inventory: Done
- [ ] Phase 1: Shared foundations
- [ ] Phase 2: Quick fixes
- [ ] Phase 3: Articles list and reader
- [ ] Phase 4: Search
- [ ] Phase 5: Fatwas list
- [ ] Phase 6: Videos
- [ ] Phase 7: Designs and viewer
- [ ] Phase 8: Profile
- [ ] Phase 9: About the Sheikh
- [ ] Phase 10: Institute
- [ ] Phase 11: فاسألوا
- [ ] Phase 12: Consistency pass
- [ ] Phase 13: Final report

## Phase 0: inventory

- **Baseline build:** `./gradlew assembleDebug testDebugUnitTest` passed (1m 29s). Later phases are verified.
- **Theme:** `core/core-ui/.../theme/`: `Theme.kt` (`HassanAlHawaryTheme`, light + dark Material schemes from top-level `Color.kt`), `BrandColors.kt` (`BrandTokens` fixed dark palette; `BrandPalette` with `DarkBrandPalette` / `LightBrandPalette` provided through `LocalBrandPalette`, read with `Brand.colors`). Both light and dark exist. Existing brand tokens: background, surface, divider, goldSoft, gold, goldText, goldStroke, onGold, textPrimary, textSecondary, textMuted. `app` and `admin` theme files delegate to core-ui.
- **Top bar:** there is no shared secondary-screen top bar. Each screen builds its own M3 `TopAppBar` (article reader, videos, audio list, etc.). `app/.../ui/components/TopAppBar.kt` exists but is unused. The root `Scaffold` in `MainActivity` paints `surfaceVariant` (#E0E0E0 gray) behind every non-home screen, including the status bar strip. Institute uses `CustomTobAppBar.kt` (teal strip).
- **Date phrases:** `core-ui/util/RelativeTime.kt` (`getRelativeTimeText`), used with `R.string.published_since` in `feature-article/.../ArticleItem.kt` and `feature-video/.../VideoCard.kt` (this causes "نُشر منذ منذ…" and "نُشر منذ ٤ أبريل"). Home uses `ArabicDates.relative` (off-limits screen).
- **Existing helpers (reused, never duplicated):** `core-domain/.../text/`: `ArabicNumerals` (digits, durations, Hijri/Gregorian formatting), `ArabicDates` (Arabic number agreement, `relative`, `readingTime`), `ArticleTextCleaner` (title/body/excerpt/reading minutes), `ArticleText` (title cleaner, byline detection), `TextSanitizer` (quote cleaning), `ShareTitleParser`, `ArabicCalendarNames`, `CalendarDates`. All have JVM tests. Icons: `core-ui/icons/TablerIcons.kt` + 12 `ic_tabler_*` drawables. There is no circular sheikh-photo component (checked later in Phase 9).
- **Preference store:** DataStore `local_data` in `core-database/LocalDataStore.kt`, exposed through `DataStoreRepository` (core-domain) → `DataStoreRepositoryImpl` (data). Theme: `dark_theme_enabled` boolean (default false = light), `brand_theme` string. `MainActivityViewModel.themeState` reads it.
- **Unit-test setup:** every module has `src/test` with JUnit. `core-domain` has the text-helper tests, and pure-Kotlin helpers go there. `core-ui` has `NoTatweelInStringsTest` (it scans every `values*/*.xml`).
- **Screens:**
  - Articles list: `feature-article/.../list/ArticleListScreen.kt` + `components/ArticleItem.kt`; mapper `data/mapper/ArticleMapper.kt`
  - Article reader: `feature-article/.../detail/ArticleDetailScreen.kt` (+ VM, UiState)
  - Fatwas list: `feature-audio/.../list/AudioListScreen.kt` + `components/AudioListItem.kt`
  - Search: `feature-search/.../presentation/SearchScreen.kt`, `SearchViewModel.kt`, components, `mapper/SearchHitMapper.kt`, `utils/HighlightingUtil.kt`
  - Profile: `feature-profile/.../profile/ProfileScreen.kt`
  - Institute: `feature-study/.../StudyScreen.kt` + `dashboard/*`
  - فاسألوا: `app/.../ui/q_a/QAScreen.kt`
  - About the Sheikh: `feature-about-dr-hassan/.../AboutDrHassanScreen.kt`
  - Designs: `feature-image/.../list/ImageGroupsScreen.kt`, `components/ImageGroupRow.kt`, viewer `detail/ImageScreen.kt`
  - Videos: `feature-video/.../list/VideoScreen.kt` + `components/VideoCard.kt`
- **Screenshots:** there is no `docs/screenshots/`. A top-level `screenshots/` folder exists but dates from March and mostly shows the admin app, so it wasn't used.
- **Edge-to-edge:** already on (`enableEdgeToEdge` in `MainActivity`, with bar styles that follow the app theme).

## Decisions

- D0.1: The rejected shell call in Phase 0 was a `cd … && cat` read. From then on I used the Read/Grep tools instead of a retry.

## Phase notes
