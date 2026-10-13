package com.example.anagramtiles

import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.data.LetterTile
import com.example.anagramtiles.domain.AnagramGameEngine
import com.example.anagramtiles.domain.AnagramPuzzleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class TileManipulationTest {

    private lateinit var dict: AnagramDictionary
    private lateinit var generator: AnagramPuzzleGenerator
    private lateinit var engine: AnagramGameEngine

    @Before
    fun setup() {
        val words = listOf("CARE", "RACE", "ACRE", "CAT", "ACT")
        dict = AnagramDictionary(words, words)
        generator = AnagramPuzzleGenerator(dict, Random(42))
        engine = AnagramGameEngine(dict, generator, random = Random(42))
        engine.startNewGame(Difficulty.NORMAL, 60)
    }

    // Test 21: Tile swapping
    @Test
    fun testTileSwapping() {
        val initialWord = engine.currentState.currentWord
        val pos0Letter = initialWord[0]
        val pos1Letter = initialWord[1]

        engine.swapTiles(0, 1)

        val swappedWord = engine.currentState.currentWord
        assertEquals(pos1Letter, swappedWord[0])
        assertEquals(pos0Letter, swappedWord[1])
    }

    // Test 22: Tile selection and swap via tap
    @Test
    fun testTileSelectionAndSwap() {
        // Tap first tile -> selected
        val swap1 = engine.selectOrSwapTile(0)
        assertFalse(swap1)
        assertEquals(0, engine.currentState.selectedPosition)

        // Tap second tile -> swapped!
        val swap2 = engine.selectOrSwapTile(1)
        assertTrue(swap2)
        assertEquals(null, engine.currentState.selectedPosition)
    }

    // Test 23: Reset
    @Test
    fun testResetRestoresInitialOrder() {
        val initialOrder = engine.currentState.initialOrder
        val initialWord = engine.currentState.currentWord

        // Modify order
        engine.swapTiles(0, 1)
        assertNotEquals(initialWord, engine.currentState.currentWord)

        // Reset
        engine.resetTiles()
        assertEquals(initialOrder, engine.currentState.currentOrder)
        assertEquals(initialWord, engine.currentState.currentWord)
    }

    // Test 24: Shuffle
    @Test
    fun testShuffleRearrangesTiles() {
        val originalLetters = engine.currentState.currentWord.toCharArray().sorted()
        engine.shuffleTiles()
        val shuffledLetters = engine.currentState.currentWord.toCharArray().sorted()

        // Same letters must be preserved
        assertEquals(originalLetters, shuffledLetters)
    }

    // Test 25: Shuffle changes order
    @Test
    fun testShuffleChangesOrderWhenPossible() {
        var changed = false
        val originalOrder = engine.currentState.currentOrder
        for (i in 0 until 5) {
            engine.shuffleTiles()
            if (engine.currentState.currentOrder != originalOrder) {
                changed = true
                break
            }
        }
        assertTrue(changed)
    }

    // Test 26: Current word generation
    @Test
    fun testCurrentWordGeneration() {
        val tiles = listOf(
            LetterTile(1, 'C'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'E')
        )
        val order = listOf(3, 1, 4, 2) // R, C, E, A
        val word = AnagramGameEngine.buildWord(order, tiles)
        assertEquals("RCEA", word)
    }
}
