package com.skretch.scratch.component

import android.net.Uri
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Bitmap as AndroidBitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint

/**
 * Loops a video as a scratch cover via [com.skretch.scratch.config.ScratchLayerConfig.custom].
 *
 * Pass the result into `ScratchLayerConfig(custom = { ScratchVideoCover(uri) })`.
 * Lottie is not bundled; wrap your Lottie composable the same way with `custom`.
 *
 * @param uri local or remote video URI
 * @param modifier layout modifier
 * @author uditbhaskar
 */
@Composable
fun ScratchVideoCover(
    uri: Uri,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            VideoView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
                setVideoURI(uri)
                setOnPreparedListener { player ->
                    player.isLooping = true
                    start()
                }
            }
        },
        update = { view ->
            if (!view.isPlaying) {
                view.setVideoURI(uri)
                view.start()
            }
        },
    )
}

/**
 * Reward UI that peeks under the foil: barcode stripes plus optional QR [ImageBitmap].
 *
 * Put this in [com.skretch.scratch.config.MainLayerConfig.custom] so partial scratching
 * reveals the code gradually.
 *
 * @param code human-readable code shown under the barcode
 * @param qrBitmap optional QR (or other) bitmap; when null a placeholder frame is drawn
 * @param background reward background color
 * @param barcodeColor barcode stripe color
 * @param modifier layout modifier
 * @author uditbhaskar
 */
@Composable
fun ScratchBarcodeReward(
    code: String,
    qrBitmap: ImageBitmap? = null,
    background: Color = Color(0xFFFFF8E7),
    barcodeColor: Color = Color(0xFF1A1D24),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp),
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap,
                    contentDescription = code,
                    modifier = Modifier.size(96.dp),
                )
            } else {
                PlaceholderQr(
                    color = barcodeColor,
                    modifier = Modifier.size(96.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(modifier = Modifier.size(width = 180.dp, height = 44.dp)) {
                val barCount = 42
                val gap = size.width / barCount
                for (i in 0 until barCount) {
                    val wide = (i * 7) % 5 == 0
                    val w = if (wide) gap * 0.85f else gap * 0.35f
                    drawRect(
                        color = barcodeColor,
                        topLeft = Offset(i * gap, 0f),
                        size = Size(w, size.height),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = barcodeColor,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Scratch to reveal",
                style = MaterialTheme.typography.bodySmall,
                color = barcodeColor.copy(alpha = 0.55f),
            )
        }
    }
}

/**
 * Draws a simple QR-like placeholder frame when no bitmap is provided.
 *
 * @param color stroke / block color
 * @param modifier layout modifier
 * @author uditbhaskar
 */
@Composable
private fun PlaceholderQr(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val inset = size.minDimension * 0.08f
        drawRect(color = color, style = Stroke(width = inset * 0.35f))
        val cell = (size.minDimension - inset * 2f) / 7f
        fun block(cx: Int, cy: Int, n: Int = 3) {
            drawRect(
                color = color,
                topLeft = Offset(inset + cx * cell, inset + cy * cell),
                size = Size(cell * n, cell * n),
            )
        }
        block(0, 0)
        block(4, 0)
        block(0, 4)
        drawRect(
            color = color,
            topLeft = Offset(inset + 3 * cell, inset + 3 * cell),
            size = Size(cell, cell),
        )
    }
}

/**
 * Builds a tiny monochrome QR-like [ImageBitmap] from [payload] for demos (not a real QR encoder).
 *
 * For production QR codes, generate a real bitmap (e.g. ZXing) and pass it to [ScratchBarcodeReward].
 *
 * @param payload string hashed into a deterministic pattern
 * @param sizePx bitmap width/height in pixels
 * @return procedural placeholder bitmap
 * @author uditbhaskar
 */
fun scratchDemoQrBitmap(payload: String, sizePx: Int = 256): ImageBitmap {
    val bitmap = AndroidBitmap.createBitmap(sizePx, sizePx, AndroidBitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.BLACK
        style = AndroidPaint.Style.FILL
    }
    val modules = 21
    val cell = sizePx / modules.toFloat()
    val hash = payload.hashCode()
    fun bit(x: Int, y: Int): Boolean {
        val v = hash * 31 + x * 131 + y * 17
        return (v and 1) == 1
    }
    fun finder(ox: Int, oy: Int) {
        for (y in 0 until 7) {
            for (x in 0 until 7) {
                val edge = x == 0 || y == 0 || x == 6 || y == 6
                val core = x in 2..4 && y in 2..4
                if (edge || core) {
                    canvas.drawRect(
                        (ox + x) * cell,
                        (oy + y) * cell,
                        (ox + x + 1) * cell,
                        (oy + y + 1) * cell,
                        paint,
                    )
                }
            }
        }
    }
    finder(0, 0)
    finder(modules - 7, 0)
    finder(0, modules - 7)
    for (y in 0 until modules) {
        for (x in 0 until modules) {
            val inFinder =
                (x < 8 && y < 8) ||
                    (x >= modules - 8 && y < 8) ||
                    (x < 8 && y >= modules - 8)
            if (inFinder) continue
            if (bit(x, y)) {
                canvas.drawRect(
                    x * cell,
                    y * cell,
                    (x + 1) * cell,
                    (y + 1) * cell,
                    paint,
                )
            }
        }
    }
    return bitmap.asImageBitmap()
}
