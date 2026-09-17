![App Logo](/Users/andrew/workspace/kid-snap/lookhere_icon_fullbleed_v3.png)
# LookHere

An Android camera app built for the Samsung Galaxy Z Fold: while you shoot a
photo with the rear camera, the phone's **cover screen** (facing the subject)
loops a fun GIF to grab their attention, while the **inner screen** stays the
normal camera viewfinder for whoever's taking the photo. On phones without a
second screen — or when the Fold is folded shut — it falls back to a selfie
mode that shows the GIF and viewfinder side by side on the one screen you've
got.

## Features

- **GIF search** via the [Klipy](https://klipy.com) API (a Tenor-API
  successor), filtered to G-rated content, plus a **trending** feed shown by
  default so there's always something to browse.
- **Upload your own GIF** from the device's photo library as an alternative
  to searching.
- **Recents** — the last GIFs you've picked (search or upload) show up first
  the next time you open the picker, before falling back to trending.
- **Rear-camera + cover-screen mode**: the selected GIF loops on the Fold's
  outer display via Jetpack WindowManager's rear-display "window area" API,
  while the inner screen shows a live CameraX viewfinder.
- **Selfie mode**: flip to the front camera and the GIF plays in an in-app
  pane alongside the viewfinder instead (there's no one else to show the
  cover screen to). Automatically kicks in when folded shut or on a
  single-display phone, and live-switches to it if you fold the phone mid-session.
- Photos save straight to the system camera roll, with a one-tap thumbnail
  to jump into your gallery app afterward.
- Light/dark theming with a custom color palette (see
  `lookhere-color-palette.md`), custom app icon, and a branded splash screen.

## Tech stack

| Purpose                        | Library                                   |
|---------------------------------|-------------------------------------------|
| UI                              | Jetpack Compose + Material 3               |
| Camera capture & preview        | CameraX                                    |
| GIF search API client           | Retrofit + kotlinx.serialization           |
| GIF decoding/looping            | Glide                                      |
| Cover-screen (dual-display) rendering | Jetpack WindowManager (`androidx.window.area`) |
| Local persistence (selected GIF, recents, settings) | Jetpack DataStore (Preferences) |
| Splash screen                   | `androidx.core:core-splashscreen`          |
| Async                           | Kotlin Coroutines / Flow                   |

Written in Kotlin, built with Gradle's Kotlin DSL. Min SDK 26, target/compile
SDK 37.

## Project structure

```
app/src/main/java/com/beardydev/lookhere/
├── LookHereApp.kt              Application class, owns the manual DI container
├── MainActivity.kt             Single Activity, hosts the Compose tree + splash screen
├── di/                         Manual dependency container (no Hilt/Dagger)
├── data/
│   ├── klipy/                  Retrofit API, DTOs, and repository for Klipy GIF search
│   └── settings/                Selected-GIF + recents persistence (DataStore)
├── display/                     Rear-display "window area" controller for the cover screen
└── ui/
    ├── theme/                   Color palette + MaterialTheme setup
    ├── navigation/               Top-level screen routing (GIF picker <-> camera)
    ├── gifsearch/                GIF search/recents/upload screen
    └── camera/                   Camera screen (viewfinder, capture, selfie split layout)
```

## Setup

1. Open the project in Android Studio (or build from the CLI with the
   included Gradle wrapper — no separate Android SDK/JDK setup needed if
   you're using Android Studio's bundled versions).
2. Get a Klipy API key: sign up at
   [partner.klipy.com](https://partner.klipy.com), create a test key (100
   calls/hour is enough for development), and request production access
   when you're ready to rely on it day to day.
3. Copy `local.properties.sample` to `local.properties` (already
   gitignored) and fill in:
   ```properties
   KLIPY_API_KEY=your-key-here
   ```
4. Build and run. The GIF search screen will show a "missing API key" error
   until step 3 is done.

## CI builds

Gradle reads `KLIPY_API_KEY` and the signing credentials (`storeFile`/
`storePassword`/`keyAlias`/`keyPassword`) from `local.properties` /
`keystore.properties` first; if those files don't exist (as in CI), it falls
back to environment variables of the same names instead (`KLIPY_API_KEY`,
`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). The
release workflow (`.github/workflows/release.yml`) supplies these from
repository secrets.

## Release signing

Release builds are signed with a keystore at `keystore/lookhere-release.jks`,
configured via `keystore.properties` (both gitignored -- see
`keystore.properties.sample` for the format). Without `keystore.properties`
present, `assembleRelease` still builds, just unsigned by our key.

**Back up `keystore/lookhere-release.jks` and the passwords in
`keystore.properties` somewhere durable outside this repo** (a password
manager's file storage, encrypted cloud storage, etc.) as soon as you
generate one. If it's ever lost, you cannot publish an update to an
already-released app under the same identity -- Android treats a
differently-signed build as a different app entirely, so there's no way to
regenerate a lost key and continue updating a published app. If you're
distributing through Google Play, enroll in Play App Signing so Google
retains an upload-key recovery path even if you lose your local copy.

## Known limitations

- The cover-screen GIF loop requires a real Samsung Galaxy Fold device (or
  similar dual-screen foldable) with vendor support for Jetpack
  WindowManager's rear-display API — it can't be verified on an emulator.
- v1 scope is a single manually-picked GIF per session; auto-rotating
  through multiple GIFs per shutter press is a possible future feature, not
  built here.

## Versioning

Follows [semantic versioning](https://semver.org/); current version `0.1.0`.
`versionCode` is derived from the semver string
(`major * 10_000 + minor * 100 + patch`) so it stays in step with
`versionName` and keeps increasing across releases.
