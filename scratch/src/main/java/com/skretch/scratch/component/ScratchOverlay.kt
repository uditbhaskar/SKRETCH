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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.config.MainLayerConfig
import com.skretch.scratch.config.ScratchAccessibility
import com.skretch.scratch.config.ScratchBrush
import com.skretch.scratch.config.ScratchBrushStyle
import com.skretch.scratch.config.ScratchHapticIntensity
import com.skretch.scratch.config.ScratchLayerConfig
import com.skretch.scratch.config.ScratchRevealAnimation
import com.skretch.scratch.config.ScratchSoundConfig
import com.skretch.scratch.design.ScratchFoilDrawer
import com.skretch.scratch.state.ScratchState
import com.skretch.scratch.util.ScratchBitmapEraser
import com.skretch.scratch.util.ScratchBrushMetrics
import com.skretch.scratch.util.ScratchHaptics

/**
 * Renders main content under a scratchable cover and wires gestures to [state].
 *
 * Brush radius is derived from [brush] and density so restored cards keep the active size
 * after configuration changes.
 *
 * @param state scratch progress and reveal status
 * @param scratchLayer cover pattern, image, or custom composable
 * @param mainLayer revealed content under the cover
 * @param brush stamp style, diameter, and hardness
 * @param enabled when false, gestures are ignored
 * @param multiTouchEnabled when true, all active pointers scratch
 * @param revealAnimation how the cover disappears after reveal
 * @param hapticIntensity first-scratch haptic strength
 * @param sound optional scratch / reveal sound hooks
 * @param accessibility TalkBack labels and reveal action
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
    sound: ScratchSoundConfig,
    accessibility: ScratchAccessibility,
    onScratchStarted: () -> Unit,
    onScratchProgress: (Float) -> Unit,
    onRevealed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val brushWidthPx = with(density) { brush.width.toPx() }
    val brushRadiusPx = ScratchBrushMetrics.radiusFromWidthPx(brushWidthPx)
    val brushStyle = brush.style
    val brushHardness = brush.hardness
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
            if (sound.enabled) sound.onScratchStarted?.invoke()
        }
    }

    LaunchedEffect(scratchProgress) {
        onScratchProgress(scratchProgress)
    }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            onRevealed()
            if (sound.enabled) sound.onRevealed?.invoke()
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

            if (hasCustomCover) {
                CustomCoverLayer(
                    scratchLayer = scratchLayer,
                    eraseMask = eraseBitmap,
                    modifier = coverModifier,
                    redrawTrigger = redrawTrigger,
                    enabled = enabled,
                    isRevealed = isRevealed,
                    multiTouchEnabled = multiTouchEnabled,
                    brushRadiusPx = brushRadiusPx,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    hapticIntensity = hapticIntensity,
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
                    brushRadiusPx = brushRadiusPx,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    hapticIntensity = hapticIntensity,
                    state = state,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    onErased = { redrawTrigger++ },
                )
            }

            ScratchShimmerOverlay(
                visible = scratchLayer.shimmer && !isRevealed && foilAlpha > 0.2f,
            )
        }
    }
}

/** Draws the procedural foil bitmap and routes single- or multitouch erase gestures. */
@Composable
private fun BuiltInCoverLayer(
    foilOverlay: ImageBitmap,
    modifier: Modifier,
    redrawTrigger: Int,
    enabled: Boolean,
    isRevealed: Boolean,
    multiTouchEnabled: Boolean,
    brushRadiusPx: Float,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    hapticIntensity: ScratchHapticIntensity,
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
            brushRadiusPx,
            brushStyle,
            brushHardness,
            multiTouchEnabled,
        ) {
            if (!enabled || isRevealed) return@pointerInput
            if (multiTouchEnabled) {
                detectMultiTouchScratch(
                    state = state,
                    brushRadiusPx = brushRadiusPx,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    bitmap = foilOverlay,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    hapticIntensity = hapticIntensity,
                    onErased = onErased,
                )
            } else {
                detectScratchGestures(
                    state = state,
                    brushRadiusPx = brushRadiusPx,
                    brushStyle = brushStyle,
                    brushHardness = brushHardness,
                    bitmap = foilOverlay,
                    hapticFeedback = hapticFeedback,
                    view = view,
                    hapticIntensity = hapticIntensity,
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

/** Draws a custom cover composable masked by an erase bitmap. */
@Composable
private fun CustomCoverLayer(
    scratchLayer: ScratchLayerConfig,
    eraseMask: ImageBitmap,
    modifier: Modifier,
    redrawTrigger: Int,
    enabled: Boolean,
    isRevealed: Boolean,
    multiTouchEnabled: Boolean,
    brushRadiusPx: Float,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    hapticIntensity: ScratchHapticIntensity,
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
                brushRadiusPx,
                brushStyle,
                brushHardness,
                multiTouchEnabled,
            ) {
                if (!enabled || isRevealed) return@pointerInput
                if (multiTouchEnabled) {
                    detectMultiTouchScratch(
                        state = state,
                        brushRadiusPx = brushRadiusPx,
                        brushStyle = brushStyle,
                        brushHardness = brushHardness,
                        bitmap = eraseMask,
                        hapticFeedback = hapticFeedback,
                        view = view,
                        hapticIntensity = hapticIntensity,
                        onErased = onErased,
                    )
                } else {
                    detectScratchGestures(
                        state = state,
                        brushRadiusPx = brushRadiusPx,
                        brushStyle = brushStyle,
                        brushHardness = brushHardness,
                        bitmap = eraseMask,
                        hapticFeedback = hapticFeedback,
                        view = view,
                        hapticIntensity = hapticIntensity,
                        onErased = onErased,
                    )
                }
            },
    ) {
        custom()
    }
}

/** Single-pointer drag path that stamps / strokes the foil and updates [state]. */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectScratchGestures(
    state: ScratchState,
    brushRadiusPx: Float,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    bitmap: ImageBitmap,
    hapticFeedback: HapticFeedback,
    view: View,
    hapticIntensity: ScratchHapticIntensity,
    onErased: () -> Unit,
) {
    detectDragGestures(
        onDragStart = { offset ->
            val isFirstScratch = !state.hasStarted
            val stamp = state.handleDragStart(offset)
            if (stamp != null) {
                if (isFirstScratch) {
                    ScratchHaptics.performFirstScratch(hapticFeedback, view, hapticIntensity)
                }
                ScratchBitmapEraser.eraseStamp(bitmap, stamp, brushRadiusPx, brushStyle, brushHardness)
                onErased()
            }
        },
        onDrag = { change, _ ->
            change.consume()
            val segment = state.handleDrag(change.position)
            if (segment != null) {
                if (segment.from == segment.to) {
                    ScratchBitmapEraser.eraseStamp(
                        bitmap,
                        segment.to,
                        brushRadiusPx,
                        brushStyle,
                        brushHardness,
                    )
                } else {
                    ScratchBitmapEraser.eraseStroke(
                        bitmap,
                        segment.from,
                        segment.to,
                        brushRadiusPx,
                        brushStyle,
                        brushHardness,
                    )
                }
                onErased()
            }
        },
        onDragEnd = { state.handleDragEnd() },
        onDragCancel = { state.handleDragEnd() },
    )
}

/** Multi-pointer path that scratches with every active finger. */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectMultiTouchScratch(
    state: ScratchState,
    brushRadiusPx: Float,
    brushStyle: ScratchBrushStyle,
    brushHardness: Float,
    bitmap: ImageBitmap,
    hapticFeedback: HapticFeedback,
    view: View,
    hapticIntensity: ScratchHapticIntensity,
    onErased: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val isFirstScratch = !state.hasStarted
        val stamp = state.handleDragStart(down.position)
        if (stamp != null) {
            if (isFirstScratch) {
                ScratchHaptics.performFirstScratch(hapticFeedback, view, hapticIntensity)
            }
            ScratchBitmapEraser.eraseStamp(bitmap, stamp, brushRadiusPx, brushStyle, brushHardness)
            onErased()
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
                            brushRadiusPx,
                            brushStyle,
                            brushHardness,
                        )
                        erased = true
                    }
                    change.consume()
                }
            }
            if (erased) onErased()
        } while (event.changes.any { it.pressed })
        state.handleDragEnd()
    }
}
