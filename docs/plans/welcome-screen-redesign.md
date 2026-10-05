# Welcome / sign-in screen redesign — plan

Status: **implemented** (commits `ec0b804`..`fd08fa4` on `android-ci`). Decisions: full name
"الشيخ د. حسن أحمد الهواري"; official "G" as crops of the kit's dark PNGs; privacy URL is a blank
`AppLinks.PRIVACY_POLICY_URL` with a TODO; `androidx.browser` and test-only
`kotlinx-coroutines-test` added; admin app gets the same screen. Device checklist at the end.

## What exists today

| Piece | Where | Notes |
|---|---|---|
| Screen | `feature/feature-auth/.../presentation/auth/AuthScreen.kt` (`AuthScreen` + stateless `AuthScreenContent`) | Gradient `surfaceVariant → surface` background, `safeDrawingPadding()`, content centred as one block. Errors shown as a `Toast` of the raw exception message. |
| Header block | `.../components/WelcomeSectionComposelable.kt` (`WelcomeScreen`) | 140dp `Surface` + 3dp gradient border + shadow + 6dp inner padding → the "triple ring". Uses `R.drawable.dr_hassan_image`. Shows an admin badge when `isAdmin`. |
| Button | `.../components/LoginWithGoogleComp.kt` | M3 `Button`, theme `primary` fill (the cream/gold), 16dp corners, `R.drawable.google` (500px PNG). |
| State | `AuthScreenState` (data class: `isSignInSuccessful`, `showSignInProgressBar`, `errorMessage`) | Not sealed. Double-tap is already guarded in `AuthViewModel.loginWithGoogle`. |
| ViewModel | `AuthViewModel` → `LoginWithGoogleUseCase` → `AuthRepository.loginWithGoogle(activityContext: Any)` → `AuthRepositoryImpl` → `GoogleAuthUiClient.login` | |
| Sign-in client | `core/core-network/.../data_firebase/GoogleAuthUiClient.kt` | **Already Credential Manager** (`androidx.credentials` 1.5.0 + `googleid` 1.1.1), but with `GetGoogleIdOption` (bottom-sheet flow). ID token → `GoogleAuthProvider.getCredential` → `FirebaseAuth.signInWithCredential`. Server client ID from `BuildConfig.GOOGLE_WEB_CLIENT` (secret, not hardcoded). |
| Theme | `core/core-ui/.../theme/BrandColors.kt` (`BrandTokens`) | All tokens the spec names exist: `background`, `surface`, `goldSoft`, `gold`, `goldStroke`, `textPrimary`, `textSecondary`, `textMuted`. |
| Callers | `app/.../MainActivity.kt:171` (outside the NavHost, shown when logged out) and `admin/.../MainActivity.kt:203` (`isAdmin = true`) | **The redesign also changes the admin app's login screen.** Both activities call `enableEdgeToEdge()`. |

## Findings that change the spec

1. **The photo "corner" isn't a clipping bug; it's in the image.** `dr_hassan_image.png` (512², RGBA) is a finished logo: dark rounded-square background, cream ring, and a cream speech-bubble tail at the bottom-right. The tail sits *inside* the circle's area, so `clip(CircleShape)` can't remove it.
   → Switch to `R.drawable.dr_hassan_photo` (791² JPG, plain square photo, already in `core-ui`), with `clip(CircleShape)` + `ContentScale.Crop` on the `Image`. That removes the artefact at every density, because nothing in the source is non-square.
