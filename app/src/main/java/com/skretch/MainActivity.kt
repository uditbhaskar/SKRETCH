package com.skretch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.skretch.demo.ScratchCatalogScreen
import com.skretch.ui.theme.SKRETCHTheme

/**
 * Hosts the SKRETCH demo catalog ([ScratchCatalogScreen]).
 */
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
                    ScratchCatalogScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
