# Motion pass log

Spec: `docs/plans/motion-pass.md`. Branch: `ui/motion` (from `116c053 chore: snapshot before motion pass`).

This session: Ali asked for phases 0–7 in one go; phases 8–12 follow in a later session.

## Phases

- [x] Phase 0: Setup and inventory
- [ ] Phase 1: Foundations
- [ ] Phase 2: Screen transitions
- [ ] Phase 3: Loading / empty / error states; lists
- [ ] Phase 4: Player
- [ ] Phase 5: Profile and theme
- [ ] Phase 6: Search and chips
- [ ] Phase 7: Reader
- [ ] Phase 8: Container transform (next session)
- [ ] Phase 9: Onboarding, splash, sign-in (next session)
- [ ] Phase 10: Share preview, quote selection, image viewer (next session)
- [ ] Phase 11: Haptics and polish check (next session)
- [ ] Phase 12: Reduced-motion pass and final report (next session)

## Phase 0: inventory

- Versions (`gradle/libs.versions.toml`): Compose BOM `2025.07.00` → compose foundation / animation / ui **1.8.x**; material3 `1.4.0` (explicit `material3Android`); Navigation Compose **2.9.2**; `androidx.core:core-splashscreen` **1.2.0 is a dependency** (used in `MainActivity`: `installSplashScreen()`).
  - Every **Needs** item is satisfied: `Modifier.animateItem()` (foundation 1.7+), `SharedTransitionLayout` / `sharedBounds` (animation 1.7+), predictive back scrubbing pop transitions (nav 2.8+).
  - `HapticFeedbackType.Confirm` / `SegmentTick` etc. exist from compose ui 1.8 → available.
- `NavHost`: `app/src/main/java/com/example/hassanalhawary/MainActivity.kt` → `MainAppContent()` (string routes, start `home_screen`). Admin app has its own, out of scope.
- Bottom-nav destinations: `app/.../ui/navigation/BottomNavItem.kt` — `home_screen`, `search_screen`, `study_screen` (registered as `study_screen?data={data}`), `profile_screen`. `BottomNavigationBar.kt` navigates with `popUpTo(start){saveState}`, `launchSingleTop`, `restoreState`.
- Theme: `core/core-ui/.../theme/Theme.kt` (`HassanAlHawaryTheme`, provides `LocalBrandPalette` + MaterialTheme); `BrandColors.kt` (`BrandPalette`, `Light/DarkBrandPalette`, `LocalBrandPalette` = `staticCompositionLocalOf`, `Brand.colors`). App's `ui/theme/Theme.kt` just forwards to core-ui.
- System bars: `MainActivity` sets them in a `DisposableEffect(isDarkTheme)` via `enableEdgeToEdge(...)`.
- Existing animations to reuse: `Modifier.shimmer()` (`core-ui/components/CustomShimmer.kt`, infinite transition); home carousel indicator width (`ImageCarousel.kt`, `animateDpAsState`); share generation progress; study dashboard indicator/pulse; `LessonDetailScreen` AnimatedContent; video player controls `AnimatedVisibility`.
- Existing `BackHandler`s: only `VideoPlayerScreen` (exits fullscreen). Image viewer / share preview use no BackHandler.
- Unit tests: `core-ui` has `src/test` + `testImplementation(junit)` → Motion helpers and tests go there.

## Decisions

- The spec says "same rules as overnight-ui-pass A–C". Its off-limits list (Home, sign-in, carousel, feature-share) is superseded here: this spec explicitly asks for motion on Home, sign-in and share, so those are in scope for the motion items listed.
- Ali was present at the start and asked for phases 0–7 only; stopping after Phase 7 is intentional, not a skip.

## Commits

- `116c053` chore: snapshot before motion pass
