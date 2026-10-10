# Tablet design log

Spec: `docs/plans/tablet-design.md`. Branch: `ui/tablet` (from `16503e9`).

## Progress

- [x] 1. Explore
- [x] 2. Foundations
- [ ] 3. Screens: Articles + Reader, Home, Audio categories + Fatwas + Player, Search, Profile, Videos, Designs, Designs viewer, About, Fasaloo, Institute, Share preview, first-run and system screens
- [ ] 4. Verification pass

## 1. Explore (findings)

- Navigation: one `NavHost` with string routes in `app/.../MainActivity.kt` (`MainAppContent`). Bottom bar: `app/.../ui/navigation/BottomNavigationBar.kt` (`BrandBottomBar`), items in `BottomNavItem.kt` (Home, Search, Study = المعهد, Profile), shown only on the four tab roots.
- Theme: `core/core-ui/.../theme/` — `BrandColors.kt` (`BrandPalette`, `Brand.colors`), `Theme.kt` (`HassanAlHawaryTheme`, provides the palette, reduced motion), `Type.kt` (`CairoTypography`). No spacing or radius token objects exist in code; screens use literal dp values that equal the Figma `space/*` and `radius/*` tokens.
- `viewerBackground` / `onViewerBackground`: not defined anywhere in the app (see the Designs viewer entry).
- Manifest: no `screenOrientation` lock and no `resizeableActivity="false"` on any activity. Nothing to remove.
- Versions: Compose BOM 2025.12.00 (Compose 1.10), material3 1.4.0, Navigation Compose 2.9.2. `androidx.window` 1.5.0 is already on the classpath (through material3).
- Test tooling: `app` has `ui-test-junit4` for instrumented tests; no screenshot-testing tool (Paparazzi, Roborazzi, Compose Preview Screenshot Testing) in the repo.
- Device: the running emulator is the Resizable AVD in tablet mode, 1920×1200 px at 240 dpi = 1280×800 dp, the same as the Pixel Tablet and the Figma frames.

### Deviation: no `material3-adaptive` libraries

The shell has no internet access in this session (TLS connections to Google Maven, Maven Central and GitHub all fail), and the adaptive libraries (`adaptive`, `adaptive-layout`, `adaptive-navigation`, `material3-adaptive-navigation-suite`) are not in the Gradle cache, so they cannot be added. Instead:

- Window class: `WindowClass` + `windowClassForWidth()` in core-ui, the same breakpoints as `currentWindowAdaptiveInfo().windowSizeClass` without `supportLargeAndXLargeWidth` (under 600 Compact, under 840 Medium, otherwise Expanded), computed from the window width.
- Shell: the "equivalent `Row`" the spec allows instead of `NavigationSuiteScaffold`.
- Two panes: a small `TwoPaneLayout` (list 400, gap 24, detail fills) instead of `ListDetailPaneScaffold`; selection is saved with `rememberSaveable`, so it survives rotation and resizing.

These can be swapped for the libraries later without changing the screens.

## 2. Foundations (done)

