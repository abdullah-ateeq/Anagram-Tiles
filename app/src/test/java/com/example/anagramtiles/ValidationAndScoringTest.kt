package com.example.anagramtiles

import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.LetterTile
import com.example.anagramtiles.data.SubmissionResult
import com.example.anagramtiles.domain.AnagramScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidationAndScoringTest {

    private val sampleDictionary = setOf(
        "CAT", "ACT", "CARE", "RACE", "ACRE", "TREE", "TRIANGLE", "INTEGRAL"
    )

    // Test 13: Valid 3-letter answer
    @Test
    fun testValid3LetterAnswer() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'T')
        )
        val result = AnagramDictionary.validateAnswer(
            word = "CAT",
            tiles = tiles,
            dictionary = sampleDictionary,
            usedAnswers = emptySet()
        )
        assertEquals(SubmissionResult.VALID, result)
        assertEquals(20, AnagramScoreCalculator.baseWordScore("CAT"))
    }

    // Test 14: Valid 8-letter answer
    @Test
    fun testValid8LetterAnswer() {
        val tiles = "TRIANGLE".mapIndexed { idx, ch -> LetterTile(idx + 1, ch) }
        val result = AnagramDictionary.validateAnswer(
            word = "TRIANGLE",
            tiles = tiles,
            dictionary = sampleDictionary,
            usedAnswers = emptySet()
        )
        assertEquals(SubmissionResult.VALID, result)
        assertEquals(100, AnagramScoreCalculator.baseWordScore("TRIANGLE"))
    }

    // Test 15: Too-short answer
    @Test
    fun testTooShortAnswer() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'E')
        )
        val result = AnagramDictionary.validateAnswer(
            word = "CAR", // Only 3 letters for a 4-letter tile set
            tiles = tiles,
            dictionary = sampleDictionary,
            usedAnswers = emptySet()
        )
        assertEquals(SubmissionResult.TOO_SHORT, result)
    }

    // Test 16: Too-long answer
    @Test
    fun testTooLongAnswer() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'T')
        )
        val result = AnagramDictionary.validateAnswer(
            word = "CATS",
            tiles = tiles,
            dictionary = sampleDictionary,
            usedAnswers = emptySet()
        )
        assertEquals(SubmissionResult.TOO_LONG, result)
    }

    // Test 17: Invalid dictionary word
    @Test
    fun testInvalidDictionaryWord() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'E')
        )
        // REAC uses all tiles but is not in dictionary
        val result = AnagramDictionary.validateAnswer(
            word = "REAC",
            tiles = tiles,
            dictionary = sampleDictionary,
            usedAnswers = emptySet()
        )
        assertEquals(SubmissionResult.INVALID_WORD, result)
    }

    // Test 18: Wrong tile letters
    @Test
    fun testWrongTileLetters() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'E')
        )
        // CARD has letter D not present in tiles
        val result = AnagramDictionary.validateAnswer(
            word = "CARD",
            tiles = tiles,
            dictionary = sampleDictionary + "CARD",
            usedAnswers = emptySet()
        )
        assertEquals(SubmissionResult.WRONG_TILES, result)
    }

    // Test 19: Used answer
    @Test
    fun testUsedAnswer() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'E')
        )
        val result = AnagramDictionary.validateAnswer(
            word = "CARE",
            tiles = tiles,
            dictionary = sampleDictionary,
            usedAnswers = setOf("CARE")
        )
        assertEquals(SubmissionResult.WORD_ALREADY_FOUND, result)
    }

    // Test 20: Multiple valid anagrams
    @Test
    fun testMultipleValidAnagrams() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'E')
        )

        val res1 = AnagramDictionary.validateAnswer("CARE", tiles, sampleDictionary, emptySet())
        val res2 = AnagramDictionary.validateAnswer("RACE", tiles, sampleDictionary, setOf("CARE"))
        val res3 = AnagramDictionary.validateAnswer("ACRE", tiles, sampleDictionary, setOf("CARE", "RACE"))

        assertEquals(SubmissionResult.VALID, res1)
        assertEquals(SubmissionResult.VALID, res2)
        assertEquals(SubmissionResult.VALID, res3)
    }

    // Test 27: Word score table
    @Test
    fun testWordScoreTable() {
        assertEquals(20, AnagramScoreCalculator.baseWordScore("CAT"))
        assertEquals(30, AnagramScoreCalculator.baseWordScore("CARE"))
        assertEquals(45, AnagramScoreCalculator.baseWordScore("PLANT"))
        assertEquals(60, AnagramScoreCalculator.baseWordScore("PLANET"))
        assertEquals(80, AnagramScoreCalculator.baseWordScore("MORNING"))
        assertEquals(100, AnagramScoreCalculator.baseWordScore("TRIANGLE"))
    }

    // Test 28: Anagram bonus formula
    @Test
    fun testAnagramBonusFormula() {
        // 1st answer -> 0
        assertEquals(0, AnagramScoreCalculator.anagramBonus(1))
        // 2nd answer -> 5
        assertEquals(5, AnagramScoreCalculator.anagramBonus(2))
        // 3rd answer -> 10
        assertEquals(10, AnagramScoreCalculator.anagramBonus(3))
        // 4th answer -> 15
        assertEquals(15, AnagramScoreCalculator.anagramBonus(4))

        // Total score calculation
        // 1st 4-letter: 30 + 0 = 30
        assertEquals(30, AnagramScoreCalculator.totalAnswerScore("CARE", 1))
        // 2nd 4-letter: 30 + 5 = 35
        assertEquals(35, AnagramScoreCalculator.totalAnswerScore("RACE", 2))
    }

    // Test 29: Completion bonus
    @Test
    fun testCompletionBonus() {
        assertEquals(25, AnagramScoreCalculator.puzzleCompletionBonus())
    }
}
