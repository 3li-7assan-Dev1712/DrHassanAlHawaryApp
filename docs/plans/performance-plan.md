# Performance improvement plan

Based on the measurements in [`docs/reports/performance-audit.md`](../reports/performance-audit.md) (Redmi Note 13 Pro, Android 16, 120 Hz, release build, 2026-09-29).

**Goals (measured the same way as the audit, on the same phone):**

| Metric | Now | Target |
|---|---|---|
| `assembleRelease` | ❌ fails | ✅ green, same Compose version in debug and release |
| Cold start to first frame (fresh install) | ≈ 880 ms | ≤ 650 ms |
| Time to first real content | not measured (no `reportFullyDrawn`) | reported, ≤ 1.2 s |
| Screen-open transition p50 / p90 | 10–15 ms / 13–24 ms | ≤ 8.3 ms / ≤ 12 ms |
| Worst transitions (home card → article, image viewer) p90 | 42 / 30 ms | ≤ 16 ms |
| Peak graphics memory (images tour) | 344 MB | ≤ 120 MB |
| Largest bundled bitmap | ≈ 127 MB (`telegram_img`) | ≤ 2 MB |
| Collectors started per foreground | +1 each time | exactly 1 per process |

Phases are ordered by **impact ÷ effort**, and each can be its own PR. Phases 1–3 are small and fix real bugs, so do them first.

---

## Phase 0 — Make release build (blocker, ~1 h)

1. Move `composeBom` in `gradle/libs.versions.toml` to a BOM that maps to Compose **1.10.x** (2025.12.00 or newer), since the code already depends on 1.10 APIs (`ResizeMode.scaleToBounds` in `core-ui/.../SharedElements.kt`).
2. Remove the version drift so debug can't silently run ahead of release again:
   - Make the tooling deps BOM-aligned (`debugImplementation(platform(libs.androidx.compose.bom))` next to `debugApi(libs.androidx.ui.tooling)` in `core-ui`), or
   - Add a CI / pre-push check: `./gradlew :app:assembleRelease` (and optionally `dependencies` diff of `debugRuntimeClasspath` vs `releaseRuntimeClasspath` for `androidx.compose.*`).
3. Check the uncommitted `leakcanary` entry (`com.android.tools.studio.leakcanary:leakcanary:1.0.0`). That's Android Studio's internal artifact, not Square's `com.squareup.leakcanary:leakcanary-android`. Either switch to the Square artifact or drop the change.
4. Re-run the smoke tour on the **release** build: R8 has never processed the motion-pass code.

**Done when:** `./gradlew :app:assembleRelease` passes on a clean checkout, and both variants resolve the same `androidx.compose.animation` version.

## Phase 1 — Fix the collector leak in `HiltApplication` (~1–2 h)

Problem: `onStart(owner)` launches a new, uncancelled `observeAuthStateUseCase().collectLatest {…}` on every foreground. That stacks auth listeners, Room observers, FCM topic subscriptions and (once levels exist) duplicate sync runs.

1. Move the auth → sync/subscribe pipeline out of `onStart` into **one** job, started once in `onCreate`:
   ```kotlin
   private var syncJob: Job? = null
   override fun onCreate() {
       …
       syncJob = appScope.launch { observeAuthStateUseCase().distinctUntilChanged().collectLatest { … } }
   }
   ```
   If it should only run while in the foreground, keep it in `onStart` but cancel it in `onStop` (`syncJob?.cancel()`).
2. Make the FCM topic subscription idempotent. Subscribe only when `studentData.batch` / `isCourseMember` actually changes (`distinctUntilChangedBy`), and don't re-subscribe on every Room emission.
3. Replace "run sync whenever the `levels` Flow emits" with a one-shot trigger (e.g. `getLevelsUseCase().first { it.isNotEmpty() }`), or with a WorkManager unique periodic job (`ExistingPeriodicWorkPolicy.KEEP`). Otherwise a sync that writes to Room will re-trigger itself.
4. Decide on a single owner for auth observation: either `HiltApplication` or `MainActivityViewModel`, not both.

**Verify:** background/foreground the app 5× and confirm `HiltApplication` logs one pipeline, `dumpsys activity service` shows no growing listener count, and there's one sync per real change.

## Phase 2 — Oversized drawables (~1–2 h, biggest memory win per minute)

Resources in plain `res/drawable/` are treated as **mdpi**, so they get scaled up ×2.75 on a 440 dpi phone.

