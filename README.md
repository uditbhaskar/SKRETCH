# SKRETCH

**GitHub description:** Jetpack Compose scratch-card UI library with silver foil overlays, drag-to-reveal, and customizable card chrome. Ships as a JitPack-ready `:scratch` module.

SKRETCH is a Jetpack Compose library for scratch-card interactions: users drag across a metallic foil layer to reveal whatever you place underneath (rewards, coupons, images, copy, and so on). The `:scratch` module is the publishable artifact; `:app` is a demo only.

This README is the project source of truth for scope, API, setup, and roadmap.

---

## Features

This README is the main reference for what we're building and how the project is organized. Useful if you're picking the project back up, onboarding someone, or continuing with AI assistance.

---

## Quick start

### Run the demo

```bash
./gradlew :app:assembleDebug
```

Open the project in Android Studio and run the `app` configuration. The demo includes random prize outcomes and a circular restart control after reveal.

### Use the library (local module)

```kotlin
// settings.gradle.kts
include(":scratch")

// app/build.gradle.kts
dependencies {
    implementation(project(":scratch"))
}
```

```kotlin
import com.example.skretch.scratch.component.ScratchCard

ScratchCard(
    modifier = Modifier.size(width = 320.dp, height = 200.dp),
    revealThreshold = 0.45f,
    brushWidth = 52.dp,
    onScratchProgress = { progress -> /* 0f .. 1f */ },
    onRevealed = { /* threshold reached; progress reports 1f */ },
) {
    // Your hidden reward content
    RewardContent()
}
```

### Use the library (JitPack)

Replace `YOUR_USERNAME` in `scratch/build.gradle.kts` before tagging a release.

```kotlin
// settings.gradle.kts or root build.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.YOUR_USERNAME:SKRETCH:0.1.0")
}
```

Tag a release on GitHub (for example `0.1.0`). JitPack builds from that tag using `jitpack.yml` (JDK 17).

---

## Public API

### `ScratchCard`

| Parameter | Default | Description |
|-----------|---------|-------------|
| `modifier` | `Modifier` | Size and layout of the card |
| `revealThreshold` | `0.45f` | Fraction scratched before auto-reveal |
| `brushWidth` | `52.dp` | Diameter of the scratch brush |
| `cornerRadius` | `12.dp` | Card corner radius |
| `enabled` | `true` | When false, gestures are ignored |
| `onScratchStarted` | `{}` | First touch on the foil |
| `onScratchProgress` | `{}` | Coverage updates; jumps to `1f` on reveal |
| `onRevealed` | `{}` | Fired once when threshold is met |
| `content` | required | Composable revealed under the foil |

### `ScratchConstants`

Default grid size, brush width, reveal threshold, fade duration, and foil label text.

### `ScratchDefaults`

Default foil palette and card chrome (elevation, border).

### `ScratchState`

Plain Kotlin state holder (usable if you build a custom overlay later). Tracks coverage, reveal status, and supports `reset()`.

---

## How it works

```
┌─────────────────────────────┐
│  ScratchCard                │
│  ┌───────────────────────┐  │
│  │  content()            │  │  ← reward / hidden UI
│  └───────────────────────┘  │
│  ┌───────────────────────┐  │
│  │  foil bitmap overlay  │  │  ← silver texture, erased per touch
│  └───────────────────────┘  │
└─────────────────────────────┘
```

1. **Content layer** — your `content` composable is drawn first.
2. **Foil bitmap** — a silver texture is rasterized once for the card size.
3. **Erasure** — each drag stamps `PorterDuff.Mode.CLEAR` circles into the foil bitmap (`ScratchBitmapEraser`).
4. **Coverage** — a 24×24 grid estimates how much area was scratched (`ScratchGrid`).
5. **Reveal** — at the threshold, `scratchProgress` becomes `1f`, the foil fades out, and `onRevealed` runs.

Gesture handling, haptics, and redraw invalidation live in `ScratchOverlay`. Logic stays in `ScratchState` for unit tests.

---

## Project layout

```
SKRETCH/
├── app/                    # MainActivity, NavGraph, theme, DI
├── core/
│   └── scratch/            # The scratch-card library
├── feature-catalog/        # Demo screens (planned)
├── gradle/
│   └── libs.versions.toml
└── docs/
    └── ANDROID_CODING_GUIDELINES.md
```

### Package map (`com.example.skretch.scratch`)

| Package | Contents |
|---------|----------|
| `component` | `ScratchCard`, `ScratchOverlay` |
| `state` | `ScratchState`, `ScratchGrid`, `StrokeSegment` |
| `design` | `ScratchDefaults`, `ScratchFoilDrawer` |
| `util` | `ScratchBitmapEraser` |

`:scratch` is the module consumers install. Everything else exists to develop and show it off.

### Dependencies

