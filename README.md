![App Logo](lookhere_icon_fullbleed_v3.png)
# LookHere

An Android camera app built for the Samsung Galaxy Z Fold: while you shoot a
photo with the rear camera, the phone's **cover screen** (facing the subject)
loops a fun GIF to grab their attention, while the **inner screen** stays the
normal camera viewfinder for whoever's taking the photo. On phones without a
second screen (or when the Fold is folded shut), it falls back to a selfie
mode that shows the GIF and viewfinder side by side on the one screen you've
got.

## Download

Grab the latest APK from the [Releases page](https://github.com/beardy-dev/LookHere/releases/latest).

## Installing

This isn't on the Play Store, so your phone needs to allow installing an
APK from outside it:

1. Download the `.apk` file from the [latest release](https://github.com/beardy-dev/LookHere/releases/latest)
2. When you open it, Android will likely block the install and prompt you
   to allow it: tap through to **Settings** and enable "Install unknown
   apps" for whichever app you downloaded it with (Chrome, Files, etc.)
3. You may also see a Play Protect warning like *"Unknown app"* or *"App
   not verified"*. This is expected for any app distributed outside the
   Play Store from a developer with no install history yet, not a sign
   something is wrong. You can review the source code in this repo
   yourself if you'd like to verify what it does before installing.
4. Once allowed, open the APK again to install normally.

## Permissions

LookHere requests:

- **Camera**: to show the viewfinder and take photos, obviously.
- **Photos/Media**: to save your photos to the camera roll, and to let
  you upload your own GIF from your device instead of searching.
- **Internet**: to search GIFs via the [Klipy](https://klipy.com) API.

That's it: no location, no contacts, no background access.

## Features

- **GIF search** via the [Klipy](https://klipy.com) API (a Tenor-API
  successor), filtered to G-rated content.
- **Recent / Uploads / Trending tabs**: a blank search shows a tabbed
  browse area: your last 12 picks, just the ones you uploaded from your
  device, and Klipy's trending feed. Typing a search query replaces the
  tabs with search results; clearing it brings them back. Recent and
  Uploads work without a Klipy API key since they're local data; only
  search and Trending need the network.
- **Upload your own GIF** from the device's photo library as an alternative
  to searching; the last several uploads stay available in the Uploads tab.
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

Package-based Clean Architecture in a single module: `domain` holds models,
repository interfaces, and the use cases that actually contain
orchestration/branching logic; `data` implements those interfaces; `ui`
depends only on `domain`, never on `data` directly. Wiring is manual (no
Hilt/Dagger) via `di/AppContainer`.

```
app/src/main/java/com/beardydev/lookhere/
├── LookHereApp.kt              Application class, owns the manual DI container
├── MainActivity.kt             Single Activity, hosts the Compose tree + splash screen
├── di/                         Manual dependency container (no Hilt/Dagger)
├── domain/
│   ├── model/                  GifResult, SelectedGif, GifBrowseResult, TrendingState
│   ├── repository/              GifRepository / SelectedGifRepository interfaces
│   ├── error/                   AppError sealed type
│   └── usecase/                 ObserveGifBrowseResultsUseCase, ObserveCoverScreenGifUseCase, SelectGifUseCase
├── data/
│   ├── klipy/                  Retrofit API, DTOs, and GifRepositoryImpl for Klipy GIF search
│   └── settings/                SelectedGifRepositoryImpl: selected-GIF + recents persistence (DataStore)
├── display/                     Rear-display "window area" controller for the cover screen
└── ui/
    ├── theme/                   Color palette + MaterialTheme setup
    ├── navigation/               Top-level screen routing (GIF picker <-> camera)
    ├── gifsearch/                GIF search/tabbed-browse/upload screen
    └── camera/                   Camera screen (viewfinder, capture, selfie split layout)
```

## Setup

1. Open the project in Android Studio (or build from the CLI with the
   included Gradle wrapper, no separate Android SDK/JDK setup needed if
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

## Testing

Unit tests cover the domain use cases (`./gradlew testDebugUnitTest`),
using hand-rolled fakes for the repository interfaces rather than a mocking
library. No API key or device/emulator needed to run them.

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
  WindowManager's rear-display API. It can't be verified on an emulator.
- v1 scope is a single manually-picked GIF per session; auto-rotating
  through multiple GIFs per shutter press is a possible future feature, not
  built here.

## Versioning

Follows [semantic versioning](https://semver.org/); current version `0.3.1`.
`versionCode` is derived from the semver string
(`major * 10_000 + minor * 100 + patch`) so it stays in step with
`versionName` and keeps increasing across releases.
