package com.skretch.scratch.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skretch.scratch.config.MainLayerConfig
import com.skretch.scratch.config.MainLayerText
import com.skretch.scratch.config.RevealThreshold
import com.skretch.scratch.config.ScratchAccessibility
import com.skretch.scratch.config.ScratchBrush
import com.skretch.scratch.config.ScratchCardChrome
import com.skretch.scratch.config.ScratchCardPreset
import com.skretch.scratch.config.ScratchCoverPattern
import com.skretch.scratch.config.ScratchHapticIntensity
import com.skretch.scratch.config.ScratchLayerConfig
import com.skretch.scratch.config.ScratchPresets
import com.skretch.scratch.config.ScratchRevealAnimation
import com.skretch.scratch.config.ScratchSoundConfig
import com.skretch.scratch.config.ScratchSurfaceText
import com.skretch.scratch.state.ScratchState
import com.skretch.scratch.state.rememberScratchState

/**
 * A scratch card with a configurable cover surface and a revealed main surface underneath.
 *
 * @param modifier modifier applied to this card
 * @param scratchLayer cover color, pattern, image, text, shimmer, or custom composable
 * @param mainLayer revealed color, text, or custom composable
 * @param brush brush style, diameter, and hardness
 * @param chrome elevation, border, and outline shape
 * @param revealThreshold coverage required before auto-reveal
 * @param revealAnimation how the cover disappears
 * @param hapticIntensity first-scratch haptic strength
 * @param sound optional scratch / reveal sound hooks
 * @param accessibility TalkBack labels and reveal action
 * @param autoReveal when false, call [ScratchState.reveal] yourself
 * @param multiTouchEnabled when true, all active pointers scratch
 * @param enabled when false, scratch gestures are ignored
 * @param state optional hoisted scratch state; when null a state is remembered internally
 * @param onScratchStarted called the first time the user starts scratching
 * @param onScratchProgress called when scratch coverage changes; reports `1f` after reveal
 * @param onRevealed called once when the card reveals
 * @author uditbhaskar
 */
