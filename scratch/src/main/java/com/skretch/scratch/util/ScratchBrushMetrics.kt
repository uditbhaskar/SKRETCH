package com.skretch.scratch.util

/**
 * Converts scratch brush width in pixels to the eraser radius used by coverage tracking and foil erasure.
 *
 * Uses [WIDTH_TO_RADIUS_FACTOR] (`0.5f`) to convert diameter to radius, with a floor of [MIN_RADIUS_PX].
 *
 * @author uditbhaskar
 */
internal object ScratchBrushMetrics {

    private const val WIDTH_TO_RADIUS_FACTOR = 0.5f
    private const val MIN_RADIUS_PX = 1f

    /**
     * Returns half of [brushWidthPx] with a minimum of [MIN_RADIUS_PX].
     *
     * @param brushWidthPx brush diameter in pixels
     * @return brush radius in pixels
     * @author uditbhaskar
     */
    fun radiusFromWidthPx(brushWidthPx: Float): Float =
        maxOf(brushWidthPx * WIDTH_TO_RADIUS_FACTOR, MIN_RADIUS_PX)
}
