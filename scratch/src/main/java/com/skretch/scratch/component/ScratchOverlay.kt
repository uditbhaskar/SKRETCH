package com.skretch.scratch.component

import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import android.os.SystemClock
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.config.MainLayerConfig
import com.skretch.scratch.config.ScratchAccessibility
import com.skretch.scratch.config.ScratchBrush
import com.skretch.scratch.config.ScratchBrushStyle
import com.skretch.scratch.config.ScratchHapticIntensity
import com.skretch.scratch.config.ScratchHapticMode
import com.skretch.scratch.config.ScratchLayerConfig
import com.skretch.scratch.config.ScratchRevealAnimation
import com.skretch.scratch.config.ScratchSoundConfig
import com.skretch.scratch.design.ScratchDefaults
import com.skretch.scratch.design.ScratchFoilDrawer
import com.skretch.scratch.state.ScratchState
import com.skretch.scratch.util.ScratchBitmapEraser
import com.skretch.scratch.util.ScratchBrushMetrics
import com.skretch.scratch.util.ScratchHaptics
import com.skretch.scratch.util.ScratchSoundPlayer
import com.skretch.scratch.util.ScratchVelocity
import com.skretch.scratch.util.rememberScratchSoundPlayer

/**
 * Renders main content under a scratchable cover and wires gestures to [state].
 *
 * @param state scratch progress and reveal status
 * @param scratchLayer cover pattern, image, or custom composable
 * @param mainLayer revealed content under the cover
 * @param brush stamp style, diameter, hardness, and optional velocity scaling
 * @param enabled when false, gestures are ignored
 * @param multiTouchEnabled when true, all active pointers scratch
 * @param revealAnimation how the cover disappears after reveal
 * @param hapticIntensity scratch haptic strength
 * @param hapticMode first-touch vs continuous drag haptics
 * @param particlesEnabled when true, foil flakes emit under the finger
 * @param sound optional scratch / reveal sound hooks and built-in samples
 * @param accessibility TalkBack labels and reveal action
 * @param tiltX current parallax tilt on X (degrees), updated while dragging when chrome tilt is on
 * @param tiltY current parallax tilt on Y (degrees)
 * @param onTiltChange reports normalized drag position for chrome tilt
 * @param onScratchStarted called the first time scratching begins
 * @param onScratchProgress called when coverage changes
 * @param onRevealed called once when the card reveals
 * @param modifier layout modifier
 * @author uditbhaskar
 */
