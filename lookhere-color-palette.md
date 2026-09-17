# LookHere — Color Palette

Colors pulled directly from the app logo. Use these for the light and dark
themes.

## Light Mode

| Role         | Hex       | Usage                                              |
|--------------|-----------|-----------------------------------------------------|
| Background   | `#FFF8F1` | App background                                     |
| Surface      | `#FFFFFF` | Cards, sheets, elevated surfaces                   |
| Surface Variant | `#F2E4DC` | Chip/search-bar backgrounds, subtle containers  |
| Primary      | `#FD594D` | App bar, primary buttons, shutter button           |
| On Primary   | `#FFFFFF` | Text/icons on top of Primary                       |
| Primary Container | `#FFDAD4` | Low-emphasis primary containers              |
| On Primary Container | `#410002` | Text/icons on top of Primary Container  |
| Secondary    | `#FBB748` | Highlights, selected states, badges                |
| On Secondary | `#402800` | Text/icons on top of Secondary                     |
| Secondary Container | `#FFDFA6` | Low-emphasis secondary containers          |
| On Secondary Container | `#2A1800` | Text/icons on top of Secondary Container|
| Tertiary     | `#2F8F87` | Secondary actions, GIF-picker UI accents           |
| On Tertiary  | `#FFFFFF` | Text/icons on top of Tertiary                      |
| Tertiary Container | `#B6EEE6` | Low-emphasis tertiary containers            |
| On Tertiary Container | `#00201D` | Text/icons on top of Tertiary Container  |
| Accent       | `#FB9F3A` | Sparing emphasis — active indicator, small icon    |
| Text Primary | `#241512` | Main body/heading text                             |
| Text Secondary | `#7A655D` | Secondary/muted text                             |
| Outline      | `#8C7A72` | Borders, dividers                                  |
| Outline Variant | `#D6C4BB` | Subtle borders, decorative dividers            |
| Error        | `#B23A48` | Error states                                       |
| On Error     | `#FFFFFF` | Text/icons on top of Error                         |
| Error Container | `#FFDAD9` | Low-emphasis error containers                  |
| On Error Container | `#410008` | Text/icons on top of Error Container     |
| Scrim        | `#000000` | Modal/dialog scrims                                |
| Inverse Surface | `#3A2D28` | Snackbars and other inverted surfaces          |
| Inverse On Surface | `#FFEDE6` | Text/icons on top of Inverse Surface       |
| Inverse Primary | `#FF8A7B` | Primary-toned content on Inverse Surface       |
| Surface Tint | `#FD594D` | Elevation tint (matches Primary)                   |

## Dark Mode

| Role         | Hex       | Usage                                              |
|--------------|-----------|-----------------------------------------------------|
| Background   | `#18120F` | App background                                     |
| Surface      | `#241B18` | Cards, sheets, elevated surfaces                   |
| Surface Variant | `#52443D` | Chip/search-bar backgrounds, subtle containers  |
| Primary      | `#FF8A7B` | App bar, primary buttons, shutter button           |
| On Primary   | `#3D0F08` | Text/icons on top of Primary                       |
| Primary Container | `#5F1207` | Low-emphasis primary containers              |
| On Primary Container | `#FFDAD4` | Text/icons on top of Primary Container  |
| Secondary    | `#FFCE7A` | Highlights, selected states, badges                |
| On Secondary | `#402C00` | Text/icons on top of Secondary                     |
| Secondary Container | `#5C3F00` | Low-emphasis secondary containers          |
| On Secondary Container | `#FFDFA6` | Text/icons on top of Secondary Container|
| Tertiary     | `#5FC2BA` | Secondary actions, GIF-picker UI accents           |
| On Tertiary  | `#00332F` | Text/icons on top of Tertiary                      |
| Tertiary Container | `#004D46` | Low-emphasis tertiary containers            |
| On Tertiary Container | `#B6EEE6` | Text/icons on top of Tertiary Container  |
| Accent       | `#FFB35C` | Sparing emphasis — active indicator, small icon    |
| Text Primary | `#F5E9E3` | Main body/heading text                             |
| Text Secondary | `#C2AFA7` | Secondary/muted text                             |
| Outline      | `#8C7A72` | Borders, dividers                                  |
| Outline Variant | `#52443D` | Subtle borders, decorative dividers            |
| Error        | `#FF9A9E` | Error states                                       |
| On Error     | `#680009` | Text/icons on top of Error                         |
| Error Container | `#930012` | Low-emphasis error containers                  |
| On Error Container | `#FFDAD9` | Text/icons on top of Error Container     |
| Scrim        | `#000000` | Modal/dialog scrims                                |
| Inverse Surface | `#F5E9E3` | Snackbars and other inverted surfaces          |
| Inverse On Surface | `#241512` | Text/icons on top of Inverse Surface       |
| Inverse Primary | `#FD594D` | Primary-toned content on Inverse Surface       |
| Surface Tint | `#FF8A7B` | Elevation tint (matches Primary)                   |

