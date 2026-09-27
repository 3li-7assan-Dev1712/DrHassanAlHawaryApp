# Overnight UI pass: log

Spec: `docs/plans/overnight-ui-pass.md`. Branch: `ui/overnight-polish` (from `a4833e9` + the spec snapshot commit).

## Checklist

- [x] Phase 0: Setup and inventory: Done
- [x] Phase 1: Shared foundations: Done
- [x] Phase 2: Quick fixes: Done (2.1 no app change: not app code)
- [x] Phase 3: Articles list and reader: Done (bookmark and selection-share skipped)
- [x] Phase 4: Search: Done (no audio durations in hits)
- [x] Phase 5: Fatwas list: Done (playing state/chips/search icon skipped per spec)
- [x] Phase 6: Videos: Done (chips/duration skipped: data not on screen/model)
- [x] Phase 7: Designs and viewer: Done
- [x] Phase 8: Profile: Done
- [x] Phase 9: About the Sheikh: Done
- [x] Phase 10: Institute: Done (channel button omitted: no URL)
- [x] Phase 11: فاسألوا: Done
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
- D8.1: New installs (no stored theme key) now start on "تلقائي" (follow the phone), as the spec asks. Before this, a new install defaulted to light.

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

### Phase 4: Done (audio durations not available)
- Tab-root `AppTopBar` "البحث" (no back arrow). New `SearchBar`: 48dp rounded `surface` field with a 0.5dp divider border, Tabler search icon, `textMuted` placeholder "ابحث في المقالات والصوتيات والفيديوهات" (bodyMedium, gone once text exists, distinct from `textPrimary` input), and a Tabler x clear button.
- Minimum query + debounce: `ArabicSearchText.isSearchable` (after a leading "ال" is removed, some word must have 2+ letters). Otherwise the new `SearchUiState.TooShort` shows "اكتب كلمة أطول قليلًا" and nothing is sent. Typing searches after a 300 ms debounce; the keyboard's search button searches immediately.
- Chip counts: the query asks Algolia for `facets = [type]`. This is a query parameter only: `type` is already filtered on, and index settings are untouched. Counts come from the latest unfiltered ("الكل") response and are kept while a type chip is selected ("مقالات ٥"). With no facet data there are no counts.
- "الكل" groups results by type (مقالات, صوتيات, فيديوهات, صور) with a header "صوتيات · ٩" and "عرض الكل", which selects that chip. Each group previews 3 rows from the current page of hits.
- Rows (`SearchResultRow`): a type icon (notebook/headphones/video/photo) in a 34dp `accentContainer` circle, the cleaned title (article → `ArticleTextCleaner.cleanTitle`, audio → `AudioTitleCleaner`, design → `DesignTitle`), and meta "مقال · قراءة ٥ دقائق" / "صوتية" / "فيديو" / "تصميم".
- Whole-word highlighting (bold `accentText`) from the new pure-Kotlin `ArabicSearchText` (tashkeel/tatweel/hamza/ة/ى/ؤ/ئ normalization plus one-prefix stripping). Tested: "احكام" matches "أحكام"; "الصيام" matches "صيام" and "والصيام"; "ال" matches nothing; no fragment highlights. Algolia's `<em>` fragment highlights and the old `HighlightingUtil` / result cards were removed.
- Article snippet: about 100 characters of `cleanBody` around the first matching word, snapped to word boundaries, with "…" on the cut sides (`ArabicSearchText.snippet`, tested).
- Empty state: the large magnifier illustration is replaced by suggested topic chips (الزكاة، الصيام، الحج، البيوع، الأسرة; a tap searches) and up to 8 recent searches (new DataStore key `recent_searches`, saved when a search is submitted or a result opened) with "مسح".
- Not done: audio durations in the meta ("صوتية · ١٨:٢٠"). Algolia hits don't carry a duration field in this codebase, so audio rows show "صوتية" only.
- `DesignTitle` (Phase 7 helper) was added here because Search needed it: "تصميم - 9 ذو الحجة 1447هـ" → "٩ ذو الحجة ١٤٤٧هـ", plus the صورة/صورتان/٣ صور/١١ صورة count.

### Phase 5: Done (playing state, category chips and search icon skipped by the spec's rules)
- Titles go through `AudioTitleCleaner` (display only; the title passed to the player route is unchanged).
- Compact rows: 40dp `accentContainer` play circle with an `onAccentContainer` Tabler play icon, title (max 2 lines), and the duration via `ArabicNumerals.formatDuration` (Arabic-Indic). Cards have 12dp corners, a 0.5dp divider and a `surface` fill.
- The lone ✓ (`DownloadDone`) is replaced by a Tabler download icon + "محفوظ" (new string `audio_saved`), driven by the existing `isDownloaded` flag.
- Playing state: skipped. The list screen has no access to the player, and `isPlaying` is always false from the mapper. The row keeps an `accentStrong` + pause look for when that flag is ever set.
- Category chips: skipped. The list is already opened per category (`categoryId` route argument from the category screen), so chips would duplicate that screen.
- Top-bar search icon: omitted. The Search route takes no preset filter.
- `AppTopBar` on `background` replaces the M3 TopAppBar and the gray gradient. The old `formatDuration` moved to `components/DurationFormat.kt` because the audio detail screen still uses it.

