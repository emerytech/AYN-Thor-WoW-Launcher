# Privacy policy draft — WOW this game is FOREVER

This draft must be reviewed and published at a public URL before submission.
Developer contact must match the final Play listing; no personal contact details
from a user's game account should be used here.

The launcher copies only a game folder explicitly selected with Android's picker
into app storage. Imported games, settings and optional frame-generation DLL stay
on that device unless the user separately shares them. Uninstalling removes the
app's private and app-specific files.

Optional saved game login stores the password encrypted with Android Keystore.
The user can forget it in the launcher. Automatic login and direct fill provide
credentials to the game client for sign-in. The client communicates with its
account/game provider; its services and privacy terms are separate from this app.
The launcher does not require sending a password to the fork maintainer.

Temporary Copy is user-initiated. It places the password on Android's clipboard,
marks it sensitive and attempts to remove its own copy after 30 seconds while
foregrounded, or when the app regains clipboard access. It leaves newer unrelated
clipboard copies intact. Direct Fill does not use the Android clipboard.

The Play edition does not check GitHub for app updates or download game files.
Game software can communicate with its publisher or other services, including
account, gameplay and optional voice features. Verify that behavior and any
bundled library telemetry before completing Data safety; do not declare that no
data leaves the device merely because the maintainer runs no backend.

Logs should exclude typed credentials. User-generated diagnostics may still
contain device details and file paths; users should review them before sharing.
