# SKRETCH

[![](https://jitpack.io/v/uditbhaskar/SKRETCH.svg)](https://jitpack.io/#uditbhaskar/SKRETCH)

Jetpack Compose scratch cards for promos and rewards. Every look and feel is configurable: cover, reward, brush, chrome, reveal, haptics, sound, particles, and accessibility.

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
    implementation("com.github.uditbhaskar:SKRETCH:0.1.3")
}
```

## Demo

https://github.com/user-attachments/assets/bca137a7-0787-471a-8a8a-32b7ccf1c141

## Quick start

```kotlin
val state = rememberScratchState()

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

Custom reward UI (trailing lambda — same as `mainLayer = MainLayerConfig(custom = …)`):

```kotlin
ScratchCard(
    scratchLayer = ScratchLayerConfig(pattern = ScratchCoverPattern.Gold),
    brush = ScratchBrush.circular(),
    state = state,
) {
    Text("₹100")
}
```

Optional shortcut: `ScratchCard(preset = ScratchPresets.wallet(…))` — see [Presets](#presets-optional).

---

## Customization reference

Everything a consumer can change is listed below.

### 1. `ScratchCard` — entry points

Three overloads:

| Overload | Use when |
|----------|----------|
| Full config | You pass `scratchLayer`, `mainLayer`, `brush`, `chrome`, … |
| Trailing `content` | Same as full config, but reward is the trailing `@Composable` |
| `preset = …` | Start from a `ScratchCardPreset` (still override sound / a11y / state / callbacks) |

#### Full / trailing-content parameters

| Parameter | Type | Default | Meaning |
|-----------|------|---------|---------|
| `modifier` | `Modifier` | `Modifier` | Applied to the card |
| `scratchLayer` | `ScratchLayerConfig` | `ScratchLayerConfig.Default` | Cover the user scratches away |
| `mainLayer` | `MainLayerConfig` | `MainLayerConfig.Default` | Revealed surface (full overload only) |
| `content` | `@Composable () -> Unit` | *(required)* | Revealed surface (trailing overload only) |
| `brush` | `ScratchBrush` | `ScratchBrush.Circular` | Erase style, size, hardness |
| `chrome` | `ScratchCardChrome` | `ScratchCardChrome.Default` | Shape, border, elevation |
| `revealThreshold` | `RevealThreshold` | `RevealThreshold.Default` (`0.45f`) | Coverage before auto-reveal |
| `revealAnimation` | `ScratchRevealAnimation` | `Fade` | How the cover disappears |
| `hapticIntensity` | `ScratchHapticIntensity` | `Medium` | First-scratch vibration |
| `sound` | `ScratchSoundConfig` | `Off` | Optional sound callbacks |
| `accessibility` | `ScratchAccessibility` | `Default` | TalkBack strings + reveal action |
| `autoReveal` | `Boolean` | `true` | If `false`, call `state.reveal()` yourself |
| `multiTouchEnabled` | `Boolean` | `false` | All active pointers scratch when `true` |
| `enabled` | `Boolean` | `true` | Gestures on/off |
| `state` | `ScratchState?` | `null` (remembered internally) | Hoist with `rememberScratchState()` |
| `onScratchStarted` | `() -> Unit` | `{}` | First time the user starts scratching |
| `onScratchProgress` | `(Float) -> Unit` | `{}` | Coverage `0f..1f` (reports `1f` after reveal) |
| `onRevealed` | `() -> Unit` | `{}` | Once when the card reveals |

#### Preset overload parameters

| Parameter | Type | Default | Meaning |
|-----------|------|---------|---------|
| `preset` | `ScratchCardPreset` | *(required)* | Bundled layers / brush / chrome / reveal / haptic / autoReveal |
| `modifier` | `Modifier` | `Modifier` | Applied to the card |
| `state` | `ScratchState?` | `null` | Optional hoisted state |
| `enabled` | `Boolean` | `true` | Gestures on/off |
| `multiTouchEnabled` | `Boolean` | `false` | Multi-finger scratch |
| `sound` | `ScratchSoundConfig` | `Off` | Sound callbacks |
| `accessibility` | `ScratchAccessibility` | `Default` | TalkBack |
| `onScratchStarted` | `() -> Unit` | `{}` | First scratch |
| `onScratchProgress` | `(Float) -> Unit` | `{}` | Coverage |
| `onRevealed` | `() -> Unit` | `{}` | Revealed |

---

### 2. Cover — `ScratchLayerConfig`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `color` | `Color` | mid silver (`#C4C9D1`) | Tint mixed into procedural pattern; ignored if `image` / `custom` set |
| `text` | `ScratchSurfaceText?` | `"SCRATCH HERE"` hint | Optional label on the foil |
| `pattern` | `ScratchCoverPattern` | `Silver` | Built-in foil when `image` / `custom` are null |
| `image` | `ImageBitmap?` | `null` | Bitmap cover (wins over pattern) |
| `shimmer` | `Boolean` | `true` | Light sweep across unused cover |
| `sparkle` | `Boolean` | `false` | Optional sparkle loop on unused cover |
| `custom` | `(@Composable () -> Unit)?` | `null` | Fully custom cover (video / Lottie / anything) |

**Draw priority:** `custom` > `image` > `pattern` + `color` + `text`.

For video covers use `ScratchVideoCover(uri)` inside `custom`. Lottie is not bundled; wrap your own Lottie composable the same way.

#### `ScratchCoverPattern`

`Silver`, `Gold`, `Matte`, `Holographic`, `Grain`, `Bronze`, `RoseGold`, `Neon`, `Confetti`.

#### QR / barcode peek

Put `ScratchBarcodeReward(code = …, qrBitmap = …)` (or your own QR composable) in `MainLayerConfig.custom` so the code peeks through as the user scratches. `scratchDemoQrBitmap()` is a demo placeholder only; use a real QR encoder in production.
---

### 3. Reward — `MainLayerConfig`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `color` | `Color` | `Color.White` | Background when `custom` is null |
| `text` | `MainLayerText?` | `null` | Built-in title / subtitle |
| `custom` | `(@Composable () -> Unit)?` | `null` | Fully custom reward UI (replaces color + text) |

#### `MainLayerText`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `title` | `String` | *(required)* | Primary reward line |
| `subtitle` | `String?` | `null` | Optional supporting line |
| `titleColor` | `Color` | `#1A73E8` | Title color |
| `subtitleColor` | `Color` | `#5F6368` | Subtitle color |

```kotlin
MainLayerConfig(
    color = Color(0xFFFFF8E7),
    text = MainLayerText(
        title = "₹50",
        subtitle = "Added to wallet",
        titleColor = Color(0xFFB0892E),
        subtitleColor = Color(0xFF5F6368),
    ),
)
```

---

### 4. Brush — `ScratchBrush`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `style` | `ScratchBrushStyle` | `Circular` | Stamp shape while dragging |
| `width` | `Dp` | **52.dp** | Brush diameter |
| `hardness` | `Float` | `0.65f` | Soft (`0f`) → hard (`1f`); mainly affects `Smooth` |
| `velocityResponsive` | `Boolean` | `false` | Faster swipes widen the trail |
| `velocityMinScale` / `velocityMaxScale` | `Float` | `0.75` / `1.65` | Scale range when velocity is on |

Styles: `Circular`, `Smooth`, `Hairy`, `Glitter`.

Factories: `circular()`, `smooth()`, `hairy()`, `glitter()` (each accepts `velocityResponsive`).
---

### 5. Frame — `ScratchCardChrome`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `elevation` | `Dp` | `8.dp` | Card shadow |
| `borderWidth` | `Dp` | `0.5.dp` | Stroke width |
| `borderColor` | `Color` | translucent gray | Stroke color |
| `shape` | `ScratchCardShape` | `RoundedRect` | Outline family |
| `cornerRadius` | `Dp` | `12.dp` | Used by `RoundedRect` and `Ticket` |
| `glowColor` | `Color` | teal | Neon glow color |
| `glowWidth` | `Dp` | `0.dp` | Outer glow; `0.dp` disables |
| `tiltEnabled` | `Boolean` | `false` | 3D parallax toward the finger |
| `tiltDegrees` | `Float` | `8f` | Max tilt when enabled |

Shapes: `RoundedRect`, `Circle`, `Ticket`.
---

### 6. Reveal

| API | Options | Meaning |
|-----|---------|---------|
| `RevealThreshold.of(fraction)` | `0f..1f` (clamped) | Coverage needed for auto-reveal |
| `RevealThreshold.Default` | `0.45f` | Library default |
| `ScratchRevealAnimation` | `Fade`, `ScalePop`, `None` | How the cover disappears |
| `autoReveal` | `true` / `false` | If `false`, call `state.reveal()` yourself |

```kotlin
val state = rememberScratchState(autoReveal = false)

ScratchCard(
    scratchLayer = ScratchLayerConfig(),
    mainLayer = MainLayerConfig(text = MainLayerText("Reward")),
    state = state,
    autoReveal = false,
    revealThreshold = RevealThreshold.of(0.6f),
    revealAnimation = ScratchRevealAnimation.ScalePop,
)

// When your own logic says so:
state.reveal()
```

---

### 7. Haptics / particles / sound

| API | Notes |
|-----|-------|
| `ScratchHapticIntensity` | `Off`, `Light`, `Medium`, `Strong` |
| `ScratchHapticMode` | `FirstTouch` or `Continuous` (default) drag ticks |
| `particlesEnabled` | Foil flake bursts under the finger (default `true`) |
| `ScratchSoundConfig.BuiltIn` | Packaged scratch + reveal WAV samples |
| `ScratchSoundConfig.Off` | No audio |

Callbacks on `ScratchSoundConfig` still fire when `enabled = true` if you want your own players too.
---

### 9. Accessibility — `ScratchAccessibility`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `coverContentDescription` | `String` | `"Scratch card cover. Scratch to reveal the reward."` | TalkBack while cover present |
| `revealedContentDescription` | `String` | `"Scratch card reward revealed."` | TalkBack after reveal |
| `revealActionLabel` | `String` | `"Reveal reward"` | Custom action that forces reveal |
| `announceOnReveal` | `String` | `"Reward revealed."` | Spoken announcement on reveal |

Companion: `ScratchAccessibility.Default`.

---

### 10. Interaction flags

| Parameter | Default | Meaning |
|-----------|---------|---------|
| `enabled` | `true` | When `false`, scratch gestures are ignored |
| `multiTouchEnabled` | `false` | When `true`, every active pointer scratches |
| `autoReveal` | `true` | See [Reveal](#6-reveal) |

---

### 11. Callbacks

| Callback | Signature | When |
|----------|-----------|------|
| `onScratchStarted` | `() -> Unit` | First time the user starts scratching |
| `onScratchProgress` | `(Float) -> Unit` | Coverage changes; `1f` after reveal |
| `onRevealed` | `() -> Unit` | Once when the card reveals |

---

### 12. State — `rememberScratchState` / `ScratchState`

```kotlin
val state = rememberScratchState(
    revealThreshold = RevealThreshold.Default,
    autoReveal = true,
)
```

Coverage and brush settings survive process death / config changes when you use `rememberScratchState()`.

#### Readable

| Property | Type | Meaning |
|----------|------|---------|
| `scratchProgress` | `Float` | Coverage `0f..1f` (snaps to `1f` after reveal when auto-reveal is on) |
| `isRevealed` | `Boolean` | Cover has been revealed |
| `hasStarted` | `Boolean` | User started scratching at least once |

#### Control

| Method | Meaning |
|--------|---------|
| `reveal()` | Force reveal; progress → `1f` |
| `reset()` | Clear progress / reveal and start over |
| `snapshot()` | Capture state for your own persistence |
| `restore(ScratchStateSnapshot)` | Restore from a snapshot |

You can also update threshold / auto-reveal at runtime via `updateRevealThreshold(…)` and `updateAutoReveal(…)`.

---

### 13. `ScratchCardPreset` (for the preset overload)

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `scratchLayer` | `ScratchLayerConfig` | *(required)* | Cover |
| `mainLayer` | `MainLayerConfig` | *(required)* | Reward |
| `brush` | `ScratchBrush` | *(required)* | Brush |
| `chrome` | `ScratchCardChrome` | `Default` | Frame |
| `revealThreshold` | `RevealThreshold` | `Default` | Auto-reveal coverage |
| `revealAnimation` | `ScratchRevealAnimation` | `Fade` | Cover exit |
| `hapticIntensity` | `ScratchHapticIntensity` | `Medium` | First-scratch haptic |
| `autoReveal` | `Boolean` | `true` | Auto-reveal on/off |

Build your own:

```kotlin
ScratchCard(
    preset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(pattern = ScratchCoverPattern.Matte),
        mainLayer = MainLayerConfig(text = MainLayerText("Offer")),
        brush = ScratchBrush.circular(width = 48.dp),
        chrome = ScratchCardChrome(cornerRadius = 16.dp),
        revealAnimation = ScratchRevealAnimation.Fade,
        hapticIntensity = ScratchHapticIntensity.Light,
    ),
)
```

---

## Presets (optional)

`ScratchPresets` factories return a `ScratchCardPreset`. Override `rewardTitle` / `rewardSubtitle` on each.

| Factory | Character |
|---------|-----------|
| `promo()` | Classic silver |
| `wallet()` | Gold + shimmer + ScalePop |
| `game()` | Holographic + ticket shape |
| `matte()` | Quiet matte |
| `party()` | Circle + grain + hairy brush |
| `minimal()` | Thin brush, no reveal animation / haptic |

```kotlin
ScratchCard(
    preset = ScratchPresets.wallet(rewardTitle = "₹50"),
    state = state,
    onRevealed = { /* claim */ },
)
```

Prefer the full `ScratchCard(…)` API above when you need exact control.

---

## Sample app

This repo’s `:app` module is a live catalog (patterns, brushes, shapes, presets).

```bash
./gradlew :app:assembleDebug
./gradlew :scratch:test
```

## Issues and contributing

SKRETCH is still in beta. Bugs and rough edges are expected.

**Found a bug or something missing?** Open an [issue](https://github.com/uditbhaskar/SKRETCH/issues) with:

- What you expected
- What actually happened
- Library version (e.g. the tag you depend on)
- A short Compose snippet if you can

**Want to contribute?** PRs are welcome.

1. Fork the repo and create a branch
2. Make your change in `:scratch` (and `:app` if the demo needs an update)
3. Run `./gradlew :scratch:test` and `:app:assembleDebug`
4. Open a PR with a short description of what changed and why

Keep PRs focused. Prefer small fixes and clear API improvements over large refactors.

## License

MIT. See [LICENSE](LICENSE).
