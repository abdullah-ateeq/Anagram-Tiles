package com.example.anagramtiles.data

/**
 * Complete immutable UI and gameplay state for Anagram Tiles.
 */
data class AnagramGameState(
    val currentPuzzle: Int = 1,
    val totalPuzzles: Int = 10,
    val tiles: List<LetterTile> = emptyList(),
    val currentOrder: List<Int> = emptyList(), // Tile IDs in current arrangement
    val initialOrder: List<Int> = emptyList(), // Tile IDs in initial puzzle scramble
    val selectedPosition: Int? = null,
    val currentWord: String = "",
    val possibleAnswerCount: Int? = null, // Only revealed after puzzle completes
    val usedAnswers: Set<String> = emptySet(),
    val playerAnswers: List<AnagramAnswer> = emptyList(),
    val cpuAnswers: List<AnagramAnswer> = emptyList(),
    val playerScore: Int = 0,
    val cpuScore: Int = 0,
    val playerInvalidAttempts: Int = 0,
    val cpuInvalidAttempts: Int = 0,
    val playerPasses: Int = 0,
    val cpuPasses: Int = 0,
    val timeRemaining: Int = 60,
    val puzzlePhase: PuzzlePhase = PuzzlePhase.PLAYER_TURN,
    val gamePhase: GamePhase = GamePhase.PLAYING,
    val lastFeedback: String? = null,
    val lastSubmittedWord: String? = null,
    val lastScore: Int = 0,
    val playerLongestWord: String? = null,
    val cpuLongestWord: String? = null,
    val cpuThinking: Boolean = false,
    val cpuLastWord: String? = null,
    val puzzlePossibleAnswers: List<String> = emptyList(),
    val isProcessing: Boolean = false,
    val consecutivePasses: Int = 0,
    val result: GameResult? = null
) {
    val playerTotalScore: Int get() = playerScore
    val cpuTotalScore: Int get() = cpuScore
    val playerTotalAnswers: Int get() = playerAnswers.size
    val cpuTotalAnswers: Int get() = cpuAnswers.size
    val phase: GamePhase get() = gamePhase
}
