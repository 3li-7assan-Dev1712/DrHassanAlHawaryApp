# Motion pass log

Spec: `docs/plans/motion-pass.md`. Branch: `ui/motion` (from `116c053 chore: snapshot before motion pass`).

This session: Ali asked for phases 0–7 in one go; phases 8–12 follow in a later session.

## Phases

- [x] Phase 0: Setup and inventory
- [x] Phase 1: Foundations
- [x] Phase 2: Screen transitions
- [x] Phase 3: Loading / empty / error states; lists
- [x] Phase 4: Player
- [x] Phase 5: Profile and theme
- [x] Phase 6: Search and chips
- [ ] Phase 7: Reader
- [ ] Phase 8: Container transform (next session)
- [ ] Phase 9: Onboarding, splash, sign-in (next session)
- [ ] Phase 10: Share preview, quote selection, image viewer (next session)
- [ ] Phase 11: Haptics and polish check (next session)
- [ ] Phase 12: Reduced-motion pass and final report (next session)

## Phase 0: inventory

- Versions (`gradle/libs.versions.toml`): Compose BOM `2025.07.00`, but other dependencies pull the resolved compose foundation / animation / ui up to **1.10.1** (checked with `:app:dependencies`); material3 `1.4.0` (explicit `material3Android`); Navigation Compose **2.9.2**; `androidx.core:core-splashscreen` **1.2.0 is a dependency** (used in `MainActivity`: `installSplashScreen()`).
  - Every **Needs** item is satisfied: `Modifier.animateItem()` (foundation 1.7+), `SharedTransitionLayout` / `sharedBounds` (animation 1.7+), predictive back scrubbing pop transitions (nav 2.8+).
  - `HapticFeedbackType.Confirm` / `SegmentTick` etc. exist from compose ui 1.8 → available.
- `NavHost`: `app/src/main/java/com/example/hassanalhawary/MainActivity.kt` → `MainAppContent()` (string routes, start `home_screen`). Admin app has its own, out of scope.
- Bottom-nav destinations: `app/.../ui/navigation/BottomNavItem.kt` — `home_screen`, `search_screen`, `study_screen` (registered as `study_screen?data={data}`), `profile_screen`. `BottomNavigationBar.kt` navigates with `popUpTo(start){saveState}`, `launchSingleTop`, `restoreState`.
- Theme: `core/core-ui/.../theme/Theme.kt` (`HassanAlHawaryTheme`, provides `LocalBrandPalette` + MaterialTheme); `BrandColors.kt` (`BrandPalette`, `Light/DarkBrandPalette`, `LocalBrandPalette` = `staticCompositionLocalOf`, `Brand.colors`). App's `ui/theme/Theme.kt` just forwards to core-ui.
- System bars: `MainActivity` sets them in a `DisposableEffect(isDarkTheme)` via `enableEdgeToEdge(...)`.
- Existing animations to reuse: `Modifier.shimmer()` (`core-ui/components/CustomShimmer.kt`, infinite transition); home carousel indicator width (`ImageCarousel.kt`, `animateDpAsState`); share generation progress; study dashboard indicator/pulse; `LessonDetailScreen` AnimatedContent; video player controls `AnimatedVisibility`.
- Existing `BackHandler`s: only `VideoPlayerScreen` (exits fullscreen). Image viewer / share preview use no BackHandler.
- Unit tests: `core-ui` has `src/test` + `testImplementation(junit)` → Motion helpers and tests go there.

## Phase 1: Foundations (done)

- `core-ui/theme/Motion.kt`: durations (`SHORT` 150, `MEDIUM` 300, `LONG` 400, content swap 200), the three M3 easings, `stateChange()`, `fadeThroughEnter/Exit`, `sharedAxisEnter/Exit/PopEnter/PopExit`, `contentSwap`, `countSlide`.
- `SharedAxis.enterSign/exitSign` (pure; `core-ui/src/test/.../SharedAxisTest.kt`, 4 tests).
- `LocalReducedMotion` (+ `reducedMotion` shorthand, `stateChangeSpec()` which snaps when reduced), provided inside `HassanAlHawaryTheme` from `rememberSystemReducedMotion()` (reads `ANIMATOR_DURATION_SCALE` and follows changes with a `ContentObserver`). Being in the theme, it covers onboarding and sign-in too, not just the NavHost.
- Already-existing motion made to respect it: `Modifier.shimmer()` draws a still placeholder; the home carousel stops autoplaying; its indicator uses `stateChangeSpec()`.

## Phase 2: Screen transitions (done)

- `MainActivity.MainAppContent`: enter/exit/popEnter/popExit set once on the `NavHost`. Fade through when both initial and target routes are in `routesWithBottomNav` (the same set that decides whether the bar shows), otherwise shared axis X with a 30dp offset, direction from `LocalLayoutDirection` (RTL: forward enters from the left).
- `android:enableOnBackInvokedCallback="true"` on `<application>`. Navigation 2.9.2 ≥ 2.8, so the back gesture scrubs the pop transition.
- `BackHandler`s: only `VideoPlayerScreen` has one (pops back); it keeps working (callbacks still fire under the new back dispatch). Dialogs and the dropdowns handle back themselves. The image viewer and share preview have none.

## Phase 3: Loading / empty / error; lists (done)

