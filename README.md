# SKRETCH

[![](https://jitpack.io/v/uditbhaskar/SKRETCH.svg)](https://jitpack.io/#uditbhaskar/SKRETCH)

A Jetpack Compose scratch card library. Customize the cover and reward layers, pick a brush, or drop in a preset and go.

By [uditbhaskar](https://github.com/uditbhaskar).

## Install

```kotlin
maven { url = uri("https://jitpack.io") }
implementation("com.github.uditbhaskar:SKRETCH:0.1.1")
```

In a multi-module project you can also use `implementation(projects.scratch)`.

## Demo

https://github.com/user-attachments/assets/bca137a7-0787-471a-8a8a-32b7ccf1c141

## Quick usage

```kotlin
val state = rememberScratchState()

ScratchCard(
    preset = ScratchPresets.wallet(rewardTitle = "₹50"),
    state = state,
    onRevealed = { },
)
```

## Customization

Every option below is a public type. In Android Studio, **Navigate to Declaration** (Cmd/Ctrl+B) on a class or property to read its KDoc.

### Scratch layer (cover)

`ScratchLayerConfig` draws what the user scratches away.

| Property | What it does |
|----------|----------------|
| `pattern` | Built-in foil: `Silver`, `Gold`, `Matte`, `Holographic`, `Grain` |
| `color` | Tint mixed into the pattern |
| `text` | Optional cover label (`ScratchSurfaceText`) |
| `image` | Bitmap cover instead of a pattern |
| `shimmer` | Light sweep across unused cover |
| `custom` | Fully custom cover composable |

Priority: `custom` > `image` > `pattern` + `color` + `text`.

```kotlin
ScratchLayerConfig(
    pattern = ScratchCoverPattern.Holographic,
    text = ScratchSurfaceText("SCRATCH", color = Color.White.copy(alpha = 0.5f)),
    shimmer = true,
)
```

### Main layer (reward)

`MainLayerConfig` is what shows under the foil.

| Property | What it does |
|----------|----------------|
| `color` | Background when not using custom content |
| `text` | Title / subtitle (`MainLayerText`) |
| `custom` | Fully custom reward composable |

```kotlin
MainLayerConfig(
    text = MainLayerText(
        title = "You won!",
        subtitle = "Cashback unlocked",
        titleColor = Color(0xFF1A73E8),
    ),
)
```

Or pass a trailing lambda:

```kotlin
ScratchCard(scratchLayer = ScratchLayerConfig(...)) {
    MyRewardUi()
}
```

### Brush

`ScratchBrush` controls how the foil erases.

| Property | What it does |
|----------|----------------|
| `style` | `Circular` (hard), `Smooth` (soft edge), `Hairy` (bristles) |
| `width` | Brush diameter (default **52dp**) |
| `hardness` | Soft to hard (`0f..1f`), mainly for Smooth |

```kotlin
ScratchBrush.smooth(width = 56.dp, hardness = 0.3f)
ScratchBrush.circular()
ScratchBrush.hairy(width = 64.dp)
```

### Chrome (card frame)

`ScratchCardChrome` is the card outline and elevation.

| Property | What it does |
|----------|----------------|
| `shape` | `RoundedRect`, `Circle`, or `Ticket` |
| `cornerRadius` | Used by RoundedRect and Ticket |
| `elevation` | Shadow depth |
| `borderWidth` / `borderColor` | Outline stroke |

### Reveal, haptics, sound, a11y

| Type | Role |
|------|------|
| `RevealThreshold.of(0.45f)` | How much must be scratched before auto-reveal |
| `ScratchRevealAnimation` | `Fade`, `ScalePop`, or `None` |
| `ScratchHapticIntensity` | `Off`, `Light`, `Medium`, `Strong` |
| `autoReveal` | When `false`, call `state.reveal()` yourself |
| `ScratchSoundConfig` | Optional start / reveal sound callbacks (you supply audio) |
| `ScratchAccessibility` | TalkBack descriptions + "Reveal reward" action |

### State

```kotlin
val state = rememberScratchState(
    revealThreshold = RevealThreshold.Default,
    autoReveal = true,
)

state.scratchProgress  // 0f..1f
state.isRevealed
state.hasStarted
state.reset()          // clear foil and start over
state.reveal()         // force reveal (e.g. after server confirm)
```

### Full custom example

```kotlin
ScratchCard(
    scratchLayer = ScratchLayerConfig(
        pattern = ScratchCoverPattern.Holographic,
        text = ScratchSurfaceText("SCRATCH"),
        shimmer = true,
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
    onRevealed = { },
)
```

### Presets

`ScratchPresets` returns a `ScratchCardPreset` (layers + brush + chrome + reveal/haptic defaults).

| Preset | Cover | Brush | Shape |
|--------|-------|-------|-------|
| `promo` | Silver | Circular | RoundedRect |
| `wallet` | Gold + shimmer | Smooth | RoundedRect |
| `game` | Holographic + shimmer | Smooth | Ticket |
| `matte` | Matte | Circular | RoundedRect |
| `party` | Grain + shimmer | Hairy | Circle |
| `minimal` | Silver | Smooth (thin) | RoundedRect |

```kotlin
ScratchCard(preset = ScratchPresets.game(rewardTitle = "Bonus unlocked"))
```

## Sample app

The `:app` module in this repo is an interactive catalog (patterns, brushes, shapes, presets).

```bash
./gradlew :app:assembleDebug
./gradlew :scratch:test
```

## License

MIT. See [LICENSE](LICENSE).
