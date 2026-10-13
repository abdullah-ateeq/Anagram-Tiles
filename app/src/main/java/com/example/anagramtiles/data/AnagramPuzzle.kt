package com.example.anagramtiles.data

/**
 * Represents a single anagram puzzle.
 */
data class AnagramPuzzle(
    val id: Int,
    val tiles: List<LetterTile>,
    val initialOrder: List<Int>,
    val possibleAnswers: List<String>
)
