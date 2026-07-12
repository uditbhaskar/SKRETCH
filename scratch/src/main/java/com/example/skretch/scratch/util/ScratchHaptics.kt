package com.example.skretch.scratch.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Scratch-specific haptic feedback fired on the first drag of a card.
 *
 * @author udit
 */
internal object ScratchHaptics {

    /**
     * Plays a short tactile pulse when the user begins scratching.
     *
     * Uses both Compose and platform haptics so the feedback is noticeable on real devices.
     *
     * @param hapticFeedback Compose haptic bridge from [androidx.compose.ui.platform.LocalHapticFeedback]
     * @param view host view from [androidx.compose.ui.platform.LocalView]
     * @author udit
     */
    fun performFirstScratch(hapticFeedback: HapticFeedback, view: View) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        vibrateScratchPulse(view.context)
    }

    /**
     * Plays a short device vibration to reinforce the first scratch gesture.
     *
     * @param context context used to resolve the vibrator service
     * @author udit
     */
    @SuppressLint("MissingPermission")
    private fun vibrateScratchPulse(context: Context) {
        val vibrator = resolveVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }

    /**
     * Resolves the default device vibrator.
     *
     * @param context context used to resolve [Context.VIBRATOR_MANAGER_SERVICE]
     * @return default vibrator, or null when unavailable
     * @author udit
     */
    private fun resolveVibrator(context: Context): Vibrator? {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        return manager?.defaultVibrator
    }
}
