# Motion pass: transitions and micro-interactions for the whole app

> Spec for a Claude Code session that may run unattended. Ali may be asleep and can't answer.
> This file is the source of truth. Progress is tracked in `docs/plans/motion-pass-log.md`.
> Same operating rules and hard boundaries as `docs/plans/overnight-ui-pass.md` sections A–C: never ask or wait; log decisions; don't end your turn until every phase is Done or Skipped and the final report is written; skip items, not phases; time-box each phase to about 25 minutes; re-read this spec and the log after any context compaction.

## A. Boundaries

- UI and animation code only. No database, backend, navigation-graph (routes) or module changes, and no new dependencies or version bumps. No edits to Gradle or `*.properties` files.
- Some items need a minimum library version (noted below as **Needs**). In Phase 0, read the versions from the version catalog. If a version is too old, skip the item and log it for Ali; don't upgrade anything.
- Work on a new branch `ui/motion` from HEAD. Build and test after every phase (`./gradlew assembleDebug testDebugUnitTest`), fix what you broke (3 attempts, then stash the item and log it), and commit `motion: phase N - <name>`.
- Right to left: every horizontal slide uses Start/End semantics, never Left/Right. "Forward" (going deeper) enters from the END side and moves toward START; "back" does the reverse. Media timelines, waveforms and transport controls stay left to right, as they are today.
- Reduced motion (Phase 1) must be respected by everything you add.

## B. Motion rules (Phase 1 creates them; every later phase uses only these)

- Durations: `short` 150ms (small state changes: color, icon swap, chip), `medium` 300ms (screen transitions, theme change), `long` 400ms (container transform only).
- Easing (Material 3): emphasizedDecelerate `CubicBezier(0.05, 0.7, 0.1, 1.0)` for entering, emphasizedAccelerate `CubicBezier(0.3, 0.0, 0.8, 0.15)` for leaving, standard `CubicBezier(0.2, 0.0, 0.0, 1.0)` for state changes. Springs (medium-low stiffness, no bounce above 0.8 damping) only for gesture-driven motion: drag, zoom, swipe to dismiss.
- Never animate longer than `long`. No bouncy, rotating or flipping transitions. No looping decorative animation.

---

## Phase 0: Setup and inventory

