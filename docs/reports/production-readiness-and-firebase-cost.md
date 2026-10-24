# Production readiness, performance and Firebase cost

**Date:** 2026-10-24 · **Branch:** `feature/series` (`f6c27b6`, version 1.2.0 / versionCode 12) · **Compared with:** `master`

This report answers three questions: is the app ready for production, how does it perform today, and what will Firebase cost as usage grows, with ways to make it cheaper.

**How it was checked**
- Read the code: release config, manifest, Room migrations, every Firestore, Storage and Functions call, the sync and paging logic, the player and search.
- Ran `./gradlew :app:assembleRelease testDebugUnitTest --offline`.
- Read the app's cached Room database on the physical test phone (read only, debug build) for content counts, audio durations and file hosts.
- Sent HTTP `HEAD` requests (headers only, no file data) to every cached audio file and a sample of images, for file sizes and cache headers.
- Prices come from the official Firebase, Google Cloud and Algolia pages (sources at the end).

Not done: lint, a new on-device performance measurement, and anything that needs the Firebase console (real usage, database location, current bill).

---

## 1. Verdict

**Almost ready.** The code is releasable:

| Check | Result |
|---|---|
| `:app:assembleRelease` (R8 minify and resource shrinking) | ✅ passes, `app-release-unsigned.apk` 15.8 MB |
| Unit tests (`testDebugUnitTest`, all modules) | ✅ 42 tests, 0 failures, 0 errors |
| Room migration from the version in production (DB 39, app 1.1.0) to 40 | ✅ `MIGRATION_39_40` exists |
| R8 keep rules for the new Firestore models (`SeriesDto`, `CategoryDto`) | ✅ covered by `-keep class com.example.data_firebase.model.**` |
| Forced and optional update mechanism | ✅ `config/app_config` (`minVersionCode`, `latestVersionCode`) |
| Lint | not run |

What is left is process and backend work, listed below.

---

## 2. Before uploading (must do)

1. **Merge `feature/series` into `master`.** The branch holds 25 commits that are not on `master`, including the series feature, the performance fixes, the admin category work and the version bump to 1.2.0 (12). CI (`.github/workflows/android-ci.yml`) only runs on pushes to `master` and on PRs to it, so open a PR and let CI build it.
2. **Finish the backend items this release depends on:**
   - Firestore rules must let signed-in users read `categories`, and the `uploadContent` / `updateContent` / `deleteContent` callables must accept `collectionName: "categories"`. Until then the user app falls back to the built-in categories (so users see no difference), but admin category edits fail.
   - 4 audio documents store `publishDate` as a number instead of a Timestamp. Fix them on the server.
3. **Test a signed release build on a real phone** before uploading: the series screens and «متفرقات», the player, downloads and the share video export. Most of the recent testing ran on debug builds, and R8 only runs on release.
4. **After Play approves the update,** set `config/app_config.latestVersionCode = 12` so installed apps show the optional update prompt. Raise `minVersionCode` only to force the update.
5. **Tablet layout:** 12 instrumented tablet tests are written but have never run, and the screenshot comparison with the Figma frames is still pending (`docs/plans/tablet-design-log.md`, "Waiting for the emulator"). Phones are not affected. Run these before promoting tablet support.
6. **Uncommitted change:** `feature/feature-profile/.../ProfileScreen.kt` only has IDE reformatting (imports and line wrapping, no behaviour change). Commit it or discard it.

## 3. Should do (not blockers)

| # | Item | Where | Why |
|---|---|---|---|
| 1 | Add Crashlytics | `app/build.gradle.kts` | Only Play vitals report crashes today. Crashlytics is free and adds custom logs and non-fatal errors. |
| 2 | Set a budget alert | Google Cloud console, Billing | The Blaze plan has no spending cap. Alerts at e.g. $25, $50 and $100 catch abuse or a runaway loop early. |
| 3 | Move Algolia to the Grow plan | Algolia dashboard | The free Build plan "is provided only for evaluation purposes and may not be relied on … for production". Grow keeps the same 10K free searches per month. |
| 4 | Remove the `TelegramVerificationActivity` stub or set `exported="false"` | `app/src/main/AndroidManifest.xml:37` | It is exported with no intent filter, does nothing, and any app can crash it by sending malformed `data`. |
| 5 | Close the Room migration gap 36 → 37 | `core/core-database/.../di/LocalDataModule.kt:107-118` | versionCode 3 (April 2026) shipped DB 36. With no 36 → 37 migration, `fallbackToDestructiveMigration(true)` wipes favourites, download flags and history for anyone updating from it. Downloaded files stay on disk but the app forgets them. |
| 6 | Remove the unused `firebase-database` dependency | `app/build.gradle.kts`, `FirebaseModule.kt:33` | Realtime Database is provided but never injected anywhere. |

