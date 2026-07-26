# SKRETCH

[![](https://jitpack.io/v/uditbhaskar/SKRETCH.svg)](https://jitpack.io/#uditbhaskar/SKRETCH)

A Jetpack Compose scratch card library. Customize the cover and reward layers, pick a brush, or drop in a preset and go.

By [uditbhaskar](https://github.com/uditbhaskar).

## Install

```kotlin
maven { url = uri("https://jitpack.io") }
implementation("com.github.uditbhaskar.SKRETCH:scratch:0.1.0")
```

In a multi-module project you can also use `implementation(projects.scratch)`.

## Quick usage

```kotlin
val state = rememberScratchState()

ScratchCard(
    preset = ScratchPresets.wallet(rewardTitle = "₹50"),
    state = state,
    onRevealed = { },
)

// Fully custom:
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

If you don't pass a brush width, it defaults to 52dp (`ScratchConstants.DEFAULT_BRUSH_WIDTH_DP`).

## What's included

| | |
|--|--|
| Scratch layer | color, text, patterns (Silver, Gold, Matte, Holographic, Grain), image, shimmer, custom composable |
| Main layer | color, text, or your own composable |
| Brush | Circular, Smooth, Hairy (width + hardness) |
| Chrome | elevation, border, RoundedRect / Circle / Ticket |
| Reveal | threshold, Fade / ScalePop / None, autoReveal or `state.reveal()` |
| State | `rememberScratchState()` keeps progress across rotation; also snapshot / restore / reset |
| Extra | haptics, optional sound hooks, TalkBack labels + Reveal action |
| Presets | promo, wallet, game, matte, party, minimal |

### Presets

| Preset | Cover | Brush | Shape |
|--------|-------|-------|-------|
| promo | Silver | Circular | RoundedRect |
| wallet | Gold + shimmer | Circular | RoundedRect |
| game | Holographic + shimmer | Smooth | Ticket |
| matte | Matte | Circular | RoundedRect |
| party | Grain + shimmer | Hairy | Circle |
| minimal | Silver | Smooth (thin) | RoundedRect |

## Demo

Run the `:app` module to try patterns, brushes, shapes, and presets. Presets live on their own screen.

```bash
./gradlew :app:assembleDebug
./gradlew :scratch:test
```

## License

MIT. See [LICENSE](LICENSE).

Compose Multiplatform and Glance are out of scope for this Android release.
