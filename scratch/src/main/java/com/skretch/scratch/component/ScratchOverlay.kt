package com.skretch.scratch.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.design.ScratchFoilDrawer
import com.skretch.scratch.state.ScratchState
import com.skretch.scratch.util.ScratchBitmapEraser
import com.skretch.scratch.util.ScratchHaptics

/**
 * Renders the foil layer, plays first-scratch haptics, and wires drag gestures to [state].
 *
 * @param state scratch state for this card
 * @param brushWidth width of the scratch stroke
 * @param enabled when false, scratch gestures are ignored
 * @param onScratchStarted called the first time the user starts scratching
 * @param onScratchProgress called when scratch coverage changes; reports `1f` after auto-reveal
 * @param onRevealed called once when the reveal threshold is reached
 * @param modifier modifier applied to this overlay
 * @param content content drawn beneath the foil
 * @author udit
 */
@Composable
internal fun ScratchOverlay(
    state: ScratchState,
    brushWidth: Dp,
    enabled: Boolean,
    onScratchStarted: () -> Unit,
    onScratchProgress: (Float) -> Unit,
    onRevealed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val brushWidthPx = with(density) { brushWidth.toPx() }
    val brushRadiusPx = maxOf(brushWidthPx * 0.5f, 1f)
    val isRevealed = state.isRevealed
    val scratchProgress = state.scratchProgress
    val layerSize = state.layerSize
    val resetGeneration = state.resetGeneration
    var redrawTrigger by remember { mutableIntStateOf(0) }

    val foilAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 1f,
        animationSpec = tween(durationMillis = ScratchConstants.REVEAL_FADE_DURATION_MILLIS),
        label = "foilAlpha",
    )

    val foilOverlay = remember(layerSize, resetGeneration, density, layoutDirection) {
        if (layerSize.width > 0 && layerSize.height > 0) {
            ScratchFoilDrawer.renderFoilBitmap(
                width = layerSize.width,
                height = layerSize.height,
                density = density,
                layoutDirection = layoutDirection,
            )
        } else {
            null
        }
    }

    LaunchedEffect(brushWidthPx) {
        state.updateBrushWidthPx(brushWidthPx)
    }

    LaunchedEffect(enabled) {
        state.updateScratchEnabled(enabled)
    }

    LaunchedEffect(state.hasStarted) {
        if (state.hasStarted) {
            onScratchStarted()
        }
    }

    LaunchedEffect(scratchProgress) {
        onScratchProgress(scratchProgress)
    }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            onRevealed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size: IntSize ->
                state.updateLayerSize(size)
            },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        if (foilAlpha > 0f && foilOverlay != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(enabled, isRevealed, foilOverlay) {
                        if (!enabled || isRevealed) return@pointerInput
                        detectDragGestures(
                            onDragStart = { offset ->
                                val isFirstScratch = !state.hasStarted
                                val stamp = state.handleDragStart(offset)
                                if (stamp != null) {
                                    if (isFirstScratch) {
                                        ScratchHaptics.performFirstScratch(
                                            hapticFeedback = hapticFeedback,
                                            view = view,
                                        )
                                    }
                                    ScratchBitmapEraser.eraseCircle(
                                        bitmap = foilOverlay,
                                        center = stamp,
                                        radius = brushRadiusPx,
                                    )
                                    redrawTrigger++
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val segment = state.handleDrag(change.position)
                                if (segment != null) {
                                    if (segment.from == segment.to) {
                                        ScratchBitmapEraser.eraseCircle(
                                            bitmap = foilOverlay,
                                            center = segment.to,
                                            radius = brushRadiusPx,
                                        )
                                    } else {
                                        ScratchBitmapEraser.eraseStroke(
                                            bitmap = foilOverlay,
                                            from = segment.from,
                                            to = segment.to,
                                            radius = brushRadiusPx,
                                        )
                                    }
                                    redrawTrigger++
                                }
                            },
                            onDragEnd = {
                                state.handleDragEnd()
                            },
                            onDragCancel = {
                                state.handleDragEnd()
                            },
                        )
                    },
            ) {
                @Suppress("UNUSED_EXPRESSION")
                redrawTrigger
                drawImage(image = foilOverlay, alpha = foilAlpha)
            }
        }
    }
}
