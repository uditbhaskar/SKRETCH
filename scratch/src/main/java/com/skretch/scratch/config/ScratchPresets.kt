package com.skretch.scratch.config

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * One-line presets that fill both layers, brush, chrome, and reveal defaults.
 *
 * Factories: [promo], [wallet], [game], [matte], [party], [minimal].
 * Each returns a [ScratchCardPreset] for [com.skretch.scratch.component.ScratchCard].
 *
 * @author uditbhaskar
 */
object ScratchPresets {

    /**
     * Classic silver promo card.
     *
     * @param rewardTitle main-layer title
     * @param rewardSubtitle optional main-layer subtitle
     * @author uditbhaskar
     */
    fun promo(
        rewardTitle: String = "You won!",
        rewardSubtitle: String? = "Scratch to claim",
    ): ScratchCardPreset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(
            pattern = ScratchCoverPattern.Silver,
            text = ScratchSurfaceText.DefaultScratchHint,
        ),
        mainLayer = MainLayerConfig(
            color = Color.White,
            text = MainLayerText(title = rewardTitle, subtitle = rewardSubtitle),
        ),
        brush = ScratchBrush.circular(),
        chrome = ScratchCardChrome(shape = ScratchCardShape.RoundedRect),
    )

    /**
     * Gold wallet-style reward card.
     *
     * @param rewardTitle main-layer title
     * @param rewardSubtitle optional main-layer subtitle
     * @author uditbhaskar
     */
    fun wallet(
        rewardTitle: String = "₹50",
        rewardSubtitle: String? = "Added to wallet",
    ): ScratchCardPreset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(
            pattern = ScratchCoverPattern.Gold,
            text = ScratchSurfaceText("SCRATCH TO CLAIM"),
            shimmer = true,
        ),
        mainLayer = MainLayerConfig(
            color = Color(0xFFFFF8E7),
            text = MainLayerText(
                title = rewardTitle,
                subtitle = rewardSubtitle,
                titleColor = Color(0xFFB0892E),
            ),
        ),
        brush = ScratchBrush.smooth(width = 56.dp),
        chrome = ScratchCardChrome(
            shape = ScratchCardShape.RoundedRect,
            cornerRadius = 16.dp,
            elevation = 10.dp,
        ),
        revealAnimation = ScratchRevealAnimation.ScalePop,
        hapticIntensity = ScratchHapticIntensity.Strong,
    )

    /**
     * Playful holographic game card with a smooth brush and ticket chrome.
     *
     * @param rewardTitle main-layer title
     * @param rewardSubtitle optional main-layer subtitle
     * @author uditbhaskar
     */
    fun game(
        rewardTitle: String = "Bonus unlocked",
        rewardSubtitle: String? = "Keep playing",
    ): ScratchCardPreset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(
            pattern = ScratchCoverPattern.Holographic,
            text = ScratchSurfaceText("SCRATCH!"),
            shimmer = true,
        ),
        mainLayer = MainLayerConfig(
            color = Color(0xFF10131A),
            text = MainLayerText(
                title = rewardTitle,
                subtitle = rewardSubtitle,
                titleColor = Color(0xFFE8F7FF),
                subtitleColor = Color(0xFFA78BFA),
            ),
        ),
        brush = ScratchBrush.smooth(width = 60.dp, hardness = 0.35f),
        chrome = ScratchCardChrome(shape = ScratchCardShape.Ticket, cornerRadius = 18.dp),
        revealAnimation = ScratchRevealAnimation.ScalePop,
        hapticIntensity = ScratchHapticIntensity.Medium,
    )

    /**
     * Soft matte rounded card for quieter promo surfaces.
     *
     * @param rewardTitle main-layer title
     * @param rewardSubtitle optional main-layer subtitle
     * @author uditbhaskar
     */
    fun matte(
        rewardTitle: String = "Offer inside",
        rewardSubtitle: String? = "Scratch to unlock",
    ): ScratchCardPreset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(
            pattern = ScratchCoverPattern.Matte,
            text = ScratchSurfaceText("SCRATCH"),
        ),
        mainLayer = MainLayerConfig(
            color = Color(0xFFF3F4F6),
            text = MainLayerText(
                title = rewardTitle,
                subtitle = rewardSubtitle,
                titleColor = Color(0xFF374151),
                subtitleColor = Color(0xFF6B7280),
            ),
        ),
        brush = ScratchBrush.circular(width = 48.dp),
        chrome = ScratchCardChrome(shape = ScratchCardShape.RoundedRect, cornerRadius = 16.dp),
        revealAnimation = ScratchRevealAnimation.Fade,
        hapticIntensity = ScratchHapticIntensity.Light,
    )

    /**
     * Circular grain card with a bristly hairy brush.
     *
     * @param rewardTitle main-layer title
     * @param rewardSubtitle optional main-layer subtitle
     * @author uditbhaskar
     */
    fun party(
        rewardTitle: String = "Surprise!",
        rewardSubtitle: String? = "You found a reward",
    ): ScratchCardPreset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(
            pattern = ScratchCoverPattern.Grain,
            text = ScratchSurfaceText("SCRATCH ME"),
            shimmer = true,
        ),
        mainLayer = MainLayerConfig(
            color = Color(0xFFFFF1F2),
            text = MainLayerText(
                title = rewardTitle,
                subtitle = rewardSubtitle,
                titleColor = Color(0xFFBE123C),
                subtitleColor = Color(0xFF9F1239),
            ),
        ),
        brush = ScratchBrush.hairy(width = 64.dp),
        chrome = ScratchCardChrome(shape = ScratchCardShape.Circle, cornerRadius = 999.dp),
        revealAnimation = ScratchRevealAnimation.ScalePop,
        hapticIntensity = ScratchHapticIntensity.Strong,
    )

    /**
     * Minimal silver card with a thin smooth brush.
     *
     * @param rewardTitle main-layer title
     * @param rewardSubtitle optional main-layer subtitle
     * @author uditbhaskar
     */
    fun minimal(
        rewardTitle: String = "Revealed",
        rewardSubtitle: String? = "Thanks for playing",
    ): ScratchCardPreset = ScratchCardPreset(
        scratchLayer = ScratchLayerConfig(
            pattern = ScratchCoverPattern.Silver,
            text = ScratchSurfaceText("SCRATCH"),
        ),
        mainLayer = MainLayerConfig(
            color = Color.White,
            text = MainLayerText(
                title = rewardTitle,
                subtitle = rewardSubtitle,
                titleColor = Color(0xFF111827),
                subtitleColor = Color(0xFF6B7280),
            ),
        ),
        brush = ScratchBrush.smooth(width = 36.dp, hardness = 0.25f),
        chrome = ScratchCardChrome(
            shape = ScratchCardShape.RoundedRect,
            cornerRadius = 10.dp,
            elevation = 2.dp,
        ),
        revealAnimation = ScratchRevealAnimation.None,
        hapticIntensity = ScratchHapticIntensity.Off,
        autoReveal = true,
    )
}

