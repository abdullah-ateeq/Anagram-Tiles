package com.example.anagramtiles.data

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val bestScore: Int = 0,
    val longestWord: String = "",
    val bestPuzzle: Int = 0, // Max anagrams found in a single puzzle
    val bestAccuracy: Float = 0f,
    val currentWinStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val perfectAnagramCount: Int = 0,
    val totalAnswers: Int = 0,
    val totalValidAnswers: Int = 0,
    val totalInvalidAttempts: Int = 0,
    val totalPasses: Int = 0,
    val totalPuzzles: Int = 0
) {
    val overallAccuracy: Float
        get() {
            val totalAttempts = totalValidAnswers + totalInvalidAttempts
            return if (totalAttempts > 0) (totalValidAnswers.toFloat() / totalAttempts) * 100f else 0f
        }
}
