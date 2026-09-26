# Share video frame redesign — plan

Scope: `:feature:feature-share` only, plus the minimum plumbing to pass the real
category/date in from `feature-audio` + the NavHost. No change to the Transformer
engine, output format, clip-window logic, or the §7 file-cleanup policy of
`share-audio-video.md`.

Target: the attached mockup (header → chip → title → date → quote card →
waveform + times → footer), full-bleed on the video frame. The rounded corners
in the mockup belong to the phone frame; a video frame can't have them. The
on-screen preview card keeps its rounded corners.

---

## 1. What's wrong today, and why (from reading the code)

| # | Problem | Root cause found | Fix |
|---|---|---|---|
| 1 | Title hidden by the play button | `ShareCardSpec.title` sits at y 0.49–0.66, dead centre | New layout keeps the title block above y = 0.375 |
| 2 | Broken bidi on the title/date string | The raw server title (`"خطبة بعنوان: … - الجمعة: ( ٢٧ … 2026/5/15م"`) is drawn as one paragraph | `ShareTitleParser` splits it into kind / title / hijri / gregorian and strips the parentheses. Each part is drawn as its own RTL `StaticLayout` |
| 3 | Mixed numerals | The Gregorian date comes from the server in Western digits | `ArabicNumerals` is the only formatter. Every number on the card goes through it, and Gregorian months use Arabic names |
| 4 | Stray "audio" label | `AudioDetailViewModel` passes `audio.type` (the value is literally `"audio"`) as `category` | Pass `audio.categoryId` (`khotab`, `fatawah`, …) instead and map it to a chip label. Never draw `type` |
| 5 | "Black letterbox bars" | **Not the pipeline:** the base PNG is exactly 1080×1920 and `Presentation` scale-to-fits it into 720×1280, the same aspect, so no bars are possible there. **Most likely the design itself:** `scrimStops` puts 60% black over the top 24% and 85% near-black over the bottom, on a brown gradient, which reads as bars | Solid full-bleed brand background, no photo scrim. Verified by pulling an export off the device and sampling the corner/edge pixels of a decoded frame (step 0) |
| 6 | Right half of the waveform flat, no progress, no time | Four candidates, not yet proven: **(a)** one loud transient sets the whole-clip peak, so normal speech normalises to ~0; **(b)** the decoder is assumed to output 16-bit PCM and is never checked (`KEY_PCM_ENCODING`); **(c)** the overlay windows 48 buckets around `presentationTimeUs × fps`, so any timestamp offset clamps it to the clip's tail; **(d)** the preview uses a different envelope (a resampled whole-track overview) from the export | Step 0 logs the envelope stats, PCM encoding, and the first/last `presentationTimeUs` on the device, and the fix targets whatever that shows. Regardless: normalise per clip at the 95th percentile (not the max) with a floor, handle float PCM, and use one envelope for both preview and export |

### Step 0 results: a real export pulled from the device (2026-09-26)

- **#5 confirmed as the design, not the pipeline.** The MP4 is 720×1280
  (stored 1280×720 + rotation), 20 fps, and the decoded frame is full-size.
  Row 0 is `(7,5,3)` and the bottom row is `(10,8,6)`: that's the scrim over the
  brown gradient, not bars.
- **#6: three real bugs, plus one non-bug.**
  1. **Desync.** The overlay computes `frame = t × 20 fps` but indexes an
     envelope with 30 buckets per second. At 60 s the bars show audio from 40 s,
     and the last third of the clip is never drawn.
  2. **Wrong scale.** The strip bitmap is drawn at the 1080-px reference width
     but composited 1:1 onto the 720-px output frame (`setScale(1, 1)`), so it
     is 126% too wide and the bars run off both edges.
  3. **Low contrast.** The bars are brand brown `#342C2B` on a near-black
     background.
  4. **Not a bug.** The "flat right half" at t = 1 s is real audio: buckets
     26–47 of the clip sit at 0.00–0.05 of peak. A 48-bucket (1.6 s) window
     makes any short pause fill half the strip.

  The whole-clip bars + time-based progress fix 1 and 4. Drawing the strip at
  output resolution fixes 2. Gold/gray tokens fix 3.