2. **No sign-in migration needed.** It's already Credential Manager. Change is limited to: use `GetSignInWithGoogleOption(serverClientId)` for the button tap (per the spec) instead of `GetGoogleIdOption`, and map the exceptions to typed errors. Firebase token exchange unchanged. No new dependencies.
3. **Official "G" logo can't be a VectorDrawable.** The current Google kit (`signin-assets.zip`, downloaded from developers.google.com) draws the new gradient "G" with a conic gradient inside `<foreignObject>` plus Gaussian-blur filters. VectorDrawable supports neither, so converting it would be an approximation, which the spec forbids.
   → Proposed: crop the 20×20 "G" area out of the kit's official **Dark / icon-only** PNGs (@1x→mdpi, @2x→xhdpi, @3x→xxhdpi, @4x→xxxhdpi = 20/40/60/80 px). That's pixel-for-pixel Google's asset; its backdrop is `#131314`, the same as the button fill, so there's no visible edge. The existing `google.png` is the older flat 4-colour "G" of unknown provenance; it would be replaced. See Q2.
4. **Release build risk, likely today.** `app/proguard-rules.pro` doesn't have the rule the Credential Manager docs require:
   ```
   -if class androidx.credentials.CredentialManager
   -keep class androidx.credentials.playservices.** { *; }
   ```
   Will add it (to `app` and `admin`). The drawables are referenced from code via `R.drawable`, so resource shrinking keeps them. I'll still add a `res/raw/keep.xml` to make that explicit.
5. **Registered SHA-1s** (from `google-services.json`, package `app.netlify.devalihassan`): `cd2c93…4562`, `cbcc00…9510`, `1364d9…62f6`. `cbcc00…9510` is this machine's **debug** key. The other two are presumably the upload key and the **Play App Signing** key, but that can't be verified from the repo (no `signingConfig`; release is signed outside Gradle). **Please confirm the Play Console → App signing SHA-1 matches one of those**; if it doesn't, sign-in in the Play-distributed build fails with "no credentials" / developer error.

## Privacy policy URL: none exists

Only `app/src/main/assets/privacy.md`, which the in-app `LegalTextScreen` shows (Profile → Privacy). There's no web URL in code, config or docs. Per the spec: one constant `PRIVACY_POLICY_URL` in `core-ui` with a `TODO`, used by the privacy line. See Q3.

## Plan (files and commits)

