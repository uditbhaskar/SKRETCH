package com.skretch.scratch.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skretch.scratch.config.MainLayerConfig

/**
 * Renders the main (revealed) surface from [config].
 *
 * Prefers [MainLayerConfig.custom] when set; otherwise draws color and optional text.
 *
 * @param config main-layer color, text, or custom content
 * @author udit
 */
@Composable
internal fun MainLayerContent(config: MainLayerConfig) {
    val custom = config.custom
    if (custom != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            custom()
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(config.color),
        contentAlignment = Alignment.Center,
    ) {
        val text = config.text ?: return@Box
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = text.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = text.titleColor,
                textAlign = TextAlign.Center,
            )
            text.subtitle?.let { subtitle ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = text.subtitleColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
