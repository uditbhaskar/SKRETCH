package com.skretch.scratch.util

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.config.ScratchBrushStyle
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Erases brush stamps from a foil or mask [ImageBitmap] using clear compositing.
 *
 * @author uditbhaskar
 */
internal object ScratchBitmapEraser {

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**
     * Erases a stamp at [center] using [style].
     *
     * @param bitmap mutable foil or mask bitmap
     * @param center stamp center in layer coordinates
     * @param radius brush radius in pixels from [ScratchBrushMetrics]
     * @param style circular, smooth, or hairy stamp
     * @param hardness edge firmness from `0f` to `1f`; mainly affects smooth / hairy stamps
     * @author uditbhaskar
     */
    fun eraseStamp(
        bitmap: ImageBitmap,
        center: Offset,
        radius: Float,
        style: ScratchBrushStyle = ScratchBrushStyle.Circular,
        hardness: Float = 0.65f,
    ) {
        if (radius <= 0f) return
        val clampedHardness = hardness.coerceIn(0f, 1f)
        when (style) {
            ScratchBrushStyle.Circular -> eraseCircle(bitmap, center, radius)
            ScratchBrushStyle.Smooth -> eraseSmooth(bitmap, center, radius, clampedHardness)
            ScratchBrushStyle.Hairy -> eraseHairy(bitmap, center, radius, clampedHardness)
            ScratchBrushStyle.Glitter -> eraseGlitter(bitmap, center, radius, clampedHardness)
        }
    }

    /**
     * Erases along the segment between [from] and [to] so fast swipes leave a continuous trail.
     *
     * @param bitmap mutable foil or mask bitmap
     * @param from previous touch position in layer coordinates
     * @param to current touch position in layer coordinates
     * @param radius brush radius in pixels from [ScratchBrushMetrics]
     * @param style circular, smooth, or hairy stamp
     * @param hardness edge firmness from `0f` to `1f`
     * @author uditbhaskar
     */
    fun eraseStroke(
        bitmap: ImageBitmap,
        from: Offset,
        to: Offset,
        radius: Float,
        style: ScratchBrushStyle = ScratchBrushStyle.Circular,
        hardness: Float = 0.65f,
    ) {
        if (radius <= 0f) return
        val distance = (to - from).getDistance()
        if (distance <= 0f) {
            eraseStamp(bitmap, to, radius, style, hardness)
            return
        }

        val step = maxOf(radius * ScratchConstants.ERASER_STAMP_STEP_FRACTION, 1f)
        var traveled = 0f
        while (traveled < distance) {
            val fraction = traveled / distance
            val point = Offset(
                x = from.x + ((to.x - from.x) * fraction),
                y = from.y + ((to.y - from.y) * fraction),
            )
            eraseStamp(bitmap, point, radius, style, hardness)
            traveled += step
        }
        eraseStamp(bitmap, to, radius, style, hardness)
    }

    /**
     * Erases a hard circular brush stamp at [center].
     *
     * @param bitmap mutable foil or mask bitmap
     * @param center stamp center in layer coordinates
     * @param radius brush radius in pixels
     * @author uditbhaskar
     */
    fun eraseCircle(bitmap: ImageBitmap, center: Offset, radius: Float) {
        if (radius <= 0f) return
        val canvas = Canvas(bitmap.asAndroidBitmap())
        canvas.drawCircle(center.x, center.y, radius, clearPaint)
    }

    /**
     * Erases a soft circular stamp with a blurred edge controlled by [hardness].
     *
     * @param bitmap mutable foil or mask bitmap
     * @param center stamp center in layer coordinates
     * @param radius brush radius in pixels
     * @param hardness edge firmness from `0f` (soft) to `1f` (hard)
     * @author uditbhaskar
     */
    private fun eraseSmooth(
        bitmap: ImageBitmap,
        center: Offset,
        radius: Float,
        hardness: Float,
    ) {
        val canvas = Canvas(bitmap.asAndroidBitmap())
        val blurFactor = 0.15f + (1f - hardness) * 0.55f
        val softPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            maskFilter = BlurMaskFilter(radius * blurFactor, BlurMaskFilter.Blur.NORMAL)
        }
        val coreFactor = 0.35f + hardness * 0.45f
        canvas.drawCircle(center.x, center.y, radius * 0.9f, softPaint)
        canvas.drawCircle(center.x, center.y, radius * coreFactor, clearPaint)
    }

    /**
     * Erases an irregular bristly stamp around [center].
     *
     * @param bitmap mutable foil or mask bitmap
     * @param center stamp center in layer coordinates
     * @param radius brush radius in pixels
     * @param hardness edge firmness from `0f` to `1f`; higher values add more bristles
     * @author uditbhaskar
     */
    private fun eraseHairy(
        bitmap: ImageBitmap,
        center: Offset,
        radius: Float,
        hardness: Float,
    ) {
        val canvas = Canvas(bitmap.asAndroidBitmap())
        val seed = (center.x * 1000f).toInt() xor (center.y * 1000f).toInt()
        val random = Random(seed)
        val core = 0.25f + hardness * 0.35f
        canvas.drawCircle(center.x, center.y, radius * core, clearPaint)
        val bristles = 6 + (hardness * 8).toInt()
        repeat(bristles) {
            val angle = random.nextFloat() * (Math.PI * 2.0)
            val distance = radius * (0.2f + random.nextFloat() * 0.75f)
            val bristleRadius = radius * (0.1f + random.nextFloat() * (0.35f - hardness * 0.12f))
            val x = center.x + (cos(angle) * distance).toFloat()
            val y = center.y + (sin(angle) * distance).toFloat()
            canvas.drawCircle(x, y, bristleRadius, clearPaint)
        }
    }

    /**
     * Erases a glitter / sparkle stamp of scattered micro-holes around [center].
     *
     * @param bitmap mutable foil or mask bitmap
     * @param center stamp center in layer coordinates
     * @param radius brush radius in pixels
     * @param hardness controls sparkle density
     * @author uditbhaskar
     */
    private fun eraseGlitter(
        bitmap: ImageBitmap,
        center: Offset,
        radius: Float,
        hardness: Float,
    ) {
        val canvas = Canvas(bitmap.asAndroidBitmap())
        val seed = (center.x * 733f).toInt() xor (center.y * 911f).toInt()
        val random = Random(seed)
        canvas.drawCircle(center.x, center.y, radius * (0.18f + hardness * 0.2f), clearPaint)
        val sparks = 10 + (hardness * 14).toInt()
        repeat(sparks) {
            val angle = random.nextFloat() * (Math.PI * 2.0)
            val distance = radius * random.nextFloat()
            val sparkRadius = radius * (0.04f + random.nextFloat() * 0.14f)
            val x = center.x + (cos(angle) * distance).toFloat()
            val y = center.y + (sin(angle) * distance).toFloat()
            canvas.drawCircle(x, y, sparkRadius, clearPaint)
        }
    }
}
