# SKRETCH

> **GitHub description (short):** A Jetpack Compose scratch-card UI library — foil overlays, drag-to-reveal, and a design-system-style customization API. Built with modular Android architecture.

SKRETCH is an Android project that provides **reusable scratch-card components** in Jetpack Compose. Users scratch a metallic foil layer to reveal hidden content underneath — a familiar pattern for rewards, coupons, games, and promotional UIs.

This README is the **source of truth for project intent and architecture**. Use it to resume development after context resets, onboard contributors, or guide AI-assisted implementation.

---

## What We Are Building

### Product

A **Compose component library** (not just a demo app) with:

- **Scratch interaction** — drag to erase a foil overlay and reveal content below
- **Polished card visuals** — metallic foil, rounded corners, reveal animation
- **Design-system customization** — tokens, presets, and config objects for foil, brush, shapes, thresholds, and motion
- **Showcase app** — gallery and playground screens to preview presets and tweak options live

### Core Technical Approach

Scratch cards in Compose rely on isolated blend modes:

1. Draw hidden **content** (reward, coupon, image, text)
2. Draw a **foil overlay** on top
3. Track touch path and erase foil with `BlendMode.Clear`
4. Use `Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` so clear blending does not punch through parent UI
5. Track scratch **coverage** via a grid-bucket model (performant; no per-pixel alpha checks)
6. When coverage crosses a **threshold**, trigger reveal callback and optional vanish animation

**Logic lives in plain Kotlin** (`ScratchState`). **UI stays in composables** (`ScratchCard`, `ScratchOverlay`). This split keeps the library testable and keeps composables thin.

---

## Architecture

