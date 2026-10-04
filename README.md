# Smriti for Android

Smriti is packaged as an Android app with native exact alarms. Scheduled alarms
are handled by Android rather than by a browser timer, so closing Smriti's
window or removing it from the recent-apps list does not stop them. The phone's
default alarm ringtone repeats until the alarm is dismissed or snoozed.

## Publish the website

Create a **public GitHub repository** for this project and push the project
files to its `main` or `master` branch. The GitHub Actions workflow at
`.github/workflows/pages.yml` publishes `index.html` to GitHub Pages on each
push. In the repository, enable **Settings > Pages > Build and deployment >
Source: GitHub Actions** if Pages is not enabled automatically. When the
workflow completes, its deployment step shows the public website URL, usually
`https://<github-username>.github.io/<repository-name>/`.

The hosted website saves notes in that visitor's browser and can alert them
while the page is open. Browsers do not run a ringing alarm after the website
is closed; closed-app alarms require installing the Android app below.

## Build and install

1. Install Node.js 22 or newer and Android Studio.
2. In Android Studio, install Android SDK Platform 36 and the Android SDK Build
   Tools. Set **Settings > Build, Execution, Deployment > Build Tools > Gradle >
   Gradle JDK** to JDK 21. If Android Studio only offers its bundled JDK 25,
   select **Download JDK...** and install JDK 21 for the Gradle project.
3. From this project folder, run:

   ```powershell
   npm install
   npm run android:sync
   npm run android:open
   ```

4. In Android Studio, run the app on a connected Android phone, or use
   **Build > Build APK(s)** to create an APK. The debug APK is written to
   `android/app/build/outputs/apk/debug/app-debug.apk`.
5. On the phone, allow notifications and grant Smriti **Alarms & reminders**
   access when Android asks. Keep alarm volume turned up.

After changing the HTML, run `npm run android:sync` again before building.

## Alarm behavior and Android limits

- Exact alarms wake the device at the reminder time and use the phone's default
  alarm ringtone. The ringtone loops until **Done** or **Snooze** is selected.
- Android may delay or suppress alarms if Smriti is force-stopped, its alarms
  permission or notifications are disabled, the device is powered off, or
  manufacturer-specific battery restrictions prevent background work. Alarms
  are restored after a normal device restart when Android permits it.
- Notes saved in a web browser are separate from notes in the installed Android
  app; browser storage is not copied automatically.

This repository contains the app source. An APK must be built and installed
using Android Studio and the Android SDK.
