package com.skretch.scratch.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.config.ScratchBrushStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScratchStateTest {

    private lateinit var scratchState: ScratchState

    @Before
    fun setup() {
        scratchState = ScratchState(
            initialRevealThreshold = 0.5f,
            initialBrushWidthPx = 40f,
        )
        scratchState.updateLayerSize(IntSize(width = 400, height = 400))
    }

    @Test
    fun updateLayerSize_initializesEmptyState() {
        assertEquals(0f, scratchState.scratchProgress, 0.001f)
        assertFalse(scratchState.isRevealed)
        assertFalse(scratchState.hasStarted)
        assertEquals(0, scratchState.resetGeneration)
    }

    @Test
    fun handleDragStart_returnsStampedOffset_whenScratchingIsActive() {
        val stamped = scratchState.handleDragStart(Offset(100f, 100f))

        assertTrue(stamped == Offset(100f, 100f))
        assertTrue(scratchState.hasStarted)
        assertTrue(scratchState.scratchProgress > 0f)
    }

    @Test
    fun handleDrag_returnsStrokeSegment_fromPreviousDragPoint() {
        scratchState.handleDragStart(Offset(100f, 100f))
        val segment = scratchState.handleDrag(Offset(200f, 200f))

        assertNotNull(segment)
        assertTrue(segment!!.from == Offset(100f, 100f))
        assertTrue(segment.to == Offset(200f, 200f))
    }

    @Test
    fun handleDrag_withoutPriorPoint_returnsPointSegment() {
        val segment = scratchState.handleDrag(Offset(150f, 150f))

        assertNotNull(segment)
        assertTrue(segment!!.from == Offset(150f, 150f))
        assertTrue(segment.to == Offset(150f, 150f))
    }

    @Test
    fun handleDrag_increasesProgress() {
        scratchState.handleDragStart(Offset(200f, 200f))
        scratchState.handleDrag(Offset(250f, 250f))
        scratchState.handleDragEnd()

        assertTrue(scratchState.scratchProgress > 0f)
        assertTrue(scratchState.hasStarted)
    }

    @Test
    fun fastDrag_interpolatesStamps_andCoversMoreThanSinglePoint() {
        scratchState.handleDragStart(Offset(20f, 200f))
        scratchState.handleDrag(Offset(380f, 200f))
        scratchState.handleDragEnd()
        val fastSwipeProgress = scratchState.scratchProgress

        scratchState.reset()
        scratchState.handleDragStart(Offset(200f, 200f))
        scratchState.handleDragEnd()
        val singlePointProgress = scratchState.scratchProgress

        assertTrue(fastSwipeProgress > singlePointProgress)
    }

    @Test
    fun handleDrag_reachesThreshold_setsRevealedAndSnapsProgressToOne() {
        scratchUntilRevealed(scratchState)

        assertTrue(scratchState.isRevealed)
        assertEquals(1f, scratchState.scratchProgress, 0.001f)
    }

    @Test
    fun updateRevealThreshold_revealsAtLowerThreshold() {
        scratchState.updateRevealThreshold(0.05f)

        listOf(180f, 220f).forEach { y ->
            scratchState.handleDragStart(Offset(20f, y))
            scratchState.handleDrag(Offset(380f, y))
            scratchState.handleDragEnd()
        }

        assertTrue(scratchState.isRevealed)
        assertEquals(1f, scratchState.scratchProgress, 0.001f)
    }

    @Test
    fun updateBrushWidthPx_widerBrushCoversMoreArea() {
        val narrowState = ScratchState(initialRevealThreshold = 0.5f, initialBrushWidthPx = 10f)
        narrowState.updateLayerSize(IntSize(400, 400))
        val wideState = ScratchState(initialRevealThreshold = 0.5f, initialBrushWidthPx = 80f)
        wideState.updateLayerSize(IntSize(400, 400))

        narrowState.handleDragStart(Offset(200f, 200f))
        narrowState.handleDragEnd()
        wideState.handleDragStart(Offset(200f, 200f))
        wideState.handleDragEnd()

        assertTrue(wideState.scratchProgress > narrowState.scratchProgress)
    }

    @Test
    fun reset_clearsStateAndIncrementsResetGeneration() {
        scratchState.handleDragStart(Offset(100f, 100f))
        scratchState.handleDrag(Offset(200f, 200f))
        scratchState.handleDragEnd()
        val generationBeforeReset = scratchState.resetGeneration

        scratchState.reset()

        assertEquals(0f, scratchState.scratchProgress, 0.001f)
        assertFalse(scratchState.isRevealed)
        assertFalse(scratchState.hasStarted)
        assertEquals(generationBeforeReset + 1, scratchState.resetGeneration)
    }

    @Test
    fun handleDrag_whenDisabled_returnsNullAndDoesNotScratch() {
        scratchState.updateScratchEnabled(false)

        assertNull(scratchState.handleDragStart(Offset(100f, 100f)))
        assertNull(scratchState.handleDrag(Offset(200f, 200f)))
        scratchState.handleDragEnd()

        assertEquals(0f, scratchState.scratchProgress, 0.001f)
        assertFalse(scratchState.hasStarted)
    }

    @Test
    fun handleDrag_whenAlreadyRevealed_returnsNullAndKeepsProgressAtOne() {
        scratchState.updateRevealThreshold(0.05f)
        scratchState.handleDragStart(Offset(20f, 200f))
        scratchState.handleDrag(Offset(380f, 200f))
        scratchState.handleDragEnd()
        assertTrue(scratchState.isRevealed)

        assertNull(scratchState.handleDragStart(Offset(50f, 50f)))
        assertNull(scratchState.handleDrag(Offset(350f, 350f)))
        assertEquals(1f, scratchState.scratchProgress, 0.001f)
    }

    @Test
    fun handleDrag_withoutLayerSize_returnsNull() {
        val emptyState = ScratchState(initialBrushWidthPx = 40f)

        assertNull(emptyState.handleDragStart(Offset(100f, 100f)))
        assertEquals(0f, emptyState.scratchProgress, 0.001f)
    }

    @Test
    fun progressStaysBelowOne_untilRevealThresholdIsReached() {
        scratchState.handleDragStart(Offset(200f, 200f))
        scratchState.handleDragEnd()

        assertTrue(scratchState.scratchProgress < 1f)
        assertFalse(scratchState.isRevealed)
    }

    @Test
    fun defaultRevealThreshold_matchesScratchConstants() {
        val defaultState = ScratchState(initialBrushWidthPx = 40f)
        defaultState.updateLayerSize(IntSize(400, 400))
        defaultState.updateRevealThreshold(ScratchConstants.DEFAULT_REVEAL_THRESHOLD)

        defaultState.handleDragStart(Offset(20f, 200f))
        defaultState.handleDrag(Offset(380f, 200f))
        defaultState.handleDragEnd()

        assertFalse(defaultState.isRevealed)

        scratchUntilRevealed(defaultState)

        assertTrue(defaultState.isRevealed)
        assertEquals(1f, defaultState.scratchProgress, 0.001f)
    }

    @Test
    fun reveal_forcesRevealedWithoutThreshold() {
        scratchState.reveal()

        assertTrue(scratchState.isRevealed)
        assertEquals(1f, scratchState.scratchProgress, 0.001f)
    }

    @Test
    fun autoRevealDisabled_doesNotRevealAtThreshold() {
        val gated = ScratchState(
            initialRevealThreshold = 0.05f,
            initialBrushWidthPx = 40f,
            autoReveal = false,
        )
        gated.updateLayerSize(IntSize(400, 400))
        gated.handleDragStart(Offset(20f, 200f))
        gated.handleDrag(Offset(380f, 200f))
        gated.handleDragEnd()

        assertFalse(gated.isRevealed)
        assertTrue(gated.scratchProgress > 0f)

        gated.reveal()
        assertTrue(gated.isRevealed)
    }

    @Test
    fun snapshotAndRestore_preservesCoverageAndReveal() {
        scratchUntilRevealed(scratchState)
        val snapshot = scratchState.snapshot()

        scratchState.reset()
        assertFalse(scratchState.isRevealed)
        assertEquals(0f, scratchState.scratchProgress, 0.001f)

        scratchState.restore(snapshot)
        assertTrue(scratchState.isRevealed)
        assertEquals(1f, scratchState.scratchProgress, 0.001f)
    }

    @Test
    fun snapshotAndRestore_preservesBrushStyle() {
        scratchState.updateBrushStyle(ScratchBrushStyle.Hairy, hardness = 0.4f)
        scratchState.updateBrushWidthPx(60f)
        scratchState.handleDragStart(Offset(100f, 100f))
        scratchState.handleDragEnd()
        val snapshot = scratchState.snapshot()

        val other = ScratchState(initialBrushWidthPx = 10f, autoReveal = true)
        other.restore(snapshot)

        assertEquals(ScratchBrushStyle.Hairy, other.snapshot().brushStyle)
        assertEquals(0.4f, other.snapshot().brushHardness, 0.001f)
        assertEquals(60f, other.snapshot().brushWidthPx, 0.001f)
    }

    @Test
    fun scratchStateSaver_restoresWhenBundleWidensFloatsToDouble() {
        scratchState.updateBrushStyle(ScratchBrushStyle.Smooth, hardness = 0.3f)
        scratchState.updateBrushWidthPx(48f)
        scratchState.handleDragStart(Offset(120f, 120f))
        scratchState.handleDragEnd()
        val snapshot = scratchState.snapshot()
        val bundleLike = listOf(
            snapshot.layerWidth.toDouble(),
            snapshot.layerHeight.toDouble(),
            snapshot.scratchedCells.map { if (it) 1 else 0 },
            snapshot.scratchProgress.toDouble(),
            if (snapshot.isRevealed) 1 else 0,
            if (snapshot.hasStarted) 1 else 0,
            snapshot.revealThreshold.toDouble(),
            if (snapshot.autoReveal) 1 else 0,
            snapshot.brushWidthPx.toDouble(),
            snapshot.brushStyle.name,
            snapshot.brushHardness.toDouble(),
        )

        val restored = ScratchStateSaver.restore(bundleLike)!!
        assertEquals(scratchState.scratchProgress, restored.scratchProgress, 0.001f)
        assertEquals(scratchState.isRevealed, restored.isRevealed)
        assertEquals(ScratchBrushStyle.Smooth, restored.snapshot().brushStyle)
        assertEquals(0.3f, restored.snapshot().brushHardness, 0.001f)
        assertEquals(48f, restored.snapshot().brushWidthPx, 0.001f)
    }

    private fun scratchUntilRevealed(state: ScratchState) {
        listOf(60f, 100f, 140f, 180f, 220f, 260f, 300f, 340f).forEach { y ->
            state.handleDragStart(Offset(20f, y))
            state.handleDrag(Offset(380f, y))
            state.handleDragEnd()
        }
    }
}
