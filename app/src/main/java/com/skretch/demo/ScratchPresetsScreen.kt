package com.skretch.demo

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skretch.R
import com.skretch.scratch.config.ScratchCardPreset
import com.skretch.scratch.config.ScratchPresets

/**
 * Ready-made look picker for the demo. Selecting a preset returns via [onPresetSelected]
 * and [onBack]; does not edit individual Pattern / Brush / Shape chips.
 */
@Composable
internal fun ScratchPresetsScreen(
    selectedPreset: CatalogPreset,
    onPresetSelected: (CatalogPreset) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.demo_presets_back_cd),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.demo_presets_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF202124),
                )
                Text(
                    text = stringResource(R.string.demo_presets_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5F6368),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            items(CatalogPreset.readyMade, key = { it.name }) { preset ->
                PresetChoiceRow(
                    title = stringResource(preset.labelRes),
                    description = stringResource(preset.descriptionRes),
                    selected = selectedPreset == preset,
                    onClick = { onPresetSelected(preset) },
                )
            }
        }
    }
}

@Composable
private fun PresetChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) Color(0xFFE8F0FE) else Color(0xFFF8F9FA)
    val borderHint = if (selected) Color(0xFF1A73E8) else Color(0xFFE8EAED)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5F6368),
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = borderHint,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Demo catalog entry for a [ScratchPresets] look, plus [None] for free customization.
 */
internal enum class CatalogPreset(
    @param:StringRes val labelRes: Int,
    @param:StringRes val descriptionRes: Int,
) {
    None(R.string.demo_presets_none_selected, R.string.demo_presets_none_selected),
    Promo(R.string.demo_preset_promo, R.string.demo_preset_promo_desc),
    Wallet(R.string.demo_preset_wallet, R.string.demo_preset_wallet_desc),
    Game(R.string.demo_preset_game, R.string.demo_preset_game_desc),
    Matte(R.string.demo_preset_matte, R.string.demo_preset_matte_desc),
    Party(R.string.demo_preset_party, R.string.demo_preset_party_desc),
    Minimal(R.string.demo_preset_minimal, R.string.demo_preset_minimal_desc),
    ;

    /** Maps this catalog choice to a library [ScratchCardPreset], or null for [None]. */
    fun toLibraryPreset(): ScratchCardPreset? = when (this) {
        None -> null
        Promo -> ScratchPresets.promo()
        Wallet -> ScratchPresets.wallet()
        Game -> ScratchPresets.game()
        Matte -> ScratchPresets.matte()
        Party -> ScratchPresets.party()
        Minimal -> ScratchPresets.minimal()
    }

    companion object {
        /** Presets shown on the browse screen (excludes [None]). */
        val readyMade: List<CatalogPreset> = listOf(Promo, Wallet, Game, Matte, Party, Minimal)
    }
}