Follows project standards in [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md), inspired by [Now in Android](https://github.com/android/nowinandroid) modularization and its `core:designsystem` pattern.

### Module Layout (Target)

```
SKRETCH/
├── app/                    # App shell: MainActivity, NavGraph, theme, DI entry
├── core/
│   └── scratch/            # Scratch-card UI library (Android library module)
├── feature-catalog/        # Demo: preset gallery + config playground (planned)
├── gradle/
│   └── libs.versions.toml  # Centralized dependency versions
└── docs/
    └── ANDROID_CODING_GUIDELINES.md
```

### Dependency Direction

```
app  →  feature-catalog + core:scratch
feature-catalog  →  core:scratch only
core:scratch  →  Compose + Material3 only (no Koin, no network)
```

- Features **never** depend on each other
- Features **never** depend on `app`
- `core:scratch` is a **pure UI library** — no ViewModel, no DI inside the library

### Current State

| Module | Status |
|--------|--------|
| `:app` | Exists — default Compose template (`MainActivity`, `SKRETCHTheme`) |
| `:core:scratch` | **Not created yet** |
| `:feature-catalog` | **Not created yet** |

---

## Library API (Planned)

### Public Composable

```kotlin
@Composable
fun ScratchCard(
    config: ScratchCardConfig = ScratchCardConfig(),
    modifier: Modifier = Modifier,
    onScratchStarted: () -> Unit = {},
    onScratchProgress: (Float) -> Unit = {},
    onRevealed: () -> Unit = {},
    content: @Composable () -> Unit,           // hidden reward
    foilContent: (@Composable () -> Unit)? = null,  // optional custom foil
)
```

### Config & Design Tokens

| Type | Purpose |
|------|---------|
| `ScratchCardConfig` | Single customization entry point |
| `ScratchFoilStyle` | Gradient, texture image, shimmer |
| `ScratchBrushStyle` | Stroke width, cap, softness |
| `ScratchColors` | Foil gradient stops, borders |
| `ScratchShapes` | Corner radius, clip shapes |
| `ScratchMotion` | Vanish duration, easing |
| `ScratchPresets` | `Classic`, `Gold`, `Minimal`, etc. |

### State (Internal / Testable)

```kotlin
class ScratchState {
    fun handleDragStart(offset: Offset)
    fun handleDrag(offset: Offset)
    fun handleDragEnd()
    val scratchProgress: Float   // 0f .. 1f
    val isRevealed: Boolean
    fun reset()
}
```

### Package Structure (`core:scratch`)

```
com.example.skretch.core.scratch/
├── component/     # ScratchCard, ScratchOverlay
├── state/         # ScratchState, coverage tracker
├── config/        # ScratchCardConfig, brush, threshold
├── design/        # Tokens and presets
└── util/          # Path builders, grid bucket math
```

---

## Implementation Roadmap

Track progress by checking off phases below.

### Phase 1 — Foundation
- [ ] Create `:core:scratch` Android library module
- [ ] Register in `settings.gradle.kts`; wire `app` dependency
- [ ] Implement `ScratchState` with grid-bucket coverage
- [ ] Implement basic `ScratchCard` with default silver foil
- [ ] Unit tests for `ScratchState`
- [ ] Replace `Greeting` in `MainActivity` with a working scratch demo

### Phase 2 — Design System & Customization
- [ ] `ScratchCardConfig` + `ScratchPresets`
- [ ] Foil variants: gradient, bitmap texture, shimmer
- [ ] Brush variants: round, soft-edge
- [ ] Custom clip shapes
- [ ] Reveal animations: fade, scale, instant
- [ ] `ScratchCardDefaults` (Material `Defaults` pattern)

### Phase 3 — Polish & Accessibility
- [ ] Haptic feedback on scratch start
- [ ] TalkBack / content descriptions
- [ ] Disable scratch when revealed or `enabled = false`
- [ ] Optional progress persistence (`ScratchStateSaver`)

### Phase 4 — Showcase (`feature-catalog`)
- [ ] Preset gallery screen
- [ ] Live config playground (sliders for brush, threshold, foil)
- [ ] `ScreenRoot` / `ScreenContent` split per guidelines
- [ ] Compose UI tests for showcase screens

### Phase 5 — Library Hardening
- [ ] `@Stable` / `@Immutable` on public config types
- [ ] ProGuard rules if needed
- [ ] KDoc on public API
- [ ] Consumer usage docs in this README

---

## Coding Standards

All implementation must follow [`docs/ANDROID_CODING_GUIDELINES.md`](docs/ANDROID_CODING_GUIDELINES.md).

Key rules:

- **No raw string literals** — use constants or `stringResource(R.string.*)`
- **Screen split** — `*ScreenRoot` (ViewModel, navigation) vs `*ScreenContent` (pure UI)
- **ViewModel pattern** — `StateFlow`, `handleAction`, navigation flags
- **Versions** — only in `gradle/libs.versions.toml`
- **Dependency injection** — Koin in app/features; **not** in `core:scratch`

When prompting AI to continue work:

> Follow `docs/ANDROID_CODING_GUIDELINES.md` in full. Use `README.md` for project scope and roadmap.

---

## Tech Stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Min SDK | 31 |
| Compile SDK | 36 |
| JVM | 11 |
| DI (app/features) | Koin (when modules are added) |
| Testing | JUnit, Compose UI Test |

---

## Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 11+

### Build & Run

```bash
./gradlew :app:assembleDebug
```

Open the project in Android Studio and run the `app` configuration.

### Run Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Key References

- [Now in Android](https://github.com/android/nowinandroid) — modular architecture reference
- [Android modularization guide](https://developer.android.com/topic/modularization)
- [Compose graphics modifiers](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers) — `graphicsLayer`, `CompositingStrategy.Offscreen`, blend modes
- [Project coding guidelines](docs/ANDROID_CODING_GUIDELINES.md)

---

## AI Continuation Notes

When resuming work on this repo:

1. Read this `README.md` for scope, module plan, and roadmap checkboxes
2. Read `docs/ANDROID_CODING_GUIDELINES.md` for file layout and conventions
3. Check **Current State** table above — implement the next unchecked roadmap item
4. Prefer minimal, focused diffs; match existing naming and patterns
5. Do not add network/DI to `core:scratch` — it stays a pure UI library

**Next recommended step:** Phase 1 — create `:core:scratch`, implement `ScratchState` + `ScratchCard`, wire a demo in `MainActivity`.

---

## License

TBD — add a license before public release if sharing on GitHub.
