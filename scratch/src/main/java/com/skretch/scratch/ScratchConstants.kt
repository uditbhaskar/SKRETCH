package com.skretch.scratch

/**
 * Library-wide defaults for scratch coverage, brush size, foil rendering, and reveal animation.
 *
 * @author udit
 */
object ScratchConstants {
    /** Horizontal buckets for coverage estimation. */
    const val GRID_COLUMNS = 24

    /** Vertical buckets for coverage estimation. */
    const val GRID_ROWS = 24

    /** Default fraction of scratched area required before auto-reveal. */
    const val DEFAULT_REVEAL_THRESHOLD = 0.45f

    /** Default brush diameter in dp for [com.skretch.scratch.config.ScratchBrush]. */
    const val DEFAULT_BRUSH_WIDTH_DP = 52f

    /** Default corner radius in dp for rounded chrome. */
    const val DEFAULT_CORNER_RADIUS = 12

    /** Duration of the fade / scale-pop reveal animation. */
    const val REVEAL_FADE_DURATION_MILLIS = 400

    /** Spacing between procedural foil brush lines. */
    const val FOIL_BRUSH_LINE_SPACING_PX = 4f

    /** Fraction of brush radius used as the stamp step along a stroke. */
    const val ERASER_STAMP_STEP_FRACTION = 0.35f

    /** Default hint label drawn on the scratch cover. */
    const val FOIL_LABEL_TEXT = "SCRATCH HERE"
}
