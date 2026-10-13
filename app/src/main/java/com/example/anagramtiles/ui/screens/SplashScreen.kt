package com.example.anagramtiles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anagramtiles.data.LetterTile
import com.example.anagramtiles.ui.components.LetterTileView
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Initial scrambled state: T, A, C
    var currentLetters by remember { mutableStateOf(listOf('T', 'A', 'C')) }
    var showBonus by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 1. Initial pause with scrambled T A C
        delay(400)
        // 2. Rearrange to C A T
        currentLetters = listOf('C', 'A', 'T')
        delay(350)
        // 3. Show CAT formed and +20
        showBonus = true
        delay(300)
        // 4. Show title
        showTitle = true
        delay(450)
        // 5. Navigate to Home
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tile scramble animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentLetters.forEachIndexed { index, ch ->
                    LetterTileView(
                        tile = LetterTile(id = index, letter = ch, position = index),
                        positionIndex = index,
                        isSelected = false,
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bonus badge
            AnimatedVisibility(
                visible = showBonus,
                enter = fadeIn() + scaleIn()
            ) {
                Text(
                    text = "CAT  +20",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App identity
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(300))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ANAGRAM TILES",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"Shuffle letters. Find the word.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
