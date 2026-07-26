# SKRETCH

[![](https://jitpack.io/v/uditbhaskar/SKRETCH.svg)](https://jitpack.io/#uditbhaskar/SKRETCH)

Jetpack Compose scratch cards for promos and rewards. Every look and feel is configurable: cover, reward, brush, chrome, reveal, haptics, sound, and accessibility.

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

### Seeing KDocs in Android Studio

Sources ship with the library. After syncing:

1. Open **External Libraries** → `SKRETCH-0.1.3`
2. Right-click → **Download Sources** (if needed)
3. Cmd/Ctrl+click any type to open source with comments

Hover a parameter and press **F1** / **Quick Documentation** for the same KDoc.

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
| `shimmer` | `Boolean` | `false` | Light sweep across unused cover |
| `custom` | `(@Composable () -> Unit)?` | `null` | Fully custom cover (highest priority) |

**Draw priority:** `custom` > `image` > `pattern` + `color` + `text`.

#### `ScratchCoverPattern`

| Value | Look |
|-------|------|
| `Silver` | Cool metallic silver |
| `Gold` | Warm metallic gold |
| `Matte` | Flat low-sheen gray |
| `Holographic` | Iridescent multi-hue |
| `Grain` | Soft paper-like grain |

#### `ScratchSurfaceText` (cover hint only)

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `text` | `String` | *(required)* | Label content |
| `color` | `Color` | translucent gray | Text color |
| `fontSize` | `TextUnit` | `Unspecified` | Unspecified → sized from layer width |

Companion: `ScratchSurfaceText.DefaultScratchHint` → `"SCRATCH HERE"`.

For reward title/subtitle use `MainLayerText`, not this type.

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

#### `ScratchBrushStyle`

| Value | Feel |
|-------|------|
| `Circular` | Hard round stamp |
| `Smooth` | Soft round stamp, feathered edges |
| `Hairy` | Irregular bristly clusters |

#### Factories / presets

| API | Defaults |
|-----|----------|
| `ScratchBrush.Circular` | Circular, 52.dp, hardness `0.65f` |
| `ScratchBrush.Smooth` | Smooth, 52.dp, hardness `0.35f` |
| `ScratchBrush.Hairy` | Hairy, 52.dp, default hardness |
| `ScratchBrush.circular(width, hardness)` | `width = 52.dp`, `hardness = 1f` |
| `ScratchBrush.smooth(width, hardness)` | `width = 52.dp`, `hardness = 0.35f` |
| `ScratchBrush.hairy(width, hardness)` | `width = 52.dp`, `hardness = 0.5f` |

```kotlin
ScratchBrush.circular()
ScratchBrush.smooth(width = 56.dp, hardness = 0.3f)
ScratchBrush.hairy(width = 64.dp)
```

---

### 5. Frame — `ScratchCardChrome`

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `elevation` | `Dp` | `8.dp` | Card shadow |
| `borderWidth` | `Dp` | `0.5.dp` | Stroke width |
| `borderColor` | `Color` | translucent gray | Stroke color |
| `shape` | `ScratchCardShape` | `RoundedRect` | Outline family |
| `cornerRadius` | `Dp` | `12.dp` | Used by `RoundedRect` and `Ticket` |

#### `ScratchCardShape`

| Value | Outline |
|-------|---------|
| `RoundedRect` | Rounded rectangle using `cornerRadius` |
| `Circle` | True circle inscribed in the bounds (works when width ≠ height) |
| `Ticket` | Ticket outline with uneven corner radii |

```kotlin
ScratchCardChrome(
    shape = ScratchCardShape.Ticket,
    cornerRadius = 18.dp,
    elevation = 10.dp,
    borderWidth = 1.dp,
    borderColor = Color.Black.copy(alpha = 0.2f),
)
```

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

### 7. Haptics — `ScratchHapticIntensity`

| Value | Meaning |
|-------|---------|
| `Off` | No haptic |
| `Light` | Subtle tick |
| `Medium` | Standard first-scratch pulse (default) |
| `Strong` | Stronger confirm pulse |

Fired once on the first scratch (not continuously while dragging).

---

### 8. Sound — `ScratchSoundConfig`

The library ships **no** audio assets. Wire your own players.

| Property | Type | Default | Meaning |
|----------|------|---------|---------|
| `enabled` | `Boolean` | `false` | When `false`, callbacks are never invoked |
| `onScratchStarted` | `(() -> Unit)?` | `null` | Once on first scratch drag when enabled |
| `onRevealed` | `(() -> Unit)?` | `null` | When the card reveals when enabled |

Companion: `ScratchSoundConfig.Off`.

```kotlin
ScratchSoundConfig(
    enabled = true,
    onScratchStarted = { /* play scratch SFX */ },
    onRevealed = { /* play reveal SFX */ },
)
```

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

## License

MIT. See [LICENSE](LICENSE).
