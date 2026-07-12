package com.example.skretch.scratch.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.skretch.scratch.ScratchConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun updateLayerSize_initializesGrid() {
        assertEquals(0f, scratchState.scratchProgress, 0.001f)
        assertFalse(scratchState.isRevealed)
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
    fun handleDrag_reachesThreshold_setsRevealed() {
        listOf(60f, 100f, 140f, 180f, 220f, 260f, 300f, 340f).forEach { y ->
            scratchState.handleDragStart(Offset(20f, y))
            scratchState.handleDrag(Offset(380f, y))
            scratchState.handleDragEnd()
        }

        assertTrue(scratchState.scratchProgress >= ScratchConstants.DEFAULT_REVEAL_THRESHOLD)
        assertTrue(scratchState.isRevealed)
        assertEquals(1f, scratchState.scratchProgress, 0.001f)
    }

    @Test
    fun reset_clearsScratchState() {
        scratchState.handleDragStart(Offset(100f, 100f))
        scratchState.handleDrag(Offset(200f, 200f))
        scratchState.handleDragEnd()

        scratchState.reset()

        assertEquals(0f, scratchState.scratchProgress, 0.001f)
        assertFalse(scratchState.isRevealed)
        assertFalse(scratchState.hasStarted)
    }

    @Test
    fun handleDrag_whenDisabled_doesNotScratch() {
        scratchState.updateScratchEnabled(false)

        scratchState.handleDragStart(Offset(100f, 100f))
        scratchState.handleDrag(Offset(200f, 200f))
        scratchState.handleDragEnd()

        assertEquals(0f, scratchState.scratchProgress, 0.001f)
        assertFalse(scratchState.hasStarted)
    }

    @Test
    fun handleDrag_whenAlreadyRevealed_doesNotIncreaseProgress() {
        listOf(60f, 100f, 140f, 180f, 220f, 260f, 300f, 340f).forEach { y ->
            scratchState.handleDragStart(Offset(20f, y))
            scratchState.handleDrag(Offset(380f, y))
            scratchState.handleDragEnd()
        }

        val progressAfterReveal = scratchState.scratchProgress
        assertTrue(scratchState.isRevealed)

        scratchState.handleDragStart(Offset(50f, 50f))
        scratchState.handleDrag(Offset(350f, 350f))
        scratchState.handleDragEnd()

        assertEquals(progressAfterReveal, scratchState.scratchProgress, 0.001f)
    }
}
