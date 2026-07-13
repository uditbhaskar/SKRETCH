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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skretch.scratch.ScratchConstants
import com.skretch.scratch.design.ScratchDefaults
import com.skretch.scratch.state.ScratchState

/**
 * A scratch card that hides [content] behind a foil layer the user can scratch away.
 *
 * @param modifier modifier applied to this card
 * @param revealThreshold fraction of the card that must be scratched before reveal, between 0 and 1
 * @param brushWidth brush diameter; grid coverage and foil erasure share the same pixel radius
 * @param cornerRadius corner radius of the card
 * @param enabled when false, scratch gestures are ignored
 * @param onScratchStarted called the first time the user starts scratching
 * @param onScratchProgress called when scratch coverage changes; reports `1f` after auto-reveal
 * @param onRevealed called once when [revealThreshold] is reached
 * @param content content revealed underneath the foil
 * @author udit
 */
@Composable
fun ScratchCard(
    modifier: Modifier = Modifier,
    revealThreshold: Float = ScratchConstants.DEFAULT_REVEAL_THRESHOLD,
    brushWidth: Dp = ScratchConstants.DEFAULT_BRUSH_WIDTH_DP.dp,
    cornerRadius: Dp = ScratchConstants.DEFAULT_CORNER_RADIUS.dp,
    enabled: Boolean = true,
    onScratchStarted: () -> Unit = {},
    onScratchProgress: (Float) -> Unit = {},
    onRevealed: () -> Unit = {},
    content: @Composable () -> Unit,
) {

    val density = LocalDensity.current
    val brushWidthPx = with(density) { brushWidth.toPx() }
    val scratchState = remember {
        ScratchState(
            initialRevealThreshold = revealThreshold,
            initialBrushWidthPx = brushWidthPx,
        )
    }
    val cardShape = RoundedCornerShape(cornerRadius)

    LaunchedEffect(revealThreshold) {
        scratchState.updateRevealThreshold(revealThreshold)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = ScratchDefaults.cardElevation,
                shape = cardShape,
                clip = false,
            )
            .clip(cardShape)
            .border(
                width = ScratchDefaults.cardBorderWidth,
                color = ScratchDefaults.foilBorderDark.copy(alpha = 0.18f),
                shape = cardShape,
            ),
    ) {
        ScratchOverlay(
            state = scratchState,
            brushWidth = brushWidth,
            enabled = enabled,
            onScratchStarted = onScratchStarted,
            onScratchProgress = onScratchProgress,
            onRevealed = onRevealed,
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}

@Preview(name = "Default", showBackground = true, backgroundColor = 0xFFF2F4F8)
@Composable
private fun ScratchCardPreview() {
    MaterialTheme {
        ScratchCard(
            modifier = Modifier
                .padding(16.dp)
                .size(width = 340.dp, height = 220.dp),
            content = { ScratchCardPreviewContent() },
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
