# Brag Plan: LookHere

## What is this app?
A camera app that plays a fun GIF right where your subject is looking, to
grab their attention while you shoot — built with kids in mind, since
they'd rather look at anything except the lens. Works on any phone via the
front-camera split-pane; on a folding phone like the Z Fold, the GIF moves
to the outer cover screen so you can shoot with the rear camera instead.

## The angle
Every parent's nemesis: a kid who will not look at the camera. LookHere's
whole premise is making that moment fun instead of a fight — the phone
plays a genuinely funny GIF right where the kid's eyes already are, so
they smile at the camera instead of away from it. It's earnest, not
corporate, because the app genuinely only does this one thing well: make
picture time more fun for the kid and easier for the parent.

## Hook (first 2-3 seconds)
Bold type, alone on the cream background: **"Kids won't look at the camera."**
A beat of recognition before the reveal — no logo yet, just the problem.

## Key moments (the middle)
- The GIF picker: real search UI, typing a search and landing on a
  genuinely charming pick.
- The hero hold: the picked GIF looping right above the live viewfinder,
  one real screenshot, held long enough to read as "this is the whole
  trick" — and a beat about the swap-sides control that lines the GIF up
  with the actual camera lens.
- The payoff, in that same shot: a clean tap of the shutter, a flash, the
  new thumbnail popping into the corner — no scene cut, because the shot
  that got the GIF is the shot that took the picture.

## Outro / punchline
Logo + wordmark on the cream/primary palette, then the tagline: **"Finally, a
good picture."** Small byline: "Any phone. Even more fun on a Fold."

## User flow worth showing
1. **Entry** — open the GIF picker, search, pick a GIF.
2. **Key action** — aim the front camera; the GIF plays right alongside the
   viewfinder, in the same frame the kid is looking at.
3. **Result** — tap the shutter, in that same frame; the shot lands,
   thumbnail appears instantly.

## Tone
- Preset: default
- Creative direction: warm, funny-because-it's-true, zero corporate gloss —
  the tone of someone showing a friend the dumb-smart thing they built.
- Interpretation: comfortable 3-5s scenes (one longer hero hold), mixed-case
  type that's allowed to be a little playful, crossfades/clean slides
  between beats, restraint on jokes — the product's premise is funny
  enough on its own.

## Format: landscape — 1920x1080
## Duration: 20s

## Visual identity (from the project)
- Background: `#FFF8F1` (light mode app background — warm cream)
- Accent/Primary: `#FD594D` (shutter button / primary red-orange)
- Secondary accent: `#FBB748` (warm yellow, sparing highlight use)
- Tertiary: `#2F8F87` (teal, GIF-picker accent — good for a secondary UI beat)
- Text: `#241512` (primary), `#7A655D` (secondary/muted)
- Display/body font: no custom brand font — app uses Material 3 default
  (Roboto-family); video can use a clean, rounded modern sans for a friendly
  match without needing to be pixel-identical.
- Strongest visual element: the real split-pane shot — a looping GIF stacked
  directly over the live camera viewfinder. That single frame *is* the
  product's whole pitch, and it's also where the shutter tap happens — one
  shot carries the whole middle of the video.

## Share copy (draft)
We built a camera app that gives kids something fun to look at, right where
the lens is, so picture time is fun for them and easy for you. Meet
LookHere. 📸

## Audio direction
- Role: warm, upbeat bed with tasteful UI-accurate SFX
- Music: `happy-beats-business-moves-vol-1-by-ende-dot-app.mp3` (120 BPM,
  bundled preset available)
- Music treatment: start at 0s under the hook at low-mid volume, swell
  slightly into the reveal, sit steady through the flow scenes, short pull-
  down into the outro card, clean fade-out on the last beat.
- Music cue guidance: preset read from
  `assets/music/cues/happy-beats-business-moves-vol-1-by-ende-dot-app.music-cues.json`.
  Strong cues in the first 20s cluster around 16.0s, 17.0s, 17.5s, 18.5s,
  20.0s — good targets for the shutter-tap and hero→outro transitions.
  Beat grid runs a steady ~0.5s spacing from ~3s onward — usable for the
  thumbnail pop-in.