@Composable
internal fun ScratchOverlay(
    state: ScratchState,
    scratchLayer: ScratchLayerConfig,
    mainLayer: MainLayerConfig,
    brush: ScratchBrush,
    enabled: Boolean,
    multiTouchEnabled: Boolean,
    revealAnimation: ScratchRevealAnimation,
    hapticIntensity: ScratchHapticIntensity,
    hapticMode: ScratchHapticMode,
    particlesEnabled: Boolean,
    sound: ScratchSoundConfig,
    accessibility: ScratchAccessibility,
    onTiltChange: (Float, Float) -> Unit,
    onScratchStarted: () -> Unit,
    onScratchProgress: (Float) -> Unit,
    onRevealed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val soundPlayer = rememberScratchSoundPlayer(sound)
    val particleController = remember { ScratchParticleController() }
    val brushWidthPx = with(density) { brush.width.toPx() }
    val brushRadiusPx = ScratchBrushMetrics.radiusFromWidthPx(brushWidthPx)
    val brushStyle = brush.style
    val brushHardness = brush.hardness
    val foilParticleColor = ScratchDefaults.paletteFor(scratchLayer.pattern, scratchLayer.color).light
    SideEffect {
        state.updateBrushWidthPx(brushWidthPx)
        state.updateBrushStyle(style = brushStyle, hardness = brushHardness)
    }
    val isRevealed = state.isRevealed
    val scratchProgress = state.scratchProgress
    val layerSize = state.layerSize
    val resetGeneration = state.resetGeneration
    val hasCustomCover = scratchLayer.custom != null
    var redrawTrigger by remember { mutableIntStateOf(0) }

    val foilAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 1f,
        animationSpec = tween(
            durationMillis = when (revealAnimation) {
                ScratchRevealAnimation.None -> 0
                else -> ScratchConstants.REVEAL_FADE_DURATION_MILLIS
            },
        ),
        label = "foilAlpha",
    )
    val foilScale by animateFloatAsState(
        targetValue = when {
            !isRevealed -> 1f
            revealAnimation == ScratchRevealAnimation.ScalePop -> 0.92f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = when (revealAnimation) {
                ScratchRevealAnimation.None -> 0
                else -> ScratchConstants.REVEAL_FADE_DURATION_MILLIS
            },
        ),
        label = "foilScale",
    )

    val eraseBitmap = remember(
        layerSize,
        resetGeneration,
        density,
        layoutDirection,
        scratchLayer,
        hasCustomCover,
        brushRadiusPx,
        brushStyle,
        brushHardness,
    ) {
        if (layerSize.width <= 0 || layerSize.height <= 0) {
            null
        } else {
            val bitmap = if (hasCustomCover) {
                ScratchFoilDrawer.createOpaqueMask(layerSize.width, layerSize.height)
            } else {
                ScratchFoilDrawer.renderFoilBitmap(
                    width = layerSize.width,
                    height = layerSize.height,
                    density = density,
                    layoutDirection = layoutDirection,
                    layerConfig = scratchLayer,
                )
            }
            state.paintCoverageOnto(
                bitmap = bitmap,
                radius = brushRadiusPx,
                style = brushStyle,
                hardness = brushHardness,
            )
            bitmap
        }
    }

    LaunchedEffect(enabled) {
        state.updateScratchEnabled(enabled)
    }

    LaunchedEffect(state.hasStarted) {
        if (state.hasStarted) {
            onScratchStarted()
            if (sound.enabled) {
                sound.onScratchStarted?.invoke()
                soundPlayer?.playScratch(force = true)
            }
        }
    }

    LaunchedEffect(scratchProgress) {
        onScratchProgress(scratchProgress)
    }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            onRevealed()
            if (sound.enabled) {
                sound.onRevealed?.invoke()
                soundPlayer?.playReveal()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = if (isRevealed) {
                    accessibility.revealedContentDescription
                } else {
                    accessibility.coverContentDescription
                }
                if (!isRevealed) {
                    customActions = listOf(
                        CustomAccessibilityAction(accessibility.revealActionLabel) {
                            state.reveal()
                            true
                        },
                    )
                }
            }
            .onSizeChanged { size: IntSize ->
                state.updateLayerSize(size)
            },
    ) {
        MainLayerContent(config = mainLayer)

        if (foilAlpha > 0f && eraseBitmap != null) {
            val coverModifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = foilAlpha
                    scaleX = foilScale
                    scaleY = foilScale
                }

            val gestureExtras = ScratchGestureExtras(
                brush = brush,
                baseRadiusPx = brushRadiusPx,
                hapticIntensity = hapticIntensity,
                hapticMode = hapticMode,
                particlesEnabled = particlesEnabled,
                particleController = particleController,
                foilColor = foilParticleColor,
                soundPlayer = soundPlayer,
                soundEnabled = sound.enabled && sound.useBuiltIn,
                onTiltChange = onTiltChange,
            )

            if (hasCustomCover) {
                CustomCoverLayer(
                    scratchLayer = scratchLayer,
                    eraseMask = eraseBitmap,
                    modifier = coverModifier,
                    redrawTrigger = redrawTrigger,
                    enabled = enabled,
                    isRevealed = isRevealed,
                    multiTouchEnabled = multiTouchEnabled,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    extras = gestureExtras,
                    state = state,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    onErased = { redrawTrigger++ },
                )
            } else {
                BuiltInCoverLayer(
                    foilOverlay = eraseBitmap,
                    modifier = coverModifier,
                    redrawTrigger = redrawTrigger,
                    enabled = enabled,
                    isRevealed = isRevealed,
                    multiTouchEnabled = multiTouchEnabled,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    extras = gestureExtras,
                    state = state,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    onErased = { redrawTrigger++ },
                )
            }

            ScratchShimmerOverlay(
                shimmer = scratchLayer.shimmer,
                sparkle = scratchLayer.sparkle,
                visible = !isRevealed && foilAlpha > 0.2f,
            )
            ScratchParticleOverlay(
                controller = particleController,
                visible = particlesEnabled && !isRevealed && foilAlpha > 0.2f,
            )
        }
    }
}

/**
 * Shared gesture extras for cover layers.
 *
 * @author uditbhaskar
 */
internal data class ScratchGestureExtras(
    val brush: ScratchBrush,
    val baseRadiusPx: Float,
    val hapticIntensity: ScratchHapticIntensity,
    val hapticMode: ScratchHapticMode,
    val particlesEnabled: Boolean,
    val particleController: ScratchParticleController,
    val foilColor: androidx.compose.ui.graphics.Color,
    val soundPlayer: ScratchSoundPlayer?,
    val soundEnabled: Boolean,
    val onTiltChange: (Float, Float) -> Unit,
)

/**
 * Draws the procedural foil bitmap and routes single- or multitouch erase gestures.
 *
 * @author uditbhaskar
 */
