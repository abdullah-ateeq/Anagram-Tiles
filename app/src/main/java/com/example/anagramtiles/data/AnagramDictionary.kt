package com.example.anagramtiles.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Loads and indexes dictionary words for fast O(1) anagram lookups and validation.
 */
class AnagramDictionary(
    curatedWords: Collection<String> = emptyList(),
    allWords: Collection<String> = emptyList()
) {
    // Map of signature -> list of valid curated anagrams
    private val anagramGroups = mutableMapOf<String, MutableList<String>>()

    // Full set of valid dictionary words for general validation
    private val dictionaryWords = mutableSetOf<String>()

    init {
        loadFromCollections(curatedWords, allWords)
    }

    private fun loadFromCollections(curated: Collection<String>, all: Collection<String>) {
        for (raw in curated) {
            val normalized = normalizeWord(raw)
            if (normalized.length in 3..8 && normalized.all { it in 'A'..'Z' }) {
                dictionaryWords.add(normalized)
                val sig = createAnagramSignature(normalized)
                val list = anagramGroups.getOrPut(sig) { mutableListOf() }
                if (!list.contains(normalized)) {
                    list.add(normalized)
                }
            }
        }

        for (raw in all) {
            val normalized = normalizeWord(raw)
            if (normalized.length in 3..8 && normalized.all { it in 'A'..'Z' }) {
                dictionaryWords.add(normalized)
            }
        }
    }

    fun isWordInDictionary(word: String): Boolean {
        return dictionaryWords.contains(normalizeWord(word))
    }

    fun getPossibleAnswers(signature: String): List<String> {
        return anagramGroups[signature]?.toList() ?: emptyList()
    }

    fun getAllAnagramGroups(): Map<String, List<String>> {
        return anagramGroups.toMap()
    }

    fun getFullDictionary(): Set<String> {
        return dictionaryWords.toSet()
    }

    fun getSignaturesForLength(length: Int): List<String> {
        return anagramGroups.keys.filter { it.length == length }
    }

    companion object {
        fun normalizeWord(word: String): String {
            return word.trim().uppercase().filter { it in 'A'..'Z' }
        }

        fun createSignature(word: String): String {
            return createAnagramSignature(word)
        }

        fun createAnagramSignature(letters: String): String {
            val normalized = normalizeWord(letters)
            val chars = normalized.toCharArray()
            chars.sort()
            return String(chars)
        }

        fun matchesPuzzleLetters(word: String, tiles: List<LetterTile>): Boolean {
            val normalized = normalizeWord(word)
            if (normalized.length != tiles.size) return false

            val wordFreq = IntArray(26)
            for (ch in normalized) {
                wordFreq[ch - 'A']++
            }

            val tileFreq = IntArray(26)
            for (tile in tiles) {
                val ch = tile.letter.uppercaseChar()
                if (ch in 'A'..'Z') {
                    tileFreq[ch - 'A']++
                }
            }

            for (i in 0 until 26) {
                if (wordFreq[i] != tileFreq[i]) return false
            }
            return true
        }

        fun validateAnswer(
            word: String,
            tiles: List<LetterTile>,
            dictionary: Set<String>,
            usedAnswers: Set<String>
        ): SubmissionResult {
            val normalized = normalizeWord(word)
            if (normalized.length < tiles.size) {
                return SubmissionResult.TOO_SHORT
            }
            if (normalized.length > tiles.size) {
                return SubmissionResult.TOO_LONG
            }
            if (!matchesPuzzleLetters(normalized, tiles)) {
                return SubmissionResult.WRONG_TILES
            }
            if (!dictionary.contains(normalized)) {
                return SubmissionResult.INVALID_WORD
            }
            if (usedAnswers.contains(normalized)) {
                return SubmissionResult.WORD_ALREADY_FOUND
            }
            return SubmissionResult.VALID
        }

        fun loadFromAssets(context: Context): AnagramDictionary {
            val curated = mutableListOf<String>()
            val full = mutableListOf<String>()

            try {
                readLines(context.assets.open("anagram_words.txt")) { curated.add(it) }
            } catch (e: Exception) {
                // Fallback to minimal default curated list
            }

            try {
                readLines(context.assets.open("word_list.txt")) { full.add(it) }
            } catch (e: Exception) {
                // Fallback to curated
                full.addAll(curated)
            }

            return AnagramDictionary(curated, full)
        }

        private inline fun readLines(stream: InputStream, crossinline onLine: (String) -> Unit) {
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        onLine(trimmed)
                    }
                    line = reader.readLine()
                }
            }
        }
    }
}
