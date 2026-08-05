package com.skretch.scratch.config

/**
 * How often haptics fire while scratching.
 *
 * - [FirstTouch]: pulse only when scratching begins
 * - [Continuous]: light ticks while dragging (throttled), plus a stronger first pulse
 *
 * @author uditbhaskar
 */
enum class ScratchHapticMode {
    FirstTouch,
    Continuous,
}
