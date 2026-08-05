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
 * Use [custom] for video ([com.skretch.scratch.component.ScratchVideoCover]) or Lottie covers.
 *
 * Companion [Default] is a silver pattern cover with shimmer and the default scratch hint.
 *
 * @param color tint mixed into the procedural [pattern]; ignored when [image] or [custom] is used
 * @param text optional hint label drawn on the cover (e.g. "SCRATCH HERE")
 * @param pattern built-in foil texture when [image] and [custom] are null
 * @param image bitmap cover; takes priority over [pattern]
 * @param shimmer when true, a light sweep animates across unused cover (on by default)
 * @param sparkle when true, optional sparkle loop overlays the unused cover
 * @param custom fully custom cover composable; highest priority when non-null
 * @author uditbhaskar
 */
@Immutable
data class ScratchLayerConfig(
    val color: Color = ScratchDefaults.foilBaseMid,
    val text: ScratchSurfaceText? = ScratchSurfaceText.DefaultScratchHint,
    val pattern: ScratchCoverPattern = ScratchCoverPattern.Silver,
    val image: ImageBitmap? = null,
    val shimmer: Boolean = true,
    val sparkle: Boolean = false,
    val custom: (@Composable () -> Unit)? = null,
) {
    companion object {
        val Default = ScratchLayerConfig()
    }
}

/**
 * Configuration for the revealed content under the scratch cover.
 *
 * When [custom] is non-null it replaces [color] and [text].
 *
 * Companion [Default] is a plain white main layer with no text.
 *
 * @param color background fill when [custom] is null
 * @param text built-in title / subtitle; ignored when [custom] is set
 * @param custom fully custom reward content; takes priority over [color] and [text]
 * @author uditbhaskar
 */
@Immutable
data class MainLayerConfig(
    val color: Color = Color.White,
    val text: MainLayerText? = null,
    val custom: (@Composable () -> Unit)? = null,
) {
    companion object {
        val Default = MainLayerConfig()
    }
}

/**
 * Built-in title and subtitle for the main (reward) layer.
 *
 * @param title primary reward line
 * @param subtitle optional supporting line under [title]
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
 * Scratch brush used for coverage tracking and foil erasure.
 *
 * Prefer [circular], [smooth], [hairy], or [glitter] factories. Default [width] is
 * [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP] (52dp).
 *
 * Companion presets: [Circular], [Smooth], [Hairy], [Glitter].
 *
 * @param style stamp style applied while dragging
 * @param width brush diameter
 * @param hardness edge firmness from `0f` (soft) to `1f` (hard); mainly affects [ScratchBrushStyle.Smooth]
 * @param velocityResponsive when true, faster swipes widen the trail
 * @param velocityMinScale minimum width scale when moving slowly
 * @param velocityMaxScale maximum width scale when moving quickly
 * @author uditbhaskar
 */
@Immutable
data class ScratchBrush(
    val style: ScratchBrushStyle = ScratchBrushStyle.Circular,
    val width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
    val hardness: Float = 0.65f,
    val velocityResponsive: Boolean = false,
    val velocityMinScale: Float = 0.75f,
    val velocityMaxScale: Float = 1.65f,
) {
    companion object {
        val Circular = ScratchBrush(style = ScratchBrushStyle.Circular)

        val Smooth = ScratchBrush(style = ScratchBrushStyle.Smooth, hardness = 0.35f)

        val Hairy = ScratchBrush(style = ScratchBrushStyle.Hairy)

        val Glitter = ScratchBrush(style = ScratchBrushStyle.Glitter, hardness = 0.55f)

        /**
         * Hard circular brush.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         * @param velocityResponsive when true, faster swipes widen the trail
         * @return configured [ScratchBrush]
         * @author uditbhaskar
         */
        fun circular(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 1f,
            velocityResponsive: Boolean = false,
        ) = ScratchBrush(
            style = ScratchBrushStyle.Circular,
            width = width,
            hardness = hardness,
            velocityResponsive = velocityResponsive,
        )

        /**
         * Soft circular brush with feathered edges.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         * @param velocityResponsive when true, faster swipes widen the trail
         * @return configured [ScratchBrush]
         * @author uditbhaskar
         */
        fun smooth(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 0.35f,
            velocityResponsive: Boolean = false,
        ) = ScratchBrush(
            style = ScratchBrushStyle.Smooth,
            width = width,
            hardness = hardness,
            velocityResponsive = velocityResponsive,
        )

        /**
         * Irregular bristly brush.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         * @param velocityResponsive when true, faster swipes widen the trail
         * @return configured [ScratchBrush]
         * @author uditbhaskar
         */
        fun hairy(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 0.5f,
            velocityResponsive: Boolean = false,
        ) = ScratchBrush(
            style = ScratchBrushStyle.Hairy,
            width = width,
            hardness = hardness,
            velocityResponsive = velocityResponsive,
        )

        /**
         * Sparkle / glitter brush with scattered micro-holes.
         *
         * @param width brush diameter; defaults to [ScratchConstants.DEFAULT_BRUSH_WIDTH_DP]
         * @param hardness edge firmness `0f..1f`
         * @param velocityResponsive when true, faster swipes widen the trail
         * @return configured [ScratchBrush]
         * @author uditbhaskar
         */
        fun glitter(
            width: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
            hardness: Float = 0.55f,
            velocityResponsive: Boolean = false,
        ) = ScratchBrush(
            style = ScratchBrushStyle.Glitter,
            width = width,
            hardness = hardness,
            velocityResponsive = velocityResponsive,
        )
    }
}
