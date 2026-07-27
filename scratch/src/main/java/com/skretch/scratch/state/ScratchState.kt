package com.skretch.scratch.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.config.RevealThreshold
import com.skretch.scratch.util.ScratchBitmapEraser
import com.skretch.scratch.util.ScratchBrushMetrics
import com.skretch.scratch.config.ScratchBrushStyle

/**
 * Tracks scratch gestures, coverage, and reveal status for a single card.
 *
 * **App-facing APIs:** [scratchProgress], [isRevealed], [hasStarted], [reveal], [reset],
 * [snapshot], [restore]. Prefer creating state with [rememberScratchState].
 *
 * Gesture and brush plumbing (`handleDrag*`, layer size / brush updates, `paintCoverageOnto`)
 * is used by the library overlay; you normally do not call those from app code.
 *
 * Observable state:
 * - [layerSize]: measured size of the scratch layer in pixels
 * - [scratchProgress]: scratched coverage from `0f` to `1f`; snaps to `1f` after reveal when auto-reveal is on
 * - [isRevealed]: whether the cover has been revealed
 * - [hasStarted]: whether the user has started scratching at least once
 * - [resetGeneration]: increments when [reset] is called so the foil bitmap can be recreated
 * - [brushRadiusPx]: brush radius in pixels derived from the current brush width
 *
 * When auto-reveal is enabled, [isRevealed] becomes `true` once coverage reaches the reveal
 * threshold and progress snaps to `1f`.
 *
 * @param initialRevealThreshold fraction of the card that must be scratched before reveal
 * @param initialBrushWidthPx brush width in pixels; kept in sync with foil erasure by ScratchCard
 * @param autoReveal when false, threshold does not auto-reveal; call [reveal] manually
 * @author uditbhaskar
 */