@Composable
private fun BuiltInCoverLayer(
    foilOverlay: ImageBitmap,
    modifier: Modifier,
    redrawTrigger: Int,
    enabled: Boolean,
    isRevealed: Boolean,
    multiTouchEnabled: Boolean,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    extras: ScratchGestureExtras,
    state: ScratchState,
    hapticFeedback: HapticFeedback,
    view: View,
    onErased: () -> Unit,
) {
    Canvas(
        modifier = modifier.pointerInput(
            enabled,
            isRevealed,
            foilOverlay,
            extras.baseRadiusPx,
            brushStyle,
            brushHardness,
            multiTouchEnabled,
            extras.brush.velocityResponsive,
        ) {
            if (!enabled || isRevealed) return@pointerInput
            if (multiTouchEnabled) {
                detectMultiTouchScratch(
                    state = state,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    bitmap = foilOverlay,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    extras = extras,
                    onErased = onErased,
                )
            } else {
                detectScratchGestures(
                    state = state,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    bitmap = foilOverlay,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    extras = extras,
                    onErased = onErased,
                )
            }
        },
    ) {
        @Suppress("UNUSED_EXPRESSION")
        redrawTrigger
        drawImage(image = foilOverlay)
    }
}

/**
 * Draws a custom cover composable masked by an erase bitmap.
 *
 * @author uditbhaskar
 */
@Composable
private fun CustomCoverLayer(
    scratchLayer: ScratchLayerConfig,
    eraseMask: ImageBitmap,
    modifier: Modifier,
    redrawTrigger: Int,
    enabled: Boolean,
    isRevealed: Boolean,
    multiTouchEnabled: Boolean,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    extras: ScratchGestureExtras,
    state: ScratchState,
    hapticFeedback: HapticFeedback,
    view: View,
    onErased: () -> Unit,
) {
    val custom = scratchLayer.custom ?: return
    Box(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                @Suppress("UNUSED_EXPRESSION")
                redrawTrigger
                drawImage(image = eraseMask, blendMode = BlendMode.DstIn)
            }
            .pointerInput(
                enabled,
                isRevealed,
                eraseMask,
                extras.baseRadiusPx,
                brushStyle,
                brushHardness,
                multiTouchEnabled,
                extras.brush.velocityResponsive,
            ) {
                if (!enabled || isRevealed) return@pointerInput
                if (multiTouchEnabled) {
                    detectMultiTouchScratch(
                        state = state,
                        brushStyle = brushStyle,
                        brushHardness = brushHardness,
                        bitmap = eraseMask,
                        hapticFeedback = hapticFeedback,
                        view = view,
                        extras = extras,
                        onErased = onErased,
                    )
                } else {
                    detectScratchGestures(
                        state = state,
                        brushStyle = brushStyle,
                        brushHardness = brushHardness,
                        bitmap = eraseMask,
                        hapticFeedback = hapticFeedback,
                        view = view,
                        extras = extras,
                        onErased = onErased,
                    )
                }
            },
    ) {
        custom()
    }
}

/**
 * Applies erase + feel effects for a stamp at [point].
 *
 * @author uditbhaskar
 */
private fun applyScratchStamp(
    bitmap: ImageBitmap,
    point: Offset,
    radius: Float,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    extras: ScratchGestureExtras,
    hapticFeedback: HapticFeedback,
    view: View,
    isFirst: Boolean,
    onErased: () -> Unit,
) {
    ScratchBitmapEraser.eraseStamp(bitmap, point, radius, brushStyle, brushHardness)
    if (isFirst) {
        ScratchHaptics.performFirstScratch(hapticFeedback, view, extras.hapticIntensity)
    } else {
        ScratchHaptics.performDragTick(
            hapticFeedback = hapticFeedback,
            view = view,
            intensity = extras.hapticIntensity,
            mode = extras.hapticMode,
        )
    }
    if (extras.particlesEnabled) {
        extras.particleController.emit(point, extras.foilColor)
    }
    if (extras.soundEnabled && !isFirst) {
        extras.soundPlayer?.playScratch(force = false)
    }
    onErased()
}

/**
 * Reports normalized tilt from a layer point.
 *
 * @author uditbhaskar
 */
private fun reportTilt(point: Offset, size: IntSize, extras: ScratchGestureExtras) {
    if (size.width <= 0 || size.height <= 0) return
    val nx = ((point.x / size.width) * 2f - 1f).coerceIn(-1f, 1f)
    val ny = ((point.y / size.height) * 2f - 1f).coerceIn(-1f, 1f)
    extras.onTiltChange(ny, -nx)
}

