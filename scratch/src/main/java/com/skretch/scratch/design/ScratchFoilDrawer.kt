package com.skretch.scratch.design

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.config.ScratchCoverPattern
import com.skretch.scratch.config.ScratchLayerConfig
import com.skretch.scratch.config.ScratchSurfaceText
import kotlin.math.max

/**
 * Procedural scratch cover rendered once into a bitmap overlay.
 *
 * @author uditbhaskar
 */
object ScratchFoilDrawer {

    /**
     * Renders the scratch cover into a bitmap sized to the card.
     *
     * @param width layer width in pixels
     * @param height layer height in pixels
     * @param density screen density for text sizing
     * @param layoutDirection layout direction for drawing
     * @param layerConfig cover color, pattern, and text
     * @return opaque scratchable cover bitmap
     * @author uditbhaskar
     */
    fun renderFoilBitmap(
        width: Int,
        height: Int,
        density: Density,
        layoutDirection: LayoutDirection,
        layerConfig: ScratchLayerConfig = ScratchLayerConfig.Default,
    ): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        val palette = ScratchDefaults.paletteFor(layerConfig.pattern, layerConfig.color)
        CanvasDrawScope().draw(
            density = density,
            layoutDirection = layoutDirection,
            canvas = canvas,
            size = Size(width.toFloat(), height.toFloat()),
        ) {
            val image = layerConfig.image
            if (image != null) {
                drawImageCover(size = size, image = image)
            } else {
                drawCoverPattern(size = size, pattern = layerConfig.pattern, palette = palette)
            }
            layerConfig.text?.let { drawFoilLabel(size = size, text = it) }
        }
        return bitmap
    }

    private fun DrawScope.drawImageCover(size: Size, image: ImageBitmap) {
        drawImage(
            image = image,
            dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()),
        )
    }

    /**
     * Creates a fully opaque mask used when the cover is a custom composable.
     *
     * @param width layer width in pixels
     * @param height layer height in pixels
     * @return white opaque mask bitmap
     * @author uditbhaskar
     */
    fun createOpaqueMask(width: Int, height: Int): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(width.toFloat(), height.toFloat()),
        ) {
            drawRect(color = Color.White, size = size)
        }
        return bitmap
    }

    /**
     * Draws a built-in cover texture.
     *
     * @param size layer size in pixels
     * @param pattern cover preset
     * @param palette colors for the preset
     * @author uditbhaskar
     */
    fun DrawScope.drawCoverPattern(
        size: Size,
        pattern: ScratchCoverPattern,
        palette: ScratchDefaults.PatternPalette,
    ) {
        if (size.width <= 0f || size.height <= 0f) return
        when (pattern) {
            ScratchCoverPattern.Silver,
            ScratchCoverPattern.Gold,
            ScratchCoverPattern.Matte,
            -> drawMetallicBase(size, palette)
            ScratchCoverPattern.Holographic -> drawHolographicBase(size, palette)
            ScratchCoverPattern.Grain -> drawGrainBase(size, palette)
        }
    }

    /**
     * Draws the centered cover hint label.
     *
     * @param size layer size in pixels
     * @param text label configuration
     * @author uditbhaskar
     */
    fun DrawScope.drawFoilLabel(size: Size, text: ScratchSurfaceText) {
        val resolvedSize = if (text.fontSize == TextUnit.Unspecified) {
            size.width * 0.048f
        } else {
            text.fontSize.toPx()
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = text.color.toArgb()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = resolvedSize
            letterSpacing = 0.08f
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                text.text,
                size.width / 2f,
                size.height * 0.56f,
                labelPaint,
            )
        }
    }

    private fun DrawScope.drawMetallicBase(size: Size, palette: ScratchDefaults.PatternPalette) {
        val width = size.width
        val height = size.height
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    palette.light,
                    palette.mid,
                    palette.dark,
                    palette.mid,
                    palette.light,
                ),
                start = Offset(0f, 0f),
                end = Offset(width, height * 0.35f),
            ),
            size = size,
        )
        drawGrainLines(size, palette.grain)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(palette.highlight.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(width * 0.28f, height * 0.22f),
                radius = width * 0.55f,
            ),
            size = size,
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, palette.tint.copy(alpha = 0.22f)),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = max(width, height) * 0.8f,
            ),
            size = size,
        )
    }

    private fun DrawScope.drawHolographicBase(size: Size, palette: ScratchDefaults.PatternPalette) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.dark, palette.mid, palette.light, palette.highlight, palette.tint),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
            size = size,
        )
        drawGrainLines(size, palette.grain.copy(alpha = 0.35f))
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.7f, size.height * 0.3f),
                radius = size.width * 0.45f,
            ),
            size = size,
        )
    }

    private fun DrawScope.drawGrainBase(size: Size, palette: ScratchDefaults.PatternPalette) {
        drawRect(color = palette.mid, size = size)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.light.copy(alpha = 0.55f), palette.dark.copy(alpha = 0.35f)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
            size = size,
        )
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                val seed = ((x.toInt() * 31) xor (y.toInt() * 17)) and 0xFF
                if (seed % 5 == 0) {
                    drawCircle(
                        color = palette.grain.copy(alpha = 0.08f + (seed % 10) * 0.01f),
                        radius = 1.2f,
                        center = Offset(x, y),
                    )
                }
                x += 3f
            }
            y += 3f
        }
    }

    private fun DrawScope.drawGrainLines(size: Size, grain: Color) {
        var lineY = 0f
        while (lineY < size.height) {
            val alpha = if (lineY.toInt() % 8 == 0) 0.09f else 0.04f
            drawLine(
                color = grain.copy(alpha = alpha),
                start = Offset(0f, lineY),
                end = Offset(size.width, lineY),
                strokeWidth = 1f,
            )
            lineY += ScratchConstants.FOIL_BRUSH_LINE_SPACING_PX
        }
    }
}
