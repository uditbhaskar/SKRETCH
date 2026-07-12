package com.skretch.scratch.state

import androidx.compose.ui.geometry.Offset
import com.skretch.scratch.ScratchConstants
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
    fun defaultGridSize_matchesScratchConstants() {
        val defaultGrid = ScratchGrid()
        defaultGrid.resize(width = 240f, height = 240f)

        assertEquals(
            ScratchConstants.GRID_COLUMNS * ScratchConstants.GRID_ROWS,
            defaultGrid.totalCells,
        )
    }

    @Test
    fun stampBrush_increasesScratchedCountAndProgress() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 30f)

        assertTrue(grid.scratchedCount > 0)
        assertTrue(grid.progress > 0f)
        assertTrue(grid.progress < 1f)
    }

    @Test
    fun stampBrush_doesNotDoubleCountCells() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 30f)
        val countAfterFirstStamp = grid.scratchedCount

        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 30f)

        assertEquals(countAfterFirstStamp, grid.scratchedCount)
    }

    @Test
    fun stampBrush_largeRadius_canCoverEntireGrid() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 150f)

        assertEquals(grid.totalCells, grid.scratchedCount)
        assertEquals(1f, grid.progress, 0.001f)
    }

    @Test
    fun reset_clearsProgressWithoutChangingDimensions() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 40f)
        assertTrue(grid.progress > 0f)

        grid.reset()

        assertEquals(0, grid.scratchedCount)
        assertEquals(0f, grid.progress, 0.001f)
        assertEquals(100, grid.totalCells)
    }

    @Test
    fun resize_withInvalidDimensions_clearsGrid() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 40f)

        grid.resize(width = 0f, height = 0f)

        assertEquals(0, grid.scratchedCount)
        assertEquals(0f, grid.progress, 0.001f)
    }

    @Test
    fun resize_whenDimensionsChange_clearsScratchedCells() {
        grid.stampBrush(center = Offset(100f, 100f), brushRadius = 40f)
        assertTrue(grid.scratchedCount > 0)

        grid.resize(width = 300f, height = 300f)

        assertEquals(0, grid.scratchedCount)
        assertEquals(0f, grid.progress, 0.001f)
    }

    @Test
    fun stampBrush_beforeResize_doesNothing() {
        val uninitializedGrid = ScratchGrid(columns = 10, rows = 10)

        uninitializedGrid.stampBrush(center = Offset(50f, 50f), brushRadius = 20f)

        assertEquals(0, uninitializedGrid.scratchedCount)
        assertEquals(0f, uninitializedGrid.progress, 0.001f)
    }
}
