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
 * Companion [DefaultScratchHint] is the default “SCRATCH HERE” hint for the cover.
 *
 * @param text label content (e.g. "SCRATCH HERE")
 * @param color text color
 * @param fontSize text size; [TextUnit.Unspecified] lets the foil drawer pick a size from the layer width
 * @author uditbhaskar
 */
@Immutable
data class ScratchSurfaceText(
    val text: String,
    val color: Color = Color(0xFF5A606C).copy(alpha = 0.45f),
    val fontSize: TextUnit = TextUnit.Unspecified,
) {
    companion object {
        val DefaultScratchHint = ScratchSurfaceText(
            text = ScratchConstants.FOIL_LABEL_TEXT,
        )
    }
}

/**
 * Built-in cover textures for [ScratchLayerConfig.pattern].
 *
 * - [Silver]: cool metallic silver foil
 * - [Gold]: warm metallic gold foil
 * - [Matte]: flat low-sheen gray surface
 * - [Holographic]: iridescent multi-hue foil
 * - [Grain]: soft paper-like grain
 * - [Bronze]: warm copper / bronze foil
 * - [RoseGold]: pink-gold metallic foil
 * - [Neon]: vivid neon gradient foil
 * - [Confetti]: festive multi-color speckled foil
 *
 * @author uditbhaskar
 */
enum class ScratchCoverPattern {
    Silver,
    Gold,
    Matte,
    Holographic,
    Grain,
    Bronze,
    RoseGold,
    Neon,
    Confetti,
}

/**
 * Brush stamp style for [ScratchBrush.style].
 *
 * - [Circular]: hard round stamp
 * - [Smooth]: soft round stamp with feathered edges
 * - [Hairy]: irregular clustered stamps that feel like bristles
 * - [Glitter]: sparkle-style stamp with scattered micro-holes
 *
 * @author uditbhaskar
 */
enum class ScratchBrushStyle {
    Circular,
    Smooth,
    Hairy,
    Glitter,
}