@Composable
fun ScratchCard(
    modifier: Modifier = Modifier,
    scratchLayer: ScratchLayerConfig = ScratchLayerConfig.Default,
    mainLayer: MainLayerConfig = MainLayerConfig.Default,
    brush: ScratchBrush = ScratchBrush.Circular,
    chrome: ScratchCardChrome = ScratchCardChrome.Default,
    revealThreshold: RevealThreshold = RevealThreshold.Default,
    revealAnimation: ScratchRevealAnimation = ScratchRevealAnimation.Fade,
    hapticIntensity: ScratchHapticIntensity = ScratchHapticIntensity.Medium,
    sound: ScratchSoundConfig = ScratchSoundConfig.Off,
    accessibility: ScratchAccessibility = ScratchAccessibility.Default,
    autoReveal: Boolean = true,
    multiTouchEnabled: Boolean = false,
    enabled: Boolean = true,
    state: ScratchState? = null,
    onScratchStarted: () -> Unit = {},
    onScratchProgress: (Float) -> Unit = {},
    onRevealed: () -> Unit = {},
) {
    val density = LocalDensity.current
    val brushWidthPx = with(density) { brush.width.toPx() }
    val scratchState = state ?: rememberScratchState(
        revealThreshold = revealThreshold,
        autoReveal = autoReveal,
    )
    SideEffect {
        scratchState.updateBrushWidthPx(brushWidthPx)
    }

    LaunchedEffect(revealThreshold) {
        scratchState.updateRevealThreshold(revealThreshold)
    }
    LaunchedEffect(autoReveal) {
        scratchState.updateAutoReveal(autoReveal)
    }

    val cardShape = chrome.toShape()
    Box(
        modifier = modifier
            .shadow(
                elevation = chrome.elevation,
                shape = cardShape,
                clip = false,
            )
            .clip(cardShape)
            .border(
                width = chrome.borderWidth,
                color = chrome.borderColor,
                shape = cardShape,
            ),
    ) {
        ScratchOverlay(
            state = scratchState,
            scratchLayer = scratchLayer,
            mainLayer = mainLayer,
            brush = brush,
            enabled = enabled,
            multiTouchEnabled = multiTouchEnabled,
            revealAnimation = revealAnimation,
            hapticIntensity = hapticIntensity,
            sound = sound,
            accessibility = accessibility,
            onScratchStarted = onScratchStarted,
            onScratchProgress = onScratchProgress,
            onRevealed = onRevealed,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Applies a [ScratchCardPreset] in one call.
 *
 * Cover, main layer, brush, chrome, reveal threshold / animation, haptic intensity, and
 * auto-reveal are taken from [preset]. You can still override [sound], [accessibility],
 * multitouch, and callbacks.
 *
 * @param preset bundled layer, brush, chrome, and reveal settings
 * @param modifier modifier applied to this card
 * @param state optional hoisted scratch state
 * @param enabled when false, scratch gestures are ignored
 * @param multiTouchEnabled when true, all active pointers scratch
 * @param sound optional scratch / reveal sound hooks
 * @param accessibility TalkBack labels and reveal action
 * @param onScratchStarted called the first time the user starts scratching
 * @param onScratchProgress called when scratch coverage changes
 * @param onRevealed called once when the card reveals
 * @author uditbhaskar
 */
@Composable
fun ScratchCard(
    preset: ScratchCardPreset,
    modifier: Modifier = Modifier,
    state: ScratchState? = null,
    enabled: Boolean = true,
    multiTouchEnabled: Boolean = false,
    sound: ScratchSoundConfig = ScratchSoundConfig.Off,
    accessibility: ScratchAccessibility = ScratchAccessibility.Default,
    onScratchStarted: () -> Unit = {},
    onScratchProgress: (Float) -> Unit = {},
    onRevealed: () -> Unit = {},
) {
    ScratchCard(
        modifier = modifier,
        scratchLayer = preset.scratchLayer,
        mainLayer = preset.mainLayer,
        brush = preset.brush,
        chrome = preset.chrome,
        revealThreshold = preset.revealThreshold,
        revealAnimation = preset.revealAnimation,
        hapticIntensity = preset.hapticIntensity,
        sound = sound,
        accessibility = accessibility,
        autoReveal = preset.autoReveal,
        multiTouchEnabled = multiTouchEnabled,
        enabled = enabled,
        state = state,
        onScratchStarted = onScratchStarted,
        onScratchProgress = onScratchProgress,
        onRevealed = onRevealed,
    )
}

/**
 * Convenience overload that keeps a trailing [content] lambda for the main (reward) layer.
 *
 * Equivalent to passing `mainLayer = MainLayerConfig(custom = content)`.
 *
 * @param modifier modifier applied to this card
 * @param scratchLayer cover color, pattern, image, text, shimmer, or custom composable
 * @param brush brush style, diameter, and hardness
 * @param chrome elevation, border, and outline shape
 * @param revealThreshold coverage required before auto-reveal
 * @param revealAnimation how the cover disappears
 * @param hapticIntensity first-scratch haptic strength
 * @param sound optional scratch / reveal sound hooks
 * @param accessibility TalkBack labels and reveal action
 * @param autoReveal when false, call [ScratchState.reveal] yourself
 * @param multiTouchEnabled when true, all active pointers scratch
 * @param enabled when false, scratch gestures are ignored
 * @param state optional hoisted scratch state; when null a state is remembered internally
 * @param onScratchStarted called the first time the user starts scratching
 * @param onScratchProgress called when scratch coverage changes; reports `1f` after reveal
 * @param onRevealed called once when the card reveals
 * @param content composable drawn as the revealed main layer
 * @author uditbhaskar
 */
@Composable
fun ScratchCard(
    modifier: Modifier = Modifier,
    scratchLayer: ScratchLayerConfig = ScratchLayerConfig.Default,
    brush: ScratchBrush = ScratchBrush.Circular,
    chrome: ScratchCardChrome = ScratchCardChrome.Default,
    revealThreshold: RevealThreshold = RevealThreshold.Default,
    revealAnimation: ScratchRevealAnimation = ScratchRevealAnimation.Fade,
    hapticIntensity: ScratchHapticIntensity = ScratchHapticIntensity.Medium,
    sound: ScratchSoundConfig = ScratchSoundConfig.Off,
    accessibility: ScratchAccessibility = ScratchAccessibility.Default,
    autoReveal: Boolean = true,
    multiTouchEnabled: Boolean = false,
    enabled: Boolean = true,
    state: ScratchState? = null,
    onScratchStarted: () -> Unit = {},
    onScratchProgress: (Float) -> Unit = {},
    onRevealed: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    ScratchCard(
        modifier = modifier,
        scratchLayer = scratchLayer,
        mainLayer = MainLayerConfig(custom = content),
        brush = brush,
        chrome = chrome,
        revealThreshold = revealThreshold,
        revealAnimation = revealAnimation,
        hapticIntensity = hapticIntensity,
        sound = sound,
        accessibility = accessibility,
        autoReveal = autoReveal,
        multiTouchEnabled = multiTouchEnabled,
        enabled = enabled,
        state = state,
        onScratchStarted = onScratchStarted,
        onScratchProgress = onScratchProgress,
        onRevealed = onRevealed,
    )
}

@Preview(name = "Default", showBackground = true, backgroundColor = 0xFFF2F4F8)
@Composable
private fun ScratchCardPreview() {
    MaterialTheme {
        ScratchCard(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 340.dp, height = 220.dp),
            scratchLayer = ScratchLayerConfig(
                pattern = ScratchCoverPattern.Silver,
                text = ScratchSurfaceText.DefaultScratchHint,
            ),
            mainLayer = MainLayerConfig(
                color = Color.White,
                text = MainLayerText(
                    title = PREVIEW_REWARD_VALUE,
                    subtitle = PREVIEW_REWARD_SUBTITLE,
                ),
            ),
        )
    }
}

@Preview(name = "Wallet preset", showBackground = true, backgroundColor = 0xFFF2F4F8)
@Composable
private fun ScratchCardWalletPreview() {
    MaterialTheme {
        ScratchCard(
            preset = ScratchPresets.wallet(),
            modifier = Modifier
                .padding(16.dp)
                .size(width = 340.dp, height = 220.dp),
        )
    }
}

@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = 0xFF10131A)
@Composable
private fun ScratchCardDarkPreview() {
    MaterialTheme {
        ScratchCard(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 340.dp, height = 220.dp),
            content = { ScratchCardPreviewContent() },
        )
    }
}

@Composable
private fun ScratchCardPreviewContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "🏆",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = PREVIEW_REWARD_VALUE,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A73E8),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = PREVIEW_REWARD_SUBTITLE,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5F6368),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val PREVIEW_REWARD_VALUE = "₹10"
private const val PREVIEW_REWARD_SUBTITLE = "Credited to your account"
