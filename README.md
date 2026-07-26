# SKRETCH

[![](https://jitpack.io/v/uditbhaskar/SKRETCH.svg)](https://jitpack.io/#uditbhaskar/SKRETCH)

Jetpack Compose scratch cards for promos and rewards. Custom foil, brushes, shapes, and ready-made presets.

By [uditbhaskar](https://github.com/uditbhaskar).

## Install

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencies {
    implementation("com.github.uditbhaskar:SKRETCH:0.1.2")
}
```

### Seeing KDocs in Android Studio

Sources ship with the library. After syncing:

1. Open **External Libraries** → `SKRETCH-0.1.2`
2. Right-click → **Download Sources** (if needed)
3. Cmd/Ctrl+click any type (`ScratchLayerConfig`, `ScratchBrush`, …) to open the source with comments

Hover a parameter and press **F1** / **Quick Documentation** to read the same KDoc without leaving your file.

## Demo

https://github.com/user-attachments/assets/bca137a7-0787-471a-8a8a-32b7ccf1c141

## Quick start

### Preset (fastest)

```kotlin
val state = rememberScratchState()

ScratchCard(
    preset = ScratchPresets.wallet(rewardTitle = "₹50"),
    state = state,
    onRevealed = { /* claim reward */ },
)
```

### Fully custom

```kotlin
ScratchCard(
    scratchLayer = ScratchLayerConfig(
        pattern = ScratchCoverPattern.Holographic,
        text = ScratchSurfaceText("SCRATCH"),
        shimmer = true,
    ),
    mainLayer = MainLayerConfig(
        text = MainLayerText(
            title = "You won!",
            subtitle = "Cashback unlocked",
        ),
    ),
    brush = ScratchBrush.smooth(width = 56.dp, hardness = 0.3f),
    chrome = ScratchCardChrome(
        shape = ScratchCardShape.Ticket,
        cornerRadius = 18.dp,
        elevation = 8.dp,
    ),
    revealThreshold = RevealThreshold.of(0.45f),
    revealAnimation = ScratchRevealAnimation.ScalePop,
    hapticIntensity = ScratchHapticIntensity.Strong,
    autoReveal = true,
    multiTouchEnabled = false,
    state = state,
    onScratchStarted = { },
    onScratchProgress = { progress -> },
    onRevealed = { },
)
```

### Custom reward UI (trailing lambda)

```kotlin
ScratchCard(
    scratchLayer = ScratchLayerConfig(pattern = ScratchCoverPattern.Gold),
    brush = ScratchBrush.circular(),
    state = state,
) {
    // Your composable under the foil
    Text("₹100")
}
```

## API reference

### `ScratchCard` parameters

| Parameter | Type | Default | Meaning |
|-----------|------|---------|---------|
| `scratchLayer` | `ScratchLayerConfig` | silver foil | What the user scratches away |
| `mainLayer` | `MainLayerConfig` | white | What shows underneath |
| `brush` | `ScratchBrush` | circular 52dp | How the foil is erased |
| `chrome` | `ScratchCardChrome` | rounded rect | Shape, border, elevation |
| `revealThreshold` | `RevealThreshold` | `0.45` | Fraction scratched before auto-reveal |
| `revealAnimation` | `ScratchRevealAnimation` | `Fade` | How the cover disappears |
| `hapticIntensity` | `ScratchHapticIntensity` | `Medium` | First-scratch vibration |
| `sound` | `ScratchSoundConfig` | off | Optional sound callbacks |
| `accessibility` | `ScratchAccessibility` | English defaults | TalkBack strings + reveal action |
| `autoReveal` | `Boolean` | `true` | If `false`, call `state.reveal()` yourself |
| `multiTouchEnabled` | `Boolean` | `false` | All fingers scratch when true |
| `enabled` | `Boolean` | `true` | Gestures on/off |
| `state` | `ScratchState?` | remembered | Hoist with `rememberScratchState()` |
| `onScratchStarted` | `() -> Unit` | no-op | First drag |
| `onScratchProgress` | `(Float) -> Unit` | no-op | Coverage `0f..1f` |
| `onRevealed` | `() -> Unit` | no-op | Once when revealed |

Preset overload: `ScratchCard(preset = …)` fills layers/brush/chrome/reveal/haptics from `ScratchCardPreset`. You can still pass `sound`, `accessibility`, `state`, and callbacks.

---

### Cover: `ScratchLayerConfig`

What the user scratches.

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `pattern` | `ScratchCoverPattern` | `Silver` | Built-in foil look |
| `color` | `Color` | mid silver | Tint mixed into the pattern |
| `text` | `ScratchSurfaceText?` | "SCRATCH HERE" | Hint label on the foil |
| `image` | `ImageBitmap?` | `null` | Bitmap cover instead of pattern |
| `shimmer` | `Boolean` | `false` | Light sweep on unused cover |
| `custom` | `@Composable (() -> Unit)?` | `null` | Fully custom cover |

**Priority:** `custom` > `image` > `pattern` + `color` + `text`.

`ScratchCoverPattern`: `Silver`, `Gold`, `Matte`, `Holographic`, `Grain`.

`ScratchSurfaceText(text, color, fontSize)` is **cover-only**. For reward title/subtitle use `MainLayerText`.

```kotlin
ScratchLayerConfig(
    pattern = ScratchCoverPattern.Gold,
    color = Color(0xFFFFD54F),
    text = ScratchSurfaceText(
        text = "SCRATCH TO CLAIM",
        color = Color.Black.copy(alpha = 0.35f),
    ),
    shimmer = true,
)
```

---

### Reward: `MainLayerConfig`

What sits under the foil.

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `color` | `Color` | white | Background if no custom content |
| `text` | `MainLayerText?` | `null` | Built-in title / subtitle |
| `custom` | `@Composable (() -> Unit)?` | `null` | Fully custom reward UI |

`MainLayerText(title, subtitle?, titleColor, subtitleColor)`.

```kotlin
MainLayerConfig(
    color = Color(0xFFFFF8E7),
    text = MainLayerText(
        title = "₹50",
        subtitle = "Added to wallet",
        titleColor = Color(0xFF1A73E8),
        subtitleColor = Color(0xFF5F6368),
    ),
)
```

---

### Brush: `ScratchBrush`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `style` | `ScratchBrushStyle` | `Circular` | Stamp shape |
| `width` | `Dp` | **52.dp** | Brush diameter |
| `hardness` | `Float` | `0.65` | Soft (`0f`) → hard (`1f`); mainly for Smooth |

Styles:

- `Circular` – hard round stamp  
- `Smooth` – soft feathered edge  
- `Hairy` – irregular bristle clusters  

```kotlin
ScratchBrush.circular()
ScratchBrush.smooth(width = 56.dp, hardness = 0.3f)
ScratchBrush.hairy(width = 64.dp)
```

---

### Frame: `ScratchCardChrome`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `shape` | `ScratchCardShape` | `RoundedRect` | Outline |
| `cornerRadius` | `Dp` | `12.dp` | For RoundedRect / Ticket |
| `elevation` | `Dp` | `8.dp` | Shadow |
| `borderWidth` | `Dp` | `0.5.dp` | Stroke width |
| `borderColor` | `Color` | translucent gray | Stroke color |

Shapes: `RoundedRect`, `Circle` (true circle in the bounds), `Ticket`.

---

### Reveal & feedback

| API | Options / notes |
|-----|-----------------|
| `RevealThreshold.of(0.45f)` | `0f..1f` coverage needed for auto-reveal |
| `ScratchRevealAnimation` | `Fade`, `ScalePop`, `None` |
| `ScratchHapticIntensity` | `Off`, `Light`, `Medium`, `Strong` |
| `autoReveal = false` | Gate reveal yourself with `state.reveal()` |
| `ScratchSoundConfig(enabled, onScratchStarted, onRevealed)` | You own the audio players |
| `ScratchAccessibility(...)` | Cover/revealed descriptions, reveal action label, announce text |

Manual reveal example:

```kotlin
val state = rememberScratchState(autoReveal = false)

ScratchCard(
    preset = ScratchPresets.promo(),
    state = state,
    autoReveal = false,
)

// After your own check:
state.reveal()
```

---

### State: `rememberScratchState` / `ScratchState`

```kotlin
val state = rememberScratchState(
    revealThreshold = RevealThreshold.Default,
    autoReveal = true,
)
```

Readable:

| Property / method | Meaning |
|-------------------|---------|
| `scratchProgress` | `0f..1f` scratched amount |
| `isRevealed` | Cover gone |
| `hasStarted` | User started scratching |
| `reset()` | Clear foil and start over |
| `reveal()` | Force reveal |

Progress survives process recreation when you use `rememberScratchState()` (it saves coverage and brush settings).

---

### Presets: `ScratchPresets`

Each returns a `ScratchCardPreset` (layers + brush + chrome + reveal/haptic defaults).

| Preset | Cover | Brush | Shape |
|--------|-------|-------|-------|
| `promo()` | Silver | Circular | RoundedRect |
| `wallet()` | Gold + shimmer | Smooth 56dp | RoundedRect |
| `game()` | Holographic + shimmer | Smooth 60dp | Ticket |
| `matte()` | Matte | Circular 48dp | RoundedRect |
| `party()` | Grain + shimmer | Hairy 64dp | Circle |
| `minimal()` | Silver | Smooth 36dp | RoundedRect |

```kotlin
ScratchCard(
    preset = ScratchPresets.game(
        rewardTitle = "Bonus unlocked",
        rewardSubtitle = "Keep playing",
    ),
)
```

## Sample app

This repo’s `:app` module is a live catalog (patterns, brushes, shapes, presets).

```bash
./gradlew :app:assembleDebug
./gradlew :scratch:test
```

## License

MIT. See [LICENSE](LICENSE).