---

## 4. Performance

### 4.1 Fixed since the September audit ([performance-audit.md](performance-audit.md))

| Finding (2026-09-29) | Status |
|---|---|
| Release build did not compile (Compose version drift) | ✅ fixed (`composeBom` 2025.12.00) |
| `HiltApplication.onStart` piled up collectors and syncs on every foreground | ✅ fixed (`18bf8fb`) |
| Players leaked a MediaController connection on every rotation | ✅ fixed (`d5ea292`) |
| No app Baseline Profile (92% of cold start ran in the interpreter) | ✅ added (`92c6ff3`, `app/src/main/baseline-prof.txt`) |
| Article text cleaning on Home's main thread | ✅ moved off the main thread and twice as fast (`3b888e6`) |
| `telegram_img.png` decoded at about 127 MB | ✅ removed |
| About the app crashed on Android 12 and lower | ✅ fixed (`29ab6b0`) |
| A failing share export closed the app | ✅ fixed (`bd609cd`) |

### 4.2 Last measured numbers

Redmi Note 13 Pro, Android 16, 120 Hz, release build, 2026-09-29:

| Metric | Value |
|---|---|
| Cold start to first frame (fresh install) | ≈ 880 ms |
| List scrolling | p50 5 ms, p99 ≤ 9 ms on almost every list |
| Idle redraw | 0 frames on every screen tested |
| Screen-open transitions | p50 10–15 ms (about 70–100 fps on a 120 Hz screen) |
| Peak graphics memory (images grid and viewer) | 344 MB |
| Release APK | 15.6 MB then, 15.8 MB now |

These were not re-measured after the Baseline Profile and the fixes above. The Resizable emulator can't show Baseline Profile gains (its ART rejects `.dm` profiles), so a new measurement needs the physical phone.

### 4.3 Still open

| # | Item | Where | Impact |
|---|---|---|---|
| 1 | No app-wide Coil `ImageLoader`: the designs grid and viewer decode full-size posters | `HiltApplication` (add `ImageLoaderFactory`), `DesignTile`, image viewer | The 344 MB memory peak. On 3–4 GB phones, background audio gets killed. |
| 2 | Bitmaps in `drawable/` (treated as mdpi, scaled up 2.75× on a 440 dpi phone) | `fasalo_logo.png` 640² (≈ 12 MB decoded, Q&A screen), `bulb_image.png` 626² (≈ 12 MB, student dashboard), `dr_hassan_image.png` 512² (≈ 8 MB, decoded for **every** notification at `NotificationService.kt:68`), `dr_hassan_app_logo.png` 1024² (≈ 32 MB, old `splash_screen` route) | Memory spikes and slow screen opens. Resize to about 3× the display size, convert to WebP and move to `drawable-nodpi/`. Delete the copies duplicated in both `app` and `core-ui`. |
| 3 | Home carousel draws a live blur | `ImageCarousel.kt:201` | A full-size RenderEffect layer on every frame while the pager moves. |
| 4 | No `reportFullyDrawn()` | `MainActivity` / Home | Time to real content is not reported in Play vitals. |
| 5 | 40 `collectAsState()` calls (43 already use `collectAsStateWithLifecycle`) | features, `MainActivity` | Hidden screens keep collecting and recomposing. |
| 6 | Search subscribes to a shared searcher and never unsubscribes | `SearchViewModel.kt:71` | Each visit to Search leaves its ViewModel subscribed until the process ends. |
| 7 | No player disk cache | `PlayerModule.kt:22` | Audio is downloaded again on every replay (see the cost section). |

---

## 5. Firebase cost

### 5.1 What the app uses

| Service | Used for | Billed? |
|---|---|---|
| Cloud Firestore | All content metadata, series, categories, config, student data | Yes, above the free daily quota |
| Cloud Storage (`dr-hassan-al-hawary.appspot.com`) | Every audio file and every design image | Yes, above the free daily quota. **This is the main cost.** |
| Cloud Functions | Admin writes (`uploadContent` …), `deleteMyAccount`, `checkMembership`, `submitQuizV2`, the series counters trigger | Far below the free 2M invocations per month |
| Authentication | Google sign-in | No cost |
| Cloud Messaging (FCM) | Topics `all_users`, `student_broadcasts`, batches | No cost |
| Algolia (not Firebase) | Search (`content_index`) | Above 10K searches per month |
| YouTube | Videos and their thumbnails | No cost |

### 5.2 Measured facts