- **Another title format seen in the wild:** `محاضرة - 6 ربيع الآخر 1448هـ`
  (no title, just a kind + date). It is handled by making the kind the title and
  hiding the chip.

## 2. Architecture: one painter, two hosts (drift made impossible)

Today the Compose preview re-implements the design and deliberately omits half of
it. The fix is to stop having two implementations at all:

- `ShareFrameLayout` is the single spec. Every rect, font size, colour token,
  radius, and bar count is expressed against the 1080×1920 reference. It replaces
  `ShareCardSpec` for the video card; `TextCardSpec` is untouched.
- `ShareFramePainter.drawStatic(canvas, content, layout)` draws the background,
  header, chip, title, date, quote card, and footer onto an Android `Canvas`
  using `StaticLayout` + `TextDirectionHeuristics.RTL` only.
- `ShareFramePainter.drawWaveform(canvas, bars, progress, elapsed, total, layout)`
  draws the bars, the played/unplayed colours, and both time labels.

Host 1, **export:** the renderer calls `drawStatic` into the 1080×1920 base PNG.
`WaveformOverlay` calls `drawWaveform` into its strip bitmap every frame.

Host 2, **preview:** the preview shows *that same base bitmap*. It is rendered
off the main thread and re-rendered 200 ms after the quote text stops changing.
The live waveform is drawn through `drawIntoCanvas { nativeCanvas }` with the
canvas scaled by `width / 1080` and the same `drawWaveform` call. The preview is
therefore the export's pixels, not a lookalike.

## 3. Data model

```kotlin
data class ShareCardContent(          // existing type, new optional fields (text card unaffected)
    val title: String,
    val category: String?,            // kept for TextCard (attribution line)
    val instituteName: String,
    val background: ShareBackgroundSource,
    @DrawableRes val logoResId: Int,
    val kindLabel: String? = null,    // chip: "خطبة الجمعة", "درس علمي", …
    val hijriDate: HijriDate? = null, // day, month name, year
    val gregorianDate: SimpleDate? = null,
    val quote: String? = null,        // ≤ 120 chars, blank → card hidden
)
```

Pure-Kotlin helpers with no Android imports, so they're JVM-unit-testable:

- `ShareTitleParser.parse(raw, categoryId)`
  - Splits on `- الجمعة:` / `الجمعة:` / a trailing ` - ` before a date.
  - Strips wrapping and unbalanced `( )`.
  - Pulls out the `X بعنوان:` prefix as the kind.
  - Parses the Hijri part (Arabic-Indic or Western digits, the known month names,
    `هـ`) and the Gregorian part (`y/m/d` or `d/m/y`, trailing `م`).
  - Anything unparseable stays in the title. It never throws and never drops text.
- The chip label comes from the parsed kind plus the day word, else from
  `categoryId`: khotab → خطبة الجمعة (خطبة العيد if the title mentions عيد),
  scientific_lessons → درس علمي, lectures → محاضرة, fatawah → فتوى,
  telawat → تلاوة. If neither is available, the chip is hidden.
- `ArabicNumerals`: `digits()`, `formatHijri()`, `formatGregorian()` (يناير … ديسمبر),
  and `formatDuration()` (mm:ss / h:mm:ss).

## 4. Layout (y as a fraction of 1920; the centre band 0.375–0.625 is kept free of the title)

