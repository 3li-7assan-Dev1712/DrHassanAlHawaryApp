# Overnight UI polish pass: HassanAlHawary app

> Spec for an unattended Claude Code session (about 3 hours). Ali is asleep and can't answer.
> This file is the source of truth. Progress is tracked in `docs/plans/overnight-ui-pass-log.md`.

## A. Operating rules (read first)

1. **Nobody will answer you.** Never ask questions and never wait for approval. When something is unclear, choose the most conservative option that fits this spec, record it under "Decisions" in the log, and keep going.
2. **Don't end your turn early.** When a phase is finished, start the next one immediately. Stop only when every phase is marked Done or Skipped and the final report (Phase 13) is written.
3. **The log is your memory.** In Phase 0, create `docs/plans/overnight-ui-pass-log.md` with a checklist of every phase. Update it at the end of each phase. If your context gets compacted or you lose track, re-read this spec and the log, then continue from the first phase that isn't Done or Skipped.
4. **Denied commands are intentional.** Don't retry variations to get around a denial. Use another allowed approach, or skip the item and log it.
5. **Skip items, not phases.** If an item would cross a hard boundary (section B), depends on data that doesn't exist, or still fails to build after 3 attempts, skip that item, log why, and continue with the rest of the phase.
6. **Time-box.** No phase should take more than about 25 minutes. If one runs long, ship the smallest correct version, log what's left, and move on. Phases are in priority order, so it's fine if the last ones don't happen.

## B. Hard boundaries (never cross)

- **Database:** no changes to Room entities, DAO signatures, query result shapes, migrations, or the database version. No new tables or columns.
- **Backend/remote:** no changes to API/Firestore/Algolia models, field names, serialization, index settings, or remotely stored content. Content is cleaned **at display time only** (mappers, UI state, composables). Stored originals stay untouched.
- **Architecture:** no new Gradle modules, no moving code between modules, no new DI modules or changes to the dependency graph's structure, no new navigation routes or nav-graph restructuring. Pass arguments to an existing route only if it already accepts them.
- **Build:** no new dependencies, no version bumps, no edits to Gradle files, `gradle/`, `*.properties`, signing, or CI files. Never open or print `local.properties` (it contains secrets).
- **Preferences:** you may add NEW keys to a preference store that already exists (DataStore or SharedPreferences). Never change the type or meaning of an existing key. If no store exists, skip the items that need one.
- **Off-limits features:** the Home screen, the Welcome/sign-in screen, the announcement image carousel, and `:feature:feature-share` (audio video + quote image). Don't edit them; they may pick up shared theme or component fixes automatically.
- **Git:** work on a new branch. Never push, never rewrite history, never discard work. To abandon a failed attempt, use `git stash push -m "failed: <item>"`.

Allowed: new composables and pure-Kotlin helpers inside existing modules, string and plurals resources, vector drawables, theme tokens, ViewModel UI-state mapping, and styling changes.

## C. After every phase