class ScratchState(
    initialRevealThreshold: Float = ScratchConstants.DEFAULT_REVEAL_THRESHOLD,
    initialBrushWidthPx: Float = 0f,
    autoReveal: Boolean = true,
) {
    private val grid = ScratchGrid()
    private var revealThreshold = initialRevealThreshold.coerceIn(0f, 1f)
    private var brushWidthPx by mutableFloatStateOf(initialBrushWidthPx)
    private var brushStyle: ScratchBrushStyle = ScratchBrushStyle.Circular
    private var brushHardness: Float = 1f
    private var lastDragPoint: Offset? = null
    private var autoRevealEnabled = autoReveal

    var layerSize by mutableStateOf(IntSize.Zero)
        private set

    var scratchProgress by mutableFloatStateOf(0f)
        private set

    var isRevealed by mutableStateOf(false)
        private set

    var hasStarted by mutableStateOf(false)
        private set

    var resetGeneration by mutableIntStateOf(0)
        private set

    private var scratchEnabled = true

    /**
     * Updates how much of the card must be scratched before it is revealed.
     *
     * @param threshold fraction between `0f` and `1f`
     * @author uditbhaskar
     */
    fun updateRevealThreshold(threshold: Float) {
        revealThreshold = threshold.coerceIn(0f, 1f)
        updateRevealState()
    }

    /**
     * Updates the reveal threshold from a typed [RevealThreshold].
     *
     * @param threshold typed coverage threshold
     * @author uditbhaskar
     */
    fun updateRevealThreshold(threshold: RevealThreshold) {
        updateRevealThreshold(threshold.fraction)
    }

    /**
     * Enables or disables automatic reveal when coverage crosses the threshold.
     *
     * @param enabled when false, call [reveal] after your own gate (e.g. server confirm)
     * @author uditbhaskar
     */
    fun updateAutoReveal(enabled: Boolean) {
        autoRevealEnabled = enabled
        updateRevealState()
    }

    /**
     * Updates the brush width shared by grid coverage and foil erasure.
     *
     * @param widthPx brush diameter in pixels
     * @author uditbhaskar
     */
    fun updateBrushWidthPx(widthPx: Float) {
        val coerced = widthPx.coerceAtLeast(0f)
        if (brushWidthPx != coerced) {
            brushWidthPx = coerced
        }
    }

    /**
     * Updates the brush style / hardness used for coverage replay after restore.
     *
     * @param style active brush style
     * @param hardness active brush hardness `0f..1f`
     * @author uditbhaskar
     */
    fun updateBrushStyle(style: ScratchBrushStyle, hardness: Float = 1f) {
        brushStyle = style
        brushHardness = hardness.coerceIn(0f, 1f)
    }

    val brushRadiusPx: Float
        get() = ScratchBrushMetrics.radiusFromWidthPx(brushWidthPx)

    /**
     * Enables or disables scratch interaction.
     *
     * @param value when false, drag gestures are ignored
     * @author uditbhaskar
     */
    fun updateScratchEnabled(value: Boolean) {
        scratchEnabled = value
    }

    /**
     * Updates the scratch layer size and rebuilds the coverage grid.
     *
     * @param size layer size in pixels from layout
     * @author uditbhaskar
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
     * @author uditbhaskar
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
     * @author uditbhaskar
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
     * @author uditbhaskar
     */
    fun handleDragEnd() {
        lastDragPoint = null
    }

    /**
     * Forces the card into the revealed state and sets progress to `1f`.
     *
     * @author uditbhaskar
     */
    fun reveal() {
        if (isRevealed) return
        isRevealed = true
        scratchProgress = 1f
        lastDragPoint = null
    }

    /**
     * Clears scratch progress, reveal state, and increments [resetGeneration] so the foil bitmap is recreated.
     *
     * @author uditbhaskar
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
     * Captures coverage and reveal flags for process death / configuration changes.
     *
     * @return immutable snapshot of this state
     * @author uditbhaskar
     */
    fun snapshot(): ScratchStateSnapshot = ScratchStateSnapshot(
        layerWidth = layerSize.width,
        layerHeight = layerSize.height,
        scratchedCells = grid.snapshotCells(),
        scratchProgress = scratchProgress,
        isRevealed = isRevealed,
        hasStarted = hasStarted,
        revealThreshold = revealThreshold,
        autoReveal = autoRevealEnabled,
        brushWidthPx = brushWidthPx,
        brushStyle = brushStyle,
        brushHardness = brushHardness,
    )

    /**
     * Restores coverage and reveal flags from [snapshot].
     *
     * Recreates the foil via [resetGeneration] so the visual mask can be rebuilt by the caller.
     *
     * @param snapshot previously captured state
     * @author uditbhaskar
     */
    fun restore(snapshot: ScratchStateSnapshot) {
        lastDragPoint = null
        revealThreshold = snapshot.revealThreshold.coerceIn(0f, 1f)
        autoRevealEnabled = snapshot.autoReveal
        brushWidthPx = snapshot.brushWidthPx.coerceAtLeast(0f)
        brushStyle = snapshot.brushStyle
        brushHardness = snapshot.brushHardness.coerceIn(0f, 1f)
        layerSize = IntSize(snapshot.layerWidth, snapshot.layerHeight)
        grid.resize(snapshot.layerWidth.toFloat(), snapshot.layerHeight.toFloat())
        grid.restoreCells(snapshot.scratchedCells)
        hasStarted = snapshot.hasStarted
        isRevealed = snapshot.isRevealed
        scratchProgress = if (snapshot.isRevealed) {
            1f
        } else {
            snapshot.scratchProgress.coerceIn(0f, 1f)
        }
        resetGeneration++
        updateRevealState()
    }

    /**
     * Replays scratched coverage onto [bitmap] so foil holes survive configuration changes.
     *
     * Uses [style] / [hardness] when provided; otherwise the last synced brush from [updateBrushStyle].
     *
     * @param bitmap foil or mask bitmap matching [layerSize]
     * @param radius brush radius used for each cell stamp
     * @param style brush style used when the holes were scratched
     * @param hardness brush hardness used when the holes were scratched
     * @author uditbhaskar
     */
    fun paintCoverageOnto(
        bitmap: ImageBitmap,
        radius: Float = brushRadiusPx,
        style: ScratchBrushStyle = brushStyle,
        hardness: Float = brushHardness,
    ) {
        if (isRevealed || layerSize.width <= 0 || layerSize.height <= 0) return
        val stampRadius = maxOf(radius, grid.cellStampRadius(), 1f)
        grid.forEachScratchedCellCenter { center ->
            ScratchBitmapEraser.eraseStamp(
                bitmap = bitmap,
                center = center,
                radius = stampRadius,
                style = style,
                hardness = hardness,
            )
        }
    }

    /**
     * Stamps the brush at a single point and refreshes scratch progress.
     *
     * @param offset touch position in layer coordinates
     * @author uditbhaskar
     */
    private fun stampAt(offset: Offset) {
        grid.stampBrush(center = offset, brushRadius = brushRadiusPx)
        syncProgress()
    }

    /**
     * Stamps the brush along the line between two drag points so fast swipes still leave continuous coverage.
     *
     * @param from previous drag position in layer coordinates
     * @param to current drag position in layer coordinates
     * @author uditbhaskar
     */
    private fun interpolateStamps(from: Offset, to: Offset) {
        val radius = brushRadiusPx
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
     * Whether a new scratch gesture should be processed.
     *
     * @return true when scratching is enabled, the card is not yet revealed, and the layer has a valid size
     * @author uditbhaskar
     */
    private fun canScratch(): Boolean = scratchEnabled && !isRevealed && layerSize.width > 0 && layerSize.height > 0

    /**
     * Copies grid coverage into [scratchProgress] and checks whether the reveal threshold was reached.
     *
     * @author uditbhaskar
     */
    private fun syncProgress() {
        scratchProgress = grid.progress
        updateRevealState()
    }

    /**
     * Marks the card as revealed once coverage crosses [revealThreshold] when [autoRevealEnabled] is on.
     *
     * @author uditbhaskar
     */
    private fun updateRevealState() {
        if (!autoRevealEnabled || isRevealed) return
        if (scratchProgress >= revealThreshold) {
            isRevealed = true
            scratchProgress = 1f
        }
    }
}

/**
 * Serializable coverage snapshot for [ScratchState.restore].
 *
 * @param layerWidth scratch layer width in pixels
 * @param layerHeight scratch layer height in pixels
 * @param scratchedCells grid flags for scratched buckets
 * @param scratchProgress coverage from `0f` to `1f`
 * @param isRevealed whether the cover is revealed
 * @param hasStarted whether scratching has begun
 * @param revealThreshold coverage required before auto-reveal
 * @param autoReveal whether threshold auto-reveals
 * @param brushWidthPx brush diameter used when replaying holes
 * @param brushStyle brush style used when replaying holes
 * @param brushHardness brush hardness used when replaying holes
 * @author uditbhaskar
 */
data class ScratchStateSnapshot(
    val layerWidth: Int,
    val layerHeight: Int,
    val scratchedCells: BooleanArray,
    val scratchProgress: Float,
    val isRevealed: Boolean,
    val hasStarted: Boolean,
    val revealThreshold: Float,
    val autoReveal: Boolean,
    val brushWidthPx: Float = 0f,
    val brushStyle: ScratchBrushStyle = ScratchBrushStyle.Circular,
    val brushHardness: Float = 1f,
) {
    /**
     * Value equality based on coverage, reveal flags, and brush settings.
     *
     * @param other object to compare
     * @return true when all snapshot fields match, including [scratchedCells] contents
     * @author uditbhaskar
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScratchStateSnapshot) return false
        return layerWidth == other.layerWidth &&
            layerHeight == other.layerHeight &&
            scratchedCells.contentEquals(other.scratchedCells) &&
            scratchProgress == other.scratchProgress &&
            isRevealed == other.isRevealed &&
            hasStarted == other.hasStarted &&
            revealThreshold == other.revealThreshold &&
            autoReveal == other.autoReveal &&
            brushWidthPx == other.brushWidthPx &&
            brushStyle == other.brushStyle &&
            brushHardness == other.brushHardness
    }

    /**
     * Hash code matching [equals], including [scratchedCells] contents.
     *
     * @return hash of all snapshot fields
     * @author uditbhaskar
     */
    override fun hashCode(): Int {
        var result = layerWidth
        result = 31 * result + layerHeight
        result = 31 * result + scratchedCells.contentHashCode()
        result = 31 * result + scratchProgress.hashCode()
        result = 31 * result + isRevealed.hashCode()
        result = 31 * result + hasStarted.hashCode()
        result = 31 * result + revealThreshold.hashCode()
        result = 31 * result + autoReveal.hashCode()
        result = 31 * result + brushWidthPx.hashCode()
        result = 31 * result + brushStyle.hashCode()
        result = 31 * result + brushHardness.hashCode()
        return result
    }
}

/**
 * A scratch stroke segment between two touch points.
 *
 * @param from start of the stroke in layer coordinates
 * @param to end of the stroke in layer coordinates
 * @author uditbhaskar
 */
data class StrokeSegment(
    val from: Offset,
    val to: Offset,
)

/**
 * Remembers a [ScratchState] across recompositions and configuration changes.
 *
 * Persists coverage, reveal flags, and brush settings via [ScratchStateSaver], which saves through
 * [ScratchState.snapshot] / [ScratchState.restore] as Bundle-safe primitives (restore coerces numbers
 * because `listSaver` often returns [Double] for floats after configuration changes).
 *
 * @param revealThreshold initial reveal threshold
 * @param autoReveal whether crossing the threshold auto-reveals
 * @author uditbhaskar
 */
@Composable
fun rememberScratchState(
    revealThreshold: RevealThreshold = RevealThreshold.Default,
    autoReveal: Boolean = true,
): ScratchState = rememberSaveable(saver = ScratchStateSaver) {
    ScratchState(
        initialRevealThreshold = revealThreshold.fraction,
        autoReveal = autoReveal,
    )
}

val ScratchStateSaver: Saver<ScratchState, Any> = listSaver(
    save = { state ->
        val snapshot = state.snapshot()
        listOf(
            snapshot.layerWidth,
            snapshot.layerHeight,
            snapshot.scratchedCells.map { if (it) 1 else 0 },
            snapshot.scratchProgress,
            if (snapshot.isRevealed) 1 else 0,
            if (snapshot.hasStarted) 1 else 0,
            snapshot.revealThreshold,
            if (snapshot.autoReveal) 1 else 0,
            snapshot.brushWidthPx,
            snapshot.brushStyle.name,
            snapshot.brushHardness,
        )
    },
    restore = { restored ->
        val cells = (restored.getOrNull(2) as? List<*>)
            ?.map { saveableAsInt(it) != 0 }
            ?.toBooleanArray()
            ?: BooleanArray(0)
        val styleName = restored.getOrNull(9) as? String
        val brushStyle = ScratchBrushStyle.entries.firstOrNull { it.name == styleName }
            ?: ScratchBrushStyle.Circular
        ScratchState(
            initialRevealThreshold = saveableAsFloat(restored.getOrNull(6)),
            initialBrushWidthPx = saveableAsFloat(restored.getOrNull(8)),
            autoReveal = saveableAsBoolean(restored.getOrNull(7)),
        ).also { state ->
            state.restore(
                ScratchStateSnapshot(
                    layerWidth = saveableAsInt(restored.getOrNull(0)),
                    layerHeight = saveableAsInt(restored.getOrNull(1)),
                    scratchedCells = cells,
                    scratchProgress = saveableAsFloat(restored.getOrNull(3)),
                    isRevealed = saveableAsBoolean(restored.getOrNull(4)),
                    hasStarted = saveableAsBoolean(restored.getOrNull(5)),
                    revealThreshold = saveableAsFloat(restored.getOrNull(6)),
                    autoReveal = saveableAsBoolean(restored.getOrNull(7)),
                    brushWidthPx = saveableAsFloat(restored.getOrNull(8)),
                    brushStyle = brushStyle,
                    brushHardness = saveableAsFloat(restored.getOrNull(10), default = 1f),
                ),
            )
        }
    },
)

/**
 * Coerces Bundle / listSaver values to [Int] (handles [Double]/[Long]/[Boolean] from restore).
 *
 * @param value restored value from the saver list
 * @param default fallback when [value] is null or unsupported
 * @return coerced integer
 * @author uditbhaskar
 */
internal fun saveableAsInt(value: Any?, default: Int = 0): Int = when (value) {
    null -> default
    is Int -> value
    is Long -> value.toInt()
    is Double -> value.toInt()
    is Float -> value.toInt()
    is Number -> value.toInt()
    is Boolean -> if (value) 1 else 0
    else -> default
}

/**
 * Coerces Bundle / listSaver values to [Float] (handles [Double]/[Int] from restore).
 *
 * @param value restored value from the saver list
 * @param default fallback when [value] is null or unsupported
 * @return coerced float
 * @author uditbhaskar
 */
internal fun saveableAsFloat(value: Any?, default: Float = 0f): Float = when (value) {
    null -> default
    is Float -> value
    is Double -> value.toFloat()
    is Int -> value.toFloat()
    is Long -> value.toFloat()
    is Number -> value.toFloat()
    else -> default
}

/**
 * Coerces Bundle / listSaver values to [Boolean] (handles int / number flags from restore).
 *
 * @param value restored value from the saver list
 * @param default fallback when [value] is null or unsupported
 * @return coerced boolean
 * @author uditbhaskar
 */
internal fun saveableAsBoolean(value: Any?, default: Boolean = false): Boolean = when (value) {
    null -> default
    is Boolean -> value
    is Int -> value != 0
    is Long -> value != 0L
    is Double -> value != 0.0
    is Float -> value != 0f
    is Number -> value.toInt() != 0
    else -> default
}
