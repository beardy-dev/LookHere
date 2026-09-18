# Hyperframes Composition Brief: LookHere

## Objective
Create a short launch-style brag video for LookHere.

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: landscape — 1920x1080
- Duration: 20 seconds

## Source Material
- Project root: `/Users/andrew/workspace/lookhere`
- Primary files read: `README.md`, `lookhere-color-palette.md`, real on-device
  screenshots captured from a running debug build (no `index.html`/`styles.css`
  — this is a native Android app, not a website)
- Product name: LookHere
- Tagline / strongest claim: "plays a fun GIF right where your subject is
  looking, to grab their attention while you shoot"
- Key UI or visual moment to recreate: the selfie split-pane screenshot — a
  picked GIF looping directly above the live camera viewfinder in one frame.
  This is the single clearest proof of the product's whole idea and must
  anchor the video.
- Copy that must appear verbatim:
  - "LookHere"
  - "Kids won't look at the camera."

## Creative Direction
- Tone preset: default
- Creative direction: warm, funny-because-it's-true, zero corporate gloss
- Interpretation: comfortable 3-5s scenes, mixed-case playful type, clean
  crossfades/slides, restraint on jokes — the premise carries the humor
- Angle: Every parent's nemesis is a kid who won't look at the camera.
  LookHere's whole premise is making that moment fun instead of a fight —
  a genuinely funny GIF plays right where the kid's eyes already are, so
  picture time is fun for them and easy for the parent.
- Hook: "Kids won't look at the camera." — stated flat, alone on screen.
- Outro / punchline: "Finally, a good picture." under the LookHere wordmark.
- Avoid framing this as a trick played "on" the kid (no "lie to them" /
  deception language anywhere) — the story is that the app makes picture
  time more fun and easier, not that it fools anyone.
- Avoid:
  - Generic SaaS language
  - Abstract filler visuals
  - Unrelated visual redesign
  - Implying the real Z Fold cover-screen effect was captured live — it
    was not (see Source Material note below); be honest in the visual
    treatment about what's a real screenshot vs. an illustrated concept.

## Visual Identity
- Background: `#FFF8F1` (light-mode app background, warm cream)
- Text: `#241512` primary, `#7A655D` secondary/muted
- Accent: `#FD594D` primary/shutter red-orange; `#FBB748` secondary yellow;
  `#2F8F87` tertiary teal (sparing use)
- Display font: no custom brand font in the app (Material 3 default); use a
  clean, rounded modern sans for the video
- Body font: same family, regular weight
- Visual references from the project: the app icon
  (`lookhere_icon_fullbleed_v3.png`), the four real screenshots in
  `brag-output/screens/` (GIF search/browse, Trending tab, selfie split-pane
  with a picked GIF, full rear-camera viewfinder)

## Storyboard
Use the storyboard in `brag-output/brag-plan.md` as the creative contract.

Scene summary:
1. Hook — 2.5s — "Kids won't look at the camera." alone on cream background.
2. Reveal — 3s — illustrated single-phone concept: the screen lights up
   with a GIF-shaped icon, then a small camera-viewfinder icon badges in
   alongside it. Caption: "So we gave it something worth looking at."
3. Flow: entry — 3.5s — real screenshot of the GIF picker (tabs + search),
   simulated tap into Trending, simulated pick of a GIF card.
4. Flow: key action — 5s — real screenshot of the selfie split-pane (picked
   GIF looping over the live viewfinder). Hold — this is the hero frame.
5. Flow: result — 3.5s — real screenshot of the rear-camera viewfinder,
   simulated shutter tap, flash accent, thumbnail pop-in.
6. Outro — 2.5s — app icon + "LookHere" wordmark + "Finally, a good picture."
   + small "Any phone. Even more fun on a Fold." byline.

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
  shutter-tap/thumbnail-pop sequence in Scene 5.
- Audio-reactive treatment: subtle — the Scene 2 concept illustration's icon
  glow may breathe gently with the beat. Nothing waveform-literal.
- Audio-coupled moments:
  - Scene 2 — light UI tick as each icon lights up
  - Scene 3 — soft tap on the tab switch and on the GIF pick
  - Scene 5 — shutter click and thumbnail pop, beat-locked (see below)
- SFX selection guidance: soft/light UI taps for picker interactions, a clean
  camera shutter click for the payoff, a light "pop" for the thumbnail —
  choose exact files after the animation exists.
- SFX analysis guidance: use `skills/brag/assets/sfx/sfx-analysis.md` from
  the brag skill for file selection; prefer lower high-frequency-risk sounds
  for repeated moments (tab switch, GIF pick).
- Exact SFX choice: Hyperframes' call, based on the implemented animation.
- Audio files: copy the chosen music (and any selected SFX) into
  `brag-output/composition/assets/`.

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
  live capture of it anywhere, by design (see brag-plan.md). Scene 4's
  subcaption is where that folding-phone detail is mentioned, using real
  screenshot material; it is not shown as footage. Scene 2 is a phone-
  agnostic illustrated concept, not a Fold-specific diagram, and must read
  clearly as illustrated, not a screenshot.
