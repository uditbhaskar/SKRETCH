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
 * Draw priority when more than one is set: [custom] > [image] > procedural [pattern] + [color] + [text].
 *
 * Open this class in the IDE to inspect each property.
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchLayerConfig(
    /** Tint mixed into the procedural [pattern]. Ignored when [image] or [custom] is used. */
    val color: Color = ScratchDefaults.foilBaseMid,
    /** Optional hint label drawn on the cover (e.g. "SCRATCH HERE"). */
    val text: ScratchSurfaceText? = ScratchSurfaceText.DefaultScratchHint,
    /** Built-in foil texture when [image] and [custom] are null. */
    val pattern: ScratchCoverPattern = ScratchCoverPattern.Silver,
    /** Bitmap cover. Takes priority over [pattern]. */
    val image: ImageBitmap? = null,
    /** When true, a light sweep animates across unused cover. */
    val shimmer: Boolean = false,
    /** Fully custom cover composable. Highest priority when non-null. */
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
 * @author uditbhaskar
 */
@Immutable
data class MainLayerConfig(
    /** Background fill when [custom] is null. */
    val color: Color = Color.White,
    /** Built-in title / subtitle. Ignored when [custom] is set. */
    val text: MainLayerText? = null,
    /** Fully custom reward content. Takes priority over [color] and [text]. */
    val custom: (@Composable () -> Unit)? = null,
) {
    companion object {
        /** Plain white main layer with no text. */
        val Default = MainLayerConfig()
    }
}

/**
 * Built-in title and subtitle for the main (reward) layer.
 *
 * @author uditbhaskar
 */
@Immutable
data class MainLayerText(
    /** Primary reward line. */
    val title: String,
    /** Optional supporting line under [title]. */
    val subtitle: String? = null,
    /** Color for [title]. */
    val titleColor: Color = Color(0xFF1A73E8),
    /** Color for [subtitle]. */
    val subtitleColor: Color = Color(0xFF5F6368),
)

/**
 * Scratch brush used for coverage tracking and foil erasure.
 *
 * Prefer [circular], [smooth], or [hairy] factories. Default [width] is
 * [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP] (52dp).
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchBrush(
    /** Stamp style applied while dragging. */
    val style: ScratchBrushStyle = ScratchBrushStyle.Circular,
    /** Brush diameter. */
    val width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
    /**
     * Edge firmness from `0f` (soft) to `1f` (hard).
     * Mainly affects [ScratchBrushStyle.Smooth].
     */
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