| Resource | Action |
|---|---|
| `core-ui/.../drawable/telegram_img.png` (2048², decodes ≈ 127 MB) | Resize to the largest on-screen size × 3 (it's drawn as an `Icon` at 34 dp and 18 dp → **~100 px** is plenty), convert to WebP, move to `drawable-nodpi/`, or better, ship it as a vector drawable since it's a logo. |
| `feature-home/.../drawable/fasalo_logo.png` (640²) | Resize to ~3× display size, WebP, `drawable-nodpi/`. |
| `core-ui/.../drawable/bulb_image.png` (626²) | Same as above. |
| `core-ui/.../drawable/dr_hassan_image.png` (512²) | Same as above; in `NotificationService` decode with `inSampleSize` / `BitmapFactory.Options` to the notification large-icon size (64 dp), or use `ContextCompat.getDrawable(...).toBitmap(w, h)`. |
| `feature-splash-screen/.../dr_hassan_app_logo.png` (1024²) + the `splash_screen` route + `SplashScreen` composable | Nothing navigates to `splash_screen` anymore (the system splash replaced it), so **delete** the route, the composable and the image. |
| Duplicates of `dr_hassan_image.png`, `design_3.jpg` in `app` **and** `core-ui` | Keep only the `core-ui` copy. |

Add a lint guard: enable `IconDipSize` / `IconMissingDensityFolder` warnings as errors, or add a tiny Gradle check that fails when a bitmap > 512 px sits in `drawable/`.

**Verify:** open Study as a guest user (sign out). No freeze, and `dumpsys meminfo` Graphics stays roughly flat. APK size should also drop.

## Phase 3 — Network image memory (~½ day)

1. **Configure one Coil `ImageLoader`** app-wide (`HiltApplication : ImageLoaderFactory`, Coil 2.6):
   - `memoryCache { MemoryCache.Builder(ctx).maxSizePercent(0.15).build() }` (default is 25 %)
   - `diskCache { DiskCache.Builder().directory(cacheDir.resolve("img")).maxSizePercent(0.02).build() }`
   - `crossfade(true)`, `respectCacheHeaders(false)` if Firebase Storage URLs have no useful cache headers.
2. **Images grid (`DesignTile`)**: `ContentScale.FillWidth` makes Coil resolve the size from the constraints, which can be unbounded in height. Give the request an explicit size of the tile width (e.g. `ImageRequest.Builder(ctx).data(url).size(tileWidthPx, Dimension.Undefined)` or `precision(Precision.INEXACT)`), and, better, request a **thumbnail URL** from the backend (Firebase Resize-Image extension produces `_400x400` variants).
3. **Image viewer (`ImageScreen`)**:
   - Load at screen size (`size(displayWidthPx)`), and only fetch the original when zoom > 1.5×.
   - Use `placeholderMemoryCacheKey(gridThumbKey)` so the viewer shows the already-decoded grid thumbnail instantly. That also makes the open transition cheaper (p50 19 ms today).
   - The thumbnail strip (`52.dp` `AsyncImage`s) already gets sized correctly. Keep it.
4. **Home carousel (`ImageCarousel`)**: the blurred background re-draws the full image through `Modifier.blur(10.dp)` (a RenderEffect every frame the pager moves). Replace it with a **tiny pre-blurred copy**: request the same URL at ~32 px width with a `transformations(BlurTransformation)` or plain upscaling (a 32 px image upscaled with `FilterQuality.Low` looks blurred for free), and drop the live `blur`.

**Verify:** repeat the audit's "images grid + image viewer" tour and aim for Graphics ≤ 120 MB, and image viewer open p90 ≤ 16 ms.

## Phase 4 — Baseline profile + startup (~1 day)

1. Add a `:baselineprofile` module (`androidx.baselineprofile` Gradle plugin + Macrobenchmark) with journeys matching the audit:
   cold start → Home scroll → open Articles → article detail → back → Audio category → list → player → Videos list → Images grid → image viewer → each bottom tab.
   Expected: ~15–30 % faster cold start and removal of most of the "one slow UI-thread frame per navigation" (that first frame is mostly JIT/interpreted composition). The audit's full-AOT run showed at least ~120 ms of headroom on startup alone.
2. Add `androidx.profileinstaller` explicitly to `:app`, and a **startup profile** (`generateBaselineProfile` with `includeInStartupProfile = true`) so R8 lays out startup classes in the primary dex (DEX layout optimisation).
3. Make `HiltApplication` injection lazy: change the 9 field injections to `dagger.Lazy<…>` / `Provider<…>` so the Room/Firestore/DataStore graph isn't built before the first frame (it's only needed inside the coroutine).
4. Move `shareFileStore.sweepAll()` to a low-priority `WorkManager` one-shot or delay it (`delay(5_000)`) so it doesn't compete with startup I/O.
5. Call **`reportFullyDrawn()`** (or `ReportDrawnWhen { homeContentLoaded }` from `activity-compose`) when Home's first real content is visible, so time-to-content shows in Play Vitals and in Macrobenchmark's `timeToFullDisplayMs`.
6. Shorten what `appReady` waits on: theme + onboarding come from DataStore (fast), but `state.isLoading` waits on the auth check. Render Home's skeleton as soon as theme/onboarding are ready, and swap in auth-dependent parts afterwards.
7. Check who calls Play-Services `ProviderInstaller` at startup (seen in logcat, failing on this device). If it's our code or a Ktor/OkHttp setup, move it off the main thread with `installIfNeededAsync`, or remove it (minSdk 24 already has a modern TLS stack).

