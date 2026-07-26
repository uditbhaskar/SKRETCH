package com.skretch.scratch.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skretch.scratch.config.ScratchCoverPattern

/**
 * Default foil colors, cover-pattern palettes, and card chrome for [com.skretch.scratch.component.ScratchCard].
 *
 * @author udit
 */
object ScratchDefaults {

    /** Dark stop for the default silver foil. */
    val foilBaseDark = Color(0xFF9AA1AB)

    /** Mid-stop for the default silver foil (also ScratchLayerConfig default color). */
    val foilBaseMid = Color(0xFFC4C9D1)

    /** Light stop for the default silver foil. */
    val foilBaseLight = Color(0xFFE3E6EB)

    /** Highlight stop for the default silver foil. */
    val foilHighlight = Color(0xFFF5F7FA)

    /** Tint stop for the default silver foil. */
    val foilTint = Color(0xFFB0B7C2)

    /** Default chrome border color base. */
    val foilBorderDark = Color(0xFF8B9199)

    /** Default card shadow elevation. */
    val cardElevation = 8.dp

    /** Default card border stroke width. */
    val cardBorderWidth = 0.5.dp

    /**
     * Color stops used when drawing a built-in [ScratchCoverPattern].
     *
     * @author udit
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
     * @author udit
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
        }
        return base.copy(
            mid = lerp(base.mid, color, 0.35f),
            light = lerp(base.light, color, 0.2f),
            tint = lerp(base.tint, color, 0.25f),
        )
    }

    private fun lerp(from: Color, to: Color, fraction: Float): Color {
        val t = fraction.coerceIn(0f, 1f)
        return Color(
            red = from.red + (to.red - from.red) * t,
            green = from.green + (to.green - from.green) * t,
            blue = from.blue + (to.blue - from.blue) * t,
            alpha = from.alpha + (to.alpha - from.alpha) * t,
        )
    }
}
