# SKRETCH

**GitHub description:** Jetpack Compose scratch-card UI library with foil overlays, drag-to-reveal, and a customizable design system API. Modular Android project.

SKRETCH is a Jetpack Compose library for scratch cards. You drag across a metallic foil layer to reveal whatever is underneath: a reward, a coupon, an image, whatever you put there. Common pattern in promo and game UIs.

**The thing we ship is the library module (`:scratch`).** The `app` module is just a demo. We'll publish the library on JitPack so other Android projects can add it as a dependency.

This README is the main reference for what we're building and how the project is organized. Useful if you're picking the project back up, onboarding someone, or continuing with AI assistance.

## What we're building

The goal is a real component library, not just a one-off demo.

- Drag to scratch off a foil overlay and reveal content below
- Metallic foil, rounded cards, reveal animation
- Customization through tokens, presets, and config objects (foil style, brush, shape, threshold, motion)
- A small showcase app with a preset gallery and a playground to tweak options live

### How it works under the hood

Scratch cards in Compose need isolated blend modes:

1. Draw the hidden content (text, image, coupon, etc.)
2. Draw a foil overlay on top
3. Track the touch path and erase foil with `BlendMode.Clear`
4. Wrap in `Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` so the clear blend doesn't punch through parent UI
5. Track coverage with a grid bucket model (fast, no per-pixel checks)
6. When coverage hits a threshold, fire the reveal callback and optionally animate the foil away

Logic goes in plain Kotlin (`ScratchState`). UI goes in composables (`ScratchCard`, `ScratchOverlay`). Keeps things testable and composables thin.

## Architecture

