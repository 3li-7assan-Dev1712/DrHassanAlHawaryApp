# Performance audit — on-device notes

**Date:** 2026-09-29 · **Branch:** `ui/motion` (8d7625f + uncommitted LeakCanary change)
**Device:** Xiaomi Redmi Note 13 Pro (23117RA68G, "emerald", MediaTek), Android 16, 1080×2400, **120 Hz**, 8 GB RAM
**Build measured:** `release` (R8 minify + shrink), signed with the local debug key so it installed over the existing debug install (same data / signed-in account). See §0 for why a workaround was needed.

Frame budget at 120 Hz is **8.3 ms**; at 60 Hz it is 16.7 ms. Numbers below come from `dumpsys gfxinfo` (reset before each step), `am start -W`, `dumpsys meminfo` and `top`.

---

## 0. Blocker found first: the release build does not compile

`./gradlew :app:assembleRelease` fails:

```
e: core/core-ui/src/main/java/com/example/core/ui/theme/SharedElements.kt:10:78 Unresolved reference 'scaleToBounds'.
e: core/core-ui/src/main/java/com/example/core/ui/theme/SharedElements.kt:85:44 Unresolved reference 'scaleToBounds'.
```

Cause: **debug and release resolve different Compose versions.**

| Variant | `androidx.compose.animation` resolved |
|---|---|
| debug | **1.10.0** |
| release | **1.8.3** (what `composeBom = "2025.07.00"` pins) |

`debugApi(libs.androidx.ui.tooling)` in `core-ui` (plus transitive libs such as `paging-compose 3.4.2` that want runtime ≥ 1.9) pulls the debug classpath up to 1.10.0. The motion work used `ResizeMode.scaleToBounds`, an API that only exists in 1.10, so it compiles in debug and breaks release. **Every measurement done in debug so far was also on a newer Compose than the one that would ship.**

For this audit I built release with a throw-away Gradle init script (not committed) that forces Compose ui/animation/foundation/runtime to 1.10.0 — i.e. the same versions debug already uses.

---

## 1. Startup

| Scenario | TotalTime (first frame) |
|---|---|
| Cold, no compiled code (`cmd package compile --reset`, ≈ fresh install) | 847 – 939 ms, **avg ≈ 880 ms** |
| Cold, `speed-profile` (only the library baseline profiles bundled by AGP) | 719 – 817 ms, avg ≈ 760 ms |
| Cold, full AOT `speed` (upper bound) | 751 – 839 ms, avg ≈ 790 ms |
| Hot (from background) | 98 – 118 ms |

Notes
- ≈ 880 ms is only to the **system splash**. The splash is kept until `appReady` (theme DataStore + onboarding flag + auth check), so the time to real content is longer; nothing reports it (`reportFullyDrawn()` is never called, so it cannot be tracked in Play Vitals).
- Compiled code saves ~120 ms (≈ 14 %). There is **no app baseline profile** module and no `androidx.profileinstaller`/`baselineprofile` setup, so app code (Hilt graph, MainActivity, Home, NavHost) runs interpreted/JIT on the first launches after install.
- During startup, main-thread logcat shows a **791×791 JPEG decode** and **Firebase init "initializing all Firebase APIs"** plus a Play-Services `ProviderInstaller` attempt, all before the first frame.
- `HiltApplication` field-injects 9 dependencies (use-cases → repositories → Room/Firestore/DataStore) in `Application.onCreate`, so the whole data graph is built before the first activity frame.
- Right after launch the app renders ~500–1100 frames in the first 5 s (entry/splash-exit/carousel animations), then settles to **0 frames when idle** ✅.

## 2. Screens — frame timing

"open" = tap → 3–4 s; "scroll" = 3–8 flings up + some down. *Jank* is Android's own janky-frame count.

