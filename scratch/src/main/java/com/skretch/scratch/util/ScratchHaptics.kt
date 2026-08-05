package com.skretch.scratch.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.skretch.scratch.config.ScratchHapticIntensity
import com.skretch.scratch.config.ScratchHapticMode

/**
 * Scratch-specific haptic feedback for first touch and optional continuous drag ticks.
 *
 * @author uditbhaskar
 */
internal object ScratchHaptics {

    private var lastContinuousAtMs: Long = 0L

    /**
     * Plays a tactile pulse when the user begins scratching.
     *
     * @param hapticFeedback Compose haptic bridge from [androidx.compose.ui.platform.LocalHapticFeedback]
     * @param view host view from [androidx.compose.ui.platform.LocalView]
     * @param intensity how strong the pulse should feel
     * @author uditbhaskar
     */
    fun performFirstScratch(
        hapticFeedback: HapticFeedback,
        view: View,
        intensity: ScratchHapticIntensity = ScratchHapticIntensity.Medium,
    ) {
        if (intensity == ScratchHapticIntensity.Off) return
        when (intensity) {
            ScratchHapticIntensity.Light -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            ScratchHapticIntensity.Medium -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                vibrateScratchPulse(view.context, VibrationEffect.EFFECT_CLICK)
            }
            ScratchHapticIntensity.Strong -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                vibrateScratchPulse(view.context, VibrationEffect.EFFECT_HEAVY_CLICK)
            }
            ScratchHapticIntensity.Off -> Unit
        }
    }

    /**
     * Plays a light continuous tick while dragging when [mode] is [ScratchHapticMode.Continuous].
     *
     * @param hapticFeedback Compose haptic bridge
     * @param view host view for platform haptics
     * @param intensity overall haptic strength; [ScratchHapticIntensity.Off] disables ticks
     * @param mode first-touch-only vs continuous drag ticks
     * @param minIntervalMs minimum gap between continuous ticks
     * @author uditbhaskar
     */
    fun performDragTick(
        hapticFeedback: HapticFeedback,
        view: View,
        intensity: ScratchHapticIntensity,
        mode: ScratchHapticMode,
        minIntervalMs: Long = 32L,
    ) {
        if (intensity == ScratchHapticIntensity.Off || mode != ScratchHapticMode.Continuous) return
        val now = SystemClock.uptimeMillis()
        if (now - lastContinuousAtMs < minIntervalMs) return
        lastContinuousAtMs = now
        when (intensity) {
            ScratchHapticIntensity.Light -> {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            ScratchHapticIntensity.Medium -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            ScratchHapticIntensity.Strong -> {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            }
            ScratchHapticIntensity.Off -> Unit
        }
    }

    /**
     * Plays a short device vibration to reinforce the first scratch gesture.
     *
     * @param context context used to resolve the vibrator service
     * @param effect predefined vibration effect
     * @author uditbhaskar
     */
    @SuppressLint("MissingPermission")
    private fun vibrateScratchPulse(context: Context, effect: Int) {
        val vibrator = resolveVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createPredefined(effect))
    }

    /**
     * Resolves the default device vibrator.
     *
     * @param context context used to resolve [Context.VIBRATOR_MANAGER_SERVICE]
     * @return default vibrator, or null when unavailable
     * @author uditbhaskar
     */
    private fun resolveVibrator(context: Context): Vibrator? {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        return manager?.defaultVibrator
    }
}
