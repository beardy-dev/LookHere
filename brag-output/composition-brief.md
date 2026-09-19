# Hyperframes Composition Brief: LookHere

## Objective
Create a short launch-style brag video for LookHere.

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: landscape — 1920x1080
- Duration: 24.5 seconds (Scene 1 is 3.5s; a new fold beat
  was added before the outro, and the entry/hero scenes were tightened to
  keep the cut under the 25s cap)

## Source Material
- Project root: repo root (native Android app, not a website)
- Primary files read: `README.md`, `lookhere-color-palette.md`, real on-device
  screenshots captured from a running debug build in an earlier session
  (`brag-output/screens/`) — refreshed with new captures from a Fold-class
  device/emulator: `gif-selector.jpg`, `selfie-split.jpg`,
  `rear-camera-fullscreen.jpg`, `cover-screen-gif.png`
- Product name: LookHere
- Tagline / strongest claim: "plays a fun GIF right where your subject is
  looking, to grab their attention while you shoot"
- Key UI or visual moment to recreate: the selfie split-pane screenshot — a
  picked GIF looping directly above the live camera viewfinder in one frame.
  This is the single clearest proof of the product's whole idea, and it is
  also where the shutter-tap payoff happens — one continuous shot carries
  both beats, not two disconnected screenshots.
- Copy that must appear verbatim:
  - "LookHere"
  - "Kids won't look at the camera."

## Creative Direction
- Tone preset: default
- Creative direction: warm, funny-because-it's-true, zero corporate gloss
- Interpretation: comfortable 3-5s scenes plus one longer hero hold,
  mixed-case playful type, clean crossfades/slides, restraint on jokes —
  the premise carries the humor
- Angle: Every parent's nemesis is a kid who won't look at the camera.
  LookHere's whole premise is making that moment fun instead of a fight —
  a genuinely funny GIF plays right where the kid's eyes already are, so
  picture time is fun for them and easy for the parent.
- Hook: "Kids won't look at the camera." — stated flat, alone on screen.
- Outro / punchline: "Finally, a good picture." under the LookHere wordmark.
- Avoid:
  - Generic SaaS language
  - Abstract filler visuals
  - Unrelated visual redesign
  - Framing this as a trick played "on" the kid (no "lie to them" /
    deception language anywhere) — the story is that the app makes picture
    time more fun and easier, not that it fools anyone.
  - Cutting to a second, disconnected screenshot for the shutter-tap
    payoff — it happens inside the same hero shot as the GIF/viewfinder
    hold (see Storyboard).
  - Leading with the folding-phone/cover-screen angle — it's a bonus beat
    after the hero shot, not the headline.

## Visual Identity
- Background: `#FFF8F1` (light-mode app background, warm cream)
- Text: `#241512` primary, `#7A655D` secondary/muted
- Accent: `#FD594D` primary/shutter red-orange; `#FBB748` secondary yellow;
  `#2F8F87` tertiary teal (sparing use)
- Display font: no custom brand font in the app (Material 3 default); use a
  clean, rounded modern sans for the video
- Body font: same family, regular weight
- Visual references from the project: the app icon
  (`lookhere_icon_fullbleed_v3.png`), the real screenshots in
  `brag-output/screens/` — specifically `gif-selector.jpg` (landscape GIF
  picker), `selfie-split.jpg` (selfie split-pane with a picked GIF,
  including the shutter button in its normal on-screen position),
  `rear-camera-fullscreen.jpg` and `cover-screen-gif.png` (fold beat). The
  older `01-`–`04-` PNGs are superseded and not used in this cut.

## Storyboard
Use the storyboard in `brag-output/brag-plan.md` as the creative contract.

Scene summary:
1. Hook — 3.5s (trimmed from 4.5s; the freed second went to the outro) — "Trouble getting kids to focus during a picture or video?" alone on cream background.
2. Reveal — 3s — illustrated single-phone concept: the screen lights up
   with a GIF-shaped icon, then a small camera-viewfinder icon badges in
   alongside it. Caption: "Let's make it more fun!"
3. Flow: entry — 3.0s — real screenshot of the GIF picker (search bar),
   simulated typing of "snoopy" character by character (matches the
   Snoopy GIF picked in the hero shot).
