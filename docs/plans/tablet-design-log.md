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
