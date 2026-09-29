# Motion pass log

Spec: `docs/plans/motion-pass.md`. Branch: `ui/motion` (from `116c053 chore: snapshot before motion pass`).

Two sessions: phases 0–7, then 8–12 (the split Ali asked for). All phases done.

## Phases

- [x] Phase 0: Setup and inventory
- [x] Phase 1: Foundations
- [x] Phase 2: Screen transitions
- [x] Phase 3: Loading / empty / error states; lists
- [x] Phase 4: Player
- [x] Phase 5: Profile and theme
- [x] Phase 6: Search and chips
- [x] Phase 7: Reader
- [x] Phase 8: Container transform
- [x] Phase 9: Onboarding, splash, sign-in
- [x] Phase 10: Share preview, quote selection, image viewer
- [x] Phase 11: Haptics and polish check
- [x] Phase 12: Reduced-motion pass and final report

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

## Phase 7: Reader (done)

- `AppTopBar` got an optional `titleVisible: Boolean = true`; the title fades with `stateChangeSpec()` via `graphicsLayer` alpha and always keeps its space, so nothing shifts. All other callers are unchanged.
- Reader: the big title reports where it ends in the scroll content (`onGloballyPositioned`, + the 16dp top padding); `derivedStateOf { scrollState.value > titleBottom }` drives the bar title (the part before the ":", one line with ellipsis). No recomposition while scrolling except when the flag flips.
- Reading-progress line: already driven directly by the scroll (`derivedStateOf` + the lambda `progress` overload of `LinearProgressIndicator`, which does not animate). Verified, left as is.

## Phase 8: Container transform (done, not yet seen on a device)

- `core-ui/theme/SharedElements.kt`: `LocalSharedTransitionScope`, `LocalNavAnimatedVisibilityScope`, `ProvideNavAnimatedScope`, `SharedKeys`, `Modifier.sharedContainer(key, shape)` (`sharedBounds`, `scaleToBounds`, clipped overlay) and `Modifier.sharedPart(key, shape, scaleContent)`. Bounds use `tween(LONG = 400ms, emphasized decelerate)`. Both are no-ops under reduced motion or when no scope is provided (previews, other destinations), so the normal shared-axis transition plays.
- `MainActivity`: `SharedTransitionLayout` wraps the `NavHost` (it took over the `padding(innerPadding)` modifier); home, articles list, reader, player, designs and viewer destinations provide their `AnimatedContentScope`. The detail screens got only an optional `modifier` parameter (reader, player, viewer), which `MainActivity` fills with the container key built from the route argument, so the key exists from the first frame.
- Flows: Home article card and articles-list card → reader (container + title); Home audio card → player (container + gold circle → play button, re-measured, circle clip); design tile image → viewer.
- Keys: article keys include the source (`ArticleSource.Home` / `List`), because the same article can be on Home and in the list and Home → list would otherwise morph between the two. The reader registers both (`sharedArticleContainer` / `sharedArticleTitle`). Audio keys use the URL (the route argument decodes to the same string); designs use the group id.
- Back to a card that has scrolled away or isn't composed: no match, so the shared-axis pop plays (built into `sharedBounds`).
- Known limits: the reader title and the player's play button live behind the loading crossfade (Phase 3), so on the way IN they appear a few frames into the transition (Room load) and join the morph late; on the way BACK they always match. The containers themselves always match.

## Phase 9: Onboarding, splash, sign-in (done)

- Onboarding (`OnboardingScreen.kt`): each illustration is translated by 40% of its page offset against the swipe (`graphicsLayer`, read in the draw phase, RTL-aware sign), so it travels at ~60% of the finger; off under reduced motion. The dots are driven directly by the pager position (width 8→18dp and colour lerp), so the pill stretches from one dot to the next during the swipe; no animation of its own, so nothing to switch off. The button label crossfades التالي → لنبدأ (`contentSwap`).
- Splash: `core-splashscreen` is a dependency, so `setOnExitAnimationListener`: the icon fades and scales to 0.9 while the splash view fades, `medium`, emphasized accelerate (`PathInterpolator` with the same control points as `Motion.EmphasizedAccelerate`), then `remove()`. `iconView` access is wrapped in `runCatching` (it can be missing on some launch paths). Under reduced motion the splash is removed at once. `Context.animationsRemoved()` in `Motion.kt` is now public for this.
- Sign-in (`LoginWithGoogleComp`): a full-width box keeps the caller's layout; the pill inside animates its size (`animateContentSize`, `medium` standard, snap when reduced) from full width to a 48dp circle while signing in, and the logo + label crossfade to the spinner. On failure `isLoading` goes false and it grows back with the label.

