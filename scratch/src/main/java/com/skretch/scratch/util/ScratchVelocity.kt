package com.skretch.scratch.util

/**
 * Maps swipe speed to a brush radius scale when velocity-responsive brushes are enabled.
 *
 * @author uditbhaskar
 */
internal object ScratchVelocity {
    /**
     * Converts a stroke length and elapsed time into a radius scale.
     *
     * @param distancePx stroke length in pixels
     * @param elapsedMs time between samples in milliseconds
     * @param minScale minimum scale for slow movement
     * @param maxScale maximum scale for fast movement
     * @return scale factor applied to the base brush radius
     * @author uditbhaskar
     */
    fun scaleForStroke(
        distancePx: Float,
        elapsedMs: Long,
        minScale: Float,
        maxScale: Float,
    ): Float {
        if (elapsedMs <= 0L || distancePx <= 0f) return 1f
        val speed = distancePx / elapsedMs.coerceAtLeast(1L) // px per ms
        // ~0.2 px/ms slow, ~1.8 px/ms fast
        val t = ((speed - 0.2f) / 1.6f).coerceIn(0f, 1f)
        return minScale + (maxScale - minScale) * t
    }
}
