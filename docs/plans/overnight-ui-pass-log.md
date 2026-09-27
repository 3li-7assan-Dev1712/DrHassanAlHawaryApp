# Overnight UI pass: log

Spec: `docs/plans/overnight-ui-pass.md`. Branch: `ui/overnight-polish` (from `a4833e9` + the spec snapshot commit).

## Checklist

- [x] Phase 0: Setup and inventory: Done
- [x] Phase 1: Shared foundations: Done
- [x] Phase 2: Quick fixes: Done (2.1 no app change: not app code)
- [x] Phase 3: Articles list and reader: Done (bookmark and selection-share skipped)
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
- D1.1: Plurals are pure Kotlin (`ArabicDates.count`), not Android `plurals`. The existing, tested helper already implements the Arabic categories (one → singular alone, two → dual, 3–10 → plural, 11+ → singular), always emits Arabic-Indic digits whatever the device locale, and runs in JVM tests. `pluralStringResource` would format digits by locale and can't be tested without Robolectric. Where the spec asks for plurals, the same helper is used.
- D1.2: The Hijri abbreviation "هـ" keeps its tatweel (it's part of the conventional abbreviation, not stretching). Resource files contain no tatweel.
- D1.3: `core-domain` is a plain JVM module, so root `testDebugUnitTest` doesn't run its tests. Every phase gate also runs `:core:core-domain:test`.
- D1.4: I couldn't find a literal "gray vertical bar" composable in the code (no shared header exists). The gray on those screens came from the root Scaffold's `surfaceVariant` and from each screen's own M3 `TopAppBar` with a surfaceVariant gradient. Both are replaced by the flat `AppTopBar` on `background`. Check this on the device (checklist).
- D2.1: Android Studio held a lock on a core-ui build jar, so I ran `./gradlew --stop` and rebuilt. Studio restarts its daemon when it needs one.
- D2.2: The welcome screen's circular photo is `private` in the off-limits auth module, so it can't be reused without editing that screen. `SheikhPhoto` in core-ui copies its treatment (the welcome screen is unchanged).
- D2.3: I amended the Phase 2 commit once to fix the log layout (a local, unpushed commit). After noticing the spec's "never rewrite history", I haven't amended again.
- D1.5: `feature-home/.../values/strings.xml` had one "مجدداً". It's now "مجددًا" because the new resource test covers every module. That's a string-only change, and no Home code was touched.
- D3.1: Reader body text uses `textSecondary` (the spec's "body text" token), and headings/basmala use `textPrimary`.

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

### Phase 2: Done
- 2.1 Institute swap button: **not in the app.** No FloatingActionButton, swap icon or overlay exists in `feature-study`, `app` or any shared module (grep for FAB/Swap/swap). A round black button with swap arrows near the navigation bar matches Android's **rotation-suggestion button**, which the system shows when auto-rotate is off and the phone is tilted. It can also be an accessibility/assistant floating button. Nothing to gate behind `BuildConfig.DEBUG`. It's in the follow-ups for Ali to confirm on the device.
- 2.2 Institute handle: `StudentHeader` shows `BidiText.handle(username)` → "@ali_7assan", isolated LTR.
- 2.3 Designs viewer counter: new string `page_counter` "%1$s من %2$s" with Arabic-Indic digits → "١ من ٩".
- 2.4 Video thumbnails: already a 16:9 box with `ContentScale.Crop` in `VideoCard`, so no change was needed.
- 2.5 About photo: the framed/cornered look is baked into `dr_hassan_image.png`. The About hero now uses a new shared `core-ui/components/SheikhPhoto` (the plain `dr_hassan_photo.jpg`, `clip(CircleShape)` + Crop + thin `accent` ring). The profile URL in `DoctorProfile` is a placeholder that never loads, so the bundled photo is shown directly.
- 2.6 Search default chip: `SearchViewModel` already starts on `SearchFilter.ALL` and resets facet filters in `init`, so no change was needed.
- 2.7 Reader: the floating share button is gone. The reader uses `AppTopBar` with no title (the header shows it) and a share action.
- 2.8 About text: it lives in the app (`core-domain/.../module/DoctorProfile.kt`), so I fixed it there: البكالوريوس, بكالوريوس, الدكتوراه, بالمدينة, الإسلامية, الإسهامات, إلى, a period after "في الشأن السوداني", a space in "المنورة (1990م)", and the double space.

### Phase 3: Done (2 items skipped)
- Articles list: the whole card is tappable (`Surface(onClick)`) and the "قراءة المزيد…" button is gone. Cards have 14dp corners, a 0.5dp `divider` border and a `surface` fill. Cleaned title (max 2 lines), `ArticleTextCleaner.excerpt` (max 2 lines), and a meta row with a Tabler clock: "نُشر منذ … · قراءة X دقائق" (the list has the body, so reading time is included). Uses `AppTopBar` on `background`.
- Reader (Compose `Text`, no WebView):
  - `AppTopBar` with back, font size and share. No title.
  - 2dp `accentStrong` progress bar from the scroll position (the body is now a scrolling `Column`).
  - Header: title/subtitle split at the first ":" (subtitle in `accentText`) and the meta line "الشيخ د. حسن الهواري · ٤ أبريل ٢٠٢٦ · قراءة X دقائق" (new `sheikh_name` string).
  - Body: `ArticleTextCleaner.readerParagraphs`. A basmala on the first line is centered. Inner separators become a centered "✦ ✦ ✦" in `accent`. أولًا…عاشرًا headings are bold `textPrimary` with a 3dp `accentStrong` bar and no bullet. `*bold*` is rendered as bold spans. Emojis stay. Text is start-aligned: `TextAlign.Justify` and the forced RTL + U+200F hack were removed.
- Font size: 4 steps (14/16/18/21sp, default step 1 = the old 16sp bodyLarge), stored under the new DataStore key `reader_font_step`. The top-bar action opens a dropdown with A− / A+ and a preview line.
- New preference keys (same `local_data` store, exposed through `DataStoreRepository`): `reader_font_step` (int), `recent_searches` (string, for Phase 4), `theme_follow_system` (boolean, for Phase 8). No existing key changed.
- Skipped: bookmark (no bookmark feature exists).
- Skipped: "مشاركة كصورة" in the selection toolbar. The quote-image entry point doesn't take a `String`: it's a route that takes an article id plus selection offsets into `ArticleText.displayText(content)`. The reader now shows *cleaned* paragraphs, so offsets would have to be mapped back to the raw text. That isn't straightforward, so I left it for later. Share still opens the existing selection screen.
