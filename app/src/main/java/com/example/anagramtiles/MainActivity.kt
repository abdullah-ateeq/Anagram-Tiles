package com.example.anagramtiles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.anagramtiles.ui.navigation.AnagramNavGraph
import com.example.anagramtiles.ui.theme.AnagramTilesTheme
import com.example.anagramtiles.ui.viewmodel.AnagramViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AnagramViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            AnagramTilesTheme(
                themeSetting = settings.theme,
                dynamicColor = settings.dynamicColor
            ) {
                AnagramNavGraph(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}