| Screen / action | Frames | Jank | p50 | p90 | p95 | p99 | Verdict |
|---|---|---|---|---|---|---|---|
| Home scroll | 898 | 0 | 5 | 6 | 7 | 9 | ✅ smooth |
| Home idle 5 s | 0 | – | – | – | – | – | ✅ |
| Articles list — open | 360 | 1 | 7 | 15 | 20 | **125** | ⚠️ one long frame |
| Articles list — scroll | 1942 | 1 | 5 | 5 | 6 | 14 | ✅ |
| Article detail — open (from list) | 80 | 0 | 14 | 21 | 53 | 61 | ⚠️ transition < 120 fps |
| **Article detail — open from Home card (shared element)** | 88 | 1 | 15 | **42** | **48** | **133** | ❌ worst transition |
| Article detail — scroll | 916 | 0 | 5 | 5 | 5 | 6 | ✅ |
| Audio category — open | 80 | 0 | 12 | 14 | 30 | 32 | ⚠️ |
| Audio list — open | 162 | 1 | 11 | 15 | 21 | 44 | ⚠️ |
| Audio list — fling | 2096 | 1 | 5 | 5 | 6 | 8 | ✅ |
| Audio player — open | 82 | 0 | 11 | 19 | 20 | 73 | ⚠️ |
| Audio player — paused 8 s | 0 | – | – | – | – | – | ✅ |
| Audio player — **playing** 10 s | 68 (≈7 fps) | 0 | 14 | 17 | 18 | 18 | ✅ cheap |
| Video category — open | 78 | 1 | 13 | 15 | 23 | 24 | ⚠️ |
| Video list — open (loading shimmer) | 804 | 1 | 13 | 14 | 17 | 20 | ⚠️ every shimmer frame over budget |
| Video list — fling | 2336 | 0 | 5 | 6 | 6 | 7 | ✅ |
| Video player (YouTube WebView) — open | 982 | 0 | 11 | 19 | 25 | **101** | ⚠️ |
| Video player — playing 8 s | 462 (≈58 fps) | 0 | **22** | 26 | 28 | 32 | ⚠️ WebView composition ≈ 45 fps |
| Images grid — open | 120 | 1 | 12 | 24 | 28 | 40 | ⚠️ |
| Images grid — fling | 1922 | 0 | 5 | 5 | 6 | 7 | ✅ |
| **Image viewer — open (9-photo group)** | 94 | 1 | **19** | **30** | 32 | 36 | ❌ slowest open |
| Image viewer — page swipes | 402 | 0 | 5 | 5 | 5 | 8 | ✅ |
| Q&A — open | 84 | 1 | 14 | 17 | 24 | 34 | ⚠️ |
| About Sheikh — open | 80 | 1 | 13 | 20 | 46 | 53 | ⚠️ |
| Notifications — open | 82 | 1 | 13 | 15 | 17 | 22 | ⚠️ |
| Tab → Search | 204 | 1 | 10 | 11 | 11 | 46 | ⚠️ |
| Search — typing + results | 474 | 2 | 10 | 12 | 17 | 22 | ⚠️ |
| Tab → Study (Institute) | 198 | 1 | 11 | 11 | 13 | 28 | ⚠️ |
| Tab → Profile | 202 | 1 | 12 | 13 | 13 | 14 | ⚠️ |
| Profile — scroll | 848 | 0 | 5 | 5 | 5 | 6 | ✅ |
| Tab → Home (return) | 198 | 1 | 13 | 14 | 17 | 48 | ⚠️ |
| Theme dark → light | 202 | 1 | 12 | 23 | 25 | 53 | ⚠️ |
| Theme light → dark | 198 | 1 | 11 | 24 | 27 | 57 | ⚠️ |
| About app / Share app / Support — open | ~78 | 1 | 11–12 | 12–14 | 15–16 | 28–32 | ⚠️ |
| Share selection — open | 82 | 1 | 10 | 14 | 16 | 57 | ⚠️ |
| Share selection — scroll | 628 | 0 | 5 | 5 | 5 | 5 | ✅ |
| Share selection — long-press select | 130 | 1 | 5 | 19 | 22 | 25 | ⚠️ |
| Share preview — open | 78 | 0 | 11 | 13 | 16 | 17 | ⚠️ |

### Patterns
1. **Scrolling is excellent everywhere** (p50 5 ms, p99 ≤ 9 ms on almost all lists). Keys are set on all paging lists. Scroll is not the problem.
2. **Every navigation has exactly one slow UI-thread frame**, and the transition then runs at p50 **10–15 ms**, i.e. ~70–100 fps instead of 120 fps. That is the cost of composing the destination screen on the first frame of the transition plus running interpreted code (no baseline profile), combined with the shared-axis / fade-through animations (two screens drawn at once).
3. The **shared-element container transform (Home card → article)** and the **image viewer open** are the two worst transitions (p90 30–42 ms, p99 up to 133 ms).
4. Nothing redraws while idle ✅ (home, lists, paused player, image viewer: 0 frames).

## 3. Memory

| Point in the tour | Graphics | Total PSS |
|---|---|---|
| After the video player | 164 MB | 401 MB |
| **After the Images grid + image viewer** | **344 MB** | **568 MB** |
| Foreground home after the tour | 95 MB | 190 MB |
| Background + `trim-memory RUNNING_LOW` | 29 MB | 125 MB |

