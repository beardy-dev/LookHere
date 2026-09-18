# Hyperframes Composition Brief: LookHere

## Objective
Create a short launch-style brag video for LookHere.

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: landscape — 1920x1080
- Duration: 22 seconds (Scene 1 grew from 3.5s to 4.5s to give the longer
  hook line its reading-time floor; every later scene start shifted by the
  same +1s as a result)

## Source Material
- Project root: repo root (native Android app, not a website)
- Primary files read: `README.md`, `lookhere-color-palette.md`, real on-device
  screenshots captured from a running debug build in an earlier session
  (`brag-output/screens/`) — reused here since the app UI hasn't changed
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
  - Leading with the folding-phone/cover-screen angle — that's a byline-
    level bonus mention in the outro, not the headline.

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
  `brag-output/screens/` — specifically `01-launch.png` (GIF search bar)
  and `03-after-pick.png` (selfie split-pane with a picked GIF, including
  the shutter button in its normal on-screen position). `02-trending.png`
  and `04-rear-camera.png` are not used in this cut.

## Storyboard
Use the storyboard in `brag-output/brag-plan.md` as the creative contract.

Scene summary:
1. Hook — 4.5s (bumped from 3.5s for reading-time) — "Getting your kids to look at you while you snap a picture is hard." alone on cream background.
2. Reveal — 3s — illustrated single-phone concept: the screen lights up
   with a GIF-shaped icon, then a small camera-viewfinder icon badges in
   alongside it. Caption: "So we made it a little more interesting for them"
3. Flow: entry — 3.5s — real screenshot of the GIF picker (search bar),
   simulated typing of "good night" character by character.
4. Flow: hero hold — 8.52s — ONE real screenshot (the selfie split-pane,
   `03-after-pick.png`) held for the whole scene. First caption beat:
   "Loops right where they're looking." / "Swap sides to line it up with
   your camera lens." Crossfades to second caption beat "Tap. Got it." as
   a simulated shutter tap (at the real shutter button's on-screen
   position in that same screenshot) fires a flash and a thumbnail
   pop-in — no image swap, no scene cut mid-action.
5. Outro — 2.48s — app icon + "LookHere" wordmark + "Finally, taking pictures can be fun!" + small "More Fun. More Memories." byline.

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
  18.52s, 20.02s. Beat grid runs ~0.5s apart from ~3s onward — usable for the
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
  (a real second physical display) cannot be photographed — there is no
  live capture of it anywhere, by design. It is mentioned only as the
  outro byline, using no footage. Scene 2 is a phone-agnostic illustrated
  concept, not a Fold-specific diagram, and must read clearly as
  illustrated, not a screenshot.
