package com.skretch.scratch.state

import androidx.compose.ui.geometry.Offset
import com.skretch.scratch.ScratchConstants
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Grid-bucket model used to estimate scratch coverage for a card layer.
 *
 * @param columns number of horizontal buckets
 * @param rows number of vertical buckets
 * @author uditbhaskar
 */
internal class ScratchGrid(
    private val columns: Int = ScratchConstants.GRID_COLUMNS,
    private val rows: Int = ScratchConstants.GRID_ROWS,
) {
    private var layerWidth: Float = 0f
    private var layerHeight: Float = 0f
    private var scratchedCells: BooleanArray = BooleanArray(0)

    /** Total number of coverage buckets (`columns * rows`). */
    val totalCells: Int
        get() = columns * rows

    /** Number of buckets marked scratched. */
    val scratchedCount: Int
        get() = scratchedCells.count { it }

    /** Fraction of scratched buckets from `0f` to `1f`. */
    val progress: Float
        get() {
            if (totalCells == 0) return 0f
            return scratchedCount.toFloat() / totalCells.toFloat()
        }

    /**
     * Resizes the grid to match the scratch layer. Clears scratch data when the size changes.
     *
     * @param width layer width in pixels
     * @param height layer height in pixels
     * @author uditbhaskar
     */
    fun resize(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) {
            layerWidth = 0f
            layerHeight = 0f
            scratchedCells = BooleanArray(0)
            return
        }

        if (layerWidth == width && layerHeight == height && scratchedCells.isNotEmpty()) {
            return
        }

        layerWidth = width
        layerHeight = height
        scratchedCells = BooleanArray(totalCells)
    }

    /**
     * Marks all grid cells covered by a circular brush stamp.
     *
     * @param center brush center in layer coordinates
     * @param brushRadius brush radius in pixels
     * @author uditbhaskar
     */
    fun stampBrush(center: Offset, brushRadius: Float) {
        if (layerWidth <= 0f || layerHeight <= 0f) return

        val cellWidth = layerWidth / columns
        val cellHeight = layerHeight / rows
        if (cellWidth <= 0f || cellHeight <= 0f) return

        val minColumn = max(0, floor(((center.x - brushRadius) / cellWidth).toDouble()).toInt())
        val maxColumn = min(columns - 1, floor(((center.x + brushRadius) / cellWidth).toDouble()).toInt())
        val minRow = max(0, floor(((center.y - brushRadius) / cellHeight).toDouble()).toInt())
        val maxRow = min(rows - 1, floor(((center.y + brushRadius) / cellHeight).toDouble()).toInt())

        for (row in minRow..maxRow) {
            for (column in minColumn..maxColumn) {
                val cellCenterX = (column + 0.5f) * cellWidth
                val cellCenterY = (row + 0.5f) * cellHeight
                val dx = cellCenterX - center.x
                val dy = cellCenterY - center.y
                if ((dx * dx) + (dy * dy) <= brushRadius * brushRadius) {
                    scratchedCells[(row * columns) + column] = true
                }
            }
        }
    }

    /**
     * Clears all scratched cells without changing the grid dimensions.
     *
     * @author uditbhaskar
     */
    fun reset() {
        if (scratchedCells.isEmpty()) return
        scratchedCells.fill(false)
    }

    /**
     * Copies scratched cell flags for persistence.
     *
     * @return snapshot of scratched cells, or empty when uninitialized
     * @author uditbhaskar
     */
    fun snapshotCells(): BooleanArray = scratchedCells.copyOf()

    /**
     * Restores scratched cell flags from [cells] when sizes match.
     *
     * @param cells previously captured scratched flags
     * @author uditbhaskar
     */
    fun restoreCells(cells: BooleanArray) {
        if (scratchedCells.size != cells.size) return
        cells.copyInto(scratchedCells)
    }

    /**
     * Invokes [action] with the center of every scratched cell in layer coordinates.
     *
     * Used to rebuild foil holes after process death / configuration changes.
     *
     * @param action receives cell center in pixels
     * @author uditbhaskar
     */
    fun forEachScratchedCellCenter(action: (Offset) -> Unit) {
        if (layerWidth <= 0f || layerHeight <= 0f || scratchedCells.isEmpty()) return
        val cellWidth = layerWidth / columns
        val cellHeight = layerHeight / rows
        scratchedCells.forEachIndexed { index, scratched ->
            if (!scratched) return@forEachIndexed
            val column = index % columns
            val row = index / columns
            action(
                Offset(
                    x = (column + 0.5f) * cellWidth,
                    y = (row + 0.5f) * cellHeight,
                ),
            )
        }
    }

    /**
     * Approximate brush radius that covers one grid cell when replaying saved coverage.
     *
     * @author uditbhaskar
     */
    fun cellStampRadius(): Float {
        if (layerWidth <= 0f || layerHeight <= 0f) return 1f
        return maxOf(layerWidth / columns, layerHeight / rows) * 0.65f
    }
}