- Memory is reclaimed on trim, so this is cache, not a leak. The **peak is very high** for a content app: on 3–4 GB devices, other apps (and ours in the background, playing audio) get killed.
- Cause: images are decoded bigger than they are displayed.
  - Coil uses its default `ImageLoader` (no app-level config). The image grid (`DesignTile`, `ContentScale.FillWidth`) and the viewer (`SubcomposeAsyncImage` with `graphicsLayer` zoom) load the full poster images.
  - The home carousel draws **the same painter twice**, once with `Modifier.blur(10.dp)`, so there's a full-size RenderEffect layer on every frame while it's visible.
- **Oversized bundled drawables in `res/drawable/`** (no density qualifier = treated as mdpi, so Android **scales them up ×2.75** on this phone when decoding):

| Resource | Source px | Decoded on this phone | Bitmap size | Used by |
|---|---|---|---|---|
| `core-ui/.../drawable/telegram_img.png` | 2048² | ≈ 5632² | **≈ 127 MB** | Study → `GuestContent` (non-student users), admin |
| `feature-splash-screen/.../drawable/dr_hassan_app_logo.png` | 1024² | ≈ 2816² | ≈ 32 MB | `SplashScreen` composable (route `splash_screen`, appears unused) |
| `feature-home/.../drawable/fasalo_logo.png` | 640² | ≈ 1760² | ≈ 12 MB | Q&A screen logo (shown ~80 dp) |
| `core-ui/.../drawable/bulb_image.png` | 626² | ≈ 1721² | ≈ 12 MB | Student dashboard |
| `core-ui/.../drawable/dr_hassan_image.png` | 512² | ≈ 1408² | ≈ 8 MB | `NotificationService` large icon via `BitmapFactory.decodeResource` (per notification) |

  `telegram_img` alone is a likely **OOM/multi-hundred-ms freeze** for every guest user who opens the Study tab (I was signed in as a student, so this path was verified from code, not on-device).
- Duplicate resources: `dr_hassan_image.png` and `design_3.jpg` exist in both `app` and `core-ui`.

## 4. CPU / background

- Background, audio paused: **~0 % CPU** ✅.
- Background, audio playing at 2×: 13–17 % of one core (decode + time-stretch + streaming), which is acceptable.
- The process holds **~150 threads** in the foreground (82 after trim): OkHttp pools (several engines — Ktor ships three), Firebase, `AsyncTask #28` (WebView/YouTube player), multiple `DefaultDispatcher`s. That's not a hot spot, but it means more memory per thread and more work at startup.

## 5. Code-level findings (verified in code; some confirmed on device)

1. **`HiltApplication.onStart` leaks collectors** (confirmed): every time the app returns to foreground it launches a *new* `observeAuthStateUseCase().collectLatest { … }` in `appScope` that is never cancelled. Each one adds another Firebase auth listener, another Room `getLevels()` observer, another `getStudentData()` observer and repeats the three `FirebaseMessaging.subscribeToTopic` calls. Once `levels` is non-empty, every Room change would run `syncPlaylistsUseCase()` + `syncLessons()` **N times** (N = number of foregrounds in this process). Logcat showed the `onStart: […]` emission once per foreground.
2. `MainActivityViewModel` also observes auth and sync independently, so auth state is observed from both the Application and the Activity.
3. `MainActivity.setContent` collects 5 flows with `collectAsState()` (not lifecycle-aware) at the root; any emission recomposes the root `when`. 18 `collectAsState()` calls remain in features versus 20 `collectAsStateWithLifecycle`.
4. No Compose compiler **stability configuration** / reports: domain models from `core-domain` (a plain Kotlin module) count as unstable to Compose, so screens taking `List<…>`/domain objects can't skip recomposition.
5. The YouTube player is a WebView with its own compositor. At 22 ms/frame while playing, it's the only screen that renders continuously.
6. `LocaleForce.wrap` in `attachBaseContext` for both Application and Activity (cheap, but runs on the startup path).

## 6. What's already good
- Lists: keyed, paged, and scroll at 120 fps with p99 ≤ 9 ms.
- Zero idle redraw on every screen tested.
- Audio playback UI ticks at ≈ 7 fps rather than every frame.
- Release APK is 15.6 MB (R8 + resource shrinking on) versus 47 MB debug.
- Memory is released correctly on trim (no leak visible in this pass).

## 7. Not covered in this pass
- Onboarding, sign-in/register and the guest Study screen (would require signing out or clearing data on your phone).
- Search with Arabic input (adb can't type Arabic; searched with Latin text only).
- Lesson/playlist/quiz screens (Study shows "coming soon" for this account).
- The Share button itself (it hands off to other apps).
- Low-end hardware: this is a mid-range 120 Hz phone. On a 60 Hz / 3 GB device the ⚠️ transitions would mostly fit the 16.7 ms budget, but the memory findings would get **worse**.
