package com.skretch.scratch.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.skretch.scratch.ScratchConstants

/**
 * Tracks scratch gestures, coverage, and reveal status for a single card.
 *
 * [scratchProgress] is a value between `0f` and `1f`. [isRevealed] becomes `true` once
 * [scratchProgress] reaches the configured reveal threshold, then [scratchProgress] is set to `1f`.
 *
 * @param initialRevealThreshold fraction of the card that must be scratched before reveal
 * @param initialBrushWidthPx brush width in pixels used for coverage tracking
 * @author udit
 */
class ScratchState(
    initialRevealThreshold: Float = ScratchConstants.DEFAULT_REVEAL_THRESHOLD,
    initialBrushWidthPx: Float = 0f,
) {
    private val grid = ScratchGrid()
    private var revealThreshold = initialRevealThreshold
    private var brushWidthPx = initialBrushWidthPx
    private var lastDragPoint: Offset? = null

    var layerSize by mutableStateOf(IntSize.Zero)
        private set

    var scratchProgress by mutableFloatStateOf(0f)
        private set

    var isRevealed by mutableStateOf(false)
        private set

    var hasStarted by mutableStateOf(false)
        private set

    /**
     * Increments when [reset] is called so the foil bitmap can be recreated.
     *
     * @author udit
     */
    var resetGeneration by mutableIntStateOf(0)
        private set

    private var scratchEnabled = true

    /**
     * Updates how much of the card must be scratched before it is revealed.
     *
     * @param threshold fraction between 0 and 1
     * @author udit
     */
    fun updateRevealThreshold(threshold: Float) {
        revealThreshold = threshold.coerceIn(0f, 1f)
        updateRevealState()
    }

    /**
     * Updates the brush width used for coverage tracking.
     *
     * @param widthPx brush width in pixels
     * @author udit
     */
    fun updateBrushWidthPx(widthPx: Float) {
        brushWidthPx = widthPx.coerceAtLeast(0f)
    }

    /**
     * Enables or disables scratch interaction.
     *
     * @param value when false, drag gestures are ignored
     * @author udit
     */
    fun updateScratchEnabled(value: Boolean) {
        scratchEnabled = value
    }

    /**
     * Updates the scratch layer size and rebuilds the coverage grid.
     *
     * @param size layer size in pixels from layout
     * @author udit
     */
    fun updateLayerSize(size: IntSize) {
        if (layerSize == size) return
        layerSize = size
        grid.resize(size.width.toFloat(), size.height.toFloat())
        syncProgress()
    }

    /**
     * Called when the user starts a scratch drag.
     *
     * @param offset touch position in layer coordinates
     * @return the stamped position when scratching is active
     * @author udit
     */
    fun handleDragStart(offset: Offset): Offset? {
        if (!canScratch()) return null
        hasStarted = true
        lastDragPoint = offset
        stampAt(offset)
        return offset
    }

    /**
     * Called while the user is dragging across the card.
     *
     * @param offset current touch position in layer coordinates
     * @return the stroke segment to erase, or null when scratching is inactive
     * @author udit
     */
    fun handleDrag(offset: Offset): StrokeSegment? {
        if (!canScratch()) return null
        val previous = lastDragPoint
        val segment = if (previous != null) {
            interpolateStamps(previous, offset)
            StrokeSegment(from = previous, to = offset)
        } else {
            stampAt(offset)
            StrokeSegment(from = offset, to = offset)
        }
        lastDragPoint = offset
        return segment
    }

    /**
     * Called when a scratch drag ends or is canceled.
     *
     * @author udit
     */
    fun handleDragEnd() {
        lastDragPoint = null
    }

    /**
     * Clears scratch progress, reveal state, and increments [resetGeneration] so the foil bitmap is recreated.
     *
     * @author udit
     */
    fun reset() {
        lastDragPoint = null
        grid.reset()
        scratchProgress = 0f
        isRevealed = false
        hasStarted = false
        resetGeneration++
    }

    /**
     * Returns half of the current brush width in pixels, with a minimum of 1px.
     *
     * @return brush radius in pixels
     * @author udit
     */
    private fun brushRadius(): Float = maxOf(brushWidthPx * 0.5f, 1f)

    /**
     * Whether a new scratch gesture should be processed.
     *
     * @return true when scratching is enabled, the card is not yet revealed, and the layer has a valid size
     * @author udit
     */
    private fun canScratch(): Boolean = scratchEnabled && !isRevealed && layerSize.width > 0 && layerSize.height > 0

    /**
     * Stamps the brush at a single point and refreshes scratch progress.
     *
     * @param offset touch position in layer coordinates
     * @author udit
     */
    private fun stampAt(offset: Offset) {
        grid.stampBrush(center = offset, brushRadius = brushRadius())
        syncProgress()
    }

    /**
     * Stamps the brush along the line between two drag points so fast swipes still leave continuous coverage.
     *
     * @param from previous drag position in layer coordinates
     * @param to current drag position in layer coordinates
     * @author udit
     */
    private fun interpolateStamps(from: Offset, to: Offset) {
        val radius = brushRadius()
        val distance = (to - from).getDistance()
        if (distance <= 0f) {
            stampAt(to)
            return
        }

        val step = maxOf(radius * ScratchConstants.ERASER_STAMP_STEP_FRACTION, 1f)
        var traveled = 0f
        while (traveled < distance) {
            val fraction = traveled / distance
            val point = Offset(
                x = from.x + ((to.x - from.x) * fraction),
                y = from.y + ((to.y - from.y) * fraction),
            )
            grid.stampBrush(center = point, brushRadius = radius)
            traveled += step
        }
        grid.stampBrush(center = to, brushRadius = radius)
        syncProgress()
    }

    /**
     * Copies grid coverage into [scratchProgress] and checks whether the reveal threshold was reached.
     *
     * @author udit
     */
    private fun syncProgress() {
        scratchProgress = grid.progress
        updateRevealState()
    }

    /**
     * Marks the card as revealed once [scratchProgress] crosses [revealThreshold] and normalizes progress to `1f`.
     *
     * @author udit
     */
    private fun updateRevealState() {
        if (!isRevealed && scratchProgress >= revealThreshold) {
            isRevealed = true
            scratchProgress = 1f
        }
    }
}

/**
 * A scratch stroke segment between two touch points.
 *
 * @param from start of the stroke in layer coordinates
 * @param to end of the stroke in layer coordinates
 * @author udit
 */
data class StrokeSegment(
    val from: Offset,
    val to: Offset,
)
