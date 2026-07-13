package com.skretch.scratch.util

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.skretch.scratch.ScratchConstants

/**
 * Erases circular stamps from a foil [ImageBitmap] using a clear compositing mode.
 *
 * @author udit
 */
internal object ScratchBitmapEraser {

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**
     * Erases a circular brush stamp at [center].
     *
     * @param bitmap mutable foil overlay bitmap
     * @param center stamp center in layer coordinates
     * @param radius brush radius in pixels from [com.skretch.scratch.util.ScratchBrushMetrics]
     * @author udit
     */
    fun eraseCircle(bitmap: ImageBitmap, center: Offset, radius: Float) {
        if (radius <= 0f) return
        val canvas = Canvas(bitmap.asAndroidBitmap())
        canvas.drawCircle(center.x, center.y, radius, clearPaint)
    }

    /**
     * Erases along the segment between [from] and [to] so fast swipes leave a continuous trail.
     *
     * @param bitmap mutable foil overlay bitmap
     * @param from previous touch position in layer coordinates
     * @param to current touch position in layer coordinates
     * @param radius brush radius in pixels from [com.skretch.scratch.util.ScratchBrushMetrics]
     * @author udit
     */
    fun eraseStroke(bitmap: ImageBitmap, from: Offset, to: Offset, radius: Float) {
        if (radius <= 0f) return
        val distance = (to - from).getDistance()
        if (distance <= 0f) {
            eraseCircle(bitmap, to, radius)
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
            eraseCircle(bitmap, point, radius)
            traveled += step
        }
        eraseCircle(bitmap, to, radius)
    }
}
