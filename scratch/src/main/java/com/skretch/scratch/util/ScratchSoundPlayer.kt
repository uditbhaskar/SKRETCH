package com.skretch.scratch.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.skretch.scratch.R
import com.skretch.scratch.config.ScratchSoundConfig

/**
 * Plays built-in scratch / reveal samples via [SoundPool].
 *
 * @author uditbhaskar
 */
internal class ScratchSoundPlayer(context: Context) {
    private val appContext = context.applicationContext
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val scratchId: Int = pool.load(appContext, R.raw.skretch_scratch, 1)
    private val revealId: Int = pool.load(appContext, R.raw.skretch_reveal, 1)
    private var lastScratchAtMs: Long = 0L

    /**
     * Plays the built-in scratch sample, throttled while dragging.
     *
     * @param force when true, ignore the throttle (first scratch)
     * @author uditbhaskar
     */
    fun playScratch(force: Boolean = false) {
        val now = android.os.SystemClock.uptimeMillis()
        if (!force && now - lastScratchAtMs < 70L) return
        lastScratchAtMs = now
        pool.play(scratchId, 0.55f, 0.55f, 1, 0, 1f)
    }

    /**
     * Plays the built-in reveal chime.
     *
     * @author uditbhaskar
     */
    fun playReveal() {
        pool.play(revealId, 0.8f, 0.8f, 1, 0, 1f)
    }

    /**
     * Releases the underlying [SoundPool].
     *
     * @author uditbhaskar
     */
    fun release() {
        pool.release()
    }
}

/**
 * Remembers a [ScratchSoundPlayer] for the composition lifetime when built-in sounds are enabled.
 *
 * @param config sound configuration
 * @return player when [ScratchSoundConfig.useBuiltIn] is true and [ScratchSoundConfig.enabled], else null
 * @author uditbhaskar
 */
@Composable
internal fun rememberScratchSoundPlayer(config: ScratchSoundConfig): ScratchSoundPlayer? {
    val context = LocalContext.current
    val enabled = config.enabled && config.useBuiltIn
    val player = remember(enabled) {
        if (enabled) ScratchSoundPlayer(context) else null
    }
    DisposableEffect(player) {
        onDispose { player?.release() }
    }
    return player
}
