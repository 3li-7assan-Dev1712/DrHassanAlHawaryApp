# Home screen redesign — plan

Scope: the header, the content grid, the "أحدث المقالات" and "أحدث الصوتيات"
sections, the bottom nav, edge-to-edge, tatweel cleanup, article text cleaning, and
numerals. The announcement carousel and its indicator are **not touched**, and
there is no "continue listening" section.

## What exists today

| Area | Files |
|---|---|
| Screen | `feature-home/.../presentation/HomeScreen.kt`. It has an uncommitted edit of yours that removes the green brand theme. |
| Grid | `presentation/components/LessonsByCatigory.kt` (`Category`, `LessonsByCategory`) |
| Cards | `components/ArticleCard.kt` (280×200 fixed, 5-line preview), `components/AudioCard.kt` (180×120, has its own `formatDuration`), `components/LatestArticleAudioLazyRow.kt` |
| State | `HomeScreenViewModel.kt`, `HomeScreenUiState.kt` |
| Models | `ArticleFeed(id, title, contentPreview)` and `AudioFeed(id, title, duration, audioUrl)`. They drop `publishDate`/`categoryId`, which the Room entities (`ArticleEntity`, `AudioEntity`) *do* have |
| Mapping | `data/HomeRepositoryImpl.kt` (`getLatestArticles`/`getLatestAudios`). Clean text here |
| Bottom nav | `app/.../ui/navigation/BottomNavigationBar.kt` + `BottomNavItem.kt` (Material `NavigationBar`, pill indicator, filled/outline drawable pairs) |
| Edge-to-edge | `enableEdgeToEdge()` is already called in `app` and `admin` `MainActivity`. The light strip comes from what's drawn behind the status bar (to be confirmed on device), not from a missing call |
| Theme | `core-ui/theme/Color.kt`, `Theme.kt` (both have uncommitted edits of yours), plus `ShareFrameColors.kt` (the share work's gold/neutral tokens) |
| Licenses | `app/src/main/assets/licenses.md`, rendered by `LegalTextScreen` |
| Notifications | No notifications screen exists → **no bell** (brief §4) |
| Tatweel | 29 strings in 4 files: core-ui `values`/`values-ar`, feature-home `values`, feature-onboarding `values`. Examples: `app_name`, `articles`, `audios`, `videos`, `profile`, `lessons`, `latest_*`, `no_*_available`, `share_app`. No hard-coded Kotlin UI text has tatweel |
| Reusable from the share work (all in `feature-share/domain`) | `ArabicNumerals` (digits, Hijri/Gregorian formatting, durations), `ShareTitleParser` (splits "خطبة بعنوان: X - الجمعة: (date)" into title/kind/Hijri/Gregorian), `ArticleText.cleanTitle` + `TextSanitizer` (author suffixes, FB timestamps, bylines, emojis, separators) |

## Approach

1. **Shared text code moves to `core-domain`** (a pure-Kotlin JVM module), package
   `com.example.domain.text`:
   - `ArabicNumerals`, `ShareTitleParser` + dates, and `TextSanitizer`/`ArticleText` move
     there, keeping their tests.
   - New `ArticleTextCleaner`: `cleanTitle`, `cleanBody`, `excerpt`, `readingMinutes`,
     `datePublishedInText`.
   - New `ArabicDates.relative(now, then)`: "الآن / منذ دقيقة / منذ ساعتين / أمس / منذ ٣ أيام
     / منذ أسبوع…", with correct Arabic number agreement ("دقيقة، دقيقتان، ٣ دقائق، ١١ دقيقة").
   - Home, article, and share then all use one implementation. Rather than home depending on
     feature-share, the code goes where every feature can reach it.
   - Today's Hijri date needs `android.icu`, so it goes in core-ui as `HijriToday`
     (`IslamicCalendar` + `ISLAMIC_UMALQURA`, API 24+).
2. **Tokens.** I'll generalise `ShareFrameColors.kt` into `BrandColors.kt` using the brief's
   names and **its exact hex values**. The share frame's current values are 1–5 levels off
   (e.g. `F09F2A` vs `EF9F27`), so the video and quote images shift imperceptibly and every
   screen uses one palette. `goldStroke`, `textPrimary`, and `textMuted` are new.
3. **Icons.** 12 Tabler outline SVGs are downloaded and converted to
   `core-ui/res/drawable/ic_tabler_<name>.xml` (stroke 2, round caps/joins, 24×24, white
   stroke), with a Compose preview sheet. The Tabler MIT license goes in `licenses.md`.
4. **Components** (`feature-home/presentation/components/`):
   - `HomeHeader`: logo 36dp with a 1dp ring, name, Hijri date.
   - `ContentGrid`: 3×2, 12dp tiles, 48dp minimum touch target, clipped ripple.
   - `SectionHeader`: title + "عرض الكل".
   - `ArticleCard` and `AudioCard`, rewritten compact and content-height.
   - The bottom nav is restyled in `app` (no pill, `goldSoft`/`textMuted`, top hairline).
5. **Models.**
   - `ArticleFeed`: gets `title` (cleaned), `excerpt` (first meaningful paragraph),
     `publishedAt`, and `readingMinutes`. Prefers the entity's `publishDate`; falls back to a
     date parsed from the text only when the entity has none.
   - `AudioFeed`: gets `publishedAt` and `categoryId`. The display title and Hijri date come
     from `ShareTitleParser`: the topic, else the kind ("محاضرة"). The Hijri date is the one
     announced in the title, else computed from `publishDate`.
   - Stored originals are never modified.
6. **"عرض الكل":** articles → `Routes.ARTICLES_SCREEN`, audios → `Routes.AUDIO_LIST_SCREEN`,
   through the existing `onCategoryClick`.
7. **Edge-to-edge:** `enableEdgeToEdge(SystemBarStyle.dark(background))` for light icons.
   The home header/body paint `background` behind the bars and apply
   `WindowInsets.statusBars`; the bottom nav applies `navigationBars`.
8. **Nav label:** a new `nav_account` = "حسابي" for the tab only. `profile` stays for the
   profile screen's own title.

## Commits (in the brief's order)

tokens → tatweel cleanup + resource-scan unit test → Tabler icons + license →
header → grid → text code move + `ArticleTextCleaner` + tests → articles section →
audios section → formatter/relative dates → bottom nav → edge-to-edge → delete
unused old drawables.

Old drawables still used elsewhere stay: `student_icon` (admin), `audios_icon`
(notification small icon), `fasalo_logo` (Q&A screen), `articles_icon` (search
results).

Previews: full home, very long title, article with no excerpt, audio with no
topic, and bottom nav × 4 selected states. Release build verified with R8 + shrinking.

## Open questions (my defaults in bold)

1. **Light theme.** The palette is dark-only, but the app has a light/dark toggle.
   Options: **home always uses the dark palette**, whatever the toggle says (matches the
   design and the share images, but home would be dark while the rest of the app is light);
   or I derive light variants of the tokens for the light theme. Which do you want?
   *Update (2026-09-26):* changed to follow the toggle. Home and the bottom bar read
   `Brand.colors` (`LightBrandPalette` / `DarkBrandPalette` in `BrandColors.kt`); sign-in
   and the share images keep the fixed dark `BrandTokens`.
2. **Your uncommitted theme work** (green-theme removal in `HomeScreen.kt`, `Color.kt`,
   `Theme.kt`, `MainActivity.kt`, untracked `BrandTheme.kt`) overlaps files this redesign
   rewrites. **Please commit it first** (or tell me to commit it as-is as its own commit),
   so my commits don't absorb it.
3. **Hijri date source.** Umm al-Qura can differ by a day from the dates announced in
   Sudan. **Use Umm al-Qura as the brief says**, or add a ±1 day adjustment setting?
4. **"فاسألوا" tile:** its target (`Routes.Q_A_SCREEN`) and the Q&A screen's own logo stay;
   **only the home tile icon changes**.