- New helpers in `core-ui/theme/MotionModifiers.kt`: `LazyItemScope.animateListItem()`, `LazyGridItemScope.animateGridItem()`, `LazyStaggeredGridItemScope.animateStaggeredGridItem()` (tween specs, nothing when reduced), `ContentPhase` enum, `rememberFirstEntrance()` + `Modifier.staggeredEntrance(index, play)`.
- `AnimatedContent` + `Motion.contentSwap` (crossfade 200ms, size on the same tween): home latest-audio row (error ↔ row) and both home rows (row ↔ empty message), articles list, audio list, videos list, designs grid, notifications, search (keyed on state kind; `Success` split into "empty"/"results"), reader, category screens (shared `CategoryScreenContent`), player details (loading ↔ details).
- `animateItem` on keyed items: home rows, articles, audios, videos, designs, notifications, search rows and group headers.
- Home first-visit stagger: header, carousel, category grid, latest articles, latest audio; 40ms apart, 8dp rise, `medium` decelerate, drawn with `graphicsLayer` (no layout movement). `rememberSaveable` flag so going back to Home doesn't replay; the window closes after ~620ms so items composed later don't animate.
- Skipped (no stable keys, would need data changes): legal sections, study lists (levels, playlists, lessons, quiz), top students.

## Phase 4: Player (done)

All in `feature-audio/.../detail/AudioDetailScreen.kt`.
- Play ↔ pause: `AnimatedContent` on `isPlaying`, new icon scales 0.8→1 and fades in (`short`); `HapticFeedbackType.ContextClick` on tap.
- Seek thumb: 16dp → 22dp while dragging, done as a `graphicsLayer` scale so the slider track never re-lays out.
- Buffering: the button always stays; a 2dp indeterminate `accentStrong` ring (84dp, transparent track) is drawn around it whenever `isBuffering` (it used to replace the button only before the duration was known).
- Download: `AnimatedContent` idle → progress ring → check; the check pops in 0.6→1 with `spring(dampingRatio 0.8, StiffnessMediumLow)` and `HapticFeedbackType.Confirm`, only for a download that finished while the screen was open.
- Speed label: `AnimatedContent` + `Motion.countSlide` (new value from below).
- Inline download card: `AnimatedVisibility` with expandVertically + fade (`medium`) instead of `animateContentSize` on the parent: the parent is a `fillMaxSize` scroll column, so its size never changes and `animateContentSize` would do nothing. Same visible result: nothing below jumps.
- Reduced motion: icon/label swaps and the card are instant; the progress ring and buffering ring stay (functional).

## Phase 5: Profile and theme (done)

- Segmented control (`ProfileScreen.ThemeSegmentedControl`): one pill behind the three segments, `animateDpAsState` for offset (from START, so RTL-correct) and width with `stateChangeSpec()`; segment text/icon colours via `animateColorAsState`. The segments are equal width, so the width animation is a no-op today; it is there so the pill follows if the segments ever become content-sized.
- Theme change: `core-ui/theme/AnimatedTheme.kt` animates every `BrandPalette` colour AND every Material `ColorScheme` colour (`tween(medium, standard)`, `snap()` under reduced motion) inside `HassanAlHawaryTheme`. Material colours were included because many screens still read `MaterialTheme.colorScheme` (error text, notification cards); animating only the brand palette would have left those flashing.
- Status/navigation bar icons and the window background: `MainActivity` now sets them in a `LaunchedEffect(isDarkTheme)` that waits `Motion.MEDIUM` on a change (not on first launch, not under reduced motion), i.e. at the end of the colour animation.
- Reader font size: Profile's step number and the reader dropdown's number and preview line crossfade (`contentSwap`); the article body fades out (100ms), re-lays out at the new size, and fades back in (100ms). A true crossfade of the body would need two copies of a long scrollable column sharing one `ScrollState`, which Compose doesn't allow; fade-through looks the same at this speed.

## Phase 6: Search and chips (done)

- `core-ui/components/PillStyle.kt`: `animatedPillStyle(selected)` animates fill, border colour, border width (0.5→1dp) and text colour with `stateChangeSpec()`. Used by the search filter pills (and the suggestion pills, which share `FilterPill`) and the share preview clip-length chips (`ClipSelector`).
- Search pill counts: label and count are separate texts now; the count is an `AnimatedContent` with `Motion.countSlide` (up when the number grows, down when it shrinks). Same visual text as before ("صوتيات ٩").
- Quote selection counter ("٩٨ حرفًا · صورة واحدة"): `AnimatedContent` + `countSlide`, direction from the character count. It changes continuously while a handle is dragged; each change is a 150ms slide, so it reads as a rolling counter. If that feels busy on device, the simplest fix is to key it on the page count only.
- "الكل" ↔ a single type: handled by Phase 3's `animateItem` on the result rows and headers; search's `AnimatedContent` keeps the same content key for any non-empty result, so it doesn't crossfade over it.
- Reduced motion: colours snap, counts swap.

## Decisions

- The spec says "same rules as overnight-ui-pass A–C". Its off-limits list (Home, sign-in, carousel, feature-share) is superseded here: this spec explicitly asks for motion on Home, sign-in and share, so those are in scope for the motion items listed.
- Ali was present at the start and asked for phases 0–7 only; stopping after Phase 7 is intentional, not a skip.

## Commits

- `116c053` chore: snapshot before motion pass
