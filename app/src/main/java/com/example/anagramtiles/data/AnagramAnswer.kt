package com.example.anagramtiles.data

/**
 * An answer discovered by a player or CPU.
 */
data class AnagramAnswer(
    val word: String,
    val baseScore: Int,
    val bonusScore: Int = 0,
    val totalScore: Int = baseScore + bonusScore,
    val frequencyRank: Int = 0
)
