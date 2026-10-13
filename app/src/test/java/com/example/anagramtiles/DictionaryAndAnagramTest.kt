package com.example.anagramtiles

import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.LetterTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryAndAnagramTest {

    // Test 1: Dictionary loading
    @Test
    fun testDictionaryLoading() {
        val curated = listOf("CARE", "RACE", "ACRE", "CAT", "ACT")
        val full = listOf("CARE", "RACE", "ACRE", "CAT", "ACT", "DOG", "GOD")
        val dict = AnagramDictionary(curated, full)

        assertTrue(dict.isWordInDictionary("CARE"))
        assertTrue(dict.isWordInDictionary("DOG"))
        assertFalse(dict.isWordInDictionary("XYZ"))
    }

    // Test 2: Dictionary normalization
    @Test
    fun testDictionaryNormalization() {
        assertEquals("CARE", AnagramDictionary.normalizeWord("  care  "))
        assertEquals("PLANET", AnagramDictionary.normalizeWord("Planet!"))
        assertEquals("TREE", AnagramDictionary.normalizeWord("tReE"))
    }

    // Test 3: Anagram signature
    @Test
    fun testAnagramSignature() {
        assertEquals("ACT", AnagramDictionary.createAnagramSignature("CAT"))
        assertEquals("ACER", AnagramDictionary.createAnagramSignature("CARE"))
        assertEquals("EERT", AnagramDictionary.createAnagramSignature("TREE"))
    }

    // Test 4: Same signature for anagrams
    @Test
    fun testSameSignatureForAnagrams() {
        val sig1 = AnagramDictionary.createAnagramSignature("CARE")
        val sig2 = AnagramDictionary.createAnagramSignature("RACE")
        val sig3 = AnagramDictionary.createAnagramSignature("ACRE")

        assertEquals(sig1, sig2)
        assertEquals(sig2, sig3)
    }

    // Test 5: Different signature for different letters
    @Test
    fun testDifferentSignatureForDifferentLetters() {
        val sigCare = AnagramDictionary.createAnagramSignature("CARE")
        val sigCard = AnagramDictionary.createAnagramSignature("CARD")

        assertNotEquals(sigCare, sigCard)
    }

    // Test 6: Anagram grouping
    @Test
    fun testAnagramGrouping() {
        val curated = listOf("TEAM", "MEAT", "MATE", "TAME", "CAT", "ACT")
        val dict = AnagramDictionary(curated, curated)
        val teamGroup = dict.getPossibleAnswers("AEMT")

        assertEquals(4, teamGroup.size)
        assertTrue(teamGroup.contains("TEAM"))
        assertTrue(teamGroup.contains("MEAT"))
        assertTrue(teamGroup.contains("MATE"))
        assertTrue(teamGroup.contains("TAME"))
    }

    // Test 11: Duplicate letters
    @Test
    fun testDuplicateLettersMultiset() {
        val tiles = listOf(
            LetterTile(1, 'T'),
            LetterTile(2, 'R'),
            LetterTile(3, 'E'),
            LetterTile(4, 'E')
        )

        // Both E's must be used
        assertTrue(AnagramDictionary.matchesPuzzleLetters("TREE", tiles))
        assertFalse(AnagramDictionary.matchesPuzzleLetters("TRE", tiles))
        assertFalse(AnagramDictionary.matchesPuzzleLetters("TREEE", tiles))
    }

    // Test 12: Exact letter frequency matching
    @Test
    fun testExactLetterFrequencyMatching() {
        val tiles = listOf(
            LetterTile(1, 'A'),
            LetterTile(2, 'A'),
            LetterTile(3, 'R'),
            LetterTile(4, 'T')
        )

        // TARA has 2 A's, 1 R, 1 T -> matches
        assertTrue(AnagramDictionary.matchesPuzzleLetters("TARA", tiles))
        // STAR has S which is not in tiles -> fails
        assertFalse(AnagramDictionary.matchesPuzzleLetters("STAR", tiles))
        // RATA matches
        assertTrue(AnagramDictionary.matchesPuzzleLetters("RATA", tiles))
    }
}
