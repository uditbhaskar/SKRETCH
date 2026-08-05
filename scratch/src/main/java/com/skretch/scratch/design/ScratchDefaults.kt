package com.skretch.scratch.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skretch.scratch.config.ScratchCoverPattern

/**
 * Default foil colors, cover-pattern palettes, and card chrome for [com.skretch.scratch.component.ScratchCard].
 *
 * Foil stops: [foilBaseDark], [foilBaseMid] (also ScratchLayerConfig default color), [foilBaseLight],
 * [foilHighlight], [foilTint], and [foilBorderDark] (chrome border base).
 * Chrome defaults: [cardElevation], [cardBorderWidth].
 *
 * @author uditbhaskar
 */
object ScratchDefaults {

    val foilBaseDark = Color(0xFF9AA1AB)
    val foilBaseMid = Color(0xFFC4C9D1)
    val foilBaseLight = Color(0xFFE3E6EB)
    val foilHighlight = Color(0xFFF5F7FA)
    val foilTint = Color(0xFFB0B7C2)
    val foilBorderDark = Color(0xFF8B9199)
    val cardElevation = 8.dp
    val cardBorderWidth = 0.5.dp

    /**
     * Color stops used when drawing a built-in [ScratchCoverPattern].
     *
     * @param dark darkest foil stop
     * @param mid mid foil stop
     * @param light light foil stop
     * @param highlight specular / highlight stop
     * @param tint secondary tint stop
     * @param grain grain / line overlay color
     * @author uditbhaskar
     */
    data class PatternPalette(
        val dark: Color,
        val mid: Color,
        val light: Color,
        val highlight: Color,
        val tint: Color,
        val grain: Color = Color.White,
    )

    /**
     * Returns the palette for [pattern], optionally tinted by [color].
     *
     * @param pattern built-in cover texture
     * @param color caller tint blended into mid / light stops
     * @return palette with [color] mixed into mid, light, and tint
     * @author uditbhaskar
     */
    fun paletteFor(pattern: ScratchCoverPattern, color: Color): PatternPalette {
        val base = when (pattern) {
            ScratchCoverPattern.Silver -> PatternPalette(
                dark = foilBaseDark,
                mid = foilBaseMid,
                light = foilBaseLight,
                highlight = foilHighlight,
                tint = foilTint,
            )
            ScratchCoverPattern.Gold -> PatternPalette(
                dark = Color(0xFFB0892E),
                mid = Color(0xFFD4AF37),
                light = Color(0xFFF0D78C),
                highlight = Color(0xFFFFF4CC),
                tint = Color(0xFFC9A227),
            )
            ScratchCoverPattern.Matte -> PatternPalette(
                dark = Color(0xFF6B7280),
                mid = Color(0xFF9CA3AF),
                light = Color(0xFFD1D5DB),
                highlight = Color(0xFFE5E7EB),
                tint = Color(0xFF9CA3AF),
            )
            ScratchCoverPattern.Holographic -> PatternPalette(
                dark = Color(0xFF7C6BF0),
                mid = Color(0xFF57C5C5),
                light = Color(0xFFF0A0D0),
                highlight = Color(0xFFE8F7FF),
                tint = Color(0xFFA78BFA),
            )
            ScratchCoverPattern.Grain -> PatternPalette(
                dark = Color(0xFF8A8074),
                mid = Color(0xFFB5A99A),
                light = Color(0xFFD9CFC2),
                highlight = Color(0xFFF2EBE3),
                tint = Color(0xFFA89888),
            )
            ScratchCoverPattern.Bronze -> PatternPalette(
                dark = Color(0xFF8C5A2B),
                mid = Color(0xFFB87333),
                light = Color(0xFFD4A574),
                highlight = Color(0xFFF0D5B0),
                tint = Color(0xFFA66B2B),
            )
            ScratchCoverPattern.RoseGold -> PatternPalette(
                dark = Color(0xFFB76E79),
                mid = Color(0xFFE0A4A8),
                light = Color(0xFFF3D1D3),
                highlight = Color(0xFFFFF0F1),
                tint = Color(0xFFC98B93),
            )
            ScratchCoverPattern.Neon -> PatternPalette(
                dark = Color(0xFF0B1020),
                mid = Color(0xFF00F5A0),
                light = Color(0xFF00D9F5),
                highlight = Color(0xFFF5FF7A),
                tint = Color(0xFFFF4FD8),
            )
            ScratchCoverPattern.Confetti -> PatternPalette(
                dark = Color(0xFF2D2A32),
                mid = Color(0xFFFF6B6B),
                light = Color(0xFFFFD93D),
                highlight = Color(0xFF6BCB77),
                tint = Color(0xFF4D96FF),
            )
        }
        return base.copy(
            mid = blendColors(base.mid, color, 0.35f),
            light = blendColors(base.light, color, 0.2f),
            tint = blendColors(base.tint, color, 0.25f),
        )
    }

    /**
     * Linearly interpolates between [from] and [to].
     *
     * @param from start color
     * @param to end color
     * @param fraction blend amount from `0f` to `1f`
     * @return blended color
     * @author uditbhaskar
     */
    private fun blendColors(from: Color, to: Color, fraction: Float): Color {
        val t = fraction.coerceIn(0f, 1f)
        return Color(
            red = from.red + (to.red - from.red) * t,
            green = from.green + (to.green - from.green) * t,
            blue = from.blue + (to.blue - from.blue) * t,
            alpha = from.alpha + (to.alpha - from.alpha) * t,
        )
    }
}