- Run `./gradlew assembleDebug testDebugUnitTest` (on Windows, `./gradlew.bat` if `./gradlew` doesn't run). Fix failures you caused. After 3 failed attempts, stash the failing item, log it, and get the build green again before continuing.
- Commit: `ui: phase N - <name>`.
- Update the log.
- Never run instrumented tests or start an emulator.

## D. Design tokens

Add these semantic tokens to BOTH the light and dark color schemes. If tokens with the same purpose already exist (for example from the home redesign), reuse them and add only what's missing. Never rename existing tokens. Screen code uses tokens only, never raw hex.

| Token | Light | Dark | Used for |
|---|---|---|---|
| background | #F7F4EF | #1A1512 | screen background, status/nav bar area |
| surface | #FFFFFF | #2C2C2A | cards, rows, fields |
| surfaceMuted | #EFEAE2 | #241C17 | segmented-control track, subtle fills |
| divider | #E6E0D6 | #444441 | 0.5dp hairlines |
| accent | #BA7517 | #EF9F27 | icons |
| accentStrong | #EF9F27 | #EF9F27 | progress bars, active chip border, timeline dots |
| accentContainer | #FAEEDA | #412402 | chips, pills, icon circles |
| onAccentContainer | #633806 | #FAC775 | text and icons on accentContainer |
| accentText | #854F0B | #FAC775 | links ("عرض الكل"), accent subtitles |
| textPrimary | #2C2C2A | #F1EFE8 | titles |
| textSecondary | #5F5E5A | #B4B2A9 | body text, excerpts |
| textMuted | #888780 | #888780 | metadata |
| success | #1D9E75 | #5DCAA5 | status dot |
| successContainer | #E1F5EE | #04342C | status chip background |
| onSuccessContainer | #085041 | #9FE1CB | status chip text |
| danger | #A32D2D | #F09595 | delete account |
| fasalooTeal | #0F6E56 | #0F6E56 | فاسألوا button (white text) |

Shapes and spacing: cards and rows with 12–14dp corners, a 0.5dp `divider` border and no heavy shadow; pills fully rounded; 16dp screen padding; 16–20dp between sections. Font: the existing Cairo family.

---

## Phase 0: Setup and inventory

1. `git status`. If the tree isn't clean, commit everything as `chore: snapshot before overnight UI pass`.
2. `git switch -c ui/overnight-polish` from the current HEAD.
3. Baseline: `./gradlew assembleDebug testDebugUnitTest`. If it fails, don't try to fix the environment (JDK, secrets, signing): log the error, continue, and mark later phases "unverified".
4. Inventory, written into the log:
   - Theme files and existing color tokens; whether light and dark schemes both exist.
   - The shared top bar / header composable used by secondary screens.
   - Every place that builds "نُشر منذ" or any other date phrase.
   - Helpers from earlier work, if present: `ArabicFormatter`, `ArticleTextCleaner`, `TextSanitizer`, `ic_tabler_*` drawables, gold tokens, a circular sheikh-photo component. Reuse them; never create duplicates.
   - The preference store and how the theme choice is saved.
   - Modules that already have unit-test setup (`src/test` + `testImplementation`). Put new pure-Kotlin helpers and their tests there. If none exists, write the helpers anyway and log "tests skipped".
   - The code for: Articles list, Article reader, Fatwas list, Search (`:feature:feature-search`, Algolia), Profile, Institute, فاسألوا, About the Sheikh, Designs list + viewer, Videos list.
5. If `docs/screenshots/` has images, you may look at them for reference. This written spec wins over any image.

## Phase 1: Shared foundations

**1.1 Tokens** from section D.

**1.2 Shared top bar.** A gray vertical bar appears at the start of the title area on Search, Videos, Institute and the Article reader. Find it (likely in a shared header/top bar) and remove it. Secondary screens: back arrow pointing right (as today), start-aligned title, optional actions at the end. Tab-root screens (Search, Institute, Profile): start-aligned title, no back arrow.

**1.3 Published-date formatter** (pure Kotlin + `plurals` resources, unit-tested). Replace every place that builds a date phrase. Current bugs: "نُشر منذ منذ 5 ساعة" and "نُشر منذ 4 أبريل 2026". The formatter returns the complete phrase:
- under 1 minute: "نُشر الآن"
- under 60 minutes: "نُشر منذ دقيقة" / "نُشر منذ دقيقتين" / "نُشر منذ ٣ دقائق" / "نُشر منذ ١١ دقيقة"
- under 24 hours: same pattern with ساعة / ساعتين / ساعات / ساعة
- under 7 days: "نُشر أمس" for one day, then يومين / أيام
- up to 30 days: أسبوع / أسبوعين / أسابيع
- older than 30 days: "نُشر في ٤ أبريل ٢٠٢٦" with Arabic month names
- Use Android `plurals` with all Arabic quantities: one (no number), two (dual word), few 3–10 (plural noun), many 11–99 (singular noun), plus zero and other.
- All digits Arabic-Indic.

**1.4 Digits.** One helper converts digits to Arabic-Indic for text the app generates (durations, counts, dates, reading time, page counters). Never convert user data or technical strings: emails, handles, URLs, version names ("الإصدار 1.0.7" stays as it is).

**1.5 Bidi.** Wrap LTR fragments (handles, emails, URLs) in an LTR isolate (`BidiFormatter` or U+2066…U+2069). A handle must show as "@ali_7assan", never "ali_7assan@".

**1.6 Display-time cleaners** (pure Kotlin, unit-tested; extend `ArticleTextCleaner` if it exists):
- Article title: remove author suffixes (any " - الشيخ د.حسن …الهواري" variant, with or without "أحمد", "د." or "د .") and Facebook timestamps ("September 23 at 9:55 PM", "Yesterday at …"); turn the first remaining " - " into ": ".
  Test: "الأزمة الاقتصادية الطاحنة - مظاهر، أسباب، وتدابير - الشيخ د.حسن أحمد الهواري September 23 at 9:55 PM" → "الأزمة الاقتصادية الطاحنة: مظاهر، أسباب، وتدابير".
- Article body: remove header boilerplate at the top: lines equal to the title or its parts, author/timestamp lines, the service label "📝 خدمة المقالات والمقتطفات 📝", decorative separator lines (3+ of = ═ ─ - _ ـ * • plus ornaments like ✿ ❀ ❁ ✽ ✾), and runs of blank lines. Convert WhatsApp-style `*bold*` into bold spans in the reader and plain text everywhere else.
- Excerpt: the first real paragraph of the cleaned body, skipping a leading basmala line. The reader keeps the basmala (Phase 3).
- Audio title: remove "خدمة المقاطع الصوتية -", "مقطع بعنوان:", a trailing "- خدمة فضيلة الشيخ…" (to the end of the string), emojis such as ⏪, and extra spaces. If the result is empty, fall back to the original. Tests:
  - "خدمة المقاطع الصوتية - مقطع بعنوان: حكم شراء الذهب من التطبيقات والتجار" → "حكم شراء الذهب من التطبيقات والتجار"
  - "خدمة المقاطع الصوتية - مقطع بعنوان: حكم لبس النقاب - خدمة فضيلة الشيخ د. حسن" → "حكم لبس النقاب"
  - "مقطع بعنوان: ⏪(٢) التعريف بصحيح البخاري" → "(٢) التعريف بصحيح البخاري"
- Reading time: words ÷ 180, minimum 1, as plurals: "قراءة دقيقة"، "قراءة دقيقتين"، "قراءة ٣ دقائق"، "قراءة ١١ دقيقة".

**1.7 UI strings pass** (`strings.xml` and hardcoded UI text only, never remote content):
- Remove every tatweel (U+0640).
- Put the tanween before the alif: replace "اً" with "ًا" ("قريباً" → "قريبًا", "حالياً" → "حاليًا", "نهائياً" → "نهائيًا").
- Add a unit test that fails if any string resource contains U+0640 or "اً".

**1.8 Icons.** One outline icon set on every screen this pass touches: Tabler outline icons as VectorDrawables named `ic_tabler_<name>.xml` (24×24 viewport, stroke width 2, round caps and joins, white stroke so `tint` controls the color). Reuse existing ones. Download missing SVGs with curl from `https://raw.githubusercontent.com/tabler/tabler-icons/main/icons/outline/<name>.svg` and convert them by hand. Icons are not auto-mirrored: use `arrow-right` for back and `chevron-left` for forward, as the screens do today.
Needed: arrow-right, chevron-left, search, x, clock, share, text-size, player-play, player-pause, download, headphones, notebook, video, photo, school, circle-check, send, message-question, logout, trash, sun, moon, device-mobile, world, zoom-in, info-circle, star, headset, shield-lock, file-text, code.
If a download fails, keep the current icon in that spot and log it. If the app has a licenses screen ("التراخيص والمصادر"), add Tabler Icons (MIT).

## Phase 2: Quick fixes (small, high-visibility)

1. **Institute:** a round black floating button with a swap icon covers the "حسابي" tab. Find out what it does. If it's a debug tool, show it only when `BuildConfig.DEBUG`. If it's a real feature, move it into the Institute screen's top-bar actions. It must never overlap the bottom navigation.
2. **Institute:** show the handle through the bidi helper as "@handle".
3. **Designs viewer:** the counter "9 / 1" becomes "١ من ٩".
4. **Videos:** put thumbnails in a 16:9 container with `ContentScale.Crop`. YouTube's `hqdefault.jpg` is 4:3 with black bars baked in, and cropping to 16:9 removes them.
5. **About:** the sheikh photo shows a cream square corner poking out of the circle at the bottom-right. Apply `clip(CircleShape)` to the image itself with `ContentScale.Crop`, and remove the dark square frame around it.
6. **Search:** the default selected chip is "الكل" (today "مقالات" is preselected).
7. **Article reader:** remove the floating share button that covers the text; share moves to the top bar.
8. **About text fixes**, only if this content lives in the app (strings or Kotlin). If it's remote, list them for Ali instead:
   "البلاكرويس" → "البكالوريوس"; "بكلاريوس" → "بكالوريوس"; "الدكتوراة" → "الدكتوراه"; "بالمدية" → "بالمدينة"; "ا لاسلامية" → "الإسلامية"; "الاسهامات" → "الإسهامات"; "الى" → "إلى"; add a period in the bio ("… في الشأن السوداني. درس المراحل …"); add the missing space in "المنورة(1990م)"; collapse double spaces.

## Phase 3: Articles list and Article reader

Articles list:
- The whole card is tappable; remove the "قراءة المزيد…" button.
- Card: cleaned title (max 2 lines), cleaned excerpt (max 2 lines), and a meta row with a clock icon: "<date phrase> · قراءة X دقائق" (reading time only if the list has the body text; otherwise the date alone).

Article reader (Compose Text or WebView, apply the equivalent):
- Top bar: back arrow, then actions: font size and share. Remove the truncated two-line title from the top bar. Bookmark only if a bookmark feature already exists.
- A thin reading-progress bar (`accentStrong`) under the top bar, driven by the scroll position.
- Header: title and subtitle (split at the first ":" left by the cleaner; subtitle in `accentText`), then one meta line: "الشيخ د. حسن الهواري · <date> · قراءة X دقائق".
- Body: cleaned (Phase 1.6). A basmala on the first line is centered on its own line. Decorative separators inside the body become one small centered ornament "✦ ✦ ✦" in `accent`, never at the very top.
- Section headings: lines matching `^[▪▫•◦■□\s]*(أول|ثاني|ثالث|رابع|خامس|سادس|سابع|ثامن|تاسع|عاشر)(ًا|اً|ا)\s*[:：]` render as headings (bold, `textPrimary`, a short `accentStrong` bar at the start) without the bullet glyph.
- Alignment: start (right in RTL). No justification anywhere, because Android justifies Arabic by stretching the spaces between words.
- Emojis inside normal paragraphs stay; they're the author's.
- Font size: 4 steps, with the current size as the default step, stored as a new preference key if a store exists. The top-bar action opens a small popup with A− / A+ and a preview line.
- Text selection: if the quote-image share feature exposes an entry point that takes a String, add "مشاركة كصورة" to the selection toolbar through a custom `TextToolbar`. If that isn't straightforward, skip and log.

## Phase 4: Search (`:feature:feature-search`, Algolia)

Don't change Algolia index settings or records; everything here is client-side.
- Field: rounded `surface` field with a search icon, a muted placeholder "ابحث في المقالات والصوتيات والفيديوهات", and a clear (x) button. The placeholder must never look like typed text.
- Minimum query: after removing a leading "ال" from each word, at least one word must have 2+ letters. Otherwise don't search; show the hint "اكتب كلمة أطول قليلًا". Debounce 300 ms.
- Chip counts ("مقالات ٥") when hit counts are available from the responses (for example `nbHits`); otherwise no counts.
- "الكل": results grouped by type, each group with a header ("صوتيات · ٩") and "عرض الكل" that selects that chip.
- Rows: a type icon (headphones / notebook / video / photo) in a 34dp `accentContainer` circle, the cleaned title, and meta ("صوتية · ١٨:٢٠", "مقال · قراءة ٥ دقائق").
- Highlight whole words only, never fragments inside words. Use a word-level, display-only normalizer (pure Kotlin, tested): remove tashkeel (U+064B–U+0652, U+0670) and tatweel; أ إ آ ٱ → ا; ة → ه; ى → ي; ؤ → و; ئ → ي; strip one leading prefix from {وال, بال, فال, كال, لل, ال, و, ف, ب, ل}. A word matches when its normalized form starts with a normalized query word (itself stripped of "ال").
  Tests: "احكام" matches "أحكام"; "الصيام" matches "صيام" and "والصيام"; the query "ال" matches nothing.
- Article snippet: about 100 characters of cleaned text around the first matching word, snapped to word boundaries, with "…" on the cut sides. If only an excerpt is available, use the cleaned excerpt.
- Empty state: replace the large magnifier illustration with suggested topic chips (static strings: الزكاة، الصيام، الحج، البيوع، الأسرة; tapping one fills the query) and, if a preference store exists, up to 8 recent searches with a "مسح" action.

## Phase 5: Fatwas list (فتاوى)

- Titles go through the audio-title cleaner.
- Compact rows: 40dp play circle (`accentContainer` with an `onAccentContainer` icon), title (max 2 lines), duration in Arabic-Indic digits.
- Playing state (`accentStrong` circle, pause icon, thin progress bar) only if the screen already has access to the current media item; otherwise skip.
- Replace the lone ✓ with the download icon + "محفوظ", driven by the existing downloaded flag.
- Category chips only if audio items already have a category field; filter client-side.
- A top-bar search icon only if the Search route already accepts a preset filter; otherwise omit it.

## Phase 6: Videos

- Title: "الفيديوهات" when the category is "الكل"; otherwise the category name. Category chips only if all videos and their categories are already loaded on this screen (client-side filter).
- Optionally load `maxresdefault.jpg` first and fall back to the current thumbnail URL on error. Keep the 16:9 crop from Phase 2.
- Duration badge (bottom-left, dark background, Arabic-Indic digits) only if the model has a duration.
- Restyle the category chip overlay with `accentContainer`; play overlay 40dp.
- Date phrase from the Phase 1 formatter.

## Phase 7: Designs (التصاميم) and viewer

List:
- 2-column grid: `LazyVerticalStaggeredGrid`, or `LazyVerticalGrid` with 4:5 cells and `ContentScale.Crop` if staggered isn't available in the current Compose version.
- Tile: image with 10dp corners, title (max 2 lines, never cut mid-title with ".."), date meta when available.
- Titles like "تصميم - 9 ذو الحجة 1447هـ": drop the redundant "تصميم - " and show the date as the title with Arabic-Indic digits ("٩ ذو الحجة ١٤٤٧هـ").
- Multi-image posts: one tile with a count badge instead of a horizontal row ("٩ صور"; plurals: صورة، صورتان، ٣ صور، ١١ صورة).

Viewer:
- Black background; image at full width; `HorizontalPager` for multi-image posts.
- Pinch zoom (1×–4×), double-tap zoom, and bounded pan with `detectTransformGestures` (no new dependency).
- Top bar: close, title (1 line), counter "١ من ٩".
- Share button: load the image with the existing image loader, write a PNG to `cacheDir/share/`, and share it through the existing FileProvider. No save-to-gallery (log it as a follow-up).
- Thumbnail strip for multi-image posts; the current thumbnail is outlined in `accentStrong`.

## Phase 8: Profile (حسابي)

- Screen title "حسابي", matching the tab.
- Header card: 48dp avatar with an `accent` ring, name, email (LTR isolate, `textMuted`).
- "المظهر": a 3-option segmented control تلقائي / فاتح / داكن. Keep existing users' choice: add a NEW key for "follow system". If the existing theme key already has a stored value, the user chose it explicitly, so keep it (follow system off). If it has no value, default to تلقائي.
- "الإعدادات": a font-size row using the reader's preference (only if Phase 3 implemented it).
- "التطبيق" and "الدعم والسياسات": same items; outline icons in `accent`, chevron-left, dividers between rows, `surface` cards.
- Rename "منطقة الخطر" to "الحساب". The sign-out row is neutral. The delete row uses `danger` for both icon AND text: "حذف الحساب نهائيًا". If there's no confirmation dialog yet, add one (title "حذف الحساب؟", one sentence saying deletion is permanent, buttons "حذف الحساب" in `danger` and "إلغاء") that calls the existing delete logic.
- The version line stays unchanged.

## Phase 9: About the Sheikh (عن الشيخ)

- Photo: circular with a 2dp `accent` ring (the Phase 2 fix). Reuse the welcome screen's circular photo component if one exists.
- Name "الشيخ د. حسن الهواري" from one string resource. Split the role line at " - " into two `accentContainer` chips.
- Tabs (Material3 tab row, no pager): نبذة، المؤهلات، البحوث، المساهمات (use the existing section titles if they differ).
- Qualifications as a vertical timeline: an `accentStrong` dot and connecting line, a year chip parsed from "(1990م)" → "١٩٩٠م", the title, and the institution in `textMuted`. Remove the parsed year from the text.
- Research sorted newest first with year chips; items containing "لم ينشر" get a "لم يُنشر بعد" chip.
- Remove the emojis from section headers (🎓 ✍️ 📚).
- "القنوات الرسمية": the website `https://www.dr-alhawary.com`, plus official links that already exist in the codebase. Never invent URLs.

## Phase 10: Institute (المعهد)

- Profile card: avatar with an `accent` ring, name, "@handle" (Phase 2), and a status chip in `successContainer` with a `success` dot and the real status text ("من طلاب المعهد · الدفعة الأولى").
- Coming-soon card: school icon in a 52dp `accentContainer` circle, the title "منصة المعهد قريبًا", the body "نجهّز محتوى الدراسة داخل التطبيق، وسيُفعَّل قريبًا إن شاء الله.", then a separate success line with a check icon, "تم ربط حسابك بقناة المعهد على تيليجرام" (only when the account is linked).
- A "فتح قناة المعهد" button only if the channel URL already exists in the code; otherwise omit it and log.
- No "notify me" button (it needs backend work).

## Phase 11: فاسألوا

- Shared top bar with a back arrow and the title "فاسألوا"; the app `background` instead of flat gray.
- Keep the existing فاسألوا logo drawable (about 72dp).
- Title "أرسل سؤالك الشرعي"; body "فاسألوا هي المنصة الموحدة لأسئلة الفتاوى، وتعمل عبر تيليجرام."
- Steps card with numbered circles (١، ٢، ٣): "افتح فاسألوا في تيليجرام"، "اكتب سؤالك بوضوح واختصار"، "تابع الرد في المحادثة نفسها".
- "قبل أن تسأل" card: the subtitle "لعل سؤالك أُجيب عنه من قبل" and a read-only field "ابحث في الفتاوى المجاب عنها" that opens the existing Search screen (with an audio preset only if the route already supports it).
- Button: `fasalooTeal` with white text and the send icon, "افتح في تيليجرام". If the current Telegram intent can't be resolved (`ActivityNotFoundException`), fall back to the equivalent `https://t.me/…` link in the browser or a Custom Tab; if that fails too, show a Snackbar.
## Phase 12: Consistency pass

- Fix gray-card-on-white and white-on-gray inconsistencies on every touched screen: `background`, `surface` cards, hairline dividers.
- Edge-to-edge: if it isn't enabled yet, enable it, give the status and navigation bar areas the `background` color with correct icon contrast in both themes, and apply `WindowInsets` padding on the touched screens. Skip this item if it causes layout problems you can't verify.
- Add light and dark `@Preview`s for touched screens where it's cheap.

## Phase 13: Final report

Finish `docs/plans/overnight-ui-pass-log.md` with:
1. Summary: each phase Done or Skipped, with reasons.
2. Decisions you made on your own.
3. The commits on `ui/overnight-polish`.
4. **Manual follow-ups for Ali** (content, backend, or product decisions you couldn't make): the test article "اختبار / محتوى للاختبار"; the placeholder fatwa "فتوى جديدة بالتصنيف"; the typo "احام الربا" → "أحكام الربا"; the cut-off audio title "أحكام صيام المرأة ا"; descriptive titles for designs; the About text if it's remote; reviewing Algolia's Arabic settings (query/index languages "ar", ignorePlurals, removeStopWords) in the dashboard; an Institute "notify me" (needs an FCM topic and a sender); save-to-gallery in the viewer; and everything you skipped.
5. **Device checklist** for the morning: per screen, in light and dark, and at 1.3× font scale.
6. Known risks.

## Out of scope

The Home screen, Welcome/sign-in, the announcement carousel, "continue listening", guest mode, the share feature, notification settings, downloads management, bookmarks (unless they already exist), announcement expiry, and anything that needs the database, backend, new dependencies, or new modules.
