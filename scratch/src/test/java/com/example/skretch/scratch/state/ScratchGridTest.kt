package com.example.skretch.scratch.state

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScratchGridTest {

    private lateinit var grid: ScratchGrid

    @Before
    fun setup() {
        grid = ScratchGrid(columns = 10, rows = 10)
        grid.resize(width = 200f, height = 200f)
    }

    @Test
    fun stampBrush_increasesScratchedCount() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 30f)

        assertTrue(grid.scratchedCount > 0)
        assertTrue(grid.progress > 0f)
    }

    @Test
    fun reset_clearsProgress() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 40f)
        assertTrue(grid.progress > 0f)

        grid.reset()

        assertEquals(0, grid.scratchedCount)
        assertEquals(0f, grid.progress, 0.001f)
    }
}
