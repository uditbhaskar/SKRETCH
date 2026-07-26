package com.skretch.scratch.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.design.ScratchDefaults

/**
 * Configuration for the top scratchable cover surface.
 *
 * Priority: [custom] > [image] > procedural [pattern] + [color] + [text].
 *
 * @param color solid tint mixed into the cover when using a pattern
 * @param text optional hint label drawn on the cover
 * @param pattern built-in cover texture
 * @param image optional bitmap cover
 * @param shimmer when true, a light sweep animates across unused cover
 * @param custom optional fully custom cover composable
 * @author uditbhaskar
 */
@Immutable
data class ScratchLayerConfig(
    val color: Color = ScratchDefaults.foilBaseMid,
    val text: ScratchSurfaceText? = ScratchSurfaceText.DefaultScratchHint,
    val pattern: ScratchCoverPattern = ScratchCoverPattern.Silver,
    val image: ImageBitmap? = null,
    val shimmer: Boolean = false,
    val custom: (@Composable () -> Unit)? = null,
) {
    companion object {
        /** Silver pattern cover with the default scratch hint. */
        val Default = ScratchLayerConfig()
    }
}

/**
 * Configuration for the revealed content under the scratch cover.
 *
 * When [custom] is non-null it replaces [color] and [text].
 *
 * @param color background color when no custom content is set
 * @param text optional title / subtitle drawn on the main surface
 * @param custom optional fully custom content composable
 * @author uditbhaskar
 */
@Immutable
data class MainLayerConfig(
    val color: Color = Color.White,
    val text: MainLayerText? = null,
    val custom: (@Composable () -> Unit)? = null,
) {
    companion object {
        /** Plain white main layer with no text. */
        val Default = MainLayerConfig()
    }
}

/**
 * Built-in title and subtitle for the main layer.
 *
 * @param title primary reward line
 * @param subtitle supporting line under the title
 * @param titleColor color for [title]
 * @param subtitleColor color for [subtitle]
 * @author uditbhaskar
 */
@Immutable
data class MainLayerText(
    val title: String,
    val subtitle: String? = null,
    val titleColor: Color = Color(0xFF1A73E8),
    val subtitleColor: Color = Color(0xFF5F6368),
)

/**
 * Scratch brush used for both coverage tracking and foil erasure.
 *
 * @param style stamp style applied while dragging
 * @param width brush diameter
 * @param hardness edge firmness from `0f` (soft) to `1f` (hard); mainly affects [ScratchBrushStyle.Smooth]
 * @author uditbhaskar
 */
@Immutable
data class ScratchBrush(
    val style: ScratchBrushStyle = ScratchBrushStyle.Circular,
    val width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
    val hardness: Float = 0.65f,
) {
    companion object {
        /** Hard circular brush at the default width. */
        val Circular = ScratchBrush(style = ScratchBrushStyle.Circular)

        /** Soft circular brush with a lighter default hardness. */
        val Smooth = ScratchBrush(style = ScratchBrushStyle.Smooth, hardness = 0.35f)

        /** Bristly clustered brush at the default width. */
        val Hairy = ScratchBrush(style = ScratchBrushStyle.Hairy)

        /**
         * Hard circular brush.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         */
        fun circular(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 1f,
        ) = ScratchBrush(style = ScratchBrushStyle.Circular, width = width, hardness = hardness)

        /**
         * Soft circular brush with feathered edges.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         */
        fun smooth(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 0.35f,
        ) = ScratchBrush(style = ScratchBrushStyle.Smooth, width = width, hardness = hardness)

        /**
         * Irregular bristly brush.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         */
        fun hairy(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 0.5f,
        ) = ScratchBrush(style = ScratchBrushStyle.Hairy, width = width, hardness = hardness)
    }
}
