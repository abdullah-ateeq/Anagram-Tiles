package com.example.anagramtiles

import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.domain.AnagramCpuStrategy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CpuStrategyTest {

    private val possibleAnswers = listOf("CARE", "RACE", "ACRE")

    // Test 32: CPU answer
    @Test
    fun testCpuFindsAnswer() {
        val strategy = AnagramCpuStrategy(Random(42))
        val answer = strategy.selectNextAnswer(
            possibleAnswers = possibleAnswers,
            usedAnswers = emptySet(),
            cpuPreviousAnswersInRound = emptyList(),
            difficulty = Difficulty.NORMAL
        )
        assertNotNull(answer)
        assertTrue(possibleAnswers.contains(answer!!.word))
    }

    // Test 33: CPU Easy
    @Test
    fun testCpuEasyBehavior() {
        val strategy = AnagramCpuStrategy(Random(1))
        // Easy finds 1 answer
        val answer = strategy.selectNextAnswer(
            possibleAnswers = possibleAnswers,
            usedAnswers = emptySet(),
            cpuPreviousAnswersInRound = emptyList(),
            difficulty = Difficulty.EASY
        )
        assertNotNull(answer)
    }

    // Test 34: CPU Normal
    @Test
    fun testCpuNormalBehavior() {
        val strategy = AnagramCpuStrategy(Random(42))
        val answer = strategy.selectNextAnswer(
            possibleAnswers = possibleAnswers,
            usedAnswers = emptySet(),
            cpuPreviousAnswersInRound = emptyList(),
            difficulty = Difficulty.NORMAL
        )
        assertNotNull(answer)
        // Scores correctly
        assertEquals(30, answer!!.baseScore)
    }

    // Test 35: CPU Hard
    @Test
    fun testCpuHardBehavior() {
        val strategy = AnagramCpuStrategy(Random(42))
        val used = mutableSetOf<String>()
        val found = mutableListOf<String>()

        for (i in 0 until possibleAnswers.size) {
            val ans = strategy.selectNextAnswer(
                possibleAnswers = possibleAnswers,
                usedAnswers = used,
                cpuPreviousAnswersInRound = emptyList(),
                difficulty = Difficulty.HARD
            )
            if (ans != null) {
                found.add(ans.word)
                used.add(ans.word)
            }
        }

        // Hard should find all available answers
        assertEquals(3, found.size)
        assertTrue(found.containsAll(possibleAnswers))
    }

    // Test 36: CPU duplicate protection
    @Test
    fun testCpuDuplicateProtection() {
        val strategy = AnagramCpuStrategy(Random(42))
        val used = setOf("CARE", "RACE", "ACRE") // All already used

        val answer = strategy.selectNextAnswer(
            possibleAnswers = possibleAnswers,
            usedAnswers = used,
            cpuPreviousAnswersInRound = emptyList(),
            difficulty = Difficulty.HARD
        )
        assertNull(answer) // Cannot produce duplicate answer
    }

    // Test 37: Player/CPU shared answer protection
    @Test
    fun testPlayerCpuSharedAnswerProtection() {
        val strategy = AnagramCpuStrategy(Random(42))
        // Player found CARE
        val used = setOf("CARE")

        val cpuAnswer = strategy.selectNextAnswer(
            possibleAnswers = possibleAnswers,
            usedAnswers = used,
            cpuPreviousAnswersInRound = emptyList(),
            difficulty = Difficulty.NORMAL
        )

        assertNotNull(cpuAnswer)
        assertFalse(cpuAnswer!!.word == "CARE") // Cannot steal CARE
        assertTrue(cpuAnswer.word == "RACE" || cpuAnswer.word == "ACRE")
    }
}