```
app  →  feature-catalog + core:scratch
feature-catalog  →  core:scratch only
core:scratch  →  Compose + Material3 only
```

Features don't depend on each other or on `app`. `core:scratch` is a pure UI library: no ViewModel, no DI, no network.

---

| Module | Status |
|--------|--------|
| `:app` | Done. Default Compose template for now. |
| `:core:scratch` | Not started |
| `:feature-catalog` | Not started |

The library ships `consumer-rules.pro`. Apps that minify release builds pick it up automatically through:

```kotlin
// scratch/build.gradle.kts
defaultConfig {
    consumerProguardFiles("consumer-rules.pro")
}
```

### Package layout for `core:scratch`

```
com.example.skretch.core.scratch/
├── component/     # ScratchCard, ScratchOverlay
├── state/         # ScratchState, coverage tracker
├── config/        # ScratchCardConfig, brush, threshold
├── design/        # Tokens and presets
└── util/          # Path builders, grid math
```

## Roadmap

### Phase 1: Foundation
- [ ] Create `:core:scratch` library module
- [ ] Register in `settings.gradle.kts`, wire up `app` dependency
- [ ] `ScratchState` with grid-bucket coverage
- [ ] Basic `ScratchCard` with default silver foil
- [ ] Unit tests for `ScratchState`
- [ ] Swap the `Greeting` placeholder in `MainActivity` for a scratch demo

- Public types: `ScratchConstants`, `ScratchDefaults`, `ScratchFoilDrawer`, `ScratchState`, `StrokeSegment`
- `ScratchCardKt` composable facade (Kotlin mangles the method name at compile time)

Consumer apps should also apply the Compose compiler shrink rules (enabled by default with the Compose Gradle plugin).

---

## Tests

```bash
./gradlew :scratch:test
./gradlew test
./gradlew connectedAndroidTest   # when a device/emulator is attached
```

See [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md) for the full list. The important bits:

- No raw string literals. Use constants or `stringResource(R.string.*)`.
- Screens split into `*ScreenRoot` (ViewModel, nav) and `*ScreenContent` (pure UI).
- ViewModels use `StateFlow` and `handleAction`.
- Dependency versions live in `gradle/libs.versions.toml`.
- Koin in app/features only. Not in `core:scratch`.

To continue with AI:

> Follow `docs/ANDROID_CODING_GUIDELINES.md` in full. Use `README.md` for project scope and roadmap.

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min SDK | 31 |
| Compile / Target SDK | 37 |
| JVM | 11 |
| JDK (JitPack) | 17 |
| Group / version | `com.github.YOUR_USERNAME` / `0.1.0` |

---

## Roadmap

### Phase 1 — Foundation (done)
- [x] `:scratch` library module with `maven-publish` and JitPack config
- [x] `ScratchState` + grid coverage + unit tests
- [x] `ScratchCard` with silver foil and reveal animation
- [x] Demo app with scratch flow

### Phase 2 — Customization
- [ ] `ScratchCardConfig` and presets
- [ ] Custom foil styles (bitmap texture, shimmer)
- [ ] Brush and shape variants
- [ ] Reveal animation options

### Phase 3 — Polish
- [x] Haptic feedback on scratch start
- [x] Disable scratch when revealed or `enabled = false`
- [ ] TalkBack / content descriptions
- [ ] Optional progress persistence

### Phase 4 — Showcase
- [ ] `feature-catalog` module with preset gallery and playground

### Phase 5 — Library hardening
- [x] ProGuard consumer rules
- [x] KDoc on public API
- [ ] `@Stable` / `@Immutable` config types
- [ ] License file before public release

---

## Coding standards

See [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md). Highlights:

- No raw string literals in UI; use `strings.xml` in apps and constants in the library.
- Version catalogs in `gradle/libs.versions.toml`.
- Keep `:scratch` free of app-level DI and networking.

---

## Continuing development (AI / contributors)

1. Read this README for scope and current API.
2. Read `docs/ANDROID_CODING_GUIDELINES.md` for conventions.
3. Pick the next unchecked roadmap item.
4. Keep diffs focused; match existing patterns in `:scratch`.

---

## References

- [Now in Android](https://github.com/android/nowinandroid) (modular layout inspiration)
- [Compose graphics modifiers](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers)
- [Android library ProGuard](https://developer.android.com/build/shrink-code#library-rules)

1. Read this README for scope and roadmap
2. Read `docs/ANDROID_CODING_GUIDELINES.md` for conventions
3. Check the status table above and do the next unchecked item
4. Keep diffs small, match existing patterns
5. Don't add network or DI to `core:scratch`

Next up: Phase 1. Create `:core:scratch`, build `ScratchState` and `ScratchCard`, wire a demo in `MainActivity`.

## License

TBD. Add a license before the first public GitHub release.
