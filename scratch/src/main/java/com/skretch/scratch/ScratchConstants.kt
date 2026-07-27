package com.skretch.scratch

/**
 * Library-wide defaults for scratch coverage, brush size, foil rendering, and reveal animation.
 *
 * - [GRID_COLUMNS] / [GRID_ROWS]: horizontal and vertical buckets for coverage estimation
 * - [DEFAULT_REVEAL_THRESHOLD]: default fraction of scratched area required before auto-reveal
 * - [DEFAULT_BRUSH_WIDTH_DP]: default brush diameter in dp for [com.skretch.scratch.config.ScratchBrush]
 * - [DEFAULT_CORNER_RADIUS]: default corner radius in dp for rounded chrome
 * - [REVEAL_FADE_DURATION_MILLIS]: duration of the fade / scale-pop reveal animation
 * - [FOIL_BRUSH_LINE_SPACING_PX]: spacing between procedural foil brush lines
 * - [ERASER_STAMP_STEP_FRACTION]: fraction of brush radius used as the stamp step along a stroke
 * - [FOIL_LABEL_TEXT]: default hint label drawn on the scratch cover
 *
 * @author uditbhaskar
 */
object ScratchConstants {
    const val GRID_COLUMNS = 24
    const val GRID_ROWS = 24
    const val DEFAULT_REVEAL_THRESHOLD = 0.45f
    const val DEFAULT_BRUSH_WIDTH_DP = 52f
    const val DEFAULT_CORNER_RADIUS = 12
    const val REVEAL_FADE_DURATION_MILLIS = 400
    const val FOIL_BRUSH_LINE_SPACING_PX = 4f
    const val ERASER_STAMP_STEP_FRACTION = 0.35f
    const val FOIL_LABEL_TEXT = "SCRATCH HERE"
}