### Phase 6: Done (category chips and duration badge skipped by the spec's rules)
- Title: "الفيديوهات" (`R.string.videos`) when the category title is "الكل" or missing, otherwise the category name. Uses `AppTopBar` on `background`, with no gray gradient.
- Category chips: skipped. The list is paged by category (`getPaginatedVideoUseCase(categoryId)`), so all videos and their categories are never loaded on this screen.
- Thumbnail: `maxresdefault.jpg` first, falling back to the original `hqdefault.jpg` on error. Both use a 16:9 `ContentScale.Crop` box on `surfaceMuted`.
- Duration badge: skipped. `Video` has no duration field.
- Category pill restyled to an `accentContainer`/`onAccentContainer` pill. The play overlay is 40dp with a Tabler player-play icon.
- Date: `ArabicDates.published` (Phase 1) with a Tabler clock. Cards have 14dp corners, a 0.5dp divider and a `surface` fill, and the whole card is tappable.

### Phase 7: Done (save-to-gallery left as a follow-up)
- List: `LazyVerticalStaggeredGrid` with 2 columns (available in the current Compose BOM 2025.07). Each image keeps its own proportions (`ContentScale.FillWidth`); loading and error placeholders are 4:5. Uses `AppTopBar` on `background`.
- Tile (`DesignTile`, replacing the per-group horizontal `ImageGroupRow`): 10dp-corner image, title up to 2 lines that wraps rather than ellipsizing, and date meta (`ArabicDates.published`). A multi-image post is one tile with a count badge ("٩ صور", `DesignTitle.imageCount`: صورة/صورتان/٣ صور/١١ صورة). The count comes from the existing lazy `imagesForGroup` flow, and a tap opens the viewer at image 1.
- Titles: `DesignTitle.clean` ("تصميم - 9 ذو الحجة 1447هـ" → "٩ ذو الحجة ١٤٤٧هـ").
- Viewer (`ImageScreen`): black background, full-width image, `HorizontalPager`, pinch zoom 1×–4×, double-tap zoom (2.5×, toward the tapped point), and pan bounded to the zoomed image. The pager is locked while zoomed and each page resets when you swipe away. The top bar has close (x), a 1-line title and the counter "١ من ٩". A thumbnail strip appears for multi-image posts, with the current one outlined 2dp in `accentStrong`.
- Share: loads the current image through the app's Coil `imageLoader` (`allowHardware(false)`), writes `cacheDir/share/design_<group>_<index>.png`, and shares it through the existing FileProvider (`${applicationId}.provider`, whose `provider_paths.xml` already exposes `cache/share/`).
- Zoom gestures: `detectTransformGestures` consumes one-finger drags even at 1×, which would block pager swipes. The viewer uses the same primitives (`awaitEachGesture` + `calculateZoom`/`calculatePan`) and only consumes when 2 fingers are down or the image is zoomed. No new dependency.
- Follow-up: save-to-gallery (not built, per spec).

