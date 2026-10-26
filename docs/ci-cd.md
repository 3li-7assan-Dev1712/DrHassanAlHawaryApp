# CI/CD

Two GitHub Actions workflows, both for the **user app** (`app.netlify.devalihassan`). The admin app is not on the Play Store: CI only checks that it still compiles.

| Workflow | Runs on | Does |
|---|---|---|
| `android-ci.yml` | every PR to `master`, every push to `master` | lint (every module but admin), unit tests, user app debug and release builds, admin debug build |
| `release.yml` | a pushed tag `v*` (for example `v1.2.1`) | builds the signed bundle, uploads it to Play **internal testing** |

Lint: `MissingTranslation` is off for every module (`lint.xml` at the root), because the apps are Arabic only. Every other lint error fails CI. A failed run keeps the lint and test reports as a workflow artifact (`reports`).

Gradle gets a 6 GB heap in both workflows (`-Dorg.gradle.jvmargs`): the 2 GB in `gradle.properties` runs out of memory in lint and R8 when everything builds at once.

## Releasing a version

1. In `app/build.gradle.kts`, raise `versionCode` by one and set `versionName` (for example `13` and `"1.2.1"`). Merge that to `master` through a PR.
2. Tag the merged `master` and push the tag:
   ```
   git checkout master && git pull
   git tag v1.2.1
   git push origin v1.2.1
   ```
   The tag must be `v` + `versionName`, or the workflow stops before building.
3. The **Release to Play** workflow (Actions tab) builds the signed AAB, sends the R8 mapping file to Crashlytics, and uploads the AAB and the mapping file to the internal testing track. The AAB and mapping file are also kept as a workflow artifact.
4. In the Play Console, check the internal release, then promote it to production (Test and release → Internal testing → Promote release). Production is never released automatically.

If the workflow fails before the upload step, nothing reached Play: fix it, then move the tag (`git tag -f v1.2.1 <commit>` and `git push -f origin v1.2.1`). If the upload reached Play, that `versionCode` is used up: raise it and tag again.

## One-time setup

### 1. The upload key

Use the keystore you sign with today in Android Studio (Generate Signed Bundle). It must be the **upload key** Play knows: compare its SHA-1 (`keytool -list -v -keystore upload.jks`) with Play Console → Test and release → App integrity → App signing → Upload key certificate.

### 2. A service account for Play

1. In the [Google Cloud console](https://console.cloud.google.com/), choose a project (the Firebase project is fine) and enable **Google Play Android Developer API**.
2. IAM & Admin → Service accounts → Create service account. Give it no Cloud roles. Open it → Keys → Add key → JSON, and download the key.
3. In the Play Console → Users and permissions → Invite new users, enter the service account's email. Under App permissions, add this app and allow **Release apps to testing tracks**. (Some accounts still have Setup → API access; granting access there works too.) New permissions can take a few hours to apply.

### 3. GitHub secrets

Run these in Git Bash from the repo folder. `gh secret set NAME` with no input asks for the value without showing it.

```
base64 -w0 /path/to/upload.jks | gh secret set RELEASE_KEYSTORE_BASE64
gh secret set RELEASE_STORE_PASSWORD
gh secret set RELEASE_KEY_ALIAS
gh secret set RELEASE_KEY_PASSWORD
gh secret set PLAY_SERVICE_ACCOUNT_JSON < /path/to/service-account.json
```

Then delete the downloaded service account key. If it ever leaks, delete the key in the Cloud console and add a new one.

| Secret | Used by | What |
|---|---|---|
| `GOOGLE_SERVICES_JSON` | both | `app/google-services.json`, base64 |
| `ADMIN_GOOGLE_SERVICES_JSON` | CI | `admin/google-services.json`, base64 |
| `ALGOLIA_APP_ID`, `ALGOLIA_API_KEY`, `GOOGLE_WEB_CLIENT` | both | the same values as in `local.properties` |
| `RELEASE_KEYSTORE_BASE64` | release | the upload keystore, base64 |
| `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` | release | the keystore's password, key alias and key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | release | the service account's JSON key |

## Signing on your machine

Nothing changes: without the `RELEASE_*` environment variables the release build stays unsigned, and Android Studio's Generate Signed Bundle signs it as before.
