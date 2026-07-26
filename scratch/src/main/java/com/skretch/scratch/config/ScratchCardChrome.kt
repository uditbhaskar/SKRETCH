package com.skretch.scratch.config

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.design.ScratchDefaults
import kotlin.math.min

/**
 * Elevation, border, and outline shape for [com.skretch.scratch.component.ScratchCard].
 *
 * Open this class in the IDE to inspect each property.
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchCardChrome(
    /** Card shadow elevation. */
    val elevation: Dp = ScratchDefaults.cardElevation,
    /** Stroke width around the card. */
    val borderWidth: Dp = ScratchDefaults.cardBorderWidth,
    /** Stroke color. */
    val borderColor: Color = ScratchDefaults.foilBorderDark.copy(alpha = 0.18f),
    /** Outline family: rounded rect, inscribed circle, or ticket. */
    val shape: ScratchCardShape = ScratchCardShape.RoundedRect,
    /** Corner radius for [ScratchCardShape.RoundedRect] and [ScratchCardShape.Ticket]. */
    val cornerRadius: Dp = ScratchConstants.DEFAULT_CORNER_RADIUS.dp,
) {
    companion object {
        /** Default elevation, border, and rounded-rect outline. */
        val Default = ScratchCardChrome()
    }

    /**
     * Resolves a Compose [Shape] for clipping and borders.
     *
     * [ScratchCardShape.Circle] draws a true circle inscribed in the card bounds.
     *
     * @author uditbhaskar
     */
    fun toShape(): Shape = when (shape) {
        ScratchCardShape.RoundedRect -> RoundedCornerShape(cornerRadius)
        ScratchCardShape.Circle -> InscribedCircleShape
        ScratchCardShape.Ticket -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius * 0.35f,
            bottomEnd = cornerRadius,
            bottomStart = cornerRadius * 0.35f,
        )
    }
}

/**
 * Circle centered in the layout bounds using the shorter side as the diameter.
 *
 * Unlike [androidx.compose.foundation.shape.CircleShape] (`RoundedCornerShape(50%)`),
 * this stays circular even when the card width and height differ.
 *
 * @author uditbhaskar
 */
internal object InscribedCircleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val diameter = min(size.width, size.height)
        val left = (size.width - diameter) / 2f
        val top = (size.height - diameter) / 2f
        val rect = Rect(left, top, left + diameter, top + diameter)
        return Outline.Rounded(RoundRect(rect, CornerRadius(diameter / 2f)))
    }
}

/**
 * Optional scratch audio hooks. The library does not ship sound assets; wire your own players.
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchSoundConfig(
    /** When false, callbacks are never invoked. */
    val enabled: Boolean = false,
    /** Called once on the first scratch drag when [enabled] is true. */
    val onScratchStarted: (() -> Unit)? = null,
    /** Called when the card reveals when [enabled] is true. */
    val onRevealed: (() -> Unit)? = null,
) {
    companion object {
        /** Sound hooks disabled. */
        val Off = ScratchSoundConfig()
    }
}

/**
 * Accessibility strings and TalkBack reveal fallback for [com.skretch.scratch.component.ScratchCard].
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchAccessibility(
    /** Description while the scratch cover is present. */
    val coverContentDescription: String = "Scratch card cover. Scratch to reveal the reward.",
    /** Description after the reward is revealed. */
    val revealedContentDescription: String = "Scratch card reward revealed.",
    /** TalkBack custom action that forces reveal without scratching. */
    val revealActionLabel: String = "Reveal reward",
    /** Spoken announcement when the card reveals. */
    val announceOnReveal: String = "Reward revealed.",
) {
    companion object {
        /** Built-in English TalkBack strings. */
        val Default = ScratchAccessibility()
    }
}