/**
 * Single-pointer drag path that stamps / strokes the foil and updates [state].
 *
 * @author uditbhaskar
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectScratchGestures(
    state: ScratchState,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    bitmap: ImageBitmap,
    hapticFeedback: HapticFeedback,
    view: View,
    extras: ScratchGestureExtras,
    onErased: () -> Unit,
) {
    var lastTime = 0L
    detectDragGestures(
        onDragStart = { offset ->
            lastTime = SystemClock.uptimeMillis()
            val isFirstScratch = !state.hasStarted
            val stamp = state.handleDragStart(offset)
            if (stamp != null) {
                applyScratchStamp(
                    bitmap = bitmap,
                    point = stamp,
                    radius = extras.baseRadiusPx,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    extras = extras,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    isFirst = isFirstScratch,
                    onErased = onErased,
                )
                reportTilt(stamp, state.layerSize, extras)
            }
        },
        onDrag = { change, _ ->
            change.consume()
            val now = SystemClock.uptimeMillis()
            val elapsed = (now - lastTime).coerceAtLeast(1L)
            lastTime = now
            val segment = state.handleDrag(change.position)
            if (segment != null) {
                val distance = (segment.to - segment.from).getDistance()
                val scale = if (extras.brush.velocityResponsive) {
                    ScratchVelocity.scaleForStroke(
                        distancePx = distance,
                        elapsedMs = elapsed,
                        minScale = extras.brush.velocityMinScale,
                        maxScale = extras.brush.velocityMaxScale,
                    )
                } else {
                    1f
                }
                val radius = extras.baseRadiusPx * scale
                if (segment.from == segment.to) {
                    applyScratchStamp(
                        bitmap = bitmap,
                        point = segment.to,
                        radius = radius,
                        brushStyle = brushStyle,
                        brushHardness = brushHardness,
                        extras = extras,
                        hapticFeedback = hapticFeedback,
                        view = view,
                        isFirst = false,
                        onErased = onErased,
                    )
                } else {
                    ScratchBitmapEraser.eraseStroke(
                        bitmap,
                        segment.from,
                        segment.to,
                        radius,
                        brushStyle,
                        brushHardness,
                    )
                    ScratchHaptics.performDragTick(
                        hapticFeedback = hapticFeedback,
                        view = view,
                        intensity = extras.hapticIntensity,
                        mode = extras.hapticMode,
                    )
                    if (extras.particlesEnabled) {
                        extras.particleController.emit(segment.to, extras.foilColor)
                    }
                    if (extras.soundEnabled) {
                        extras.soundPlayer?.playScratch(force = false)
                    }
                    onErased()
                }
                reportTilt(segment.to, state.layerSize, extras)
            }
        },
        onDragEnd = {
            state.handleDragEnd()
            extras.onTiltChange(0f, 0f)
        },
        onDragCancel = {
            state.handleDragEnd()
            extras.onTiltChange(0f, 0f)
        },
    )
}

/**
 * Multi-pointer path that scratches with every active finger.
 *
 * @author uditbhaskar
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectMultiTouchScratch(
    state: ScratchState,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    bitmap: ImageBitmap,
    hapticFeedback: HapticFeedback,
    view: View,
    extras: ScratchGestureExtras,
    onErased: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val isFirstScratch = !state.hasStarted
        val stamp = state.handleDragStart(down.position)
        if (stamp != null) {
            applyScratchStamp(
                bitmap = bitmap,
                point = stamp,
                radius = extras.baseRadiusPx,
                brushStyle = brushStyle,
                brushHardness = brushHardness,
                extras = extras,
                hapticFeedback = hapticFeedback,
                view = view,
                isFirst = isFirstScratch,
                onErased = onErased,
            )
            reportTilt(stamp, state.layerSize, extras)
        }
        do {
            val event = awaitPointerEvent()
            var erased = false
            event.changes.forEach { change ->
                if (change.pressed) {
                    val segment = state.handleDrag(change.position)
                    if (segment != null) {
                        ScratchBitmapEraser.eraseStamp(
                            bitmap,
                            segment.to,
                            extras.baseRadiusPx,
                            brushStyle,
                            brushHardness,
                        )
                        ScratchHaptics.performDragTick(
                            hapticFeedback = hapticFeedback,
                            view = view,
                            intensity = extras.hapticIntensity,
                            mode = extras.hapticMode,
                        )
                        if (extras.particlesEnabled) {
                            extras.particleController.emit(segment.to, extras.foilColor)
                        }
                        if (extras.soundEnabled) {
                            extras.soundPlayer?.playScratch(force = false)
                        }
                        erased = true
                        reportTilt(segment.to, state.layerSize, extras)
                    }
                    change.consume()
                }
            }
            if (erased) onErased()
        } while (event.changes.any { it.pressed })
        state.handleDragEnd()
        extras.onTiltChange(0f, 0f)
    }
}