1. `git status`; commit a dirty tree as `chore: snapshot before motion pass`. Create branch `ui/motion`.
2. Baseline build and tests; if they fail, log it and mark phases "unverified" (don't fix the environment).
3. Record in the log: the Compose BOM / foundation / animation versions, Navigation Compose version, whether `androidx.core:core-splashscreen` is a dependency, where the `NavHost` lives, how the bottom-nav destinations are defined, where `Brand` / `LocalBrandPalette` and `HassanAlHawaryTheme` live, the screens and components named in this spec, and any existing animations (shimmer, carousel indicator) so you reuse them.

## Phase 1: Foundations

- Create `Motion` in `core-ui` (for example `core/core-ui/.../theme/Motion.kt`): the durations and easings from section B, plus ready-made specs: `fadeThroughEnter/Exit`, `sharedAxisEnter/Exit/PopEnter/PopExit`, `stateChange` (tween short standard), `contentSwap` (crossfade ~200ms).
- Reduced motion: a `LocalReducedMotion` CompositionLocal, provided at the app root, true when `Settings.Global.ANIMATOR_DURATION_SCALE` is 0 (the accessibility "Remove animations" setting). When true: screen transitions become a plain ~100ms fade, and decorative motion (entrance staggers, parallax, the carousel autoplay, shimmer movement, count slides) is off. Functional feedback (progress, selection color) stays, instantly.
- Unit-test anything pure (for example the direction helper that turns forward/back + layout direction into a slide sign).

## Phase 2: Screen transitions (NavHost)

- **Bottom-nav tabs → fade through.** Switching between الرئيسية، بحث، المعهد، حسابي: the old tab fades out over about 90ms (accelerate); the new tab fades in over about 210ms after a 90ms delay while scaling from 0.92 to 1.0 (decelerate). No sliding between tabs. Apply only when both the initial and target destinations are bottom-nav roots.
- **Going deeper → shared axis X.** Everything else (categories → list → player, profile → about, home → articles, and so on): the new screen enters from the END side offset by 30dp while fading in (`medium`, decelerate); the old one moves 30dp toward START while fading out. Pop (back) mirrors it. Set this once on the `NavHost` (`enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition`), not per screen.
- **Predictive back.** Add `android:enableOnBackInvokedCallback="true"` to the `<application>` in the manifest. **Needs** Navigation Compose 2.8+ for the back gesture to scrub your pop transitions; below that, still add the flag (it's harmless) but log that the gesture preview needs the upgrade. Check that every existing `BackHandler` still works (dialogs, the image viewer, the share preview).

## Phase 3: Loading, empty and error states; lists

- Everywhere a screen switches between loading / error / empty / content (Home sections, the articles list, the reader, search, categories, the player's details), switch with `AnimatedContent` keyed on the state type, using `contentSwap`. Keep the existing shimmer; the real content crossfades in over it instead of popping.
- Every `LazyColumn` / `LazyRow` with stable keys gets `Modifier.animateItem()` on its items (**Needs** Compose foundation 1.7+; older versions have `animateItemPlacement`, use that instead). This makes new articles arriving at the top of the list and search results changing with a filter move smoothly instead of jumping.
- Home, first composition only (remember it with `rememberSaveable`, so going back to Home doesn't replay it): the sections (header, carousel, grid, latest articles, latest audio) fade in and rise 8dp one after another, 40ms apart, `medium` decelerate. Off under reduced motion.

## Phase 4: Player (audio detail)

- Play ↔ pause: `AnimatedContent` between the icons with scale 0.8→1 plus fade (`short`), and a light haptic on tap.
- Seek thumb: grows from 16dp to 22dp while the user drags (`short`), back when released.
- Buffering: instead of replacing the play button with a spinner, draw a thin indeterminate ring (2dp, `accentStrong`) around the 76dp button; the button stays visible.
- Download: progress ring → check mark with a small spring scale pop (0.6→1), plus a confirm haptic, when it finishes.
- Speed label: `AnimatedContent` with a short vertical slide (new value from below) when the speed changes.
- The inline download card enters and leaves with `animateContentSize` on its parent plus a fade, so nothing jumps.

## Phase 5: Profile and theme

- Segmented control تلقائي / فاتح / داكن: one selection pill that slides and resizes between segments (`animateDpAsState` for offset and width, `stateChange`), instead of each segment toggling its own background.
- Theme change: animate every `Brand` palette color from the old value to the new one over `medium` (build the provided palette from `animateColorAsState` values inside `HassanAlHawaryTheme`), so the whole app changes theme smoothly instead of flashing. Instant under reduced motion. Also switch the status/navigation bar icon contrast at the end of the animation, not the start.
- Reader font size (the A−/A+ in Profile and in the reader): no animation of text size; crossfade the preview line and the article body (`contentSwap`).

## Phase 6: Search and chips

- Filter pills and every chip that can be selected (search filters, clip-length chips): `animateColorAsState` for fill, border and text (`stateChange`).
- Result counts inside pills ("صوتيات ٩") and the "٩٨ حرفًا · صورة واحدة" line in quote selection: `AnimatedContent` with a short vertical slide when the number changes. Off under reduced motion (just swap).
- Grouped results under "الكل" ↔ a single type: results rearrange through `animateItem` (Phase 3), no extra animation.

## Phase 7: Reader

- Collapsing title: when the article's big title scrolls out of view, a compact one-line title fades into the top bar (`short`), and fades out when it comes back. Derive visibility from the scroll state; no layout jumps.
- Reading-progress line: keep it; make sure it's driven directly by scroll (no animation lag).

## Phase 8: Container transform for three flows (the "polished" signal)

Use `SharedTransitionLayout` around the `NavHost` and `Modifier.sharedBounds(rememberSharedContentState(key), animatedVisibilityScope)` (**Needs** Compose animation 1.7+; skip the whole phase and log it if older). Pass the scopes down with CompositionLocals, without changing any screen's public parameters beyond an optional modifier. Only these three:
1. Home/list **article card → reader**: the card's bounds grow into the reader; the title morphs into the reader title.
2. Home **audio card → player**: the gold play circle morphs into the player's play button; the card grows into the screen.
3. Designs **tile → image viewer**: the image grows from its tile to full screen.
Duration `long`, emphasized. Keys must be unique per item (use the item id). If an item isn't on screen when going back, fall back to the shared-axis pop. Time-box this phase to 40 minutes; if it can't be made stable, stash it and log exactly what failed.

## Phase 9: Onboarding, splash, sign-in

- Onboarding pager: illustrations move at about 60% of the swipe speed (parallax via `graphicsLayer` translation from the pager offset), the indicator pill stretches between dots, and the button label crossfades from التالي to لنبدأ on the last page. Parallax off under reduced motion.
- Splash exit: fade and slightly shrink the logo (to 0.9) over `medium`, then let the first screen fade in. Use `core-splashscreen`'s `setOnExitAnimationListener` if it's a dependency; otherwise the platform API on Android 12+ only. Remove nothing else.
- Sign-in button: while signing in, the label crossfades to a spinner and the button animates its width to a circle (`animateContentSize`); on failure it expands back with the label.

## Phase 10: Share preview, quote selection, image viewer

- Share preview clip selector: a light tick haptic each time the clip window or a handle crosses a 5-second step while dragging, and when a length chip is chosen. The preview's waveform keeps filling in sync with playback (already there; verify it stays smooth).
- Quote selection: the highlight on a tapped sentence fades in and out (`short`) instead of appearing instantly; the bottom panel's thumbnail crossfades when it updates.
- Image viewer: swipe down to dismiss. The image follows the finger, the black background fades with the drag distance, and releasing past about 25% of the height closes the viewer (spring); otherwise it springs back. Pinch zoom (existing) keeps working; the swipe only applies when not zoomed.

## Phase 11: Haptics and polish check

- Haptics only where listed (play/pause, download complete, clip steps, length chips, selection in quote screen). Use Compose's newer types (`Confirm`, `SegmentTick`) if the Compose version has them; otherwise `LongPress` for confirm and `TextHandleMove` for ticks.
- Go through every screen: no animation longer than `long`, no looping decorative motion, nothing that animates on every scroll.

## Phase 12: Reduced-motion pass and final report

1. Review every addition against `LocalReducedMotion`. Add a debug-only way to force it on for testing, and describe it in the log.
2. Finish `docs/plans/motion-pass-log.md`:
   - Each phase Done or Skipped, with reasons (including any **Needs** version that was too old).
   - Decisions you made.
   - The commits on `ui/motion`.
   - **Device checklist:** tab switches (fade through), deeper navigation and back (shared axis, both directions look right in Arabic), the predictive back gesture on Android 14+, the three container transforms forward and back, the player interactions, the theme switch, the segmented pill, search pills and counts, reader collapsing title, onboarding parallax, splash exit, sign-in button, image viewer swipe down, and everything again with "Remove animations" turned on.
   - Known risks (for example a shared-element key that could collide).

## Out of scope

New screens or features, backend or database changes, dependency upgrades, and animation libraries (Lottie and similar).
