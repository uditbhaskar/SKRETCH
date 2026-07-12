# SKRETCH consumer ProGuard / R8 rules
# Merged into release builds of apps that depend on :scratch (via consumerProguardFiles).
# Keep rules minimal: no global flags or -keepattributes here (those belong in proguard-rules.pro).

-keep class com.example.skretch.scratch.ScratchConstants { *; }
-keep class com.example.skretch.scratch.design.ScratchDefaults { *; }
-keep class com.example.skretch.scratch.design.ScratchFoilDrawer { *; }
-keep class com.example.skretch.scratch.state.ScratchState { *; }
-keep class com.example.skretch.scratch.state.StrokeSegment { *; }
-keep class com.example.skretch.scratch.component.ScratchCardKt { *; }
