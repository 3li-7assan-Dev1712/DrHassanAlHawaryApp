# Tablet UI: design guidelines and implementation brief

Put this file at `docs/plans/tablet-design.md` and import it from `CLAUDE.md` with `@docs/plans/tablet-design.md`.

## 0. Source of truth

- Figma file: **Hawary App — Design System**, file key `gyiLOI3lrVYKrmwJbJ4qKv`
- Pages: `Tablet — Foundations` (overview frame `64:7063`, window classes `45:2`), `Tablet — Components` (NavRail set `47:419`, EmptyDetail set `57:138`), `Tablet — Screens` (80 frames, table in section 9)
- Phone design (unchanged, still the Compact layout): pages `Components` and `Screens`
- **If this file and Figma disagree, Figma wins. Stop and tell the user about the mismatch.**
- Figma frames are drawn in dp: 1 Figma px = 1 dp. Never rescale.
- The app is Arabic and RTL. Use `start`/`end`, never `left`/`right`. In the Figma frames the navigation rail is on the right and the list pane is on its left, which is the start side in RTL.

## 1. How to work (this is what makes the match exact)

For every screen:

1. Call the Figma MCP tools `get_design_context` and `get_screenshot` for the screen's frame (Light first, then Dark), and `get_variable_defs` if you need token values. The screenshot URL is short lived: download it with `curl` to `/tmp/figma/<screen>-<mode>-<theme>.png` and look at it.
2. Build the screen from the **existing phone composables** wherever the Figma frame reuses a phone component (TopBar, ArticleItem, ArticleCard, AudioCard, AudioListRow, CategoryRow, SearchBar, FilterPill, VideoCard, DesignTile, SeekBar, TransportRow, ProfileRow, Button, Tabs ...). Widen them with `Modifier.fillMaxWidth()` as Figma does. Do not rebuild them.
3. Colors: only `Brand.colors.*` / `MaterialTheme`. Figma variable names equal `BrandPalette` property names (`background`, `surface`, `surfaceMuted`, `divider`, `textPrimary`, `textSecondary`, `textMuted`, `accent`, `accentStrong`, `accentContainer`, `onAccentContainer`, `accentText`, `gold`, `goldSoft`, `goldText`, `goldStroke`, `onGold`, `success`, `danger`, `fasalooTeal`, ...). Figma also has `viewerBackground` and `onViewerBackground`; find where the app defines them. **No hex values in screens.**
4. Sizes: only `LayoutTokens` (section 3) and the existing spacing and radius tokens (Figma `space/4,8,12,16,20,24,32`, `radius/9,10,12,14,16,20,full`). Numbers in section 6 that are not tokens are measured from Figma; put them in named constants next to the screen.
5. Typography: Figma text styles map to the app's Cairo text styles by size, weight and line height. Examples: `Material/titleLarge` = Cairo Bold 22, `Nav/label` = Cairo Regular 12, `Nav/labelActive` = Cairo SemiBold 12. Read exact values from Figma.
6. **The Compact (phone) layout must not change.** Run the existing previews and tests before and after.
7. Verification, all three required before a screen is done:
   - **Dimensions:** Compose UI tests with `assertWidthIsEqualTo` / `assertHeightIsEqualTo` and position checks for the numbers listed per screen in section 6 (rail 80dp, list pane 400dp, gaps, margins).
   - **Visual:** render a preview at the reference size (`@Preview(device = "spec:width=1280dp,height=800dp,dpi=320", locale = "ar")` for Expanded, `spec:width=800dp,height=1280dp,dpi=320` for Medium, Light and Dark), view the PNG next to the Figma screenshot, and fix every visible difference. Anything you cannot match, list with the reason.
   - **Device:** run on the **Pixel Tablet** AVD (1280×800dp landscape, 800×1280dp portrait, the same sizes as the Figma frames).
8. One screen per commit. Each commit adds previews for Compact, Medium and Expanded in Light and Dark.
9. At the end of each screen, report: matched items, deviations and why, and Figma node ids used.

## 2. Window size classes

| Class | Width | Figma reference | Layout |
|---|---|---|---|
| Compact | under 600dp | phone frames (360×800) | unchanged phone layout, bottom navigation bar |
| Medium | 600–839dp | 800×1280 portrait | navigation rail + single pane |
| Expanded | 840dp and up | 1280×800 landscape | navigation rail + two panes (list and detail) where the screen has them |

