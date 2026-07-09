package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.NavigationGraph
import com.example.ui.theme.FocusBridgeTheme
import com.example.ui.screens.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entrance activity for Focus Bridge.
 * Configured with @AndroidEntryPoint for Hilt-injection support and establishes modern edge-to-edge UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Mandatory Edge-to-Edge full bleed handling
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val prefs by settingsViewModel.userPreferences.collectAsState()
            FocusBridgeTheme(
                darkTheme = prefs.darkModeEnabled,
                dynamicColor = prefs.dynamicColorEnabled
            ) {
                val navController = rememberNavController()
                NavigationGraph(navController = navController)
            }
        }
    }
}