1. **Background + edge-to-edge**: `AuthScreen.kt`: `BrandTokens.background`, soft radial glow (`goldStroke` ~10% → transparent) behind the photo; force light system-bar icons while the screen is shown. Move home's private `DarkSystemBarIcons()` into `core-ui` (e.g. `core/ui/util/SystemBars.kt`) and reuse it in both screens. Insets via `WindowInsets.safeDrawing`.
2. **Photo**: `WelcomeSectionComposelable.kt`: `dr_hassan_photo`, one 2dp `goldStroke` ring + 8dp `surface` halo, 120dp (96dp when screen height < 640dp), `contentDescription` from a string resource.
3. **Text block + strings**: `core-ui` `values/`, `values-ar/`, `values-en/strings.xml`: `welcome_to_app` → "مرحبًا بك في تطبيق" (also fixed in `feature-onboarding`, which has its own copy), name, new value line `welcome_value_line`, photo description, error/no-account/retry/privacy strings. No hardcoded Arabic.
4. **Layout**: photo+text centred in a `weight(1f)` area; button + privacy line anchored at the bottom; whole column scrolls when it doesn't fit (landscape, 1.3× font). `widthIn(max = 480.dp)` kept for tablets.
5. **Google button + asset**: rewrite `LoginWithGoogleComp.kt`: pill, 48dp, `#131314` fill, 1dp `#8E918F` stroke, `#E3E3E3` text, 20dp logo on the start side, 12dp/10dp/12dp padding per Google. These three hex values are Google's brand colours, not app theme colours, so they go in a small `GoogleButtonColors` object next to the button, not in `BrandTokens`. The spinner replaces the logo while loading; the text stays.
6. **Sign-in flow**: `GoogleAuthUiClient.kt`: `GetSignInWithGoogleOption`; map `GetCredentialCancellationException` → `Cancelled`, `NoCredentialException` → `NoAccount`, `FirebaseNetworkException` / `GetCredentialInterruptedException` / other → `Failed`. Add `val error: LoginError?` (enum: `Cancelled`, `NoAccount`, `Network`, `Unknown`) to domain `LoginResult`, keeping `errorMessage` so other callers compile. Also stops swallowing coroutine `CancellationException` (the VM's catch-all currently does).
7. **UI state**: `AuthScreenState` → sealed `AuthUiState { Idle, Loading, Success, NoAccount, Error }`; `AuthScreen` shows a `Snackbar` with retry for `Error`. For `NoAccount`, a message with an "add account" action opening `Settings.ACTION_ADD_ACCOUNT` (`EXTRA_ACCOUNT_TYPES = ["com.google"]`). `Cancelled` → back to `Idle`, silently. The Toast goes.
8. **Privacy line**: `buildAnnotatedString` + `LinkAnnotation.Url` (announced as a link by TalkBack) with a `TextLinkStyles` underline; opens through a `LinkInteractionListener` → Custom Tab, falling back to `ACTION_VIEW`.
9. **A11y pass**: `isTraversalGroup`/order check, 48dp targets, start/end only.
10. **Tests + previews**: `AuthViewModelTest` with a fake `AuthRepository`: idle→loading→success, loading→cancelled→idle, loading→error→retry→loading. Previews: idle, loading, error snackbar, 360×640, fontScale 1.3, and admin.

## Open questions

1. **Name on this screen.** The spec says "الشيخ د. حسن الهواري". `core-ui/values-ar` `app_name` currently (in your uncommitted edit) reads "الشيخ د. حسن أحمد الهواري", and you just asked for the full name on shares. Use the full name here too? (I'd add a dedicated `welcome_sheikh_name` string rather than reuse `app_name`, since `app_name` differs between `values` and `values-ar`.)
2. **"G" logo**: OK to use crops of the official kit's dark PNGs (finding 3) instead of a VectorDrawable?
3. **Privacy URL**: do you have a hosted page (e.g. on your Netlify site)? If not, I'll add the `TODO` constant. Alternative: open the existing in-app privacy screen (`privacy.md`). That needs no URL, but that screen lives in the logged-in nav graph and `privacy.md` only exists in `app`, not `admin`.
4. **Arabic button label**: Google's branding guidelines only publish English CTAs ("Localization … is permitted and encouraged"). No official Arabic string exists to copy, so I'll use the spec's "المتابعة باستخدام Google".
5. **New dependencies outside the Credential Manager exception**:
   - Custom Tabs needs `androidx.browser` (already in the catalog, used by `feature-study`). Add it to `feature-auth`, or just use `ACTION_VIEW`?
   - ViewModel tests need `kotlinx-coroutines-test` (test-only; not in the catalog yet) to replace `Dispatchers.Main`. OK to add?
6. **Admin app**: apply the same redesign there (it shares `AuthScreen`), keeping the "الإدارة" badge under the name? I assume yes.
7. **"No Google account"**: with `GetSignInWithGoogleOption`, Google's own sheet usually offers "add account" itself, so our `NoAccount` state may rarely show. I'll still handle it as specified.

## Device checklist (not yet run)

- [ ] Photo corner clean at mdpi / xhdpi / xxhdpi / xxxhdpi
- [ ] Status and navigation bar areas dark with light icons, in both the app's light and dark themes
- [ ] Sign-in succeeds (debug)
- [ ] Cancelling Google's picker returns silently to the button
- [ ] Airplane mode: error snackbar, then retry works once back online
- [ ] Device with no Google account: message + "إضافة حساب" opens the add-account screen
- [ ] Privacy link opens (after `PRIVACY_POLICY_URL` is set)
- [ ] TalkBack order: photo, welcome, name (heading), value line, button, privacy link
- [ ] **Release build**: sign-in works (needs the Play App Signing SHA-1 registered, see finding 5)
- [ ] Admin app login screen (its own primary-coloured top bar still shows above this screen)