- **Every file is hosted on Firebase Storage.** All 42 cached audios and all 113 cached images are on `firebasestorage.googleapis.com`, in the legacy `appspot.com` bucket.
- **Audio bitrates (41 files, `HEAD` requests):** from 16 to 161 kbps, median about 52 kbps, or roughly **23 MB per listening hour**. 9 of the 41 files are above 64 kbps, which is more than speech needs.
- **Images:** 65–200 KB each, about 135 KB on average.
- **Cache headers:** every file is already served with `Cache-Control: public, max-age=31536000, immutable`, so Coil's disk cache keeps images after the first load. Nothing similar exists for audio, because the player has no cache.
- **Cached content on the test phone:** 42 audios, 44 articles, 200 videos, 155 image groups and 51 series (series hold 171 audios, 139 articles, 111 videos and 71 image groups). Articles average about 4.4K characters, about 9 KB each.

### 5.3 Prices (Blaze plan)

| Item | Free | Then |
|---|---|---|
| Firestore reads | 50,000 per day | $0.03 per 100K (single region; multi-region costs more, and the database's location is not visible from the code) |
| Firestore writes / deletes | 20,000 / 20,000 per day | location pricing |
| Firestore network egress | 10 GiB per month | Google Cloud pricing |
| Storage, `appspot.com` bucket: stored | 5 GB | $0.026/GB |
| Storage, `appspot.com` bucket: downloaded | **1 GB per day** | **$0.12/GB** |
| Storage, `appspot.com` bucket: download operations | 50K per day | $0.004 per 10K |
| Storage, `firebasestorage.app` bucket in us-central1 / us-east1 / us-west1 | 100 GB downloaded per month | Cloud Storage pricing |
| Cloud Functions | 2M invocations, 400K GB-seconds per month | $0.40 per million |
| FCM, Crashlytics, App Check, Analytics | No cost | — |
| Algolia, Grow plan | 10,000 searches per month | $0.50 per 1,000 |

Since 2026-02-03 a project must be on Blaze to keep access to its Storage buckets. The app's downloads work, so the project is on Blaze.

### 5.4 Firestore reads per user (from the code)

| When | Reads | Source |
|---|---|---|
| App start, signed in | ≈ 50 | Delta sync: 1 query each for series, audios, articles, videos and image groups (≥ 5). Articles listener: 20 documents after 30 minutes away (`FirebaseArticlesSource.kt:192`). Categories listener: one per category document. `config/app_config`: 1. Home: 5 articles + 5 audios + 5 image groups + the latest group's images. |
| Each list screen opened | 10, up to 50+ | The audio, video and image-group mediators refetch page 1 on every open (`LAUNCH_INITIAL_REFRESH`). In categories made mostly of series items, they read pages of up to 50 just to find standalone items. |
| Each series screen opened | one per episode | Every episode is refetched on every open (`core-ui/.../components/Series.kt:384`). |
| Each fresh install, once | ≈ 550, growing | The first delta sync reads everything updated since 2026-10-01 (`ContentSyncRepositoryImpl.kt:116`): the ≈ 492 items the series backfill touched, plus all 51 series, plus every later edit. Fresh installs have none of these cached, so the reads are discarded. |

### 5.5 Monthly estimate

Assumptions per daily active user (DAU): about 150 Firestore reads, 30 minutes of listening (≈ 12 MB), about 2 MB of images, and 20% of users searching with about 4 Algolia requests each. Months are 30 days.

| DAU | Firestore | Storage downloads | Algolia | **Total per month** |
|---|---|---|---|---|
| 300 | $0 | $12 | $0 | **≈ $12** |
| 1,000 | $1 | $47 | $7 | **≈ $55** |
| 5,000 | $6 | $248 | $55 | **≈ $310** |
| 10,000 | $13 | $500 | $115 | **≈ $630** |

- **Rule of thumb:** every **1,000 listening hours per day costs about $80 per month** in Storage downloads (23 GB/day − 1 GB free = 22 GB × $0.12 × 30).
- **Where the money goes:** audio downloads are about 80% of the cost. Firestore stays under about $15/month even at 10,000 DAU.
- **This is a model, not a bill.** For real usage, check Firebase console → Usage and billing, and the Play Console for DAU, then put your numbers into the formulas above.

---

## 6. Reducing the cost (biggest savings first)

### 6.1 Add a disk cache to the audio player
`core/core-player/.../di/PlayerModule.kt:22` builds a plain `ExoPlayer`. Give it a `SimpleCache` (300–500 MB, `LeastRecentlyUsedCacheEvictor`) through a `CacheDataSource.Factory`. Today replays, seeking back past the buffer and resuming yesterday's lecture all download the audio again. The share clip extractor can read from the same cache. Expected saving: 10–30% of audio downloads (an estimate, not measured). The change is small and stays inside `core-player`.

### 6.2 Re-encode high-bitrate audio
9 of the 41 measured files are 70–161 kbps. For lectures, 48 kbps mono MP3 (or 32–40 kbps AAC or Opus) sounds the same. Re-encode those files and enforce the bitrate at upload, either in the admin app before upload or with a Storage-triggered function running ffmpeg. That halves the size of those files.

Side effect: the URL changes, so users who downloaded a file download it once more (downloaded files are named by URL hash in `data/FileDownloader.kt`). The delta sync picks up the new `audioUrl` automatically.

### 6.3 If listening grows a lot: serve audio from Cloudflare R2
R2 charges nothing for downloads. Past about 5–10K listening hours a day, this is the biggest structural saving. The app only stores URLs, so this is a backend and migration job, not an app change.

Moving to a new `firebasestorage.app` bucket in us-central1 is not worth it on its own: it raises the free downloads from about 30 GB to 100 GB per month (about $8/month).

### 6.4 Generate thumbnails for designs
Use the Resize Images extension to produce about 400 px WebP thumbnails, roughly 20–30 KB instead of 135 KB. The grid would download about 5× less on first view, and the memory peak in 4.3 #1 is fixed too. The viewer keeps loading the original.

### 6.5 Firestore
These save cents to a few dollars a month, but they keep the app inside the free 50K reads per day for longer.

1. **Skip the catch-up sync on fresh installs** (`ContentSyncRepositoryImpl.kt:116`). When a table is empty, set its last sync time to now instead of starting at 2026-10-01. Today each install wastes about 550 reads, a number that grows with every edit, so about 90 installs a day use up the whole free quota. The comment at line 114 ("only costs them one catch-up read") is wrong: Firestore bills every document a query returns.
2. **Remove the articles snapshot listener** started in `ArticlesRepositoryImpl.kt:46-50`. It re-reads 20 full articles (about 180 KB) on every cold start after 30 minutes away. Home's sync and the delta sync already cover new and changed articles.
3. **Stop refetching lists on every open:** return `SKIP_INITIAL_REFRESH` from the mediators (`data/.../util/*RemoteMediator.kt`) when the cache was refreshed recently. The delta sync handles edits.
4. **Only refetch a series' episodes when it changed:** compare the series' `updatedAt` / `itemCount` with the last refresh before refetching (`Series.kt:384`).
5. **Filter series items out on the server:** have the backend write `seriesId: null` explicitly on standalone items, then page with `whereEqualTo("seriesId", null)` plus a composite index. The lists then stop reading up to 50 series items per page just to skip them.

### 6.6 Algolia
- Move to the Grow plan (licensing, see section 3).
- Raise the search delay from 300 to about 450 ms (`SearchViewModel.kt:161`).
- When a filter chip changes, filter the hits already fetched instead of sending a new request, where the page holds them all.

### 6.7 Guard rails
- **Budget alert** in Google Cloud billing (no cap exists on Blaze).
- **App Check** (Play Integrity) enforced on Firestore and Functions, to block scripted reads of the database.
- Audio download URLs are public links with a token, and App Check does not cover them. If someone hotlinks the files, the budget alert is the only warning.

### 6.8 Order of work

| Step | Effort | Saves |
|---|---|---|
| Budget alert, Algolia Grow plan | minutes | protects against surprises |
| 6.5 #1 (fresh-install catch-up) and #2 (articles listener) | about 1 hour, app only | ≈ 570 reads per install, ≈ 20 reads per cold start |
| 6.1 player cache | about ½ day, app only | 10–30% of audio downloads (estimate) |
| 6.2 re-encode audio | ½–1 day, backend and admin | ≈ 50% on the high-bitrate files |
| 6.4 design thumbnails | ½ day, backend extension and app | ≈ 80% of image downloads, and the memory peak |
| 6.3 R2 | 1–2 days, backend migration | almost all of the download cost, when volume justifies it |

---

## Sources

- [Firebase pricing](https://firebase.google.com/pricing)
- [Firestore pricing](https://cloud.google.com/firestore/pricing)
- [Cloud Storage for Firebase: changes announced September 2024 (FAQ)](https://firebase.google.com/docs/storage/faqs-storage-changes-announced-sept-2024)
- [Algolia: how am I billed on the Grow plan](https://support.algolia.com/hc/en-us/articles/15745996583441-How-am-I-billed-on-the-Grow-plan)
- [Algolia Build plan terms](https://www.algolia.com/policies/build-plan-specific-terms)
- [Earlier on-device performance audit](performance-audit.md) and [improvement plan](../plans/performance-plan.md)
