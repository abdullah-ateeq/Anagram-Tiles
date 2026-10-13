package com.example.anagramtiles.domain

import com.example.anagramtiles.data.AnagramAnswer
import com.example.anagramtiles.data.Difficulty
import kotlin.random.Random

class AnagramCpuStrategy(
    private val random: Random = Random.Default
) {
    /**
     * Finds the next answer for the CPU given the available answers and used answers.
     * Returns null if CPU decides to pass or no valid answers remain.
     */
    fun selectNextAnswer(
        possibleAnswers: List<String>,
        usedAnswers: Set<String>,
        cpuPreviousAnswersInRound: List<AnagramAnswer>,
        difficulty: Difficulty
    ): AnagramAnswer? {
        // Filter out all used answers (both player and CPU used words)
        val available = possibleAnswers.filter { !usedAnswers.contains(it) }
        if (available.isEmpty()) {
            return null
        }

        return when (difficulty) {
            Difficulty.EASY -> {
                // Easy: may only find 1 answer per puzzle or occasionally pass
                if (cpuPreviousAnswersInRound.isNotEmpty() && random.nextFloat() < 0.65f) {
                    null // Stop after one answer most of the time
                } else {
                    val candidate = available[random.nextInt(available.size)]
                    createAnswer(candidate, cpuPreviousAnswersInRound.size + 1)
                }
            }

            Difficulty.NORMAL -> {
                // Normal: finds 1-3 answers, prefers higher scoring / longer words
                if (cpuPreviousAnswersInRound.size >= 3 && random.nextFloat() < 0.70f) {
                    null
                } else {
                    // Sort descending by score
                    val ranked = available.sortedByDescending { AnagramScoreCalculator.baseWordScore(it) }
                    val candidate = ranked.first()
                    createAnswer(candidate, cpuPreviousAnswersInRound.size + 1)
                }
            }

            Difficulty.HARD -> {
                // Hard: aggressively finds high-scoring answers until exhausted
                val ranked = available.sortedByDescending { AnagramScoreCalculator.baseWordScore(it) }
                val candidate = ranked.first()
                createAnswer(candidate, cpuPreviousAnswersInRound.size + 1)
            }
        }
    }

    private fun createAnswer(word: String, answerIndex: Int): AnagramAnswer {
        val base = AnagramScoreCalculator.baseWordScore(word)
        val bonus = AnagramScoreCalculator.anagramBonus(answerIndex)
        return AnagramAnswer(
            word = word,
            baseScore = base,
            bonusScore = bonus,
            totalScore = base + bonus
        )
    }

    /**
     * Determines thinking delay in milliseconds based on difficulty and number of answers already found.
     */
    fun getThinkingDelayMs(difficulty: Difficulty): Long {
        return when (difficulty) {
            Difficulty.EASY -> (500L + random.nextLong(400L)) // 500 - 900ms
            Difficulty.NORMAL -> (400L + random.nextLong(350L)) // 400 - 750ms
            Difficulty.HARD -> (300L + random.nextLong(300L)) // 300 - 600ms
        }
    }
}
