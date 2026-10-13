package com.example.anagramtiles.data

/**
 * Represents a single physical letter tile with a unique identity.
 * Even when letters are identical (e.g. two 'E' tiles), each has a unique [id].
 */
data class LetterTile(
    val id: Int,
    val letter: Char,
    val position: Int = 0
)