## Notes

- The container/outline/scrim/inverse roles above are derived tonal values
  (pale tints in light mode, dark equivalents in dark mode) following
  standard Material 3 tonal logic, hand-picked to stay consistent with the
  original hand-authored palette rather than run through a seed-color tool
  like Material Theme Builder. Revisit with Material Theme Builder (seeded
  on `#FD594D`) if higher-fidelity tonal values are ever needed.

- Dark mode brand colors (Primary/Secondary/Tertiary/Accent) are lightened
  versions of the light-mode hues, not straight inverts — fully saturated
  logo colors look muddy against a dark background otherwise.
- Backgrounds are warm off-white / warm near-black rather than pure
  white/black, to stay consistent with the coral/yellow warmth of the logo.
- Error is a distinct brick-red in both modes, kept far enough from Primary
  (coral) that error states don't visually blend into the brand color.

## Android colors.xml

The full palette lives in Compose (`ui/theme/Theme.kt`, below) since every
themed surface in the app is Compose. The only entries still needed as XML
resources are the ones the platform itself reads before/outside Compose
(status bar / window background via `themes.xml`, and the splash screen):

```xml
<!-- res/values/colors.xml (light) -->
<resources>
    <color name="background">#FFF8F1</color>

    <!-- Kept constant across light/dark (not overridden in values-night) so the
         splash screen stays on-brand regardless of system theme. -->
    <color name="splash_background">#FD594D</color>
</resources>
```

```xml
<!-- res/values-night/colors.xml (dark) -->
<resources>
    <color name="background">#18120F</color>
</resources>
```

## Jetpack Compose

`onPrimary`/`onSecondary`/`onTertiary` above are the "On Primary"/"On
Secondary"/"On Tertiary" rows; `Text Primary`/`Text Secondary` map to
`onSurface`/`onSurfaceVariant` in Material 3's role model — there's no
separate `on_background`/`text_primary` role in Compose's `ColorScheme`,
`onBackground` and `onSurface` share the same value here.

```kotlin
val LightColors = lightColorScheme(
    background = Color(0xFFFFF8F1),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF2E4DC),
    primary = Color(0xFFFD594D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFFFBB748),
    onSecondary = Color(0xFF402800),
    secondaryContainer = Color(0xFFFFDFA6),
    onSecondaryContainer = Color(0xFF2A1800),
    tertiary = Color(0xFF2F8F87),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB6EEE6),
    onTertiaryContainer = Color(0xFF00201D),
    error = Color(0xFFB23A48),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD9),
    onErrorContainer = Color(0xFF410008),
    onBackground = Color(0xFF241512),
    onSurface = Color(0xFF241512),
    onSurfaceVariant = Color(0xFF7A655D),
    outline = Color(0xFF8C7A72),
    outlineVariant = Color(0xFFD6C4BB),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF3A2D28),
    inverseOnSurface = Color(0xFFFFEDE6),
    inversePrimary = Color(0xFFFF8A7B),
    surfaceTint = Color(0xFFFD594D),
)

val DarkColors = darkColorScheme(
    background = Color(0xFF18120F),
    surface = Color(0xFF241B18),
    surfaceVariant = Color(0xFF52443D),
    primary = Color(0xFFFF8A7B),
    onPrimary = Color(0xFF3D0F08),
    primaryContainer = Color(0xFF5F1207),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFFFCE7A),
    onSecondary = Color(0xFF402C00),
    secondaryContainer = Color(0xFF5C3F00),
    onSecondaryContainer = Color(0xFFFFDFA6),
    tertiary = Color(0xFF5FC2BA),
    onTertiary = Color(0xFF00332F),
    tertiaryContainer = Color(0xFF004D46),
    onTertiaryContainer = Color(0xFFB6EEE6),
    error = Color(0xFFFF9A9E),
    onError = Color(0xFF680009),
    errorContainer = Color(0xFF930012),
    onErrorContainer = Color(0xFFFFDAD9),
    onBackground = Color(0xFFF5E9E3),
    onSurface = Color(0xFFF5E9E3),
    onSurfaceVariant = Color(0xFFC2AFA7),
    outline = Color(0xFF8C7A72),
    outlineVariant = Color(0xFF52443D),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFF5E9E3),
    inverseOnSurface = Color(0xFF241512),
    inversePrimary = Color(0xFFFD594D),
    surfaceTint = Color(0xFFFF8A7B),
)
```