| Element | y range | Notes |
|---|---|---|
| Header | 0.050–0.130 | Brown logo in a gold ring on the right. "الشيخ د. حسن الهواري" (no tatweel) + subtitle "خطب ودروس" to its left |
| Chip | 0.180–0.215 | Gold 1 px outline, dark-gold fill, gold text. Hidden if there is no label |
| Title | 0.235–0.335 | Cairo Bold, 2 lines max, auto-shrink 84 → 56 px, then ellipsize |
| Date | 0.345–0.368 | Gold. `hijri · gregorian`, or whichever one exists; hidden if neither |
| Quote card | 0.540–0.655 | Only when a quote is entered. Rounded surface-container card, «…» quotes, 3 lines max |
| Waveform | 0.770–0.825 | ~34 bars across the clip. Played = gold, filling right → left (RTL); rest = gray |
| Times | 0.835–0.852 | Elapsed on the right, total on the left, Arabic-Indic |
| Divider | 0.872 | 1 px, low-alpha |
| Footer | 0.885–0.925 | App name (right) + gold "حمّل التطبيق" pill (left). Ends inside the old `safeBottom` of 0.07 |

Deviation from the mockup: in the mockup the second title line and the date sit
at ~0.40–0.50, inside the play-button zone. I've moved the chip/title/date block
up ~7% to satisfy requirement #1. Everything else matches the mockup's order,
alignment, and proportions.

## 5. Colours: core-ui tokens

Added to `core-ui/theme/Color.kt`, and the engine reads these through `.toArgb()`:
`BrandGold` (bars, date, ring, chip text), `BrandGoldSoft` (CTA pill),
`BrandGoldContainer` (chip fill), `ShareFrameBackground` (= existing
`BrandBrown10`), `ShareFrameSurface` (quote card), `ShareFrameMuted` (unplayed
bars, subtitle). No hex literals remain in `feature-share` for this card.

## 6. Other items

- The CTA text, and the link used in the share intent's `EXTRA_TEXT` (Telegram
  uses it as a caption), come from non-translatable strings in core-ui:
  `share_cta_label` and `share_cta_url` (defaults to the Play Store URL built from
  the package name).
- The preview screen gets a quote `OutlinedTextField`: RTL, 120-char counter,
  optional.
- R8 / resource shrinking (the `app` module has `isShrinkResources = true`):
  - The logo and fonts are referenced through `R.*` and are kept, but I'll add
    `res/raw/share_keep.xml` (`tools:keep`) as a guard.
  - I'll audit for reflection. Media3 ships its own consumer rules and nothing in
    `feature-share` uses reflection today. I'll only add rules to
    `consumer-rules.pro` for anything I actually find, and document the audit in
    that file.
- The release build is verified with `:app:assembleRelease` plus an on-device export.

## 7. Commits, in order

0. **Investigate (no commit):** add temporary logging, run one export on the
   connected device, pull the MP4, and check the letterbox + envelope +
   timestamps. Then remove the logging.
1. `ShareCardContent` fields + `ShareTitleParser` + chip mapping + unit tests
   (including the exact string from problem #2).
2. `ArabicNumerals` date/time formatter + unit tests.
3. core-ui gold tokens + `ShareFrameLayout`.
4. `ShareFramePainter` + renderer; delete the old scrim/gradient path for this card.
5. Preview shows the rendered base + the shared waveform painter; quote field;
   pass `categoryId` in place of `type`.
6. Waveform: whole-clip bars, progress + times in the overlay, the analyzer
   normalisation/PCM fix, and one envelope for preview and export.
7. Keep rules / `tools:keep`, then the release build check.

## 8. Open questions (defaults in bold; I'll proceed with them unless you say otherwise)

1. **Time labels:** **clip-relative (٠٠:٠٠ → ٠١:٠٠, bars sweep the whole clip)**
   or lecture-relative (١٢:٤٠ / ٣٢:١٥ as in the mockup, where the bars barely
   move in a 60 s clip)?
2. **Output format:** the code currently exports **720×1280 @ 20 fps** (an
   earlier speed trade-off), not the 1080×1920 @ 30 fps in the spec. Keep it,
   per "don't change output format"?
3. **Items whose title has no date** (most fatawa/lessons): **hide the date line**,
   or fall back to the audio's `publishDate` (Gregorian only)?
4. **Title punctuation:** the mockup drops the comma ("فضل العشر والأضحية").
   **Keep the source text as-is** (only trim leading/trailing punctuation), or strip
   internal commas too?
5. The mockup's circle shows the placeholder text "الهواري". I'll use **the brown
   logo image**.
