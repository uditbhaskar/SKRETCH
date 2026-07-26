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
 * @param elevation card shadow elevation
 * @param borderWidth stroke width around the card
 * @param borderColor stroke color
 * @param shape outline family
 * @param cornerRadius used by [ScratchCardShape.RoundedRect] and [ScratchCardShape.Ticket]
 * @author udit
 */
@Immutable
data class ScratchCardChrome(
    val elevation: Dp = ScratchDefaults.cardElevation,
    val borderWidth: Dp = ScratchDefaults.cardBorderWidth,
    val borderColor: Color = ScratchDefaults.foilBorderDark.copy(alpha = 0.18f),
    val shape: ScratchCardShape = ScratchCardShape.RoundedRect,
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
     * @author udit
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
 * @author udit
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
 * Optional scratch audio hooks. The library does not ship sound assets.
 *
 * @param enabled when false, callbacks are never invoked
 * @param onScratchStarted played once on first drag
 * @param onRevealed played when the card reveals
 * @author udit
 */
@Immutable
data class ScratchSoundConfig(
    val enabled: Boolean = false,
    val onScratchStarted: (() -> Unit)? = null,
    val onRevealed: (() -> Unit)? = null,
) {
    companion object {
        /** Sound hooks disabled. */
        val Off = ScratchSoundConfig()
    }
}

/**
 * Accessibility strings and reveal fallback for TalkBack.
 *
 * @param coverContentDescription description while the cover is present
 * @param revealedContentDescription description after reveal
 * @param revealActionLabel TalkBack action that forces reveal without scratching
 * @param announceOnReveal spoken announcement when revealed
 * @author udit
 */
@Immutable
data class ScratchAccessibility(
    val coverContentDescription: String = "Scratch card cover. Scratch to reveal the reward.",
    val revealedContentDescription: String = "Scratch card reward revealed.",
    val revealActionLabel: String = "Reveal reward",
    val announceOnReveal: String = "Reward revealed.",
) {
    companion object {
        /** Built-in English TalkBack strings. */
        val Default = ScratchAccessibility()
    }
}
