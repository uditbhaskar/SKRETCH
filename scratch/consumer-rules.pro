# SKRETCH consumer ProGuard / R8 rules
# Merged into release builds of apps that depend on :scratch (via consumerProguardFiles).

-keep class com.skretch.scratch.ScratchConstants { *; }
-keep class com.skretch.scratch.design.ScratchDefaults { *; }
-keep class com.skretch.scratch.design.ScratchDefaults$PatternPalette { *; }
-keep class com.skretch.scratch.design.ScratchFoilDrawer { *; }
-keep class com.skretch.scratch.state.ScratchState { *; }
-keep class com.skretch.scratch.state.ScratchStateSnapshot { *; }
-keep class com.skretch.scratch.state.StrokeSegment { *; }
-keep class com.skretch.scratch.config.** { *; }
-keep class com.skretch.scratch.component.ScratchCardKt { *; }
