package com.example.anagramtiles.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anagramtiles.data.AnagramGameState
import com.example.anagramtiles.data.GamePhase
import com.example.anagramtiles.data.PuzzlePhase
import com.example.anagramtiles.data.UserSettings
import com.example.anagramtiles.ui.components.AnswerHistorySheet
import com.example.anagramtiles.ui.components.CurrentWordPreview
import com.example.anagramtiles.ui.components.LetterTileView
import com.example.anagramtiles.ui.components.ScoreBoardView
import com.example.anagramtiles.ui.components.TimerProgressView

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: AnagramGameState,
    settings: UserSettings,
    showAnswersSheet: Boolean,
    showRestartDialog: Boolean,
    onTileClicked: (Int) -> Unit,
    onShuffle: () -> Unit,
    onReset: () -> Unit,
    onCheckWord: () -> Unit,
    onPass: () -> Unit,
    onNextPuzzle: () -> Unit,
    onOpenAnswersSheet: (Boolean) -> Unit,
    onRequestRestart: () -> Unit,
    onConfirmRestart: () -> Unit,
    onDismissRestart: () -> Unit,
    onNavigateResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    // When game over reached, allow navigation to result
    if (state.gamePhase == GamePhase.GAME_OVER) {
        onNavigateResult()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ANAGRAM TILES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Puzzle ${state.currentPuzzle} of ${state.totalPuzzles}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRequestRestart,
                modifier = Modifier.semantics { contentDescription = "Restart game" }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart Game",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scoreboard
        ScoreBoardView(
            playerScore = state.playerScore,
            cpuScore = state.cpuScore,
            playerAnswersCount = state.playerAnswers.size,
            cpuAnswersCount = state.cpuAnswers.size,
            currentPuzzle = state.currentPuzzle,
            totalPuzzles = state.totalPuzzles
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Timer
        TimerProgressView(
            timeRemaining = state.timeRemaining,
            timerOption = settings.timer
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Current Word Preview
        CurrentWordPreview(currentWord = state.currentWord)

        Spacer(modifier = Modifier.height(16.dp))

        // Letter Tiles Grid / Row
        val tileMap = state.tiles.associateBy { it.id }
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            state.currentOrder.forEachIndexed { index, tileId ->
                val tile = tileMap[tileId]
                if (tile != null) {
                    LetterTileView(
                        tile = tile,
                        positionIndex = index,
                        isSelected = state.selectedPosition == index,
                        onClick = { onTileClicked(index) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feedback / Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.cpuThinking -> {
                    Text(
                        text = "CPU THINKING...",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )
                }

                state.cpuLastWord != null && state.puzzlePhase == PuzzlePhase.PLAYER_TURN -> {
                    Text(
                        text = "CPU FOUND: ${state.cpuLastWord}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )
                }

                settings.showWordFeedback && state.lastFeedback != null -> {
                    val isSuccess = state.lastFeedback.startsWith("FOUND") ||
                            state.lastFeedback.startsWith("NEW ANAGRAM") ||
                            state.lastFeedback.startsWith("ALL FOUND")
                    val feedbackColor = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                    Text(
                        text = state.lastFeedback,
                        fontWeight = FontWeight.Bold,
                        color = feedbackColor,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Controls: CHECK WORD
        Button(
            onClick = onCheckWord,
            enabled = state.puzzlePhase == PuzzlePhase.PLAYER_TURN && !state.isProcessing,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .semantics { contentDescription = "Check current word" }
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "CHECK WORD",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary Controls: SHUFFLE, RESET, PASS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = onShuffle,
                enabled = state.puzzlePhase == PuzzlePhase.PLAYER_TURN && !state.isProcessing,
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .semantics { contentDescription = "Shuffle tiles" }
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SHUFFLE", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            }

            FilledTonalButton(
                onClick = onReset,
                enabled = state.puzzlePhase == PuzzlePhase.PLAYER_TURN && !state.isProcessing,
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .semantics { contentDescription = "Reset tile order" }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RESET", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            }

            OutlinedButton(
                onClick = onPass,
                enabled = state.puzzlePhase == PuzzlePhase.PLAYER_TURN && !state.isProcessing,
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                modifier = Modifier
                    .weight(0.9f)
                    .height(46.dp)
                    .semantics { contentDescription = "Pass puzzle" }
            ) {
                Text("PASS", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Round Complete Card (if round is completed)
        AnimatedVisibility(visible = state.puzzlePhase == PuzzlePhase.PUZZLE_COMPLETE) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ROUND COMPLETE",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You found ${state.playerAnswers.size} words · CPU found ${state.cpuAnswers.size} words",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (state.currentPuzzle >= state.totalPuzzles) {
                                onNavigateResult()
                            } else {
                                onNextPuzzle()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.currentPuzzle >= state.totalPuzzles) "VIEW FINAL RESULTS" else "NEXT PUZZLE",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Found Answers Summary Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenAnswersSheet(true) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOUND WORDS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val foundSummary = if (state.playerAnswers.isNotEmpty()) {
                        state.playerAnswers.joinToString(" · ") { it.word }
                    } else {
                        "No words found yet"
                    }
                    Text(
                        text = foundSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "View all answers",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ANSWERS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modal BottomSheet for Found Answers and Revealed Anagrams
    if (showAnswersSheet) {
        AnswerHistorySheet(
            playerAnswers = state.playerAnswers,
            cpuAnswers = state.cpuAnswers,
            allPossibleAnswers = state.puzzlePossibleAnswers,
            isPuzzleCompleted = state.puzzlePhase == PuzzlePhase.PUZZLE_COMPLETE,
            sheetState = sheetState,
            onDismissRequest = { onOpenAnswersSheet(false) }
        )
    }

    // Restart Confirmation Dialog
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = onDismissRestart,
            title = { Text("Restart Game", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to restart this anagram game? Current progress will be lost.") },
            confirmButton = {
                Button(
                    onClick = onConfirmRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Restart", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRestart) {
                    Text("Cancel")
                }
            }
        )
    }
}
