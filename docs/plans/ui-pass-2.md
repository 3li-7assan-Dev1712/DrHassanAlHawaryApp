# UI pass 2: player, share preview, categories, splash, onboarding, About and Share

> Spec for a Claude Code session that may run unattended. Ali may be asleep and can't answer.
> This file is the source of truth. Progress is tracked in `docs/plans/ui-pass-2-log.md`.
> This pass builds on the first pass, `docs/plans/overnight-ui-pass.md`. Read its sections A–D before starting: its operating rules, hard boundaries, per-phase checks, and design tokens apply here unchanged.

## A. Rules

1. **Same operating rules as the first pass:** never ask questions or wait for approval; record decisions in the log; don't end your turn until every phase is Done or Skipped and the final report is written; skip items, not phases; time-box each phase to about 25 minutes; if your context is compacted, re-read this spec and the log and continue from the first unfinished phase.
2. **Same hard boundaries:** no database or schema changes; no backend or remote changes; no new modules, DI modules, or navigation routes; no new dependencies and no edits to Gradle or `*.properties` files; preferences only as NEW keys in an existing store; never open `local.properties`; work on a new branch, never push, never discard work (`git stash push -m "failed: <item>"` to abandon an attempt).
3. **Reuse the first pass's building blocks:** tokens, the digits/`ArabicFormatter` helper, the date formatter, text cleaners, the bidi helper, `ic_tabler_*` icons, the shared top bar, and the circular sheikh-photo component. If one is missing, create it exactly as the first spec describes. Never create duplicates.
4. **Playback direction (applies everywhere):** media timelines and transport controls run **left to right**, even in RTL: 0:00 on the left, rewind on the left, forward on the right. Don't mirror them. Everything else stays RTL.
5. **After every phase:** `./gradlew assembleDebug testDebugUnitTest` (on Windows `./gradlew.bat` if needed), fix what you broke (3 attempts, then stash the item and log it), commit `ui2: phase N - <name>`, update the log.

---

## Phase 0: Setup and inventory

1. `git status`. If the tree isn't clean, commit everything as `chore: snapshot before UI pass 2`.
2. Branch: if a branch `ui/overnight-polish` exists and is not already contained in HEAD (`git merge-base --is-ancestor ui/overnight-polish HEAD` fails), create `ui/pass-2` from `ui/overnight-polish`. Otherwise create it from HEAD. Log which one you used.
3. Baseline `./gradlew assembleDebug testDebugUnitTest`. If it fails, don't fix the environment: log it and mark later phases "unverified".
4. Inventory, written into the log:
   - The audio player screen and ViewModel, and where the full-screen "جار تحميل ملفات الدرس…" overlay comes from (what the download is for).
   - The share-as-video preview in `:feature:feature-share`.
   - The audio and video category screens, and where their categories, names and icons are defined.
   - Splash setup: themes, `values-v31`, any splash activity or artificial delay, and whether `androidx.core:core-splashscreen` is already a dependency.
   - Onboarding screens and their illustration assets: format (vector XML, PNG, WebP, other) and, for vectors, the list of colors each one uses.
   - The About, Rate and Share-app screens and the Profile rows that open them.
   - Screens that already have empty or error states.
   - View-based scroll containers (ScrollView, NestedScrollView, RecyclerView, WebView) and whether their scrollbars are enabled.
5. Check whether `docs/assets/qr_play_store.png` exists (Ali adds it), and read the app's `applicationId` from the build files (read only).

## Phase 1: Quick fixes

1. **Gray bar at the left edge.** A thin gray bar appears on many screens at different heights, including screens with no top bar, so it's most likely a vertical scrollbar (RTL draws scrollbars on the left), not part of the top bar. Disable scrollbars on the View-based scroll containers found in Phase 0 (`android:scrollbars="none"` or `isVerticalScrollBarEnabled = false`), or remove a custom scrollbar modifier if that's the source. If the first pass changed the top bar for this, keep that change only if it's otherwise harmless.
2. **Rate row.** "تقييم التطبيق" in Profile opens the Play Store listing directly: `market://details?id=<applicationId>`, falling back to `https://play.google.com/store/apps/details?id=<applicationId>` if no store app handles it. Stop navigating to the Rate screen. Delete that screen and its route only if nothing else references them; otherwise leave them unused and log it. Don't add the in-app review API (new dependency).
3. **Onboarding spelling** (fix the strings now in case Phase 5 is skipped): "أحصل" → "احصل" (every occurrence), "إنقطع إتصالك" → "انقطع اتصالك", "ابدا التدرج" → "ابدأ التدرج", "ملفات ال pdf" → "ملفات PDF".
4. **Skip interval:** the player's skip buttons move 10 seconds instead of 5, with matching labels ("١٠").
5. **Media times** in the player and the share preview: one unit-tested helper with Arabic-Indic digits and no leading zero on minutes: "٣:٠٩", "٢٢:٠٠", and "١:٠٥:٣٠" past an hour.