We follow [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md). Module layout is loosely based on [Now in Android](https://github.com/android/nowinandroid), especially the idea of putting shared UI in a `core` library.

### Target module layout

```
SKRETCH/
├── scratch/                # Publishable Android library (JitPack artifact)
├── app/                    # Demo app only, not published
├── feature-catalog/        # Demo screens (planned)
├── gradle/
│   └── libs.versions.toml
└── docs/
    └── ANDROID_CODING_GUIDELINES.md
```

`:scratch` is the module consumers install. Everything else exists to develop and show it off.

### Dependencies

```
app  →  feature-catalog + scratch
feature-catalog  →  scratch only
scratch  →  Compose + Material3 only
```

Features don't depend on each other or on `app`. `scratch` is a pure UI library: no ViewModel, no DI, no network.

### Where things stand

| Module | Status | Published? |
|--------|--------|------------|
| `:scratch` | Not started | Yes (JitPack) |
| `:app` | Done. Default Compose template for now. | No |
| `:feature-catalog` | Not started | No |

## Planned API

### ScratchCard

```kotlin
@Composable
fun ScratchCard(
    config: ScratchCardConfig = ScratchCardConfig(),
    modifier: Modifier = Modifier,
    onScratchStarted: () -> Unit = {},
    onScratchProgress: (Float) -> Unit = {},
    onRevealed: () -> Unit = {},
    content: @Composable () -> Unit,
    foilContent: (@Composable () -> Unit)? = null,
)
```

### Config and tokens

| Type | What it does |
|------|--------------|
| `ScratchCardConfig` | Main entry point for customization |
| `ScratchFoilStyle` | Gradient, texture, shimmer |
| `ScratchBrushStyle` | Stroke width, cap, softness |
| `ScratchColors` | Foil gradients, borders |
| `ScratchShapes` | Corner radius, clip shapes |
| `ScratchMotion` | Vanish duration, easing |
| `ScratchPresets` | `Classic`, `Gold`, `Minimal`, etc. |

### ScratchState (internal, testable)

```kotlin
class ScratchState {
    fun handleDragStart(offset: Offset)
    fun handleDrag(offset: Offset)
    fun handleDragEnd()
    val scratchProgress: Float   // 0f to 1f
    val isRevealed: Boolean
    fun reset()
}
```

### Package layout for `scratch`

```
com.example.skretch.scratch/
├── component/     # ScratchCard, ScratchOverlay
├── state/         # ScratchState, coverage tracker
├── config/        # ScratchCardConfig, brush, threshold
├── design/        # Tokens and presets
└── util/          # Path builders, grid math
```

## Roadmap

### Phase 1: Foundation
- [ ] Create `:scratch` as an `com.android.library` module (JitPack-ready)
- [ ] Register in `settings.gradle.kts`, wire up `app` dependency
- [ ] Set `group`, `version`, and `maven-publish` config for JitPack
- [ ] `ScratchState` with grid-bucket coverage
- [ ] Basic `ScratchCard` with default silver foil
- [ ] Unit tests for `ScratchState`
- [ ] Swap the `Greeting` placeholder in `MainActivity` for a scratch demo

### Phase 2: Customization
- [ ] `ScratchCardConfig` and `ScratchPresets`
- [ ] Foil variants: gradient, bitmap texture, shimmer
- [ ] Brush variants: round, soft-edge
- [ ] Custom clip shapes
- [ ] Reveal animations: fade, scale, instant
- [ ] `ScratchCardDefaults` (same idea as Material `Defaults`)

### Phase 3: Polish
- [ ] Haptic feedback on scratch start
- [ ] TalkBack / content descriptions
- [ ] Disable scratch when revealed or `enabled = false`
- [ ] Optional progress persistence (`ScratchStateSaver`)

### Phase 4: Showcase
- [ ] Preset gallery in `feature-catalog`
- [ ] Live config playground (brush size, threshold, foil style)
- [ ] `ScreenRoot` / `ScreenContent` split per guidelines
- [ ] Compose UI tests

### Phase 5: Library hardening
- [ ] `@Stable` / `@Immutable` on public config types
- [ ] ProGuard rules if needed
- [ ] KDoc on public API
- [ ] Usage docs in this README

## Coding standards

See [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md) for the full list. The important bits:

- No raw string literals. Use constants or `stringResource(R.string.*)`.
- Screens split into `*ScreenRoot` (ViewModel, nav) and `*ScreenContent` (pure UI).
- ViewModels use `StateFlow` and `handleAction`.
- Dependency versions live in `gradle/libs.versions.toml`.
- Koin in app/features only. Not in `scratch`.

## Publishing (JitPack)

Only `:scratch` gets published. JitPack builds from Git tags on GitHub.

**Planned coordinates** (update `com.example` / GitHub username before first release):

```kotlin
// settings.gradle.kts or scratch/build.gradle.kts
group = "com.github.<your-github-username>"
version = "1.0.0"
```

**Consumer dependency** (after first JitPack build):

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.<your-github-username>:SKRETCH:1.0.0")
}
```

JitPack setup checklist (do before v1.0.0):
- [ ] `:scratch` module with `com.android.library` plugin
- [ ] `maven-publish` plugin on the library module
- [ ] `jitpack.yml` if a custom JDK or build command is needed
- [ ] GitHub release tag (e.g. `1.0.0`)
- [ ] Public API surface documented (what composables/config types are stable)

The demo `app` module is never published. It only proves the library works.

To continue with AI:

> Follow `docs/ANDROID_CODING_GUIDELINES.md` in full. Use `README.md` for project scope and roadmap.

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min SDK | 31 |
| Compile SDK | 37 |
| JVM | 11 |
| DI | Koin (when feature modules are added) |
| Testing | JUnit, Compose UI Test |

## Getting started

Needs Android Studio and JDK 11+.

```bash
./gradlew :app:assembleDebug
```

Or open in Android Studio and run the `app` configuration.

Tests:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## References

- [Now in Android](https://github.com/android/nowinandroid)
- [Android modularization guide](https://developer.android.com/topic/modularization)
- [Compose graphics modifiers](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers)
- [Project coding guidelines](docs/ANDROID_CODING_GUIDELINES.md)

## Picking up where you left off

1. Read this README for scope and roadmap
2. Read `docs/ANDROID_CODING_GUIDELINES.md` for conventions
3. Check the status table above and do the next unchecked item
4. Keep diffs small, match existing patterns
5. Don't add network or DI to `scratch`

Next up: Phase 1. Create `:scratch` as a publishable library module, build `ScratchState` and `ScratchCard`, wire a demo in `MainActivity`.

## License

TBD. Add one before a public GitHub release.
