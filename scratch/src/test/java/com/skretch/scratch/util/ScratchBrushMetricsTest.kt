package com.skretch.scratch.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ScratchBrushMetricsTest {

    @Test
    fun radiusFromWidthPx_isHalfWidth() {
        assertEquals(26f, ScratchBrushMetrics.radiusFromWidthPx(52f), 0.001f)
    }

    @Test
    fun radiusFromWidthPx_clampsToMinimumOnePixel() {
        assertEquals(1f, ScratchBrushMetrics.radiusFromWidthPx(0f), 0.001f)
        assertEquals(1f, ScratchBrushMetrics.radiusFromWidthPx(1f), 0.001f)
    }
}
