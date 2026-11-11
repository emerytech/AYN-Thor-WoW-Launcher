# Play edition preparation

Public developer/support contact: **ETS3D LLC / Emery Tech Solutions**, **hello@ets3d.com**, verified against https://ets3d.com/.

Chosen listing name: **WOW this game is FOREVER**. Package: `com.emerytech.handheldruntime`.
This is a separate installation from the GitHub edition and does not inherit its
saved credentials, imported DLL or game settings.

Build with `./gradlew bundlePlayRelease assemblePlayRelease`. Signing uses `app/keystores/play-upload.properties`, a local ignored file pointing
to ETS3D’s existing Couchside upload key and Play API service account. The private
key stays in its existing location. Never upload the key or its properties.
The `modern` distribution keeps the separate GitHub-release signing key.
The GitHub edition remains the `modern` flavor; do not upload that APK to Play.

## Implemented distribution differences

- Original vector launcher icon and chosen title; no Warcraft logo used as the Play launcher icon.
- Android folder picker copies a selected installation into app-specific external storage.
  Earlier imports and original source files remain untouched. Staging is cleaned on failure.
  Temporary plaintext `login.txt` files are excluded from import.
  Large copies use an application-owned job so recreating the launcher screen does not
  cancel them; copied megabytes are shown. Keep the app open. Process-death resume
  is not implemented.
- No all-files access, legacy shared-storage permissions, APK installation permission or READ_LOGS.
- GitHub app updater disabled at its entry points. Automatic game version checks/downloads disabled.
  Users import complete authorized files and updated installations themselves.
- Some runtime components are packaged. Physical first launch exposed an additional
  `imagefs_bionic.txz` download through `PluviaMain.preLaunchApp`, and modern component
  downloaders also require an audit. Complete Play delivery of executable runtime
  components before submission; the current AAB is not ready to submit.
- Lossless.dll is user-selected through the picker. Broad-storage convenience lookup is hidden.
- Unique application ID and package-relative runtime paths.

## Submission gates (not yet a Play submission)

1. Initial launch and scoped dummy-folder import passed on an Android 15 ARM64 emulator with PAGE_SIZE=16384. Full game/rendering execution on a compatible 16 KB device remains unverified.
2. A real 49 GB folder import passed on the Thor, with the original folder retained and zero `login.txt` files copied. First launch, sign-in, saved-login fill and controller input still need full acceptance.
3. Resolve rights/branding for the chosen name, compatibility wording and all bundled assets/components.
   The chosen name deliberately references the existing game. An original icon does not establish permission.
4. Review native Wine/game execution against Play executable-code policy; the interpreter exception must not be assumed to cover this runtime.
5. Privacy policy is hosted at https://emerytech.github.io/AYN-Thor-WoW-Launcher/play-store/privacy-policy.html with hello@ets3d.com. Complete Data safety and content rating based on actual game-client network/audio behavior.
6. Enroll package and upload key in the user's Play Console / Play App Signing. Determine applicable testing-track requirements from the account.
7. Full `lintPlayRelease` currently reports 11 existing issues: TV feature declarations, API 30 display calls with minSdk 29, native-wrapper constants and a missing saved-login translation. Release vital lint and packaging pass; full lint is not clear.
8. Use real screenshots from this Play edition. The GitHub feature-tour GIF is not a substitute for store screenshots.

No approval or legal clearance is implied by producing an AAB. No Play upload is authorized by this preparation document.
