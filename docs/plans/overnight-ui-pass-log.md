# Overnight UI pass: log

Spec: `docs/plans/overnight-ui-pass.md`. Branch: `ui/overnight-polish` (from `a4833e9` + the spec snapshot commit).

## Checklist

- [x] Phase 0: Setup and inventory: Done
- [x] Phase 1: Shared foundations: Done
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
- D1.1: Plurals are pure Kotlin (`ArabicDates.count`), not Android `plurals`. The existing, tested helper already implements the Arabic categories (one → singular alone, two → dual, 3–10 → plural, 11+ → singular), always emits Arabic-Indic digits whatever the device locale, and runs in JVM tests. `pluralStringResource` would format digits by locale and can't be tested without Robolectric. Where the spec asks for plurals, the same helper is used.
- D1.2: The Hijri abbreviation "هـ" keeps its tatweel (it's part of the conventional abbreviation, not stretching). Resource files contain no tatweel.
- D1.3: `core-domain` is a plain JVM module, so root `testDebugUnitTest` doesn't run its tests. Every phase gate also runs `:core:core-domain:test`.
- D1.4: I couldn't find a literal "gray vertical bar" composable in the code (no shared header exists). The gray on those screens came from the root Scaffold's `surfaceVariant` and from each screen's own M3 `TopAppBar` with a surfaceVariant gradient. Both are replaced by the flat `AppTopBar` on `background`. Check this on the device (checklist).

- D0.1: The rejected shell call in Phase 0 was a `cd … && cat` read. From then on I used the Read/Grep tools instead of a retry.

## Phase notes

### Phase 1: Done
- 1.1 Tokens: I added `surfaceMuted, accent, accentStrong, accentContainer, onAccentContainer, accentText, success, successContainer, onSuccessContainer, danger, fasalooTeal` to `BrandPalette`, with the spec's values in both `LightBrandPalette` and `DarkBrandPalette`. Existing tokens with the same purpose (`background, surface, divider, textPrimary, textSecondary, textMuted`) are reused and keep their current values. Nothing was renamed. Read them with `Brand.colors.*`.
- 1.2 Top bar: added `core-ui/components/AppTopBar.kt` (`AppTopBar` + `AppTopBarAction`): start-aligned title, optional Tabler arrow-right back, and end actions, flat on `background`. The root `Scaffold` in `MainActivity` painted `surfaceVariant` (#E0E0E0) behind every non-home screen. It now uses `Brand.colors.background`. Each screen adopts `AppTopBar` in its own phase.
- 1.3 `ArabicDates.published(now, then)` returns the full phrase, and `ArabicDates.calendarDate` gives "٤ أبريل ٢٠٢٦". `ArticleItem` and `VideoCard` use it now. The buggy `core-ui/util/RelativeTime.kt` is deleted (it had no other callers). Tests: `PublishedPhraseTest`.
- 1.4 Digits: I reused `ArabicNumerals.digits` rather than adding a second helper.
- 1.5 `BidiText.ltr` / `BidiText.handle` (LRI…PDI).
- 1.6 `AudioTitleCleaner`, `InlineBold` (strip/parse `*bold*`), `ArticleTextCleaner.cleanBody(raw, rawTitle)` (now also drops leading title/title-part lines and author-only lines, and strips `*`), `readerParagraphs` (keeps basmala, emojis and bold markers; an inner separator becomes "✦ ✦ ✦"), `sectionHeading`, `isBasmala`. Reading time already existed (`readingMinutes` + `ArabicDates.readingTime`). Tests: `DisplayCleanersTest`.
- 1.7 "اً" → "ًا" in every `values*/strings.xml` (4 strings) and in hardcoded UI text (ForceUpdate, Designs empty state, Profile delete row, Institute cards). The admin "الـمستوى" is now "المستوى". The existing `NoTatweelInStringsTest` gained a second test for "اً".
- 1.8 Downloaded 26 Tabler outline SVGs (all succeeded), converted them to `ic_tabler_*.xml` in core-ui, and registered them in `TablerIcons`. Licenses already credit Tabler Icons (MIT).
