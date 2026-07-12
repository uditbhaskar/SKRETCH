# SKRETCH

Scratch cards for Jetpack Compose. Drag across the foil, reveal what's underneath.

**GitHub description:** Jetpack Compose scratch-card library with silver foil overlays, drag-to-reveal, and customizable card chrome. JitPack-ready.

---

## What is this?

SKRETCH gives you a `ScratchCard` composable that works like the scratch-off rewards you see in promo and wallet apps. Put any composable behind a silver foil layer. The user scratches it away with their finger. When enough area is cleared, the card auto-reveals.

You get progress callbacks, haptics on first scratch, and a fade-out animation. The library handles the hard parts: foil rendering, touch erasure, and coverage tracking.

**What ships:** the `:scratch` module (the library).  
**What doesn't ship:** the `:app` module (demo only).

---

## Try it

Clone the repo, open in Android Studio, run `app`.

```bash
./gradlew :app:assembleDebug
```

The demo scratches random rewards (cashback, free delivery, "better luck next time", etc.) and lets you restart with the circular button at the bottom.

---

## Add to your project

### Option A — JitPack

1. Replace `YOUR_USERNAME` in `scratch/build.gradle.kts` with your GitHub username.
2. Tag a release (e.g. `0.1.0`).
3. Add JitPack to your repositories and depend on the artifact:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.github.YOUR_USERNAME:SKRETCH:0.1.0")
}
```

JitPack uses JDK 17 (`jitpack.yml`).

### Option B — Local module

```kotlin
// settings.gradle.kts
include(":app", ":scratch")

// app/build.gradle.kts
dependencies {
    implementation(project(":scratch"))
}
```

---

## Usage

```kotlin
import com.example.skretch.scratch.component.ScratchCard

ScratchCard(
    modifier = Modifier.size(width = 320.dp, height = 200.dp),
    revealThreshold = 0.45f,   // auto-reveal after ~45% scratched
    brushWidth = 52.dp,
    onScratchStarted = { /* first touch */ },
    onScratchProgress = { progress ->
        // 0f .. 1f while scratching; jumps to 1f on full reveal
    },
    onRevealed = { /* threshold hit, foil fading out */ },
) {
    // Whatever you want hidden: text, image, Lottie, a whole layout
    RewardContent()
}
```

### Parameters

| Parameter | Default | What it does |
|-----------|---------|--------------|
| `revealThreshold` | `0.45f` | How much of the card must be scratched before auto-reveal |
| `brushWidth` | `52.dp` | Finger brush size |
| `cornerRadius` | `12.dp` | Card corner radius |
| `enabled` | `true` | Turn scratching on/off |
| `onScratchStarted` | — | First drag on the foil |
| `onScratchProgress` | — | Coverage updates |
| `onRevealed` | — | Threshold reached |

Other public types: `ScratchConstants`, `ScratchDefaults`, `ScratchState`.

---

## How scratching works

Your content sits at the bottom. A silver foil bitmap is drawn on top. Each drag erases the foil in real time. A grid tracks how much area is cleared. Hit the threshold and the rest of the foil fades away.

No per-pixel checks. No blocking the main thread with heavy redraws. Foil is rasterized once per card size, then only the erased regions update.

---

## Project structure

```
SKRETCH/
├── scratch/          # Library (publish this)
├── app/              # Demo app
├── docs/             # Coding guidelines
└── gradle/           # Version catalog
```

```
com.example.skretch.scratch/
├── component/        ScratchCard, ScratchOverlay
├── state/            ScratchState, ScratchGrid
├── design/           Foil colors, texture drawer
└── util/             Bitmap eraser, haptics
```

`:scratch` depends on Compose and Material 3 only. No DI, no networking, no ViewModels inside the library.

---

## Tests

```bash
./gradlew :scratch:test
```

---

## Roadmap

**Done**
- Scratch card composable with silver foil
- Bitmap erasure + coverage grid
- Auto-reveal + fade animation
- Haptics on first scratch
- Unit tests
- JitPack + ProGuard consumer rules
- Demo app

**Next**
- `ScratchCardConfig` and style presets
- Custom foil textures and shimmer
- Brush / shape variants
- Accessibility (TalkBack)
- `feature-catalog` showcase module
- License

---

## For contributors

Read [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md) before opening a PR. Match existing patterns in `:scratch`. Keep the library module free of app-level dependencies.

If you're continuing this project with AI assistance, this README plus the guidelines doc is enough context to pick up where things left off.

---

## Tech

Kotlin · Jetpack Compose · Material 3 · Min SDK 31 · JVM 11

---

## License

TBD.
