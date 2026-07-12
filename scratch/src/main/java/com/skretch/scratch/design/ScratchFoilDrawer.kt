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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.skretch.scratch.ScratchConstants
import kotlin.math.max

/**
 * Procedural silver scratch foil rendered once into a bitmap overlay.
 *
 * @author udit
 */
object ScratchFoilDrawer {

    /**
     * Renders the foil layer into a bitmap sized to the scratch card.
     *
     * @param width layer width in pixels
     * @param height layer height in pixels
     * @param density screen density for text sizing
     * @param layoutDirection layout direction for drawing
     * @return opaque scratchable foil bitmap
     * @author udit
     */
    fun renderFoilBitmap(
        width: Int,
        height: Int,
        density: Density,
        layoutDirection: LayoutDirection,
    ): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        val canvas = Canvas(bitmap)
        CanvasDrawScope().draw(
            density = density,
            layoutDirection = layoutDirection,
            canvas = canvas,
            size = Size(width.toFloat(), height.toFloat()),
        ) {
            drawMetallicFoil(size)
            drawFoilLabel(size)
        }
        return bitmap
    }

    /**
     * Draws the default silver scratch texture.
     *
     * @param size layer size in pixels
     * @author udit
     */
    fun DrawScope.drawMetallicFoil(size: Size) {
        val width = size.width
        val height = size.height
        if (width <= 0f || height <= 0f) return

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    ScratchDefaults.foilBaseLight,
                    ScratchDefaults.foilBaseMid,
                    ScratchDefaults.foilBaseDark,
                    ScratchDefaults.foilBaseMid,
                    ScratchDefaults.foilBaseLight,
                ),
                start = Offset(0f, 0f),
                end = Offset(width, height * 0.35f),
            ),
            size = size,
        )

        var lineY = 0f
        while (lineY < height) {
            val alpha = if (lineY.toInt() % 8 == 0) 0.09f else 0.04f
            drawLine(
                color = ScratchDefaults.foilGrain.copy(alpha = alpha),
                start = Offset(0f, lineY),
                end = Offset(width, lineY),
                strokeWidth = 1f,
            )
            lineY += ScratchConstants.FOIL_BRUSH_LINE_SPACING_PX
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    ScratchDefaults.foilHighlight.copy(alpha = 0.55f),
                    Color.Transparent,
                ),
                center = Offset(width * 0.28f, height * 0.22f),
                radius = width * 0.55f,
            ),
            size = size,
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    ScratchDefaults.foilTint.copy(alpha = 0.22f),
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = max(width, height) * 0.8f,
            ),
            size = size,
        )
    }

    /**
     * Draws the centered foil hint label.
     *
     * @param size layer size in pixels
     * @author udit
     */
    fun DrawScope.drawFoilLabel(size: Size) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(72, 90, 96, 108)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = size.width * 0.048f
            letterSpacing = 0.08f
        }
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                ScratchConstants.FOIL_LABEL_TEXT,
                size.width / 2f,
                size.height * 0.56f,
                labelPaint,
            )
        }
    }
}
