# LookHere — Color Palette

Colors pulled directly from the app logo. Use these for the light and dark
themes.

## Light Mode

| Role         | Hex       | Usage                                              |
|--------------|-----------|-----------------------------------------------------|
| Background   | `#FFF8F1` | App background                                     |
| Surface      | `#FFFFFF` | Cards, sheets, elevated surfaces                   |
| Primary      | `#FD594D` | App bar, primary buttons, shutter button           |
| On Primary   | `#FFFFFF` | Text/icons on top of Primary                       |
| Secondary    | `#FBB748` | Highlights, selected states, badges                |
| On Secondary | `#402800` | Text/icons on top of Secondary                     |
| Tertiary     | `#2F8F87` | Secondary actions, GIF-picker UI accents           |
| On Tertiary  | `#FFFFFF` | Text/icons on top of Tertiary                      |
| Accent       | `#FB9F3A` | Sparing emphasis — active indicator, small icon    |
| Text Primary | `#241512` | Main body/heading text                             |
| Text Secondary | `#7A655D` | Secondary/muted text                             |
| Error        | `#B23A48` | Error states                                       |

## Dark Mode

| Role         | Hex       | Usage                                              |
|--------------|-----------|-----------------------------------------------------|
| Background   | `#18120F` | App background                                     |
| Surface      | `#241B18` | Cards, sheets, elevated surfaces                   |
| Primary      | `#FF8A7B` | App bar, primary buttons, shutter button           |
| On Primary   | `#3D0F08` | Text/icons on top of Primary                       |
| Secondary    | `#FFCE7A` | Highlights, selected states, badges                |
| On Secondary | `#402C00` | Text/icons on top of Secondary                     |
| Tertiary     | `#5FC2BA` | Secondary actions, GIF-picker UI accents           |
| On Tertiary  | `#00332F` | Text/icons on top of Tertiary                      |
| Accent       | `#FFB35C` | Sparing emphasis — active indicator, small icon    |
| Text Primary | `#F5E9E3` | Main body/heading text                             |
| Text Secondary | `#C2AFA7` | Secondary/muted text                             |
| Error        | `#FF9A9E` | Error states                                       |

## Notes

- Dark mode brand colors (Primary/Secondary/Tertiary/Accent) are lightened
  versions of the light-mode hues, not straight inverts — fully saturated
  logo colors look muddy against a dark background otherwise.
- Backgrounds are warm off-white / warm near-black rather than pure
  white/black, to stay consistent with the coral/yellow warmth of the logo.
- Error is a distinct brick-red in both modes, kept far enough from Primary
  (coral) that error states don't visually blend into the brand color.

## Android colors.xml

```xml
<!-- res/values/colors.xml (light) -->
<resources>
    <color name="background">#FFF8F1</color>
    <color name="surface">#FFFFFF</color>
    <color name="primary">#FD594D</color>
    <color name="on_primary">#FFFFFF</color>
    <color name="secondary">#FBB748</color>
    <color name="on_secondary">#402800</color>
    <color name="tertiary">#2F8F87</color>
    <color name="on_tertiary">#FFFFFF</color>
    <color name="accent">#FB9F3A</color>
    <color name="text_primary">#241512</color>
    <color name="text_secondary">#7A655D</color>
    <color name="error">#B23A48</color>
</resources>
```

```xml
<!-- res/values-night/colors.xml (dark) -->
<resources>
    <color name="background">#18120F</color>
    <color name="surface">#241B18</color>
    <color name="primary">#FF8A7B</color>
    <color name="on_primary">#3D0F08</color>
    <color name="secondary">#FFCE7A</color>
    <color name="on_secondary">#402C00</color>
    <color name="tertiary">#5FC2BA</color>
    <color name="on_tertiary">#00332F</color>
    <color name="accent">#FFB35C</color>
    <color name="text_primary">#F5E9E3</color>
    <color name="text_secondary">#C2AFA7</color>
    <color name="error">#FF9A9E</color>
</resources>
```

## Jetpack Compose

```kotlin
val LightColors = lightColorScheme(
    background = Color(0xFFFFF8F1),
    surface = Color(0xFFFFFFFF),
    primary = Color(0xFFFD594D),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFFBB748),
    onSecondary = Color(0xFF402800),
    tertiary = Color(0xFF2F8F87),
    onTertiary = Color(0xFFFFFFFF),
    error = Color(0xFFB23A48),
)

val DarkColors = darkColorScheme(
    background = Color(0xFF18120F),
    surface = Color(0xFF241B18),
    primary = Color(0xFFFF8A7B),
    onPrimary = Color(0xFF3D0F08),
    secondary = Color(0xFFFFCE7A),
    onSecondary = Color(0xFF402C00),
    tertiary = Color(0xFF5FC2BA),
    onTertiary = Color(0xFF00332F),
    error = Color(0xFFFF9A9E),
)
```
