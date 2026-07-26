package com.skretch.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skretch.R
import com.skretch.scratch.component.ScratchCard
import com.skretch.scratch.config.RevealThreshold
import com.skretch.scratch.config.ScratchBrush
import com.skretch.scratch.config.ScratchBrushStyle
import com.skretch.scratch.config.ScratchCardChrome
import com.skretch.scratch.config.ScratchCardPreset
import com.skretch.scratch.config.ScratchCardShape
import com.skretch.scratch.config.ScratchCoverPattern
import com.skretch.scratch.config.ScratchHapticIntensity
import com.skretch.scratch.config.ScratchLayerConfig
import com.skretch.scratch.config.ScratchRevealAnimation
import com.skretch.scratch.config.ScratchSurfaceText
import com.skretch.scratch.state.rememberScratchState

/**
 * Interactive catalog: live card plus Pattern / Brush / Shape / Extras chips,
 * with a separate presets screen for ready-made looks.
 */
@Composable
fun ScratchCatalogScreen(modifier: Modifier = Modifier) {
    var selectedPattern by rememberSaveable(stateSaver = enumNameSaver()) {
        mutableStateOf(ScratchCoverPattern.Silver)
    }
    var selectedBrush by rememberSaveable(stateSaver = enumNameSaver()) {
        mutableStateOf(ScratchBrushStyle.Circular)
    }
    var selectedPreset by rememberSaveable(stateSaver = enumNameSaver()) {
        mutableStateOf(CatalogPreset.None)
    }
    var selectedShape by rememberSaveable(stateSaver = enumNameSaver()) {
        mutableStateOf(ScratchCardShape.RoundedRect)
    }
    var selectedRevealAnimation by rememberSaveable(stateSaver = enumNameSaver()) {
        mutableStateOf(ScratchRevealAnimation.Fade)
    }
    var selectedHaptic by rememberSaveable(stateSaver = enumNameSaver()) {
        mutableStateOf(ScratchHapticIntensity.Medium)
    }
    var shimmerEnabled by rememberSaveable { mutableStateOf(false) }
    var multiTouch by rememberSaveable { mutableStateOf(false) }
    var autoReveal by rememberSaveable { mutableStateOf(true) }
    var isFinished by rememberSaveable { mutableStateOf(false) }
    var cardSession by rememberSaveable { mutableIntStateOf(0) }
    var rewardIndex by rememberSaveable { mutableIntStateOf(randomDemoScratchRewardIndex()) }
    var showingPresets by rememberSaveable { mutableStateOf(false) }
    val skipNextFilterReset = remember { mutableStateOf(true) }
    val catalogScroll = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }
    val reward = remember(rewardIndex) { demoScratchRewardAt(rewardIndex) }
    val scratchState = rememberScratchState(autoReveal = autoReveal)

    fun applyLibraryPreset(config: ScratchCardPreset) {
        selectedPattern = config.scratchLayer.pattern
        selectedBrush = config.brush.style
        selectedShape = config.chrome.shape
        shimmerEnabled = config.scratchLayer.shimmer
        selectedRevealAnimation = config.revealAnimation
        selectedHaptic = config.hapticIntensity
        autoReveal = config.autoReveal
    }

    fun applyPreset(preset: CatalogPreset) {
        selectedPreset = preset
        preset.toLibraryPreset()?.let(::applyLibraryPreset)
    }

    fun leavePresetMode() {
        selectedPreset = CatalogPreset.None
    }

    fun resetCard() {
        cardSession++
        rewardIndex = randomDemoScratchRewardIndex()
        isFinished = false
        scratchState.reset()
    }

    fun resetOptions() {
        skipNextFilterReset.value = true
        selectedPreset = CatalogPreset.None
        selectedPattern = ScratchCoverPattern.Silver
        selectedBrush = ScratchBrushStyle.Circular
        selectedShape = ScratchCardShape.RoundedRect
        selectedRevealAnimation = ScratchRevealAnimation.Fade
        selectedHaptic = ScratchHapticIntensity.Medium
        shimmerEnabled = false
        multiTouch = false
        autoReveal = true
        resetCard()
    }

    LaunchedEffect(
        selectedPattern,
        selectedBrush,
        selectedShape,
        shimmerEnabled,
        autoReveal,
        selectedRevealAnimation,
        selectedHaptic,
    ) {
        if (skipNextFilterReset.value) {
            skipNextFilterReset.value = false
            isFinished = scratchState.isRevealed
            return@LaunchedEffect
        }
        scratchState.reset()
        isFinished = false
    }

    LaunchedEffect(scratchState.isRevealed) {
        isFinished = scratchState.isRevealed
    }

    if (showingPresets) {
        ScratchPresetsScreen(
            selectedPreset = selectedPreset,
            onPresetSelected = { preset ->
                applyPreset(preset)
                showingPresets = false
            },
            onBack = { showingPresets = false },
            modifier = modifier,
        )
        return
    }

    val (cardWidth, cardHeight) = cardSizeForShape(selectedShape)
    val activeBrush = when (selectedBrush) {
        ScratchBrushStyle.Circular -> ScratchBrush.circular()
        ScratchBrushStyle.Smooth -> ScratchBrush.smooth(hardness = 0.3f)
        ScratchBrushStyle.Hairy -> ScratchBrush.hairy()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(catalogScroll)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.demo_scratch_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.demo_catalog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F6368),
                )
            }
            FilledTonalIconButton(
                onClick = { resetOptions() },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = stringResource(R.string.demo_reset_options_cd),
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = { showingPresets = true }) {
            Icon(
                imageVector = Icons.Rounded.Style,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = if (selectedPreset == CatalogPreset.None) {
                    stringResource(R.string.demo_open_presets)
                } else {
                    stringResource(
                        R.string.demo_open_presets_active,
                        stringResource(selectedPreset.labelRes),
                    )
                },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ChipRow(title = "Pattern") {
            ScratchCoverPattern.entries.forEach { pattern ->
                CatalogFilterChip(
                    selected = selectedPattern == pattern,
                    label = pattern.name,
                    onClick = {
                        leavePresetMode()
                        selectedPattern = pattern
                    },
                )
            }
        }
        ChipRow(title = "Brush") {
            ScratchBrushStyle.entries.forEach { style ->
                CatalogFilterChip(
                    selected = selectedBrush == style,
                    label = style.name,
                    onClick = {
                        leavePresetMode()
                        selectedBrush = style
                    },
                )
            }
        }
        ChipRow(title = "Shape") {
            ScratchCardShape.entries.forEach { shape ->
                CatalogFilterChip(
                    selected = selectedShape == shape,
                    label = shapeLabel(shape),
                    onClick = {
                        leavePresetMode()
                        selectedShape = shape
                    },
                )
            }
        }
        ChipRow(title = "Extras") {
            CatalogFilterChip(
                selected = shimmerEnabled,
                label = "Shimmer",
                onClick = {
                    leavePresetMode()
                    shimmerEnabled = !shimmerEnabled
                },
            )
            CatalogFilterChip(
                selected = multiTouch,
                label = "Multi-touch",
                onClick = { multiTouch = !multiTouch },
            )
            CatalogFilterChip(
                selected = autoReveal,
                label = "Auto-reveal",
                onClick = {
                    leavePresetMode()
                    autoReveal = !autoReveal
                },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center,
        ) {
            key(
                cardSession,
                selectedPattern,
                selectedBrush,
                selectedShape,
                shimmerEnabled,
                autoReveal,
                selectedRevealAnimation,
                selectedHaptic,
            ) {
                ScratchCard(
                    modifier = Modifier.size(cardWidth, cardHeight),
                    scratchLayer = ScratchLayerConfig(
                        pattern = selectedPattern,
                        text = ScratchSurfaceText(stringResource(R.string.demo_scratch_foil_label)),
                        shimmer = shimmerEnabled,
                    ),
                    brush = activeBrush,
                    chrome = ScratchCardChrome(shape = selectedShape),
                    revealThreshold = RevealThreshold.Default,
                    revealAnimation = selectedRevealAnimation,
                    hapticIntensity = selectedHaptic,
                    autoReveal = autoReveal,
                    multiTouchEnabled = multiTouch,
                    state = scratchState,
                    onRevealed = { isFinished = true },
                ) {
                    ScratchRewardContent(reward = reward)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.demo_scratch_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF80868B),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))
        FilledTonalButton(onClick = { resetCard() }) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(R.string.demo_scratch_redo))
        }

        if (!autoReveal && !isFinished) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.demo_manual_reveal),
                color = Color(0xFF1A73E8),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { scratchState.reveal() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun cardSizeForShape(shape: ScratchCardShape): Pair<Dp, Dp> = when (shape) {
    ScratchCardShape.Circle -> 220.dp to 220.dp
    ScratchCardShape.RoundedRect,
    ScratchCardShape.Ticket,
    -> 320.dp to 200.dp
}

private fun shapeLabel(shape: ScratchCardShape): String = when (shape) {
    ScratchCardShape.RoundedRect -> "Rounded"
    ScratchCardShape.Circle -> "Circle"
    ScratchCardShape.Ticket -> "Ticket"
}

@Composable
private fun CatalogFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        selected = selected,
        onClick = onClick,
        shape = shape,
        color = if (selected) Color(0xFFE8F0FE) else Color.Transparent,
        contentColor = if (selected) Color(0xFF1967D2) else Color(0xFF3C4043),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color(0xFF1A73E8) else Color(0xFFDADCE0),
        ),
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 32.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ChipRow(
    title: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF5F6368),
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(10.dp))
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = reward.emoji, style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(6.dp))
            reward.labelRes?.let { labelRes ->
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = reward.labelColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = stringResource(reward.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = reward.titleColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(reward.subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = reward.subtitleColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