/**
 * Bundled card configuration returned by [ScratchPresets].
 *
 * Pass to [com.skretch.scratch.component.ScratchCard] via the `preset =` overload.
 * Reveal animation, haptic intensity, threshold, and [autoReveal] come from this object
 * (they are not separate parameters on the preset overload).
 *
 * @author uditbhaskar
 */
@Immutable
data class ScratchCardPreset(
    /** Cover surface (pattern, text, shimmer, etc.). */
    val scratchLayer: ScratchLayerConfig,
    /** Revealed reward surface. */
    val mainLayer: MainLayerConfig,
    /** Stamp style and size. */
    val brush: ScratchBrush,
    /** Elevation, border, and outline shape. */
    val chrome: ScratchCardChrome = ScratchCardChrome.Default,
    /** Coverage required before auto-reveal. */
    val revealThreshold: RevealThreshold = RevealThreshold.Default,
    /** How the cover disappears after reveal. */
    val revealAnimation: ScratchRevealAnimation = ScratchRevealAnimation.Fade,
    /** First-scratch haptic strength. */
    val hapticIntensity: ScratchHapticIntensity = ScratchHapticIntensity.Medium,
    /** When false, call [com.skretch.scratch.state.ScratchState.reveal] yourself. */
    val autoReveal: Boolean = true,
)
