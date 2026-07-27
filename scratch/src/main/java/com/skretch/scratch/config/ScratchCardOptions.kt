package com.skretch.scratch.config

/**
 * Reveal threshold as a fraction of scratched area between `0f` and `1f`.
 *
 * Companion [Default] uses [com.skretch.scratch.ScratchConstants.DEFAULT_REVEAL_THRESHOLD].
 *
 * @param fraction coerced coverage required before auto-reveal
 * @author uditbhaskar
 */
@JvmInline
value class RevealThreshold private constructor(val fraction: Float) {
    companion object {
        val Default = of(com.skretch.scratch.ScratchConstants.DEFAULT_REVEAL_THRESHOLD)

        /**
         * Creates a threshold clamped to `0f..1f`.
         *
         * @param fraction raw coverage fraction
         * @return clamped [RevealThreshold]
         * @author uditbhaskar
         */
        fun of(fraction: Float): RevealThreshold = RevealThreshold(fraction.coerceIn(0f, 1f))
    }
}

/**
 * How the cover disappears once the card is revealed.
 *
 * - [Fade]: fade cover alpha to zero
 * - [ScalePop]: fade and slightly scale the cover down
 * - [None]: hide the cover immediately
 *
 * @author uditbhaskar
 */
enum class ScratchRevealAnimation {
    Fade,
    ScalePop,
    None,
}

/**
 * Intensity of first-scratch haptic feedback.
 *
 * - [Off]: no haptic pulse
 * - [Light]: subtle tick
 * - [Medium]: standard first-scratch pulse
 * - [Strong]: stronger confirm pulse
 *
 * @author uditbhaskar
 */
enum class ScratchHapticIntensity {
    Off,
    Light,
    Medium,
    Strong,
}

/**
 * Card outline shape for chrome clipping.
 *
 * - [RoundedRect]: rounded rectangle using [ScratchCardChrome.cornerRadius]
 * - [Circle]: true circle inscribed in the card bounds (works when width ≠ height)
 * - [Ticket]: ticket-style outline with uneven corner radii
 *
 * @author uditbhaskar
 */
enum class ScratchCardShape {
    RoundedRect,
    Circle,
    Ticket,
}