## Phase 10: Share preview, quote selection, image viewer (done)

- Clip selector (`ClipSelector.kt`): dragging in either strip ticks `HapticFeedbackType.SegmentTick` each time the start or end crosses a 5 s mark (`ClipWindow.crossesStep`, pure, in core-domain with a unit test). Compared with the last window the drag produced (the VM's copy lags a frame, which would double-tick). Choosing a length chip ticks too. Taps, nudges and the stepper don't tick (not asked for).
- Preview waveform: the VM polls the player every 200ms, so the fill stepped 5×/s. `SharePreviewScreen.smoothPlaybackPosition` now moves linearly between polls while playing and snaps on jumps (pause, seek back, new clip). Cost: the preview column recomposes every frame while previewing (it did 5×/s before).
- Quote selection: the highlight and both handles fade in when a selection appears and out when it's cleared (`stateChangeSpec`; the last range is drawn while fading out). Resizing a selection stays immediate. The bottom panel's thumbnail is a `Crossfade` (200ms, snap when reduced) keyed on the first page.
- Image viewer: vertical `draggable` on the pager (outside its moving layer), disabled while zoomed; the image follows the finger, the black background fades with the distance, the top bar and thumbnails fade 3× faster. Past 25% of the height, letting go pops the screen immediately, so the Phase 8 container transform shrinks it from where the finger left it into its tile (or the shared-axis pop if the tile isn't on screen); otherwise it springs back (`spring(0.85, MediumLow)`, snap when reduced). Decision: no separate "fling off-screen" animation before the pop, because the container would then shrink an empty black box.
- Pinch zoom, double tap and the horizontal pager keep their own gestures; the vertical drag only claims vertical movement past the touch slop.

## Phase 11: Haptics and polish check (done)

- Haptics, whole app (grep for `performHapticFeedback`): play/pause `ContextClick`, download complete `Confirm`, clip 5 s steps and length chips `SegmentTick`, quote selection `LongPress` (start) and `TextHandleMove` (grab a handle; both pre-existing). Nothing else. The newer types exist in compose ui 1.10.1, so no fallbacks were needed.
- Rule fixes in code that predates this pass:
  - Study map (`LevelsJourneyMap`): the current level pulsed forever (`infiniteRepeatable`) → grows once to 1.15 (`medium`) and stays; the path reveal went from 1.4 s to `long` (400ms); both instant under reduced motion.
  - Study motivational pager: auto-scroll tween 600ms → `medium` emphasized; off under reduced motion.
  - Study segmented indicator: 260ms FastOutSlowIn → `stateChangeSpec()`.
  - Lesson detail play/pause: 250ms fade → `Motion.contentSwap`.
  - Home carousel autoplay: default spring → `tween(medium, emphasized)` (springs are for gestures only).
- Left alone, with reasons:
  - Shimmer (1.6 s, infinite): the spec says keep it; it is a loading indicator, not decoration, and is still under reduced motion.
  - `feature-splash-screen`'s `SplashScreen` composable (1.5 s overshoot): its `splash_screen` route is registered but nothing navigates to it (start destination is `home_screen`). Unreachable; removing it is outside this pass.
  - Indeterminate progress spinners/rings: functional.
- Nothing animates on every scroll: the reader title flips on a threshold; the reading-progress bar and onboarding parallax follow the gesture directly with no animation of their own; `animateItem` only runs on data changes.
- No animation longer than `long` remains outside the shimmer and the unreachable splash composable.

## Phase 12: Reduced-motion pass (done)

Every addition was checked against `LocalReducedMotion` (a grep for `animate*AsState`, `AnimatedContent`, `AnimatedVisibility`, `Crossfade` and `Animatable` across the app, file by file):

| Addition | Under reduced motion |
|---|---|
| Screen transitions (fade through, shared axis) | plain 100ms fade |
| Container transforms | off (plain 100ms fade) |
| Content swaps, count slides, speed label, play/pause icon, download check, sign-in label | instant swap |
| List item placement / arrival | off |
| Home entrance stagger, onboarding parallax, carousel and study pager autoplay | off |
| Shimmer | still placeholder |
| Colour and size state changes (pills, segmented pill, top-bar title, highlight, seek thumb, theme colours, study indicator) | snap |
| Reader text-size fade, quote thumbnail, sign-in width, download card | instant |
| Theme switch bar icons | set at once (no 300ms wait) |
| Splash exit | removed at once |
| Image viewer spring back | snap |
| Progress (spinners, buffering ring, download ring, reading bar, preview playhead, generation progress) | kept: functional |
| Onboarding dots, dismiss drag | kept: they follow the finger, no animation of their own |

Two pre-existing animations were also brought in line here: the video player's title overlay (default spring fade → `short`, none when reduced) and the share generation progress (default spring → `stateChangeSpec()`).

### Debug-only "force reduced motion"

`ReducedMotionOverride.forced` (core-ui `Motion.kt`) is OR-ed into `Context.animationsRemoved()`, the one function both Compose (`rememberSystemReducedMotion`) and the splash use. `MainActivity` sets it only when `BuildConfig.DEBUG` and the launch intent has the extra:

```
adb shell am start -n app.netlify.devalihassan/.MainActivity --ez force_reduced_motion true
```

It lasts for that process (force-stop the app to turn it off). Release builds never read the extra. The real setting (Settings → Accessibility → Remove animations, or Developer options → Animator duration scale → Off) still works and is picked up live, without a restart.

## Decisions

- The spec says "same rules as overnight-ui-pass A–C". Its off-limits list (Home, sign-in, carousel, feature-share) is superseded here: this spec explicitly asks for motion on Home, sign-in and share, so those are in scope for the motion items listed.
- Ali asked for the pass in two sessions (0–7, then 8–12).
- Compose resolves to 1.10.1 (not the BOM's 1.8), so no **Needs** item was skipped.
- Reduced motion is provided by the theme, not only the NavHost, so onboarding and sign-in follow it too.
- Download card: `AnimatedVisibility` (expand + fade) instead of `animateContentSize` on a parent whose size never changes.
- Theme animation covers the Material colour scheme as well as the brand palette.
- Reader text size: fade-through of the body, not a two-copy crossfade (one `ScrollState` can't drive two columns).
- Article shared-element keys carry their source (Home / list) to avoid a Home → list morph; the reader matches both.
- Image viewer: releasing past 25% pops at once and lets the container transform take the image home, rather than flinging it off first.
- Preview waveform smoothing between the VM's 200ms polls (the spec asked to verify smoothness; it stepped).
- Pre-existing looping pulse and >400ms animations in the study screens were changed to meet the section B rules; the shimmer and the unreachable `splash_screen` composable were left (see Phase 11).

## Commits on `ui/motion`

- `116c053` chore: snapshot before motion pass
- `26b762b` motion: phase 0 - setup and inventory
- `1b7d3ba` motion: phase 1 - foundations
- `4826ba5` motion: phase 2 - screen transitions
- `ae84d4a` motion: phase 3 - loading, empty and error states; lists
- `edefb30` motion: phase 4 - player
- `d663085` motion: phase 5 - profile and theme
- `0c2d4f1` motion: phase 6 - search and chips
- `6c7938e` motion: phase 7 - reader
- `d6799ca` motion: phase 8 - container transform for article, audio and design flows
- `931fd28` motion: phase 9 - onboarding, splash, sign-in
- `95e1c6c` motion: phase 10 - share preview, quote selection, image viewer
- `5a04360` motion: phase 11 - haptics and polish check
- (this commit) motion: phase 12 - reduced-motion pass and final report

Every phase built with `assembleDebug testDebugUnitTest`. Nothing has been run on a device or emulator yet.

## Device checklist

In Arabic, on a real phone:

1. **Tabs** الرئيسية / بحث / المعهد / حسابي: fade through (quick fade out, fade + slight grow in), no sliding.
2. **Deeper and back** (home → categories → list → player, profile → about): the new screen comes in from the LEFT and the old one drifts right; back mirrors it.
3. **Predictive back** (Android 14+, gesture navigation): dragging back from the edge previews the pop; letting go completes it, cancelling restores the screen.
4. **Container transforms**, forward and back: Home article card → reader, articles-list card → reader (title morphs), Home audio card → player (gold circle → play button), design tile → viewer. Also go back after scrolling the card off screen (should fall back to the slide).
5. **Home**: first open staggers header, carousel, grid, articles, audio; going back to Home or switching tabs doesn't replay it.
6. **Lists**: loading → content crossfades; search filter changes rearrange rows; "الكل" ↔ one type.
7. **Player**: play/pause swap + light haptic; seek thumb grows while dragging; buffering ring around the button (try a slow network); download ring → check pop + confirm haptic; speed label slides; download card grows/shrinks.
8. **Theme**: the segmented pill slides; light ↔ dark crossfades the whole app; the bar icons flip at the end, not the start.
9. **Reader**: A−/A+ (profile and reader) fades the body; the title appears in the top bar once scrolled past and leaves when scrolled back; the progress line tracks the scroll with no lag.
10. **Search pills**: colours and counts animate. **Quote selection**: counter rolls, highlight fades in/out, thumbnail crossfades.
11. **Share preview**: haptic ticks every 5 s while dragging the clip or a handle, and on length chips; the waveform fill glides while previewing.
12. **Onboarding**: illustrations lag the swipe (parallax), the dot pill stretches, التالي → لنبدأ crossfades.
13. **Splash**: the logo fades and shrinks as the first screen appears.
14. **Sign-in**: the button shrinks to a spinner circle; cancel the Google sheet and it grows back with the label.
15. **Image viewer**: swipe down (and up) to dismiss: the image follows, the background fades; a short swipe springs back; pinch zoom still works and blocks the swipe while zoomed.
16. **Everything again with reduced motion** (system setting, or the adb command above): quick fades only, no stagger/parallax/autoplay, still shimmer, instant swaps, splash gone at once.

## Known risks

- **Shared elements are unverified on device.** Most likely issues: the reader title / player play button joining the morph a few frames late on the way in (they sit behind the loading crossfade); `scaleToBounds` looking stretched while a full screen shrinks into a small card. If a flow misbehaves, removing its `modifier = Modifier.shared…` argument in `MainActivity` turns it back into the plain slide without touching anything else.
- Shared-element keys: articles by id + source, audio by URL, designs by group id. An audio URL appears once on Home, so no collision today; if another screen ever gets `SharedKeys.audio(...)` while Home is also composed, they would morph into each other.
- Theme switch recomposes the whole tree every frame for 300ms (the palette is a `staticCompositionLocalOf` of plain colours). Check a low-end phone.
- Share preview recomposes every frame while previewing (waveform smoothing).
- Quote counter animates on every character while a handle is dragged.
- `AnimatedContent` around paging lists keeps the hoisted `LazyListState`; if a list ever jumps to the top after a refresh, look there first.
- Recurring Windows file lock on `core-ui/.../classes.jar` while Android Studio is open; `./gradlew --stop` clears it.

## Follow-up after device testing (2026-09-29)

Ali reported janky scrolling in the articles and audios lists, and that the image viewer's background didn't match the designs grid.

- **Lists:** removed `animateItem` from the long paging lists (articles, audios, videos). The designs grid, which Ali reported as smooth, keeps it, as do the small lists (Home rows, search, notifications). Articles-list rows no longer register shared elements either: each row added a `sharedBounds` node and registered and unregistered with the `SharedTransitionLayout` as it scrolled in and out. The article → reader transform now only runs from the Home cards, so keys went back to one per article (`ArticleSource` and the reader's double registration are gone), and the articles-list destination no longer provides a nav scope.
- **Still in place and the next suspect if lists remain slow:** `SharedTransitionLayout` makes the whole NavHost a lookahead scope, so every screen, lists included, is measured in a lookahead pass as well. If a **release** build (minified; debug Compose is much slower) still stutters, the next step is removing the container transforms (Phase 8) altogether.
- **Image viewer:** background is `Brand.colors.background` (cream / dark brown with the theme) instead of black, and the title, counter, icons and error text use the brand text colours. With the same colour under and over it, a background fade would show nothing, so the swipe-to-dismiss feedback is now a slight shrink (1 → 0.85 with the distance) as the image follows the finger; the bars still fade.
