package com.skretch.scratch.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight foil-flake particle burst drawn under the finger while scratching.
 *
 * @author uditbhaskar
 */
internal class ScratchParticleController {
    val particles: SnapshotStateList<ScratchParticle> = mutableStateListOf()
    private val random = Random(System.nanoTime())

    /**
     * Spawns a small burst of foil flakes at [origin].
     *
     * @param origin finger position in layer coordinates
     * @param foilColor base flake color
     * @author uditbhaskar
     */
    fun emit(origin: Offset, foilColor: Color) {
        while (particles.size > 120) {
            particles.removeAt(0)
        }
        repeat(5) {
            val angle = random.nextFloat() * (Math.PI * 2.0)
            val speed = 40f + random.nextFloat() * 120f
            particles += ScratchParticle(
                x = origin.x,
                y = origin.y,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat() - 40f,
                life = 1f,
                size = 1.5f + random.nextFloat() * 3.5f,
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 360f,
                color = foilColor.copy(alpha = 0.55f + random.nextFloat() * 0.35f),
            )
        }
    }

    /**
     * Advances particle simulation by [dtSeconds].
     *
     * @param dtSeconds frame delta in seconds
     * @author uditbhaskar
     */
    fun tick(dtSeconds: Float) {
        if (particles.isEmpty()) return
        val next = ArrayList<ScratchParticle>(particles.size)
        for (p in particles) {
            val life = p.life - dtSeconds * 1.8f
            if (life <= 0f) continue
            next += p.copy(
                x = p.x + p.vx * dtSeconds,
                y = p.y + p.vy * dtSeconds,
                vy = p.vy + 280f * dtSeconds,
                life = life,
                rotation = p.rotation + p.spin * dtSeconds,
                color = p.color.copy(alpha = (p.color.alpha * life).coerceIn(0f, 1f)),
            )
        }
        particles.clear()
        particles.addAll(next)
    }
}

/**
 * A single foil flake particle.
 *
 * @param x horizontal position
 * @param y vertical position
 * @param vx horizontal velocity
 * @param vy vertical velocity
 * @param life remaining life `0f..1f`
 * @param size flake half-size in pixels
 * @param rotation degrees
 * @param spin degrees per second
 * @param color flake color
 * @author uditbhaskar
 */
internal data class ScratchParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val life: Float,
    val size: Float,
    val rotation: Float,
    val spin: Float,
    val color: Color,
)

/**
 * Draws and advances [controller] particles.
 *
 * @param controller particle simulation
 * @param visible when false, nothing is drawn
 * @param modifier layout modifier
 * @author uditbhaskar
 */
@Composable
internal fun ScratchParticleOverlay(
    controller: ScratchParticleController,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    LaunchedEffect(controller) {
        var last = 0L
        while (true) {
            withFrameNanos { frame ->
                if (last != 0L) {
                    val dt = ((frame - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    controller.tick(dt)
                }
                last = frame
            }
        }
    }
    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in controller.particles) {
            rotate(degrees = p.rotation, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.x - p.size, p.y - p.size * 0.45f),
                    size = Size(p.size * 2f, p.size * 0.9f),
                )
            }
        }
    }
}