### Phase 8: Done
- The title is "حسابي" (new core-ui string `my_account`, same as the tab's `nav_account`), in a tab-root `AppTopBar` with no back arrow.
- Header card: 48dp avatar with a 2dp `accent` ring (Tabler user icon on `accentContainer` when there's no photo), the name, and the email in an LTR isolate (`BidiText.ltr`) in `textMuted`.
- "المظهر": a segmented control تلقائي / فاتح / داكن on a `surfaceMuted` track. It uses the NEW key `theme_follow_system`. When that key is absent, it defaults to "follow system" only if the existing `dark_theme_enabled` key has no stored value. Existing users who picked light or dark keep their choice. `MainActivityViewModel.themeState.followSystem` + `isSystemInDarkTheme()` decide the effective theme in `MainActivity` (window background and bar icons follow it too). The meaning of the old key is unchanged.
- "الإعدادات": a "حجم خط القراءة" row with A−/A+ on the reader's `reader_font_step` preference (Phase 3). `ProfileScreenViewModel` reads and writes it through `DataStoreRepository`.
- "التطبيق" and "الدعم والسياسات": same items, now with Tabler outline icons in `accent` (info-circle, share, star, headset, shield-lock, file-text, code) and a chevron-left. Cards have 14dp corners on `surface` with 0.5dp hairline dividers.
- "منطقة الخطر" is renamed to "الحساب". Sign-out is neutral (`accent` icon, `textPrimary` text). The delete row uses `danger` for both icon and text: "حذف الحساب نهائيًا".
- A confirmation dialog already existed. It's restyled to the spec: title "حذف الحساب؟", one sentence saying deletion is permanent, "حذف الحساب" in `danger` and "إلغاء". It still calls the existing `deleteAccount()`.
- The version line is unchanged ("الإصدار 1.0.7", Western digits).
- Admin also uses `ProfileScreen(isAdmin = true)`: the new parameters have defaults, so the admin call site is unchanged.

### Phase 9: Done
- Photo: the shared `SheikhPhoto` (Phase 2) at 112dp with a 2dp `accent` ring. The welcome screen's component is private to the off-limits auth module (D2.2).
- The name comes from one string resource, `sheikh_name` ("الشيخ د. حسن الهواري", also used by the reader meta line). The role line is split at " - " into two `accentContainer` chips.
- Tabs: an M3 `TabRow` with no pager: نبذة، المؤهلات، البحوث، المساهمات. The indicator is `accentStrong` on `background`.
- المؤهلات: a vertical timeline with an `accentStrong` dot and connecting line, a year chip parsed from "(1990م)" → "١٩٩٠م" (the year is removed from the text), the title, and the institution in `textMuted`. Parsing is the new pure-Kotlin `DatedEntry` (tested).
- البحوث: sorted newest first with date chips ("نوفمبر ٢٠١٣م"). "لم ينشر" items get a "لم يُنشر بعد" chip.
- المساهمات: the media and teaching lists under plain headers. Emoji headers (🎓 📺 ✍️ 📚) are gone.
- القنوات الرسمية: the website `https://www.dr-alhawary.com` (from the spec) and فاسألوا `https://t.me/Fasalu1447` (already in `QAScreen`). The URLs are shown LTR-isolated. The `socialLinks` in `DoctorProfile` are placeholders (facebook.com, youtube.com, wa.me/123456789, example email), so they weren't used, and no URL was invented.
- Content fix (in-app data): the master's entry had an unclosed parenthesis ("… تحقيق ودراسة(1995م)"). It's now "… تحقيق ودراسة) (1995م)".

### Phase 10: Done ("فتح قناة المعهد" omitted: no URL in code)
- Tab-root `AppTopBar` ("معهد الشيخ حسن الهواري الفقهي", start-aligned, no back arrow) on `background`. It replaces the centered M3 bar.
- Profile card: 56dp avatar with a 2dp `accent` ring, the name, "@handle" via `BidiText.handle`, and a status chip on `successContainer` with a `success` dot and the real status text: `institute_student` + " · " + `formatBatchName(batch)` → "من طلاب المعهد · الدفعة الأولى". A non-member gets a neutral `surfaceMuted` chip with "ليس من طلاب المعهد".
- Coming-soon card: a Tabler school icon in a 52dp `accentContainer` circle, "منصة المعهد قريبًا", and the body "نجهّز محتوى الدراسة داخل التطبيق، وسيُفعَّل قريبًا إن شاء الله.". A hairline then separates a success line (Tabler circle-check in `success`) "تم ربط حسابك بقناة المعهد على تيليجرام", shown only when `isConnectedToTelegram`.
- "فتح قناة المعهد": omitted. No institute channel URL exists in the code (only the Telegram *login* OAuth URL and فاسألوا's `t.me/Fasalu1447`).
- No "notify me" button (per spec; it needs backend work).
- The Guest / NotChannelMember states keep their existing content. They sit on the new `background` but weren't redesigned (not in the spec).

### Phase 11: Done
- `AppTopBar` with a back arrow and "فاسألوا" on `background` (it was flat `surfaceVariant` gray). `QAScreen` gained `onNavigateBack` and `onOpenSearch` parameters (both with defaults), wired in `MainActivity`. No route was added or changed.
- The existing `fasalo_logo` drawable, now at 72dp.
- Title "أرسل سؤالك الشرعي" and the body "فاسألوا هي المنصة الموحدة لأسئلة الفتاوى، وتعمل عبر تيليجرام.".
- Steps card with numbered `accentContainer` circles (١، ٢، ٣): افتح فاسألوا في تيليجرام / اكتب سؤالك بوضوح واختصار / تابع الرد في المحادثة نفسها.
- "قبل أن تسأل" card: the subtitle "لعل سؤالك أُجيب عنه من قبل" and a read-only field "ابحث في الفتاوى المجاب عنها" that opens the existing Search screen. There's no audio preset because the route has no preset argument.
- Button: `fasalooTeal` with white text and the Tabler send icon, "افتح في تيليجرام". The current intent is already the `https://t.me/Fasalu1447` link (it opens Telegram when installed). On `ActivityNotFoundException` it retries the same link explicitly in a browser (`CATEGORY_APP_BROWSER` selector). If that fails too, a Snackbar shows "تعذّر فتح تيليجرام…" (new string `q_a_cannot_open`). Custom Tabs wasn't used because `androidx.browser` isn't a dependency of `:app`, and adding it is off-limits.
