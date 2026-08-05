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
 * Elevation, border, outline, optional glow, and tilt for [com.skretch.scratch.component.ScratchCard].
 *
 * Companion [Default] uses default elevation, border, and rounded-rect outline.
 *
 * @param elevation card shadow elevation
 * @param borderWidth stroke width around the card
 * @param borderColor stroke color
 * @param shape outline family: rounded rect, inscribed circle, or ticket
 * @param cornerRadius corner radius for [ScratchCardShape.RoundedRect] and [ScratchCardShape.Ticket]
 * @param glowColor neon / glow stroke color; ignored when [glowWidth] is `0.dp`
 * @param glowWidth outer glow stroke width
 * @param tiltEnabled when true, the card subtly parallax-tilts toward the finger
 * @param tiltDegrees max tilt in degrees for parallax
 * @author uditbhaskar
 */
@Immutable
data class ScratchCardChrome(
    val elevation: Dp = ScratchDefaults.cardElevation,
    val borderWidth: Dp = ScratchDefaults.cardBorderWidth,
    val borderColor: Color = ScratchDefaults.foilBorderDark.copy(alpha = 0.18f),
    val shape: ScratchCardShape = ScratchCardShape.RoundedRect,
    val cornerRadius: Dp = ScratchConstants.DEFAULT_CORNER_RADIUS.dp,
    val glowColor: Color = Color(0xFF57C5C5),
    val glowWidth: Dp = 0.dp,
    val tiltEnabled: Boolean = false,
    val tiltDegrees: Float = 8f,
) {
    companion object {
        val Default = ScratchCardChrome()
    }

    /**
     * Resolves a Compose [Shape] for clipping and borders.
     *
     * [ScratchCardShape.Circle] draws a true circle inscribed in the card bounds.
     *
     * @return Compose [Shape] matching [shape] and [cornerRadius]
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
    /**
     * Builds a circular outline inscribed in [size].
     *
     * @param size layout size in pixels
     * @param layoutDirection layout direction (unused; circle is direction-agnostic)
     * @param density screen density (unused; outline is in raw pixels)
     * @return rounded outline whose diameter is `min(width, height)`
     * @author uditbhaskar
     */
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
 * Optional scratch audio. Built-in WAV samples ship with the library; you can also wire callbacks.
 *
 * Companion [Off] disables sound. [BuiltIn] plays the packaged scratch / reveal samples.
 *
 * @param enabled when false, built-in playback and callbacks are never invoked
 * @param useBuiltIn when true and [enabled], plays packaged scratch / reveal sounds
 * @param onScratchStarted called once on the first scratch drag when [enabled] is true
 * @param onRevealed called when the card reveals when [enabled] is true
 * @author uditbhaskar
 */
@Immutable
data class ScratchSoundConfig(
    val enabled: Boolean = false,
    val useBuiltIn: Boolean = true,
    val onScratchStarted: (() -> Unit)? = null,
    val onRevealed: (() -> Unit)? = null,
) {
    companion object {
        val Off = ScratchSoundConfig()
        val BuiltIn = ScratchSoundConfig(enabled = true, useBuiltIn = true)
    }
}

/**
 * Accessibility strings and TalkBack reveal fallback for [com.skretch.scratch.component.ScratchCard].
 *
 * Companion [Default] provides built-in English TalkBack strings.
 *
 * @param coverContentDescription description while the scratch cover is present
 * @param revealedContentDescription description after the reward is revealed
 * @param revealActionLabel TalkBack custom action that forces reveal without scratching
 * @param announceOnReveal spoken announcement when the card reveals
 * @author uditbhaskar
 */
@Immutable
data class ScratchAccessibility(
    val coverContentDescription: String = "Scratch card cover. Scratch to reveal the reward.",
    val revealedContentDescription: String = "Scratch card reward revealed.",
    val revealActionLabel: String = "Reveal reward",
    val announceOnReveal: String = "Reward revealed.",
) {
    companion object {
        val Default = ScratchAccessibility()
    }
}