- Use `currentWindowAdaptiveInfo().windowSizeClass` **without** `supportLargeAndXLargeWidth`, so every window of 840dp or more is Expanded. (Google's Large class starts at 1200dp and our 1280dp reference falls in it only when that flag is on.) If the flag is ever enabled, treat Large and Extra-large exactly like Expanded.
- The adaptive libraries are `androidx.compose.material3.adaptive` (`adaptive`, `adaptive-layout`, `adaptive-navigation`) and `material3-adaptive-navigation-suite`. Check the version catalog (`libs.versions.toml`) for what is already used. Look up the current stable versions; never guess them.

## 3. Layout tokens (Figma collection `Layout`)

| Token | Compact | Medium | Expanded | Kotlin name (suggested) |
|---|---|---|---|---|
| grid/columns | 4 | 8 | 12 | `columns` |
| grid/margin | 16 | 24 | 32 | `margin` |
| grid/gutter | 16 | 24 | 24 | `gutter` |
| nav/railWidth | 0 | 80 | 80 | `navRailWidth` |
| pane/listWidth | 360 | 360 | 400 | `listPaneWidth` |
| pane/gap | 0 | 24 | 24 | `paneGap` |
| reading/maxWidth | 328 | 640 | 720 | `readingMaxWidth` |

`window/width` and `window/height` (360×800, 800×1280, 1280×800) are artboard sizes only, not app tokens.

Implement as an immutable `AdaptiveLayoutTokens` data class with one instance per class, provided through a `CompositionLocal`, in `core/core-ui/.../theme/` next to `BrandPalette`. Screens read `LocalLayoutTokens.current`.

## 4. Shell

**Expanded.** The window has `margin` (32) padding on all four sides. Left to right in the Figma frame (so, end to start in RTL): `detail pane (fills)` · gap 24 · `list pane (400)` · `navigation rail (80)`. Pane height is the window height minus 2 × 32.

**Medium.** Window padding `margin` (24) on all four sides, one column, then the rail (80) on the right. Content keeps the phone's own internal spacing (0 between top bar and body).

**Navigation rail stays visible on every main-app screen, including secondary ones** (Articles, Reader, Player, Videos, Designs, About, Fasaloo ...), unlike the phone's bottom bar. Selected destination is Home for everything reached from Home, Search on Search, Institute on Institute, Profile on Profile.

**No rail** on: Splash, Welcome, Onboarding, Update (optional and required), Maintenance, Designs viewer. Share preview is a dialog over the previous screen.

Use `NavigationSuiteScaffold` (or an equivalent `Row`) so the bottom bar appears only in Compact. Navigation destinations and state must survive rotation and window resizing.

## 5. Components (exact specs)

### AppNavigationRail (Figma set `47:419`: Theme × Selected)
- Width 80, fills window height. Background `background`. 1dp `divider` line on the edge that faces the content (the left edge in RTL).
- Vertical padding 32 top and bottom, 12 between destinations.
- Four destinations, top to bottom: Home (الرئيسية), Search (بحث), Institute (المعهد), Profile (حسابي). Use the same icons as the phone bottom bar.
- Each destination: indicator pill 56×32, radius 16, then a 4dp gap, then the label, all centered in the 80dp width. Icon 24.
- Selected: pill `accentContainer`, icon `onAccentContainer`, label `goldText` with Cairo SemiBold 12 (`Nav/labelActive`).
- Unselected: no pill, icon and label `textMuted`, label Cairo Regular 12 (`Nav/label`).
- Material 3's `NavigationRail` defaults differ from these numbers. Match the numbers above. If the stock component cannot, write a small custom `AppNavigationRail` in `core-ui`.

### Pane surface
Transparent fill, 1dp `divider` border, corner radius 20, content clipped. **Panes are outlined, not filled**, so phone cards keep the same look.

### Selected list item
On the list item (for example `ArticleItem`): fill `accentContainer` and a 1.5dp `accentStrong` border. The playing row in the Fatwas list keeps its existing `Playing` style.

### EmptyDetail (Figma set `57:138`: Theme × Icon, text properties Title and Subtitle)
- Shown in the detail pane when nothing is selected. Pane content centered. Width 360.
- Column, centered, 16 between items: circle 96 (`accentContainer`) with the icon at 36dp (`onAccentContainer`), title (`Material/titleMedium`, `textPrimary`), subtitle (`Material/bodyMedium`, `textSecondary`), both center aligned.
- Audio categories: «اختر قسمًا للاستماع» / «ستظهر هنا الدروس والخطب والفتاوى». Search: «اختر نتيجة لعرضها» / «ابحث في المقالات والصوتيات والفتاوى والمرئيات».

### Two-pane scaffold
Prefer `ListDetailPaneScaffold` / `NavigableListDetailPaneScaffold` so back navigation, list selection and rotation work. Configure the pane directive so the list pane is 400dp and the gap between panes is 24dp (the scaffold directive exposes the horizontal partition spacer size and the default pane width; confirm the names in the version in use). The list pane is first, which is the start (right) side in RTL.

## 6. Screens (numbers measured from Figma)

Frames in section 9. Everything below is Light and Dark.

### Articles and Reader
- **Expanded (two panes).** List pane: the phone's top bar «المقالات» (56dp, back arrow kept), then the phone's article list (16 padding, 12 between items), items fill the width; the first item is shown selected. Detail pane: the phone Reader's top bar (share and text-size buttons only, back arrow hidden), then the article body with 32dp horizontal padding.
- **Medium.** Articles is a single list. Reader is its own screen: top bar, reading progress, article body with width `readingMaxWidth` (640) centered.

### Home
- **Expanded.** Home header (80dp) full width, then two equal columns (556dp each, 24 gap). Right column (start): announcement carousel at column width, then the 3×2 category tiles; 24 between them. Left column: «أحدث المقالات» header and two ArticleCards side by side (8 gap), then «أحدث الصوتيات» header and two AudioCards side by side; 12 between blocks.
- **Medium.** One column, 12 between blocks. Carousel and tiles fill the width, section rows show two cards across.

### Audio categories, Fatwas and Player
- **Audio categories, Expanded.** List pane with the category card inset 16, detail pane `EmptyDetail` (Audio).
- **Player, Expanded.** Detail pane: player top bar (back arrow hidden), then the player content: artwork 200, chip, title, download card, seek bar, transport row and action row. **These controls keep their designed width of 312dp, centered** (the seek bar track cannot stretch). List pane: the Fatwas list, rows fill the width, the playing row uses `Playing`.
- **Medium.** Audio categories, Fatwas and Player are separate single-pane screens.

### Search
- **Expanded.** List pane: top bar «البحث», search field, filter pills (horizontally scrollable), results. Detail pane: `EmptyDetail` (Search).
- **Medium.** Single pane with the same stack.

### Profile
- **Expanded.** Top bar, then two equal columns (24 gap, 20 between items). Right column: account card, then the «المظهر», «الإعدادات» and «التطبيق» sections. Left column: «الدعم والسياسات» and «الحساب» sections, then the version text.
- **Medium.** Single column.

### Videos and Designs (grids)
- **Videos, Expanded.** Top bar, filter pills, then a grid 3 across of VideoCards at their designed **328dp** width, 24 gap, **centered**. **Medium:** 20 horizontal padding, 2 across, 24 gap.
- **Designs, Expanded.** Top bar, then tiles at their designed **156dp** width, 6 across, 24 gap, centered. **Medium:** 20 padding, 4 across, 16 gap.
- Use fixed-width cells with centered arrangement, not adaptive stretching.

### Designs viewer
Immersive, no rail, dark viewer background in both themes (`viewerBackground`). Top bar (counter, title, close), the poster scaled to 688×460, the «قرّب بإصبعين» hint, the share button, the thumbnails strip; all centered, 16 apart, window padding `margin`.

### Share preview
A dialog over the previous screen: black scrim at 45%, surface `surface`, radius 20, shadow (offset 12, blur 40, black 28%).
- **Expanded.** Width 720. Header 56 (title only, 24 horizontal padding). Body padding 24, 32 between: clip selector (328) on the left, preview on the right (existing preview scaled 1.4, about 244×434, 9:16). Full-width primary button at the bottom.
- **Medium.** Width 600. Preview above the clip selector, 24 between.

### About the Sheikh, Fasaloo, Institute
- **About, Expanded.** Right pane (400): top bar «عن الشيخ», photo 96, name, role chips, «القنوات الرسمية» and links. Left pane: tabs and the timeline (padding 16, 16 between). **Medium:** single column.
- **Fasaloo, Expanded.** Right pane (400): top bar, mark 64, title, description, and the teal button pinned to the bottom. Left pane: the steps card and the «before you ask» card. **Medium:** single column, button pinned to the bottom.
- **Institute, Expanded.** Right pane (400): root top bar and the profile card. Left pane: the existing «coming soon» card at 380dp wide, centered. The institute feature is not implemented yet: keep the placeholder behaviour. **Medium:** single column.

### First-run and system screens
Splash, Welcome, Onboarding, Update (optional, required), Maintenance. No rail. A centered column 520dp wide (Splash is full width); the flexible vertical spacers grow so the actions stay pinned to the bottom. Same on Medium with the taller window.

## 7. Plan

1. **Explore, no code.** Read the modules, navigation graph, `core-ui` theme, existing screens, the version catalog and the manifest. Report findings, risks and questions.
2. **Foundations.** `AdaptiveLayoutTokens` + local, window-class helper, `AppNavigationRail`, adaptive shell with `NavigationSuiteScaffold`, pane surface, selected-item style, `EmptyDetail`. With previews and dimension tests.
3. **Screens**, one per commit, in this order: Articles + Reader, Home, Audio categories + Fatwas + Player, Search, Profile, Videos, Designs, Designs viewer, About, Fasaloo, Institute, Share preview, first-run and system screens.
4. **Verification pass.** Screenshot tests for key screens in both themes (check what the repo already uses before adding a tool and ask first), rotation and resize tests, accessibility.

## 8. Manifest and behaviour checklist

- Remove any `screenOrientation` lock and make sure `resizeableActivity` is not `false`; do not restrict the app to portrait.
- State survives rotation, window resizing and split-screen (selected list item, scroll position, playback).
- Edge-to-edge insets are respected; nothing sits under system bars or the rail.
- Touch targets stay at least 48dp. Keyboard and mouse: logical focus order and a visible focus state on list items and the rail (Figma has none; follow Material defaults and tell the user).
- Windows narrower than 600dp behave exactly as the phone does today.
- RTL: check every preview with `locale = "ar"`.

## 9. Figma frame ids (file `gyiLOI3lrVYKrmwJbJ4qKv`)

Frame names are `<Screen> — <Expanded|Medium> — <Light|Dark>`. In Expanded, Reader is inside Articles and Fatwas is inside Player.

| Screen | Expanded Light | Expanded Dark | Medium Light | Medium Dark |
|---|---|---|---|---|
| Articles | `53:2` | `53:170` | `63:2412` | `63:2490` |
| Reader | — | — | `63:2569` | `63:2662` |
| Home | `56:192` | `56:414` | `63:2756` | `63:2969` |
| Audio categories | `57:498` | `57:870` | `63:3174` | `63:3310` |
| Fatwas | — | — | `63:3447` | `63:3556` |
| Player | `57:646` | `57:1015` | `63:3658` | `63:3719` |
| Search | `59:939` | `59:1412` | `64:3376` | `64:3466` |
| Profile | `59:1054` | `59:1522` | `64:3557` | `64:3650` |
| Share preview | `59:1158` | `59:1626` | `64:4477` | `64:4726` |
| Videos | `60:1403` | `60:1809` | `64:3744` | `64:3936` |
| Designs | `60:1597` | `60:1960` | `64:4089` | `64:4306` |
| Designs viewer | `60:1782` | `60:2086` | `64:4976` | `64:5003` |
| About the Sheikh | `61:1998` | `61:2273` | `64:6378` | `64:6446` |
| Fasaloo | `61:2118` | `61:2373` | `64:6544` | `64:6603` |
| Institute | `61:2211` | `61:2458` | `64:6686` | `64:6733` |
| Splash | `61:3803` | `61:3807` | `64:6787` | `64:6791` |
| Welcome | `61:3812` | `61:3825` | `64:6796` | `64:6812` |
| Onboarding | `61:3845` | `61:3859` | `64:6832` | `64:6851` |
| Update — optional | `61:3881` | `61:3895` | `64:6873` | `64:6887` |
| Update — required | `61:3910` | `61:3922` | `64:6902` | `64:6914` |
| Maintenance | `61:3935` | `61:3953` | `64:6927` | `64:6945` |

## 10. Known limits and open decisions (ask the user, do not guess)

- Figma has no hover, focus, pressed or keyboard states for tablet. Decide with the user.
- Windows of 1200dp and wider use the Expanded layout as drawn; no separate Large or Extra-large design exists.
- Windows shorter than about 480dp tall (split-screen landscape) are not designed.
- VideoCard, DesignTile, SeekBar and the player controls have fixed internal widths in the phone library, which is why the grids are centered at the designed width. If the app's real composables stretch, keep the Figma look anyway.
- Sample text and the account name shown in Figma are placeholders from the phone designs.
- The Android code names for the layout tokens in section 3 are suggestions; follow the repo's naming.
