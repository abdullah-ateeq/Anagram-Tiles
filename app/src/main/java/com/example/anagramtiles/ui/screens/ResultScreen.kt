package com.example.anagramtiles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anagramtiles.data.AnagramGameState
import com.example.anagramtiles.data.GameResult

@Composable
fun ResultScreen(
    state: AnagramGameState,
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resultTitle = when (state.result) {
        GameResult.PERFECT_ANAGRAM -> "PERFECT ANAGRAM"
        GameResult.PLAYER_WIN -> "YOU WIN"
        GameResult.CPU_WIN -> "CPU WINS"
        GameResult.DRAW -> "DRAW"
        null -> if (state.playerScore >= state.cpuScore) "YOU WIN" else "CPU WINS"
    }

    val resultSubtitle = when (state.result) {
        GameResult.PERFECT_ANAGRAM -> "Every single answer was correct with zero mistakes."
        GameResult.PLAYER_WIN -> "Congratulations! You outsolved the CPU."
        GameResult.CPU_WIN -> "The CPU edged ahead this time. Try again!"
        GameResult.DRAW -> "A perfectly matched duel!"
        null -> ""
    }

    val titleColor = when (state.result) {
        GameResult.PERFECT_ANAGRAM -> Color(0xFFF57F17) // Gold / Amber
        GameResult.PLAYER_WIN -> MaterialTheme.colorScheme.primary
        GameResult.CPU_WIN -> MaterialTheme.colorScheme.secondary
        GameResult.DRAW -> MaterialTheme.colorScheme.tertiary
        null -> MaterialTheme.colorScheme.primary
    }

    val totalAttempts = state.playerAnswers.size + state.playerInvalidAttempts
    val accuracyPct = if (totalAttempts > 0) {
        ((state.playerAnswers.size.toFloat() / totalAttempts) * 100).toInt()
    } else 100

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = resultTitle,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = titleColor,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = resultSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Total Score Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "TOTAL SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOU",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${state.playerScore}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "VS",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "CPU",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${state.cpuScore}",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detailed Statistics Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "GAME SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResultRow(label = "PUZZLES PLAYED", value = "${state.totalPuzzles}")
                ResultRow(
                    label = "TOTAL ANSWERS",
                    value = "You: ${state.playerAnswers.size} · CPU: ${state.cpuAnswers.size}"
                )
                ResultRow(
                    label = "LONGEST WORD",
                    value = state.playerLongestWord ?: "None"
                )
                ResultRow(
                    label = "ACCURACY",
                    value = "$accuracyPct%"
                )
                ResultRow(
                    label = "INVALID ATTEMPTS",
                    value = "${state.playerInvalidAttempts}"
                )
                ResultRow(
                    label = "PASSES",
                    value = "${state.playerPasses}"
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons
        Button(
            onClick = onPlayAgain,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "PLAY AGAIN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateHome,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "HOME",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
