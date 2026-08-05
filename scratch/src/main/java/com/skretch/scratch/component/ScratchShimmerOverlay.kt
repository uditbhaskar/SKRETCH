package com.skretch.scratch.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Light sweep and optional sparkle loop drawn above an unscratched cover.
 *
 * @param shimmer when true, draws the traveling light band
 * @param sparkle when true, draws twinkling sparkle dots
 * @param visible when false, nothing is drawn
 * @param modifier layout modifier
 * @author uditbhaskar
 */
@Composable
internal fun ScratchShimmerOverlay(
    shimmer: Boolean,
    sparkle: Boolean,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible || (!shimmer && !sparkle)) return
    val transition = rememberInfiniteTransition(label = "scratchShimmer")
    val shift by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerShift",
    )
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sparkleTwinkle",
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        if (shimmer) {
            val x = size.width * shift
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.28f),
                        Color.Transparent,
                    ),
                    start = Offset(x - size.width * 0.25f, 0f),
                    end = Offset(x + size.width * 0.25f, size.height),
                ),
            )
        }
        if (sparkle) {
            val count = 18
            for (i in 0 until count) {
                val px = ((i * 97) % 1000) / 1000f * size.width
                val py = ((i * 53) % 1000) / 1000f * size.height
                val phase = (twinkle + i * 0.07f) % 1f
                val alpha = (0.15f + 0.55f * sin(phase * Math.PI * 2).toFloat()).coerceIn(0f, 0.7f)
                val r = 1.2f + ((i * 13) % 5) * 0.35f
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = r,
                    center = Offset(px, py),
                )
                if (i % 3 == 0) {
                    val arm = r * 2.2f
                    val a = phase * Math.PI.toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.8f),
                        start = Offset(px + cos(a) * arm, py + sin(a) * arm),
                        end = Offset(px - cos(a) * arm, py - sin(a) * arm),
                        strokeWidth = 1f,
                    )
                }
            }
        }
    }
}
