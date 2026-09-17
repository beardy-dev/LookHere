# LookHere

## What this is
Android app for a Samsung Galaxy Z Fold 8. While shooting photos with the
rear camera, the phone's cover screen (facing the subject) loops a
kid-friendly GIF to grab their attention, while the main/inner screen stays
the normal camera viewfinder for the person taking the photo.

## v1 scope
- User searches for and picks ONE GIF (via Tenor's API), which loops
  continuously on the cover screen.
- Manual "change GIF" button to go back to search anytime.
- NOT in v1: auto-rotating through multiple GIFs on each shutter press
  (noted as a future feature only).

## Architecture
- **CameraX** — camera preview and photo capture
- **Tenor API** — GIF search
- **Glide** — decoding/looping the selected GIF
- **Retrofit** — network calls to Tenor
- **Jetpack WindowManager** — foldable/hinge state detection
- **Android Presentation API (DisplayManager)** — renders the GIF to the
  cover screen as a second display, while the main screen keeps the
  camera viewfinder

## Known risk / fallback plan
Whether the Fold's cover screen registers as a usable "presentation
display" via the standard Android API isn't guaranteed on all Samsung
models. Build against the standard Presentation API first and test on
the real device. If the cover screen doesn't show up as a presentation
target, fall back to Samsung's Multi-Display SDK or a Flex Mode-specific
approach.

## Dev environment
- macOS, Android Studio (bundled JDK/SDK/Gradle — no manual toolchain
  wrangling needed)
- Project created with the "No Activity" template; Claude Code builds
  out the full project structure from there.
