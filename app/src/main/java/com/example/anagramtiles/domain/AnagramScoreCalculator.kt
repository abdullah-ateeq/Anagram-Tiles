package com.example.anagramtiles.domain

object AnagramScoreCalculator {
    const val COMPLETION_BONUS = 25

    fun baseWordScore(word: String): Int {
        return when (word.length) {
            3 -> 20
            4 -> 30
            5 -> 45
            6 -> 60
            7 -> 80
            8 -> 100
            else -> if (word.length < 3) 0 else 100
        }
    }

    fun anagramBonus(answerIndex: Int): Int {
        if (answerIndex <= 1) return 0
        return (answerIndex - 1) * 5
    }

    fun totalAnswerScore(word: String, answerIndex: Int): Int {
        return baseWordScore(word) + anagramBonus(answerIndex)
    }

    fun puzzleCompletionBonus(): Int {
        return COMPLETION_BONUS
    }
}