## Phase 2: Audio player

Idle state:
- Top bar: the back arrow only. Remove the title from the top bar (today it's shown twice).
- Photo: the shared circular sheikh-photo component (single 2dp `accent` ring, `clip(CircleShape)` + `ContentScale.Crop` on the image itself). This fixes the cream square corner at the bottom-right. Remove the extra outer rings.
- Under the photo:
  - A category chip (`accentContainer`) with the item's category, only if the model has one.
  - The title through the audio-title cleaner: "مقطع بعنوان: حكم تبديل العملة بمقابل" → "حكم تبديل العملة بمقابل". For Friday sermons, the title/date parser from the share frame turns "خطبة بعنوان: … - الجمعة: ( ٢٤ صفر …" into a title plus a gold date line.
  - "الشيخ د. حسن الهواري" in `textSecondary`.
- Seek bar: one track with ONE round thumb. Remove the extra dot at one end and the tall vertical bar past the other end. Left to right per rule A.4: elapsed time on the left, total on the right.
- Transport row, left to right: rewind 10 (counter-clockwise icon) · play/pause (large gold circle) · forward 10 (clockwise icon).
- Action row under the controls, three labeled circular buttons:
  - "السرعة": shows the current speed ("١×").
  - "تحميل": download state from the existing download logic: idle icon → progress with percent → check + "محفوظ".
  - "مشاركة": opens the existing share-as-video flow.
  The standalone download icon and the top-bar share icon go away.
- Playback speed cycles ١× → ١٫٢٥× → ١٫٥× → ٢× → ٠٫٧٥× → ١× with Media3 `setPlaybackSpeed`. Persist it as a new preference key if a store exists, and apply it to later playback.

Loading state (today a full-screen dimmed overlay with "جار تحميل ملفات الدرس الرجاء الإنتظار، قد يستغرق الأمر وقتا برجاء الإنتظار" and a percentage):
- First find out what the download is for, and log it.
- Never block the whole screen. Replace the overlay with an inline card under the title: download icon, "جارٍ تحميل الدرس", the percentage in Arabic-Indic digits with "٪", a thin `accentStrong` progress bar, and a cancel (x) button that cancels the existing download job.
- If playback waits for the full download: let ExoPlayer play the remote URL directly (progressive streaming needs no cache) while the download continues in the background, and use the local file once it exists. Don't add a cache layer (`SimpleCache`) in this pass.
- If the download is for the share flow: open the share preview immediately and show the progress on its share button instead.
- If the file is already downloaded, skip the download entirely.

## Phase 3: Share-as-video preview (`:feature:feature-share`)

1. **Optional quote:** if the "اقتباس مميز (اختياري)" field still exists, remove it: the preview field and its counter, the quote card in both the Canvas renderer and the Compose preview, the `quote` field in the model, state and renderer inputs with every usage, unused strings, and related tests. Do NOT touch the article "share selected text as an image" feature, which also uses "quote" in its class names. The middle of the video frame shows the circular sheikh photo (gold ring) instead.
2. **Layout:** everything fits on one screen without scrolling. Scale the 9:16 preview to about 40% of the screen height. Pin the share button at the bottom, labeled "مشاركة الفيديو", with the encoding progress shown inside it.
3. **One clip selector** replaces the three timelines (the slider, the "من 4:32 إلى 5:32" text, and the full waveform):
   - Duration chips "١٥ ث" / "٣٠ ث" / "٦٠ ث"; the default stays whatever the current default length is.
   - A thin overview bar of the whole lecture with the selected window marked.
   - A zoomed waveform strip (about twice the clip length, centered on the selection) with a draggable window and handles, left to right per rule A.4.
   - "−٥ ث" and "+٥ ث" nudge buttons, and the range label "٤:٣٢ – ٥:٣٢".
   - A 40–48dp circular play button with the label "استمع للمقطع قبل المشاركة" that previews the selection. While it plays, the preview's own waveform fills in sync. Remove the separate slider.
4. **Video frame** (the renderer and the Compose preview share one layout spec):
   - The time labels under the waveform become much larger on the 1080px canvas, in Arabic-Indic digits without a leading zero ("٠:٢٠", "١:٠٠"). Today they render as dots.
   - The speaker name is "الشيخ د. حسن الهواري" (not "حسن أحمد").
   - Under the "حمّل التطبيق" pill, add a small text link, because a pill inside a video can't be tapped: the app's Play Store short link if one exists in the code, otherwise `dr-alhawary.com`.
   - The waveform runs left to right.

## Phase 4: Category screens (audio and video)

- One shared category definition (id, Arabic name, Tabler icon, order) used by BOTH screens:
  الكل (`layout-grid`), فتاوى (`messages`), دروس علمية (`books`), خطب الجمعة والعيدين (`building-mosque`), محاضرات (`microphone`), تلاوات (`book`).
  Map the existing category ids and names onto it. A category that exists in the data but not in this list goes at the end with a generic icon.
- Audio gains "الكل": it opens the audio list with no category filter, the same way the video screen's "الكل" works. If the audio list can't do that without architecture changes, skip it and log.
- Layout: a compact list inside one `surface` card instead of big gradient tiles. Each row: a 40dp `accentContainer` circle with the icon in `onAccentContainer`, the name (14sp, weight 500), an optional count line, `chevron-left`, and hairline dividers between rows. Dark theme uses dark surfaces (no cream gradients); light theme uses white cards.
- Count line only if the counts are already available on this screen without new queries. Use plurals with the right noun: "١٢٤ فتوى"، "٣٨ درسًا"، "٥٦ خطبة"، "١٧ محاضرة"، "٩ تلاوات"، "٢٤٤ مقطعًا".
- Titles start-aligned: "الصوتيات" and "الفيديوهات" (drop "تصنيفات").
- Download any missing icons as in the first pass (Tabler outline, `ic_tabler_<name>.xml`).

## Phase 5: Splash and onboarding

Splash:
- The logo shows a faint box around it and a stray arc at the bottom-right. Use a logo with a transparent background (prefer a vector logo if the project has one), sized for the splash icon mask: a 288dp canvas with the logo inside the central 192dp circle (inset the vector as needed).
- Set `android:windowSplashScreenBackground` and `android:windowSplashScreenAnimatedIcon` in the `values-v31` themes (use `core-splashscreen` only if it's already a dependency). Remove any icon background circle or shadow.
- Background per theme: exactly the app's dark background in `values-night`, and the app's light background in `values`, so splash → onboarding never flashes white.
- Don't remove activities. If a custom splash has an artificial delay, remove the delay.
- If only a raster logo with a baked-in background exists, set the splash background to exactly that image's background color and log that a transparent logo is needed.

Onboarding: three pages instead of four, following the app theme, keeping Ali's illustrations.
- Page 1 (illustration: the person at the computer): title "كل علم الشيخ في مكان واحد", body "خطب ودروس ومقالات وفتاوى، مرتبة في مكان واحد."
- Page 2 (illustration: the computer and server): title "انقطع اتصالك بالإنترنت؟", body "احصل على الدروس والمقاطع الصوتية دون إنترنت بعد تحميلها داخل التطبيق."
- Page 3 (illustration: the person and globe): title "شارك ما ينفع", body "شارك مقاطع الخطب والدروس كفيديو، والاقتباسات من المقالات كصور."
- Buttons: "التالي" on pages 1–2 and "لنبدأ" on page 3; "تخطي" stays. No gray band behind the buttons: they sit on the page background.
- Illustration box: fixed height (about 240dp), `ContentScale.Fit`, bottom-aligned, so the titles don't jump between pages.
- Illustrations in brand colors:
  - **Vector drawables:** list every color each one uses (write the list in the log) and classify each: white background, light neutral fill, dark ink, accent, skin. Set the accent to the brand gold `#FAC775` and make the background transparent.
  - **Dark theme:** add `drawable-night` copies with this mapping (treat close shades the same way): white and near-white fills (#FFFFFF, #F2F2F2, #F0F0F0) → #2C2C2A; light grays (#E6E6E6, #E4E4E4, #DFDFDF, #D0CDE1) → #3A332E; mid grays (#CCCCCC and similar) → #444441; dark ink (#090814, #2F2E41, #3F3D56, #000000) → #D3D1C7; accent → #FAC775; skin tones unchanged.
  - **Raster (PNG/WebP):** don't recolor. In dark theme, show the illustration on a rounded light "stage" card (#F7F4EF, 20dp corners, 16dp padding), and log that vector versions are needed.
- The illustrations from the removed pages (the journey and the PDF document) move to empty states in Phase 7.

## Phase 6: About and Share-app screens

About ("عن التطبيق"):
- Keep Ali's illustration at the top (transparent background, about 210dp wide at most, with a night variant per Phase 5).
- Below it: a 28dp logo circle next to "تطبيق الشيخ د. حسن الهواري" (instead of presenting "الشيخ د. حسن أحمد الهواري" as the app name), then "الإصدار <versionName>" from BuildConfig, in Latin digits.
- Description card: "خطب الشيخ ودروسه ومقالاته وفتاواه في مكان واحد، مع الاستماع دون إنترنت ومشاركة المقاطع والاقتباسات."
- Rows:
  - "الموقع الرسمي" with `dr-alhawary.com` as an LTR subtitle; opens `https://www.dr-alhawary.com`.
  - "تواصل معنا": the same target as Profile's "الدعم والتواصل".
  - "ما الجديد في هذا الإصدار" only if release notes already exist in the project; otherwise omit it and log.

Share app ("مشاركة التطبيق"):
- Keep Ali's illustration (about 170dp wide at most).
- Heading "شارك التطبيق لينتفع به غيرك" (replaces "مشاركة التطبيق مع الإخوة").
- Message preview card: the label "نص المشاركة", the text "تطبيق الشيخ د. حسن الهواري: خطب ودروس ومقالات وفتاوى في مكان واحد", and the Play Store link (LTR).
- Primary button "مشاركة الرابط": the system share sheet (`ACTION_SEND`, text/plain) with the message and link.
- Secondary button "نسخ الرابط": copies the link and shows the Snackbar "تم نسخ الرابط".
- Secondary button "رمز QR", only if `docs/assets/qr_play_store.png` exists AND the app's `applicationId` is `app.netlify.devalihassan` (the code encodes that ID). Copy the image into the right `res/drawable-nodpi/` folder as `qr_play_store.png`. The button opens a bottom sheet with "امسح الرمز لتحميل التطبيق", the QR image (about 200dp, on a white background in both themes), and "أظهره لمن حولك في المسجد أو الدرس". If either check fails, omit the button and log why.

## Phase 7: Empty and error states

Only where a screen already has an empty or error state. Don't create new states or new logic.
- No connection / load error: the computer-and-server illustration, "تعذّر الاتصال بالإنترنت", and the existing retry action as a button.
- No search results: the PDF document illustration, "لا توجد نتائج", and "جرّب كلمة أخرى".
- No downloads yet: the whiteboard illustration (from the removed Rate screen), "لا توجد تنزيلات بعد", and "حمّل الدروس لتستمع إليها دون إنترنت".
- The journey illustration: a "coming soon" state if one exists (for example the Institute's "منصة المعهد قريبًا" card), at most about 180dp tall.
Same fixed-height box and night variants as Phase 5.

## Phase 8: Final report

Complete `docs/plans/ui-pass-2-log.md` with:
1. Each phase Done or Skipped, with reasons.
2. Decisions you made on your own.
3. The commits on the branch.
4. **Manual follow-ups for Ali**, for example: "what's new" release-notes text; a transparent or vector logo if one was missing; vector versions of any raster illustrations; the QR if it was skipped; the in-app review API as a separate decision; switching the playback direction to right-to-left if he prefers it.
5. **Device checklist**, in light and dark: player idle and while downloading (cancel works, playback starts before the download finishes), speed cycling and persistence, the share preview with each duration chip and the nudge buttons, both category screens, splash in both themes (no box, no flash), onboarding swipe/skip/finish, About links, share sheet, copy link, QR sheet, empty states, and no gray bar anywhere.
6. Known risks.

## Out of scope

Home, Welcome/sign-in, the announcement carousel, a media cache layer, the in-app review API, skipping the category screens, and anything that needs the database, backend, new dependencies, or new modules.
