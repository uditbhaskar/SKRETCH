# SKRETCH

[![](https://jitpack.io/v/uditbhaskar/SKRETCH.svg)](https://jitpack.io/#uditbhaskar/SKRETCH)

Scratch cards for Jetpack Compose — two customizable surfaces, brush styles, presets, and reveal controls.

## Install

```kotlin
maven { url = uri("https://jitpack.io") }
implementation("com.github.uditbhaskar.SKRETCH:scratch:0.1.0")
```

Or `implementation(projects.scratch)` in a multi-module project.

**Author:** [uditbhaskar](https://github.com/uditbhaskar)

## Quick usage

```kotlin
val state = rememberScratchState()

ScratchCard(
    preset = ScratchPresets.wallet(rewardTitle = "₹50"),
    state = state,
    onRevealed = { },
)

// Or fully custom:
ScratchCard(
    scratchLayer = ScratchLayerConfig(
        pattern = ScratchCoverPattern.Holographic,
        text = ScratchSurfaceText("SCRATCH"),
        shimmer = true,
        // image = myBitmap,  // or custom = { MyCover() }
    ),
    mainLayer = MainLayerConfig(
        text = MainLayerText("You won!", "Cashback unlocked"),
    ),
    brush = ScratchBrush.smooth(width = 56.dp, hardness = 0.3f),
    chrome = ScratchCardChrome(shape = ScratchCardShape.Ticket),
    revealThreshold = RevealThreshold.of(0.45f),
    revealAnimation = ScratchRevealAnimation.ScalePop,
    hapticIntensity = ScratchHapticIntensity.Strong,
    autoReveal = true,
    multiTouchEnabled = false,
    state = state,
)
```

Default brush diameter is **52dp** (`ScratchConstants.DEFAULT_BRUSH_WIDTH_DP`) when you omit `width` on `ScratchBrush` factories.

## Features

| Area | Options |
|------|---------|
| Scratch layer | color, text, patterns (`Silver/Gold/Matte/Holographic/Grain`), image, shimmer, custom composable |
| Main layer | color, text, custom composable |
| Brush | Circular / Smooth / Hairy + width (default 52dp) + hardness |
| Chrome | elevation, border, shapes (`RoundedRect/Circle/Ticket`) |
| Reveal | `RevealThreshold`, Fade / ScalePop / None, `autoReveal` + `state.reveal()` |
| State | `rememberScratchState()` persists coverage, reveal, and brush across config changes; also `snapshot()` / `restore()` / `reset()` |
| Feedback | haptic intensity, optional sound hooks |
| A11y | content descriptions + TalkBack “Reveal reward” action |
| UI helpers | presets (`promo` / `wallet` / `game` / `matte` / `party` / `minimal`) |

### Presets at a glance

| Preset | Cover | Brush | Shape |
|--------|-------|-------|-------|
| `promo` | Silver | Circular | RoundedRect |
| `wallet` | Gold + shimmer | Circular | RoundedRect |
| `game` | Holographic + shimmer | Smooth | Ticket |
| `matte` | Matte | Circular | RoundedRect |
| `party` | Grain + shimmer | Hairy | Circle |
| `minimal` | Silver | Smooth (thin) | RoundedRect |

## Demo

Run `:app` for an interactive catalog:

- Main screen: live card + Pattern / Brush / Shape / Extras chips
- **Browse presets** opens a separate screen to pick a ready-made look (`ScratchPresetsScreen`)

```bash
./gradlew :app:assembleDebug
./gradlew :scratch:test
```

## License

MIT — see [LICENSE](LICENSE).

Compose Multiplatform and Glance widgets are intentionally out of scope for this Android library release.
