package com.skretch.scratch.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.skretch.scratch.ScratchConstants

/**
 * Hint label drawn on the scratch **cover** ([ScratchLayerConfig.text]).
 *
 * For reward title / subtitle on the main layer, use [MainLayerText] instead.
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchSurfaceText(
    /** Label content (e.g. "SCRATCH HERE"). */
    val text: String,
    /** Text color. */
    val color: Color = Color(0xFF5A606C).copy(alpha = 0.45f),
    /**
     * Text size. [TextUnit.Unspecified] lets the foil drawer pick a size from the layer width.
     */
    val fontSize: TextUnit = TextUnit.Unspecified,
) {
    companion object {
        /** Default “SCRATCH HERE” hint for the cover. */
        val DefaultScratchHint = ScratchSurfaceText(
            text = ScratchConstants.FOIL_LABEL_TEXT,
        )
    }
}

/**
 * Built-in cover textures for [ScratchLayerConfig.pattern].
 *
 * @author uditbhaskar
 */
enum class ScratchCoverPattern {
    /** Cool metallic silver foil. */
    Silver,

    /** Warm metallic gold foil. */
    Gold,

    /** Flat low-sheen gray surface. */
    Matte,

    /** Iridescent multi-hue foil. */
    Holographic,

    /** Soft paper-like grain. */
    Grain,
}

/**
 * Brush stamp style for [ScratchBrush.style].
 *
 * @author uditbhaskar
 */
enum class ScratchBrushStyle {
    /** Hard round stamp. */
    Circular,

    /** Soft round stamp with feathered edges. */
    Smooth,

    /** Irregular clustered stamps that feel like bristles. */
    Hairy,
}
