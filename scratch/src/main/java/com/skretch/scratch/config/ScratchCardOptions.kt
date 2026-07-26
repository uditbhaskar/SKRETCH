package com.skretch.scratch.config

/**
 * Reveal threshold as a fraction of scratched area between `0f` and `1f`.
 *
 * @param fraction coerced coverage required before auto-reveal
 * @author uditbhaskar
 */
@JvmInline
value class RevealThreshold private constructor(val fraction: Float) {
    companion object {
        /** Uses [com.skretch.scratch.ScratchConstants.DEFAULT_REVEAL_THRESHOLD]. */
        val Default = of(com.skretch.scratch.ScratchConstants.DEFAULT_REVEAL_THRESHOLD)

        /**
         * Creates a threshold clamped to `0f..1f`.
         *
         * @param fraction raw coverage fraction
         * @author uditbhaskar
         */
        fun of(fraction: Float): RevealThreshold = RevealThreshold(fraction.coerceIn(0f, 1f))
    }
}

/**
 * How the cover disappears once the card is revealed.
 *
 * @author uditbhaskar
 */
enum class ScratchRevealAnimation {
    /** Fade cover alpha to zero. */
    Fade,

    /** Fade and slightly scale the cover down. */
    ScalePop,

    /** Hide the cover immediately. */
    None,
}

/**
 * Intensity of first-scratch haptic feedback.
 *
 * @author uditbhaskar
 */
enum class ScratchHapticIntensity {
    /** No haptic pulse. */
    Off,

    /** Subtle tick. */
    Light,

    /** Standard first-scratch pulse. */
    Medium,

    /** Stronger confirm pulse. */
    Strong,
}

/**
 * Card outline shape for chrome clipping.
 *
 * @author uditbhaskar
 */
enum class ScratchCardShape {
    /** Rounded rectangle using [ScratchCardChrome.cornerRadius]. */
    RoundedRect,

    /** True circle inscribed in the card bounds (works when width ≠ height). */
    Circle,

    /** Ticket-style outline with uneven corner radii. */
    Ticket,
}
