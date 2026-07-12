# SKRETCH

**GitHub description:** Jetpack Compose scratch-card UI library with silver foil overlays, drag-to-reveal, and customizable card chrome. Ships as a JitPack-ready `:scratch` module.

SKRETCH is a Jetpack Compose library for scratch-card interactions: users drag across a metallic foil layer to reveal whatever you place underneath (rewards, coupons, images, copy, and so on). The `:scratch` module is the publishable artifact; `:app` is a demo only.

This README is the project source of truth for scope, API, setup, and roadmap.

---

## Features

- **Drag-to-scratch** foil overlay with a finger-sized circular brush
- **Silver foil texture** rendered once per card size, then erased in real time
- **Auto-reveal** when scratch coverage crosses a configurable threshold (default 45%)
- **Callbacks** for scratch start, progress (0f–1f), and full reveal
- **Haptic feedback** on first scratch
- **Material 3** styling defaults (elevation, border, corner radius)
- **Unit-tested** coverage tracking (`ScratchState`, `ScratchGrid`)
- **ProGuard / R8** consumer rules included in the AAR

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
├── scratch/                 # Publishable Android library (JitPack)
│   ├── consumer-rules.pro   # Merged into consumer release builds
│   └── proguard-rules.pro   # Library release shrink rules
├── app/                     # Demo app (not published)
├── gradle/libs.versions.toml
├── jitpack.yml
└── docs/ANDROID_CODING_GUIDELINES.md
```

### Package map (`com.example.skretch.scratch`)

| Package | Contents |
|---------|----------|
| `component` | `ScratchCard`, `ScratchOverlay` |
| `state` | `ScratchState`, `ScratchGrid`, `StrokeSegment` |
| `design` | `ScratchDefaults`, `ScratchFoilDrawer` |
| `util` | `ScratchBitmapEraser` |

### Dependencies

```
app  →  scratch
scratch  →  Compose UI + Material 3 only
```

No ViewModel, DI, or network code in `:scratch`.

---

## ProGuard / R8

The library ships `consumer-rules.pro`. Apps that minify release builds pick it up automatically through:

```kotlin
// scratch/build.gradle.kts
defaultConfig {
    consumerProguardFiles("consumer-rules.pro")
}
```

**What is kept**

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

---

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

---

## License

TBD. Add a license before the first public GitHub release.
