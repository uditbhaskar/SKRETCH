package com.skretch.scratch.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.skretch.scratch.ScratchConstants

/**
 * Text drawn on a scratch or main surface.
 *
 * @param text label content
 * @param color text color
 * @param fontSize text size; [TextUnit.Unspecified] lets the drawer pick a size from the layer width
 * @author udit
 */
@Immutable
data class ScratchSurfaceText(
    val text: String,
    val color: Color = Color(0xFF5A606C).copy(alpha = 0.45f),
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
 * Built-in cover textures for the scratch layer.
 *
 * @author udit
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
 * Brush style used when erasing the scratch layer.
 *
 * @author udit
 */
enum class ScratchBrushStyle {
    /** Hard round stamp. */
    Circular,

    /** Soft round stamp with feathered edges. */
    Smooth,

    /** Irregular clustered stamps that feel like bristles. */
    Hairy,
}
