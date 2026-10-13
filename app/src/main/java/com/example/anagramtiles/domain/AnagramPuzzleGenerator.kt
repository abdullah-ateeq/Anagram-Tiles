package com.example.anagramtiles.domain

import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.AnagramPuzzle
import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.data.LetterTile
import kotlin.random.Random

class AnagramPuzzleGenerator(
    private val dictionary: AnagramDictionary,
    private val random: Random = Random.Default
) {
    /**
     * Generates a puzzle for the given difficulty and puzzle index.
     * Ensures each puzzle has at least 1 valid common answer, prefer 2-5.
     */
    fun generatePuzzle(
        id: Int,
        difficulty: Difficulty,
        excludedSignatures: Set<String> = emptySet()
    ): AnagramPuzzle {
        val groups = dictionary.getAllAnagramGroups()
        if (groups.isEmpty()) {
            return fallbackPuzzle(id)
        }

        val (targetLengths, targetAnswerCounts) = when (difficulty) {
            Difficulty.EASY -> listOf(3, 4, 5) to (1..4)
            Difficulty.NORMAL -> listOf(4, 5, 6) to (2..5)
            Difficulty.HARD -> listOf(5, 6, 7, 8) to (2..6)
        }

        // Filter eligible signatures
        var candidates = groups.filter { (sig, answers) ->
            sig.length in targetLengths &&
                    answers.size in targetAnswerCounts &&
                    !excludedSignatures.contains(sig)
        }.keys.toList()

        // Fallback 1: relax answer count
        if (candidates.isEmpty()) {
            candidates = groups.filter { (sig, _) ->
                sig.length in targetLengths && !excludedSignatures.contains(sig)
            }.keys.toList()
        }

        // Fallback 2: any signature not yet excluded
        if (candidates.isEmpty()) {
            candidates = groups.filter { (sig, _) ->
                !excludedSignatures.contains(sig)
            }.keys.toList()
        }

        // Fallback 3: any available signature
        if (candidates.isEmpty()) {
            candidates = groups.keys.toList()
        }

        val chosenSignature = candidates[random.nextInt(candidates.size)]
        val possibleAnswers = groups[chosenSignature] ?: listOf(chosenSignature)

        // Pick one word from possible answers as base to derive tile set
        val baseWord = possibleAnswers[random.nextInt(possibleAnswers.size)]

        // Create tiles with unique identities
        val tiles = baseWord.mapIndexed { index, ch ->
            LetterTile(id = index + 1, letter = ch, position = index)
        }

        // Generate scrambled initial order
        val initialOrder = createScrambledOrder(tiles, possibleAnswers)

        return AnagramPuzzle(
            id = id,
            tiles = tiles,
            initialOrder = initialOrder,
            possibleAnswers = possibleAnswers
        )
    }

    /**
     * Produces a scrambled tile ordering, ensuring it does not immediately form a valid answer if possible.
     */
    private fun createScrambledOrder(
        tiles: List<LetterTile>,
        possibleAnswers: List<String>
    ): List<Int> {
        val tileIds = tiles.map { it.id }
        var shuffled = tileIds.shuffled(random)

        // Attempt up to 10 shuffles to avoid starting with a solved word
        var attempts = 0
        while (attempts < 10) {
            val word = buildStringFromOrder(shuffled, tiles)
            if (!possibleAnswers.contains(word)) {
                return shuffled
            }
            shuffled = tileIds.shuffled(random)
            attempts++
        }

        return shuffled
    }

    private fun buildStringFromOrder(order: List<Int>, tiles: List<LetterTile>): String {
        val tileMap = tiles.associateBy { it.id }
        return order.mapNotNull { tileMap[it]?.letter }.joinToString("")
    }

    private fun fallbackPuzzle(id: Int): AnagramPuzzle {
        val baseWord = "CARE"
        val tiles = baseWord.mapIndexed { index, ch ->
            LetterTile(id = index + 1, letter = ch, position = index)
        }
        return AnagramPuzzle(
            id = id,
            tiles = tiles,
            initialOrder = listOf(2, 4, 1, 3),
            possibleAnswers = listOf("CARE", "RACE", "ACRE")
        )
    }
}