- Audio-reactive treatment: subtle — the Scene 2 concept icon's glow may
  breathe gently with the beat; nothing waveform-literal.
- SFX posture: moderate — a soft UI tap for the GIF pick, a camera
  shutter click on the tap, a light "pop" for the thumbnail arriving.
- Audio-coupled moments: shutter tap synced to a strong cue; thumbnail pop-in
  synced to the following beat; GIF-card selection gets a light UI tap.
- Restraint rule: never let SFX or music upstage the shutter-click moment —
  that's the punchline beat of the whole flow and needs to read clearly.

## Storyboard

### Scene 1 — Hook — 2.5s
Cream background (`#FFF8F1`), centered bold dark text: "Kids won't look at
the camera." No logo, no chrome — just the problem, stated flat.
Sequential/interaction: none
Audio intent: quiet, a little wry — let the line land in silence-ish space
before the beat kicks in
Audio-coupled idea: none
Music: bed fades in under the line, still very low
Transition mood: clean → Scene 2

### Scene 2 — Reveal — 3s
Simple animated illustration of a single phone: the screen lights up with a
bright, bouncy sparkle/GIF-shaped icon, then a small camera-viewfinder icon
badges in alongside it — one phone, one screen, now with something fun on
it. Text overlay: "So we gave it something worth looking at." Uses the
app's primary/accent colors.
Sequential/interaction: yes — the GIF icon lights up first, the camera
badge follows a beat later
Audio intent: a small delighted "aha" — playful, not smug
Audio-coupled idea: a light UI tick as each icon lights up
Music: bed rises to full presence
Transition mood: soft → Scene 3

### Scene 3 — Flow: entry (pick a GIF) — 3.5s
Real screenshot: the GIF picker screen ("Search KLIPY" bar, tabs beneath
it). Simulate typing "good night" into the search field, character by
character. Text label, small and out of the way: "Pick any GIF."
Sequential/interaction: yes — search text types out, blinking caret at rest
Audio intent: light, snappy, gets the viewer moving through the UI
Audio-coupled idea: soft key-tick sound per typed character
Music: steady groove
Transition mood: clean → Scene 4

### Scene 4 — Flow: hero hold (key action + result, one continuous shot) — 8.52s
Real screenshot: the selfie split-pane view — the picked "Good Night" GIF
looping in the top half, the live camera viewfinder in the bottom half.
One image, held for the whole scene — this is the strongest single frame
in the video, and the shutter tap later in this same scene happens inside
it, not on a cutaway. Caption beat 1 (first ~4.5s): "Loops right where
they're looking." / subcaption: "Swap sides to line it up with your
camera lens." Caption beat 1 crossfades out; caption beat 2 crossfades in:
"Tap. Got it." Right as beat 2 lands, simulate a tap on the shutter
button in this same frame: a brief flash, then the captured-photo
thumbnail pops into the bottom-left corner.
Sequential/interaction: yes — caption crossfade partway through, then
shutter tap → flash accent → thumbnail pop-in, in that order, all within
the one held frame
Audio intent: warm and cheeky through the hold ("aha, that's clever"),
resolving into a satisfying, clean payoff click for the shutter tap
Audio-coupled idea: shutter click synced to a strong cue; thumbnail pop
synced to the next beat
Music: full groove through the hold, then a short pull-down starting
around the shutter tap, heading into the outro
Transition mood: hard → Scene 5

### Scene 5 — Outro / punchline — 2.48s
App icon + "LookHere" wordmark centered on the cream background, primary-
color accent underline. Tagline beneath: "Finally, a good picture." Small
byline, smaller/muted text: "Any phone. Even more fun on a Fold."
Sequential/interaction: none
Audio intent: warm settle, confident close
Audio-coupled idea: none
Music: final fade-out on the last beat
Transition mood: soft (end card, no further transition)

**Music mood for this video:** upbeat
**Audio summary:** A warm, upbeat 120 BPM bed carries the whole video at
steady energy, swelling slightly into the reveal and pulling down cleanly
into the outro; sparse, motion-matched UI/shutter SFX mark the typed
search, shutter tap, and thumbnail pop-in, timed to the bundled track's
strong cues around 16-20s for the hero hold's payoff.
