# LookHere

## What this is
Android camera app that plays a GIF right where your subject is looking,
to grab their attention while you shoot — built with kids in mind, since
they'd rather look at anything except the lens.

The core mode works on any phone: front camera, GIF in a pane next to the
viewfinder, swappable to whichever side lines up with the phone's actual
selfie-camera lens so your subject's eyes land near the lens instead of
just wherever's fun to look at on screen.

On a folding phone (e.g. Samsung Galaxy Z Fold 8), shooting with the rear
camera instead loops the GIF on the phone's outer cover screen (facing the
subject) while the inner screen stays a normal viewfinder for the person
taking the photo.

## v1 scope
- User searches for and picks ONE GIF (via Klipy's API), which loops
  continuously — on the cover screen on a folding phone, or in the selfie
  split-pane otherwise.
- Manual "change GIF" button to go back to search anytime.
- NOT in v1: auto-rotating through multiple GIFs on each shutter press
  (noted as a future feature only).
- NOT in v1: GIF content filtering/moderation — search and Trending
  results come straight from Klipy as-is. Planned for later.

## Architecture
- **CameraX** — camera preview and photo capture
- **Klipy API** — GIF search
- **Glide** — decoding/looping the selected GIF
- **Retrofit** — network calls to Klipy
- **Jetpack WindowManager** — foldable/hinge state detection
- **Android Presentation API (DisplayManager)** — on a folding phone,
  renders the GIF to the cover screen as a second display while the main
  screen keeps the camera viewfinder

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