4. Flow: hero hold — 7.52s — ONE real screenshot (the selfie split-pane,
   `selfie-split.jpg`) held for the whole scene. First caption beat:
   "Loops right where they're looking." / "Swap sides to line it up with
   your camera lens." Crossfades to second caption beat "Tap. Got it." as
   a simulated shutter tap (at the real shutter button's on-screen
   position in that same screenshot) fires a flash and a thumbnail
   pop-in — no image swap, no scene cut mid-action.
   Two side callouts appear during the hold: "Works on any phone. / Great
   for selfies on a single-screen phone, too." (pointing at the split-pane)
   and "Photos and videos. / Swipe between PHOTO and VIDEO." (pointing at
   the mode selector).
5. Fold beat — 4.0s — two real captures side by side: the inner-screen rear
   camera viewfinder ("You see the viewfinder") and the cover screen looping
   the GIF ("They see the GIF").
6. Outro — 3.48s — app icon + "LookHere" wordmark + "Finally, taking pictures can be fun!" + small "More memories. More fun." byline + a "Coming soon to Google Play" pill (plain text, no store badge or logo).

## Audio
- Audio role: warm upbeat bed with tasteful UI-accurate SFX
- Audio arc: fades in under the hook at low-mid volume, rises to full
  presence through the reveal and flow scenes, short pull-down heading into
  the outro, clean fade-out on the last beat.
- Music: `happy-beats-business-moves-vol-1-by-ende-dot-app.mp3`
- Music treatment: as above; no ducking needed (no voiceover in this run).
- Music cue guidance: bundled preset at
  `assets/music/cues/happy-beats-business-moves-vol-1-by-ende-dot-app.music-cues.json`
  (120.19 BPM). Strong cues useful in the first ~20s: 16.02s, 17.02s, 17.52s,
  18.52s, 20.02s. This cut lands the thumbnail pop on the 16.02s strong cue, with the
  shutter tap a second earlier on the 15.02s beat-grid pulse. Beat grid runs ~0.5s apart from ~3s onward — usable for the
  shutter-tap/thumbnail-pop sequence inside Scene 4's hero hold.
- Audio-reactive treatment: subtle — the Scene 2 concept icon's glow may
  breathe gently with the beat. Nothing waveform-literal.
- Audio-coupled moments:
  - Scene 2 — light UI tick as each icon lights up
  - Scene 3 — soft key-tick per typed character
  - Scene 4 — shutter click and thumbnail pop, beat-locked (see below)
- SFX selection guidance: soft/light UI taps for picker interactions, a clean
  camera shutter click for the payoff, a light "pop" for the thumbnail —
  choose exact files after the animation exists.
- SFX analysis guidance: use `skills/brag/assets/sfx/sfx-analysis.md` from
  the brag skill for file selection; prefer lower high-frequency-risk sounds
  for repeated moments (typed characters, icon lighting).
- Exact SFX choice: Hyperframes' call, based on the implemented animation.
- Audio files: reuse the music/SFX already copied into
  `brag-output/composition/assets/` from the prior run — no new tracks needed.

## Hyperframes Instructions
Load the composition-building Hyperframes domain skills — `hyperframes-core`
(composition contract + `data-*` timing), `hyperframes-animation` (motion),
`hyperframes-creative` (design spec, beats, audio-reactive), `hyperframes-keyframes`
(seek-safe keyframes), and `hyperframes-cli` (lint/check/render). /brag is its
own workflow: do not enter the `hyperframes` entry-point intent interview and
do not route into its generic promo / launch-video workflow. Prefer native
Hyperframes conventions over anything in `/brag`.

Requirements:
- Show at least one real UI, copy, or visual element from the source project
  — here, the real screenshots in `brag-output/screens/` (PNG files at
  1768x2208, portrait phone aspect — crop/frame them into the landscape
  canvas rather than stretching).
- Keep all text readable in the final render.
- Keep the video within 15-25 seconds.
- Include the planned music/SFX layer.
- Treat `/brag` audio notes as guidance, not a fixed cue sheet.
- Use only 1-3 strong-cue locks in this 20s video.
- Run `npx hyperframes check` before render — it is brag's single gate.
- Note for this project specifically: the folding-phone cover-screen mode
  is shown with a real capture of the cover screen (`cover-screen-gif.png`,
  an emulator render including its device bezel) beside the inner-screen
  rear-camera viewfinder. Scene 2 stays a phone-agnostic illustrated
  concept, not a Fold-specific diagram, and must read clearly as
  illustrated, not a screenshot.
