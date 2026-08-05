package com.skretch.scratch.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScratchConfigTest {

    @Test
    fun revealThreshold_clampsOutOfRangeValues() {
        assertEquals(0f, RevealThreshold.of(-1f).fraction, 0.001f)
        assertEquals(1f, RevealThreshold.of(2f).fraction, 0.001f)
        assertEquals(0.45f, RevealThreshold.Default.fraction, 0.001f)
    }

    @Test
    fun scratchBrush_presetsUseExpectedStyles() {
        assertEquals(ScratchBrushStyle.Circular, ScratchBrush.Circular.style)
        assertEquals(ScratchBrushStyle.Smooth, ScratchBrush.Smooth.style)
        assertEquals(ScratchBrushStyle.Hairy, ScratchBrush.Hairy.style)
        assertEquals(ScratchBrushStyle.Glitter, ScratchBrush.Glitter.style)
        assertEquals(64.dp, ScratchBrush.smooth(width = 64.dp).width)
        assertEquals(0.3f, ScratchBrush.smooth(hardness = 0.3f).hardness, 0.001f)
    }

    @Test
    fun scratchLayerConfig_defaultsToSilverPattern() {
        assertEquals(ScratchCoverPattern.Silver, ScratchLayerConfig.Default.pattern)
        assertTrue(ScratchLayerConfig.Default.text != null)
        assertTrue(ScratchLayerConfig.Default.custom == null)
        assertTrue(ScratchLayerConfig.Default.shimmer)
        assertFalse(ScratchLayerConfig.Default.sparkle)
    }

    @Test
    fun mainLayerConfig_keepsCustomNullByDefault() {
        assertTrue(MainLayerConfig.Default.custom == null)
        assertEquals(Color.White, MainLayerConfig.Default.color)
    }

    @Test
    fun mainLayerText_storesTitleAndSubtitle() {
        val text = MainLayerText(title = "₹20", subtitle = "Cashback")
        assertEquals("₹20", text.title)
        assertEquals("Cashback", text.subtitle)
    }

    @Test
    fun presets_returnBundledConfigs() {
        val wallet = ScratchPresets.wallet()
        assertEquals(ScratchCoverPattern.Gold, wallet.scratchLayer.pattern)
        assertTrue(wallet.scratchLayer.shimmer)
        assertEquals(ScratchRevealAnimation.ScalePop, wallet.revealAnimation)

        val game = ScratchPresets.game()
        assertEquals(ScratchBrushStyle.Smooth, game.brush.style)
        assertEquals(ScratchCardShape.Ticket, game.chrome.shape)

        val matte = ScratchPresets.matte()
        assertEquals(ScratchCoverPattern.Matte, matte.scratchLayer.pattern)
        assertEquals(ScratchCardShape.RoundedRect, matte.chrome.shape)

        val party = ScratchPresets.party()
        assertEquals(ScratchBrushStyle.Hairy, party.brush.style)
        assertEquals(ScratchCardShape.Circle, party.chrome.shape)

        val minimal = ScratchPresets.minimal()
        assertEquals(ScratchRevealAnimation.None, minimal.revealAnimation)
        assertEquals(ScratchHapticIntensity.Off, minimal.hapticIntensity)
    }
}