- `core-ui/theme/AdaptiveLayout.kt`: `WindowClass`, `windowClassForWidth()`, `AdaptiveLayoutTokens` (Compact / Medium / Expanded, the Figma `Layout` collection), `LocalLayoutTokens` + `layoutTokens`. `HassanAlHawaryTheme` provides the tokens for the window width (`LocalConfiguration.screenWidthDp`, the whole window since the app targets SDK 35+), or the ones passed in (`layoutTokens =`, for tests).
- `core-ui/components/AppNavigationRail.kt`: Figma NavRail. Custom (Material's rail has other numbers). Focus, hover and pressed: Material's ripple state layer on the pill (Figma has none).
- `core-ui/components/AdaptivePanes.kt`: `Modifier.paneSurface()` (outlined, radius 20), `WindowMargin` (grid/margin on Medium/Expanded, nothing on Compact), `TwoPaneLayout` (list 400 on the start side, gap 24, detail fills).
- `core-ui/components/SelectedItemStyle.kt`: `animatedListItemStyle(selected)` (accentContainer + 1.5dp accentStrong).
- `core-ui/components/EmptyDetail.kt` + strings for Audio and Search.
- `core-ui/components/AdaptiveShellPreview.kt`: the shell as `MainActivity` lays it out, for previews and UI tests.
- `MainActivity`: rail instead of the bottom bar on Medium/Expanded, on every main-app screen except the designs viewer; every destination except the viewer is wrapped in `WindowMargin` (`screen()` helper). Compact: unchanged.
- `BottomNavigationBar.kt`: `AppRail` (selected tab = nearest tab root in the back stack); `navigateToTab()` shared by the bar and the rail. On the phone it behaves as before; the only new case is choosing the tab you are already in from deeper in its flow (only possible on the rail): back to its root.
- Tests: `core-ui` unit test `AdaptiveLayoutTokensTest`; instrumented `app/src/androidTest/.../tablet/TabletFoundationsTest` (rail 80 × window height on the right, pill 56×32, destinations 64 apart from 32; list pane 400 × 736 at 32 from the top and rail; gap 24; detail 712; Medium margin 24; Compact no rail). The instrumented tests live in `app` because core-ui's Compose test artifacts (1.10.0) aren't in the offline Gradle cache; `app` resolves 1.10.1, which is.
- How to run them without uninstalling the app (connectedAndroidTest uninstalls it, which would sign the emulator out): `installDebug` + `installDebugAndroidTest`, then `adb shell am instrument -w -e class app.netlify.devalihassan.tablet.<Test> app.netlify.devalihassan.test/androidx.test.runner.AndroidJUnitRunner`. Screenshots land in `/sdcard/Android/data/app.netlify.devalihassan/files/tablet-shots/`.

### Finding: Cairo line boxes

Android lays a Cairo line out at the font's own height (about 1.85 × the font size: 22dp for 12sp) whatever `lineHeight`, `LineHeightStyle` mode or font padding say (fallback line spacing). Figma's text boxes use the style's line height (16 for 12sp), so every phone component's text is a little taller on device than in Figma. Phone composables are reused as they are. The rail label is centred in a 16sp slot so the destinations sit exactly where Figma has them.

## 3. Screens

### Articles + Reader (done)

Figma: Expanded `53:2` (light), `53:170` (dark); Medium articles `63:2412`, `63:2490`; Medium reader `63:2569`, `63:2662`.

- Expanded: `ArticlesAdaptiveContent` = `TwoPaneLayout`: list pane = the phone's articles screen (top bar «المقالات» with back, list 16 padding, 12 between), items fill the pane (366); detail pane = `ArticleReaderPane` (reader top bar with share and text size only, no back, no progress bar as in Figma, body 32 side padding). The newest article is open until one is chosen; the chosen one has the selected style (`ArticleItem(selected =)`).
- Selection lives in the back-stack entry's `SavedStateHandle` (`SELECTED_ARTICLE`), so it survives rotation and resizing. Narrowing to one pane after choosing an article opens that article in the reader; on the reader route, the article chosen beside the list is the one the reader shows (`ArticleDetailScreen(articleId =)`).
- The reader route (`detail_article_screen/{id}`) on Expanded shows the articles with that article selected (Figma has no reader of its own on Expanded).
- `DetailArticleViewModel.showArticle(id)`: one reader ViewModel follows the selection; a superseded load's cancellation no longer overwrites the state with an error.
- Medium: articles unchanged inside the margin; reader text column `readingMaxWidth` (640, its 16 padding included, as Figma's ArticleBody), centred.
- `paneSurface()` now insets its content by the 1dp outline (Figma: 400 pane, 398 top bar, 366 items). `WindowMargin` consumes the system-bar insets on Medium/Expanded so a phone screen's Scaffold inside a pane doesn't pad for the navigation bar a second time.
- Tests: `ArticlesTabletTest` (8, all pass): Expanded items 366 at 17 from the pane edge, 73 from the top, 12 apart; reader text 646 at 33 from the pane edges; Medium items 640; Medium reader column 608 + padding centred; Compact list and reader at the phone's 328.
- Matched: shell, panes, list, selected item, reader top bar actions, body padding, Medium reading width, light and dark colours.
- Deviations: card and text heights are a little taller than Figma (Cairo line boxes, see above). Pre-existing on the phone too, left alone because changing them would change Compact: the reader body uses the app's `bodyLarge`, which is Bold (Figma Reader/body is Regular), and the reader title uses `headlineSmall`, which `CairoTypography` doesn't define, so it renders in the system font.

### Home (code done; instrumented run pending the emulator)

Figma: Expanded `56:192`, `56:414`; Medium `63:2756`, `63:2969`.

- Expanded: `HomeHeader` (phone) full width, 24, then two equal columns 24 apart: start = `ImageCarousel` and `LessonsByCategory` at the column width (their phone 16 inset off: new `horizontalPadding` parameters, default 16), 24 between them; end = the latest articles and audios rows, 12 between header, row and next header (`LatestArticleAudioLazyRow(headerSpacing =)`), two cards across ((column - 16 - 8) / 2 = 266) with the phone's 16 start inset only.
- Medium: one column, 12 between blocks, same full-width carousel and tiles, cards 324.
- The rows stay `LazyRow`s: two cards show, more scroll in.
- Compact: the same LazyColumn as before (sections extracted to functions, same parameters).
- Tests: `HomeTabletTest` (tiles 180 / 218.67, cards 266 / 324, positions); not run yet.
- Deviations: the carousel keeps the phone composable's height (240 tall, image 184) where the tablet frame draws 224 (image 188); its indicator keeps the phone's colours and centring.

### Audio categories + Fatwas + Player (code done; instrumented run pending the emulator)

Figma: categories Expanded `57:498`, `57:870`, Medium `63:3174`, `63:3310`; Fatwas Medium `63:3447`, `63:3556`; Player Expanded `57:646`, `57:1015`, Medium `63:3658`, `63:3719`.

- Categories, Expanded: `AudioCategoriesAdaptiveContent` = the phone screen in the list pane, `EmptyDetail` (Audio: «اختر قسمًا للاستماع» / «ستظهر هنا الدروس والخطب والفتاوى») in the detail pane. A category opens its list (the next screen).
- Fatwas (audio list), Expanded: `AudioListAdaptiveContent` = the phone list in the list pane, the player (`AudioPlayerPane`) in the detail pane; before anything is chosen, `EmptyDetail` with the component's own text («اختر عنصرًا للعرض» / «سيظهر هنا محتواه»; Figma draws no frame for this state). The row open in the player uses the selected style, and the existing Playing look while it plays (`AudioListItem(selected =, playing =)`).
- Player pane: the player's top bar without the back arrow, the content at the controls' designed 312, centred. Medium player: the phone screen, controls fill the width (624, as Figma 63:3658).
- The player route on Expanded shows the (all) audio list beside the player with that audio selected.
- Playback continuity: `AudioDetailViewModel.showAudio(title, url)` switches the audio in place (loads it into the controller, doesn't start it, like opening the player). One player ViewModel per back-stack entry for both layouts, so rotation never re-creates it (a new one would restart the audio from 0). On the list route a selection kept after narrowing to one pane stays open in the player inside that destination (back returns to the list) instead of navigating to the player route.
- Also fixed in passing: the ViewModel added a new `Player.Listener` on every controller (re)connection without removing the old one.
- Tests: `AudioTabletTest` (category rows 366 at 16 inset; fatwa rows 366; pane seek bar 312 centred; Medium seek bar 624; Compact 312); not run yet.

### Search (code done; instrumented run pending the emulator)

Figma: Expanded `59:939`, `59:1412`; Medium `64:3376`, `64:3466`.

- Expanded: `SearchAdaptiveLayout` = the phone's search (top bar «البحث», field, scrollable pills, results) in the list pane; the detail pane shows the chosen result or `EmptyDetail` (Search: «اختر نتيجة لعرضها» / «ابحث في المقالات والصوتيات والفتاوى والمرئيات»).
- An article opens in the reader pane and an audio in the player pane beside the results (selected row style, `SearchResultRow(selected =)`); videos and designs still open their own (immersive) screens. The choice is kept in the entry; narrowed to one pane, it stays open on its own inside Search (back returns to the results), on the same reader / player ViewModel.
- Medium: the phone's search inside the margin.
- Tests: `SearchTabletTest` (rows 366 at 16 inset, field under the 56 top bar, empty detail centred; Medium 640; Compact 328); not run yet.

### Profile (code done; instrumented run pending the emulator)

Figma: Expanded `59:1054`, `59:1522`; Medium `64:3557`, `64:3650`.

- `ProfileScreen` split into the stateful screen (ViewModel, delete dialog, sign-out result, deleting overlay) and a stateless `ProfileContent`.
- Expanded: root top bar «حسابي», 24, then two equal columns 24 apart, sections 20 apart: account card, المظهر, الإعدادات, التطبيق at the start; الدعم والسياسات, الحساب, the version at the end. The whole page scrolls.
- Medium and Compact: the phone's LazyColumn (cards 640 on Medium, as Figma).
- Admin app (also uses `ProfileScreen`): compiles; on a wide window it gets the same two columns.
- Tests: `ProfileTabletTest` (columns 556 at 612/32, end column 24 under the top bar; Medium 640; Compact 328); not run yet.

### Videos (code done; instrumented run pending the emulator)

Figma: Expanded `60:1403`, `60:1809`; Medium `64:3744`, `64:3936`.

- Figma's tablet Videos has category pills under the top bar and draws no video categories screen for tablet; the phone app has a separate categories screen and no pills. Followed Figma (and the spec) on Medium and Expanded: the Home tile opens Videos directly, the pills (shared `FilterPill`, categories from `VideoCategoryViewModel`, «الكل» first) switch the category in place (`VideosViewModel.selectCategory`, kept in the saved state). Compact keeps the categories screen and the list.
- Grid: `LazyVerticalGrid(GridCells.FixedSize(328))`, 24 apart, centred: 3 across on Expanded, 2 on Medium. Expanded: 24 between top bar, pills and grid; Medium: none (Figma).
- Medium margin 20 for the grid screens (`AdaptivePanesDefaults.GridScreenMediumMargin`, `screen(mediumMargin =)`), as Figma draws them; 24 everywhere else.
- Title on tablet always «الفيديوهات» (the pills show the category).
- Deviation (pre-existing, phone too): the Video model has no duration, so the card has no duration badge; its play overlay is black 55% (Figma `viewerBackground`).
- Tests: `VideosTabletTest` (3 / 2 across at 328, centred 84..1116, Medium 20..700, pills 16 in; Compact list, no pills); not run yet.

### Designs (code done; instrumented run pending the emulator)

Figma: Expanded `60:1597`, `60:1960`; Medium `64:4089`, `64:4306`.

- `ImagesGroupsScreen` split: stateful (ViewModel, per-tile image counts) and stateless `DesignsScreenContent`.
- Medium and Expanded: `LazyVerticalGrid(GridCells.FixedSize(156))`, rows aligned, centred: Expanded 6 across, 24 apart, 24 under the top bar; Medium 4 across, 16 apart, directly under the top bar, with the grid screens' 20 Medium margin. Tiles keep their image's proportions inside the row (the phone's staggered grid stays on Compact).
- Tests: `DesignsTabletTest` (6 / 4 across at 156, centred 72..1128 / 24..696; Compact 2 across); not run yet.

### Designs viewer (code done; instrumented run pending the emulator)

Figma: Expanded `60:1782`, `60:2086`; Medium `64:4976`, `64:5003`.

- `viewerBackground` (#0B0B0B) and `onViewerBackground` (#F1EFE8) were not defined in the app: added to `BrandPalette` with Figma's values, the same in light and dark.
- `ImageScreen` split: ViewModel wrapper + stateless `ImageViewerContent`. Medium and Expanded (`TabletViewer`): dark viewer background in both themes, light bar icons, drawn to the window edges (no rail, no shell padding: it pads for the system bars and the window margin itself); a centred column 16 apart: top row (close, title, counter), the poster at 688 × 460 (shrinks in a short window; the phone's pager, pinch / double-tap zoom and swipe-to-close), «قرّب بإصبعين», the outlined share pill, the 40dp thumbnails (6 apart, centred, selected with the 2dp accentStrong border). Compact: the phone viewer as before.
- Shell change: on Medium/Expanded the status/navigation bar padding moved from the NavHost container into `WindowMargin` (and the viewer), so the container keeps the same bounds through the transition into and out of the immersive viewer.
- Tests: `ViewerTabletTest` (poster 688 × 460 centred, no rail; Compact has no poster frame); not run yet.

### About the Sheikh (code done; instrumented run pending the emulator)

Figma: Expanded `61:1998`, `61:2273`; Medium `64:6378`, `64:6446`.

- Expanded: profile pane (400) = top bar «عن الشيخ» with back, then (16 padding, 12 apart) the hero with a 96dp photo (`HeroSection(photoSize =)`, phone 112) and the official channels; detail pane (16 padding, 16 apart) = the phone's tab row and the selected tab's sections. The tab survives rotation (`rememberSaveable`, shared by both layouts).
- Medium and Compact: the phone's single column (sections now come from one list for both layouts; same items, same 16 spacing).
- Deviation: Figma draws the channels as three pills (يوتيوب، تيليجرام، الموقع الرسمي); the app's current channels card (website, Telegram with its QR sheet; no YouTube link exists in the app) is reused instead, so the QR sheet and the real links stay. The hero's name keeps the phone's titleLarge (Figma 18 SemiBold).
- Tests: `AboutTabletTest` (profile pane 400, channels at 16 padding, tabs at the top of the detail pane; Medium phone column); not run yet.

### Fasaloo (code done; instrumented run pending the emulator)

Figma: Expanded `61:2118`, `61:2373`; Medium `64:6544`, `64:6603`.

- `QAScreen` split into sections (intro, steps card, «قبل أن تسأل» card, teal button) shared by the layouts.
- Expanded: intro pane (400) = top bar, the mark at 64 (phone 72), title, description, then the teal button pinned 16 from the pane's bottom; detail pane (16 padding, 16 apart) = the steps and «قبل أن تسأل» cards.
- Medium: the phone column with the button pinned to the bottom. Compact: unchanged (the button at the end of the scrolling column).
- Deviation: Figma's «قبل أن تسأل» card has topic pills under the search field; the app has none (the field opens Search), so the phone card is reused as it is. The mark is the app's logo image, not Figma's teal circle placeholder.
- Tests: `FasalooTabletTest` (button pinned 16 above the pane / margin, 366 wide; steps in the detail pane); not run yet.

### Institute (code done; instrumented run pending the emulator)

Figma: Expanded `61:2211`, `61:2458`; Medium `64:6686`, `64:6733`.

- Expanded, student state: `InstituteComingSoonTwoPane` = the root top bar and the existing profile card (16 padding) in the start pane; the existing «coming soon» card at 380, centred in the detail pane. The placeholder behaviour is unchanged.
- The guest and not-a-channel-member states have no tablet design: the phone layout inside the margin. Medium: the phone column.
- Deviation: Figma's card shows an illustration placeholder and an «فتح قناة المعهد» button; the existing card (school icon, no button: the app has no channel URL) is reused, as the spec says.
- Tests: `InstituteTabletTest` (card centred in the detail pane, profile card in the start pane); not run yet.

### Share preview (code done; instrumented run pending the emulator)

Figma: Expanded `59:1158`, `59:1626`; Medium `64:4477`, `64:4726`.

- Medium and Expanded: the player's share action opens a navigation `dialog` destination (`share_preview_dialog/...`, same arguments, same ViewModel) instead of the full-screen route, so it sits over the player. The dialog window dims to Figma's 45% black; `SharePreviewDialogContent`: surface, radius 20, shadow (24dp, black 28%), title-only 56 header with 24 padding; Expanded 720 wide: the 9:16 preview (434 tall, ~244 wide: the existing card at 1.4×) and the clip selector (360 frame, controls 328) side by side, 32 apart, centred; Medium 600 wide: the preview over the selector, 24 apart; the full-width share button (16 each side, 12 under). Never wider than the window; scrolls in a short one.
- Compact: the full-screen route as before. The article quote share (TextCardPreview) has no tablet design: unchanged.
- Tests: `ShareDialogTabletTest` (720 / 600 wide, centred, button 688); not run yet.

### First-run and system screens (code done; instrumented run pending the emulator)

Figma: Splash `61:3803` / `64:6787`, Welcome `61:3812` / `64:6796`, Onboarding `61:3845` / `64:6832`, Update optional `61:3881` / `64:6873`, required `61:3910` / `64:6902`, Maintenance `61:3935` / `64:6927` (and the dark ones).

- No rail on any of them (they are outside the NavHost).
- Welcome (`AuthScreenContent`): its column is 520 wide with 16 side padding on a tablet (phone: 480 max, 20); it already kept the header centred above the bottom-anchored button.
- Onboarding: centred 520 column, 16 side padding on a tablet; the pager, dots and the bottom row as on the phone.
- Update (optional and required): on a tablet a centred 520 column with flexible space above and below the message, the actions at the bottom (24 under), inside the system bars. The phone layout is unchanged.
- Splash: the in-app `SplashScreen` composable is already a centred logo on the full window (the real launch splash is the system one); nothing to change.
- Maintenance: not in the app. `AppConfig.maintenanceMode` exists but nothing reads it and there is no screen; adding one is a feature (when to show it, its copy), so it is left out.
- Deviation: the app's update screen predates the Figma phone design (Material icon and buttons instead of the icon circle and the brand buttons); the tablet keeps its content and only takes the tablet layout.
- Tests: `FirstRunTabletTest` (Welcome Google button 488 centred; Onboarding skip at the column's edge; Update actions 24 above the bottom, 488 wide); not run yet.