**Verify:** Macrobenchmark `StartupBenchmark` (`CompilationMode.None` vs `Partial(BaselineProfileMode.Require)`), 10 iterations, compared against the audit's ≈ 880 ms.

## Phase 5 — Navigation transitions (~1 day)

The transition itself runs at 70–100 fps on 120 Hz. Beyond Phase 4:

1. **Compose stability**: add a `compose_compiler_config.conf` stability file listing the `core-domain` models (`com.example.domain.module.*`), plus `kotlinx.collections.immutable` or `@Immutable` UI models. Turn on compiler **reports/metrics** once (`composeCompiler { reportsDestination = …; metricsDestination = … }`) and fix non-skippable composables on Home, lists and detail screens.
2. **Defer heavy content until the transition ends**: for Article detail, Image viewer and Video player, render the header/placeholder during the ~300 ms enter transition and compose the body (long `Text` with many spans, WebView, pager) after `transition.isRunning == false` (`LocalNavAnimatedVisibilityScope.current.transition`). This targets the p99 61–133 ms frames.
3. **Shared element (home card → article)**: use `RemeasureToBounds` only where text must reflow. For the rest use `scaleToBounds` + `renderInSharedTransitionScopeOverlay` sparingly. Make sure the article body isn't laid out inside the shared bounds on every frame of the animation.
4. **Video player**: create the YouTube WebView **after** the enter transition, and keep a single `WebView` instance (`remember` + `DisposableEffect` `destroy()`) instead of re-creating it on recomposition. Set `setLayerType(LAYER_TYPE_HARDWARE)` only while animating.
5. Replace root-level `collectAsState()` in `MainActivity` and the remaining 18 feature calls with `collectAsStateWithLifecycle()`, so hidden screens stop collecting (and recomposing) while in the back stack or background.
6. Shimmer placeholders (video list loading: 804 frames at p50 13 ms): drive them from one shared `rememberInfiniteTransition` per list rather than per item, and draw with `drawWithCache` + a translated `Brush` (no recomposition per frame).

**Verify:** `FrameTimingMetric` Macrobenchmarks for the same journeys, targeting p50 ≤ 8.3 ms and p90 ≤ 12 ms on this phone.

## Phase 6 — Guard rails (~½ day, ongoing)

1. Add a `:macrobenchmark` module (it can share code with `:baselineprofile`) with `StartupBenchmark` and `ScrollAndNavigateBenchmark`, and run it before releases.
2. Enable `StrictMode` (thread + VM policies, `penaltyLog`) in **debug** builds to catch disk and network access on the main thread.
3. Add LeakCanary properly (`com.squareup.leakcanary:leakcanary-android`, `debugImplementation`).
4. In CI, run `assembleRelease` on every PR (catches Phase-0 regressions) and a lint check for oversized drawables (Phase 2).
5. Re-run this audit's table after each phase and append the new numbers to `docs/reports/performance-audit.md`.

---

## Suggested order and effort

| # | Phase | Effort | Main win |
|---|---|---|---|
| 0 | Release build + version drift | 1 h | Can ship at all |
| 1 | `HiltApplication` collector leak | 1–2 h | Correctness, battery, network |
| 2 | Oversized drawables | 1–2 h | −100+ MB worst case, guest OOM |
| 3 | Coil config + sized requests + carousel blur | ½ day | −200 MB peak, faster image viewer |
| 4 | Baseline/startup profile + lazy startup | 1 day | −15–30 % cold start, smoother first navigations |
| 5 | Transition work (stability, deferral, WebView) | 1 day | 120 fps transitions |
| 6 | Benchmarks + StrictMode + CI | ½ day | Stop regressions |
