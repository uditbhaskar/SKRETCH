package com.skretch

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skretch.demo.DemoScratchReward
import com.skretch.demo.randomDemoScratchReward
import com.skretch.scratch.component.ScratchCard
import com.skretch.ui.theme.SKRETCHTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SKRETCHTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFF8F9FB),
                ) { innerPadding ->
                    ScratchDemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ScratchDemoScreen(modifier: Modifier = Modifier) {
    var scratchProgress by remember { mutableFloatStateOf(0f) }
    var isFinished by remember { mutableStateOf(false) }
    var cardSession by remember { mutableIntStateOf(0) }
    val currentReward = remember(cardSession) { randomDemoScratchReward() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.demo_scratch_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.demo_scratch_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5F6368),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            key(cardSession) {
                ScratchCard(
                    modifier = Modifier.size(width = 320.dp, height = 200.dp),
                    onScratchProgress = { scratchProgress = it },
                    onRevealed = { isFinished = true },
                ) {
                    ScratchRewardContent(reward = currentReward)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScratchProgressBar(
                    progress = scratchProgress,
                    modifier = Modifier.fillMaxWidth(0.8f),
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(
                        R.string.demo_scratch_progress,
                        (scratchProgress * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF5F6368),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.demo_scratch_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF80868B),
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isFinished) {
            FilledTonalIconButton(
                onClick = {
                    cardSession++
                    scratchProgress = 0f
                    isFinished = false
                },
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.demo_scratch_redo),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun ScratchProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE0E3E8)),
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1A73E8)),
            )
        }
    }
}

@Composable
private fun ScratchRewardContent(reward: DemoScratchReward) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(reward.backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = reward.emoji,
                style = MaterialTheme.typography.displayMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            reward.labelRes?.let { labelRes ->
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = reward.labelColor,
                    letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = stringResource(reward.titleRes),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = reward.titleColor,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(reward.subtitleRes),
                style = MaterialTheme.typography.bodyMedium,
                color = reward.subtitleColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(name = "Demo screen", showBackground = true)
@Composable
private fun ScratchDemoScreenPreview() {
    SKRETCHTheme {
        ScratchDemoScreen()
    }
}

@Preview(name = "Demo screen dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScratchDemoScreenDarkPreview() {
    SKRETCHTheme {
        ScratchDemoScreen()
    }
}
