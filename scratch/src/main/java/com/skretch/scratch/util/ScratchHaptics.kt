package com.skretch.scratch.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.skretch.scratch.config.ScratchHapticIntensity

/**
 * Scratch-specific haptic feedback fired on the first drag of a card.
 *
 * @author uditbhaskar
 */
internal object ScratchHaptics {

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
