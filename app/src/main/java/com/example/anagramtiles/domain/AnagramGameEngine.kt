package com.example.anagramtiles.domain

import com.example.anagramtiles.data.AnagramAnswer
import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.AnagramGameState
import com.example.anagramtiles.data.AnagramPuzzle
import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.data.GamePhase
import com.example.anagramtiles.data.GameResult
import com.example.anagramtiles.data.LetterTile
import com.example.anagramtiles.data.PuzzlePhase
import com.example.anagramtiles.data.SubmissionResult
import kotlin.random.Random

/**
 * Pure Kotlin game engine managing the 10-puzzle game flow, tile manipulation,
 * scoring, CPU interactions, and completion rules.
 */
class AnagramGameEngine(
    private val dictionary: AnagramDictionary,
    private val puzzleGenerator: AnagramPuzzleGenerator = AnagramPuzzleGenerator(dictionary),
    private val cpuStrategy: AnagramCpuStrategy = AnagramCpuStrategy(),
    private val random: Random = Random.Default
) {
    private var state = AnagramGameState()
    private val usedSignatures = mutableSetOf<String>()
    private var playerPuzzlesAllFoundCount = 0
    private var cpuPuzzlesAllFoundCount = 0
    private var currentPuzzleData: AnagramPuzzle? = null

    val currentState: AnagramGameState
        get() = state

    /**
     * Starts a brand new 10-puzzle game with the given difficulty and timer seconds.
     */
    fun startNewGame(difficulty: Difficulty, timerSeconds: Int): AnagramGameState {
        usedSignatures.clear()
        playerPuzzlesAllFoundCount = 0
        cpuPuzzlesAllFoundCount = 0

        val firstPuzzle = puzzleGenerator.generatePuzzle(1, difficulty, usedSignatures)
        currentPuzzleData = firstPuzzle

        val sig = AnagramDictionary.createAnagramSignature(
            firstPuzzle.tiles.map { it.letter }.joinToString("")
        )
        usedSignatures.add(sig)

        val currentWord = buildWord(firstPuzzle.initialOrder, firstPuzzle.tiles)

        state = AnagramGameState(
            currentPuzzle = 1,
            totalPuzzles = 10,
            tiles = firstPuzzle.tiles,
            currentOrder = firstPuzzle.initialOrder,
            initialOrder = firstPuzzle.initialOrder,
            selectedPosition = null,
            currentWord = currentWord,
            possibleAnswerCount = null, // Hidden during gameplay
            usedAnswers = emptySet(),
            playerAnswers = emptyList(),
            cpuAnswers = emptyList(),
            playerScore = 0,
            cpuScore = 0,
            playerInvalidAttempts = 0,
            cpuInvalidAttempts = 0,
            playerPasses = 0,
            cpuPasses = 0,
            timeRemaining = timerSeconds,
            puzzlePhase = PuzzlePhase.PLAYER_TURN,
            gamePhase = GamePhase.PLAYING,
            lastFeedback = null,
            lastSubmittedWord = null,
            lastScore = 0,
            playerLongestWord = null,
            cpuLongestWord = null,
            cpuThinking = false,
            cpuLastWord = null,
            puzzlePossibleAnswers = firstPuzzle.possibleAnswers,
            isProcessing = false,
            consecutivePasses = 0,
            result = null
        )

        return state
    }

    /**
     * Handles tapping on a tile at [positionIndex] (0 until tiles.size).
     * If no tile selected -> selects this position.
     * If this position already selected -> unselects.
     * If another position selected -> swaps them!
     * Returns true if a swap occurred.
     */
    fun selectOrSwapTile(positionIndex: Int): Boolean {
        if (state.puzzlePhase != PuzzlePhase.PLAYER_TURN || state.isProcessing) {
            return false
        }
        if (positionIndex !in 0 until state.tiles.size) {
            return false
        }

        val currentSelected = state.selectedPosition
        return if (currentSelected == null) {
            state = state.copy(selectedPosition = positionIndex)
            false
        } else if (currentSelected == positionIndex) {
            state = state.copy(selectedPosition = null)
            false
        } else {
            swapTiles(currentSelected, positionIndex)
            state = state.copy(selectedPosition = null)
            true
        }
    }

    /**
     * Swaps two tile positions in currentOrder.
     */
    fun swapTiles(firstPosition: Int, secondPosition: Int) {
        if (firstPosition !in 0 until state.currentOrder.size ||
            secondPosition !in 0 until state.currentOrder.size
        ) {
            return
        }

        val newOrder = state.currentOrder.toMutableList()
        val temp = newOrder[firstPosition]
        newOrder[firstPosition] = newOrder[secondPosition]
        newOrder[secondPosition] = temp

        val updatedWord = buildWord(newOrder, state.tiles)
        state = state.copy(
            currentOrder = newOrder,
            currentWord = updatedWord,
            selectedPosition = null
        )
    }

    /**
     * Restores tiles to initial puzzle arrangement.
     */
    fun resetTiles() {
        if (state.puzzlePhase != PuzzlePhase.PLAYER_TURN || state.isProcessing) return
        val restoredWord = buildWord(state.initialOrder, state.tiles)
        state = state.copy(
            currentOrder = state.initialOrder,
            currentWord = restoredWord,
            selectedPosition = null,
            lastFeedback = null
        )
    }

    /**
     * Shuffles current tiles into a new random arrangement.
     */
    fun shuffleTiles() {
        if (state.puzzlePhase != PuzzlePhase.PLAYER_TURN || state.isProcessing) return
        val current = state.currentOrder
        var shuffled = current.shuffled(random)

        // Ensure new order != current order when size > 1
        var attempts = 0
        while (attempts < 5 && shuffled == current && current.size > 1) {
            shuffled = current.shuffled(random)
            attempts++
        }

        val updatedWord = buildWord(shuffled, state.tiles)
        state = state.copy(
            currentOrder = shuffled,
            currentWord = updatedWord,
            selectedPosition = null
        )
    }

    /**
     * Player submits the current word.
     */
    fun submitPlayerWord(): SubmissionResult {
        if (state.puzzlePhase != PuzzlePhase.PLAYER_TURN || state.isProcessing) {
            return SubmissionResult.INVALID_WORD
        }

        val word = state.currentWord
        val validation = AnagramDictionary.validateAnswer(
            word = word,
            tiles = state.tiles,
            dictionary = dictionary.getFullDictionary(),
            usedAnswers = state.usedAnswers
        )

        if (validation == SubmissionResult.VALID) {
            val answerIndex = state.playerAnswers.size + 1
            val base = AnagramScoreCalculator.baseWordScore(word)
            val bonus = AnagramScoreCalculator.anagramBonus(answerIndex)
            val total = base + bonus

            val answer = AnagramAnswer(
                word = word,
                baseScore = base,
                bonusScore = bonus,
                totalScore = total,
                frequencyRank = answerIndex
            )

            val updatedPlayerAnswers = state.playerAnswers + answer
            val updatedUsed = state.usedAnswers + word
            var newPlayerScore = state.playerScore + total

            // Check if all possible curated answers have been found by player or combined
            val allCuratedFound = state.puzzlePossibleAnswers.all { updatedUsed.contains(it) }
            var completionBonusAwarded = false
            if (allCuratedFound) {
                newPlayerScore += AnagramScoreCalculator.COMPLETION_BONUS
                playerPuzzlesAllFoundCount++
                completionBonusAwarded = true
            }

            val longest = if (state.playerLongestWord == null || word.length > state.playerLongestWord!!.length) {
                word
            } else {
                state.playerLongestWord
            }

            val feedback = when {
                completionBonusAwarded -> "ALL FOUND! +${AnagramScoreCalculator.COMPLETION_BONUS}"
                answerIndex > 1 -> "NEW ANAGRAM: $word +$total"
                else -> "FOUND: $word +$total"
            }

            state = state.copy(
                playerAnswers = updatedPlayerAnswers,
                usedAnswers = updatedUsed,
                playerScore = newPlayerScore,
                playerLongestWord = longest,
                lastFeedback = feedback,
                lastSubmittedWord = word,
                lastScore = total + (if (completionBonusAwarded) AnagramScoreCalculator.COMPLETION_BONUS else 0),
                selectedPosition = null,
                consecutivePasses = 0,
                puzzlePhase = if (allCuratedFound) PuzzlePhase.PUZZLE_COMPLETE else PuzzlePhase.CPU_TURN
            )

            if (allCuratedFound) {
                completeCurrentPuzzle()
            }
        } else {
            val feedback = when (validation) {
                SubmissionResult.TOO_SHORT -> "TOO SHORT"
                SubmissionResult.TOO_LONG -> "TOO LONG"
                SubmissionResult.WRONG_TILES -> "WRONG TILES"
                SubmissionResult.INVALID_WORD -> "NOT A WORD"
                SubmissionResult.WORD_ALREADY_FOUND -> "ALREADY FOUND"
                else -> "INVALID"
            }

            state = state.copy(
                playerInvalidAttempts = state.playerInvalidAttempts + 1,
                lastFeedback = feedback,
                lastSubmittedWord = word,
                lastScore = 0,
                selectedPosition = null,
                puzzlePhase = PuzzlePhase.CPU_TURN
            )
        }

        return validation
    }

    /**
     * Executes the CPU turn.
     */
    fun executeCpuTurn(difficulty: Difficulty): AnagramAnswer? {
        if (state.puzzlePhase != PuzzlePhase.CPU_TURN && state.puzzlePhase != PuzzlePhase.PLAYER_TURN) {
            return null
        }

        val answer = cpuStrategy.selectNextAnswer(
            possibleAnswers = state.puzzlePossibleAnswers,
            usedAnswers = state.usedAnswers,
            cpuPreviousAnswersInRound = state.cpuAnswers,
            difficulty = difficulty
        )

        if (answer != null) {
            val updatedCpuAnswers = state.cpuAnswers + answer
            val updatedUsed = state.usedAnswers + answer.word
            var newCpuScore = state.cpuScore + answer.totalScore

            val allCuratedFound = state.puzzlePossibleAnswers.all { updatedUsed.contains(it) }
            if (allCuratedFound) {
                newCpuScore += AnagramScoreCalculator.COMPLETION_BONUS
                cpuPuzzlesAllFoundCount++
            }

            val longest = if (state.cpuLongestWord == null || answer.word.length > state.cpuLongestWord!!.length) {
                answer.word
            } else {
                state.cpuLongestWord
            }

            state = state.copy(
                cpuAnswers = updatedCpuAnswers,
                usedAnswers = updatedUsed,
                cpuScore = newCpuScore,
                cpuLongestWord = longest,
                cpuLastWord = answer.word,
                cpuThinking = false,
                consecutivePasses = 0,
                puzzlePhase = if (allCuratedFound) PuzzlePhase.PUZZLE_COMPLETE else PuzzlePhase.PLAYER_TURN
            )

            if (allCuratedFound) {
                completeCurrentPuzzle()
            }
        } else {
            // CPU passes
            val newPasses = state.consecutivePasses + 1
            state = state.copy(
                cpuPasses = state.cpuPasses + 1,
                consecutivePasses = newPasses,
                cpuThinking = false,
                puzzlePhase = if (newPasses >= 2) PuzzlePhase.PUZZLE_COMPLETE else PuzzlePhase.PLAYER_TURN
            )

            if (newPasses >= 2) {
                completeCurrentPuzzle()
            }
        }

        return answer
    }

    /**
     * Player decides to PASS this puzzle.
     * Does NOT count as an invalid word.
     */
    fun passPlayerTurn() {
        if (state.puzzlePhase != PuzzlePhase.PLAYER_TURN || state.isProcessing) return

        val newPasses = state.consecutivePasses + 1
        state = state.copy(
            playerPasses = state.playerPasses + 1,
            consecutivePasses = newPasses,
            lastFeedback = "PASSED",
            selectedPosition = null,
            puzzlePhase = if (newPasses >= 2) PuzzlePhase.PUZZLE_COMPLETE else PuzzlePhase.CPU_TURN
        )

        if (newPasses >= 2) {
            completeCurrentPuzzle()
        }
    }

    /**
     * Ticks the timer by 1 second. When it reaches 0, completes the puzzle.
     */
    fun tickTimer() {
        if (state.gamePhase != GamePhase.PLAYING || state.puzzlePhase == PuzzlePhase.PUZZLE_COMPLETE) return
        if (state.timeRemaining > 1) {
            state = state.copy(timeRemaining = state.timeRemaining - 1)
        } else if (state.timeRemaining == 1) {
            state = state.copy(timeRemaining = 0)
            completeCurrentPuzzle()
        }
    }

    /**
     * Concludes the current puzzle, reveals possible answer count, and prepares transition.
     */
    fun completeCurrentPuzzle() {
        state = state.copy(
            possibleAnswerCount = state.puzzlePossibleAnswers.size,
            puzzlePhase = PuzzlePhase.PUZZLE_COMPLETE,
            gamePhase = if (state.currentPuzzle >= state.totalPuzzles) GamePhase.GAME_OVER else GamePhase.PUZZLE_TRANSITION,
            selectedPosition = null
        )

        if (state.currentPuzzle >= state.totalPuzzles) {
            finishGame()
        }
    }

    /**
     * Advances to the next puzzle in the 10-puzzle game.
     */
    fun nextPuzzle(difficulty: Difficulty, timerSeconds: Int): Boolean {
        if (state.currentPuzzle >= state.totalPuzzles) {
            finishGame()
            return false
        }

        val nextIndex = state.currentPuzzle + 1
        val puzzle = puzzleGenerator.generatePuzzle(nextIndex, difficulty, usedSignatures)
        currentPuzzleData = puzzle

        val sig = AnagramDictionary.createAnagramSignature(
            puzzle.tiles.map { it.letter }.joinToString("")
        )
        usedSignatures.add(sig)

        val currentWord = buildWord(puzzle.initialOrder, puzzle.tiles)

        state = state.copy(
            currentPuzzle = nextIndex,
            tiles = puzzle.tiles,
            currentOrder = puzzle.initialOrder,
            initialOrder = puzzle.initialOrder,
            selectedPosition = null,
            currentWord = currentWord,
            possibleAnswerCount = null,
            usedAnswers = emptySet(),
            timeRemaining = timerSeconds,
            puzzlePhase = PuzzlePhase.PLAYER_TURN,
            gamePhase = GamePhase.PLAYING,
            lastFeedback = null,
            lastSubmittedWord = null,
            lastScore = 0,
            cpuThinking = false,
            cpuLastWord = null,
            puzzlePossibleAnswers = puzzle.possibleAnswers,
            isProcessing = false,
            consecutivePasses = 0
        )

        return true
    }

    /**
     * Concludes the 10-puzzle game and evaluates the winner and tiebreakers.
     */
    fun finishGame(): GameResult {
        val result = evaluateGameResult(
            playerScore = state.playerScore,
            cpuScore = state.cpuScore,
            playerAnswersCount = state.playerAnswers.size,
            cpuAnswersCount = state.cpuAnswers.size,
            playerLongestWord = state.playerLongestWord,
            cpuLongestWord = state.cpuLongestWord,
            playerCompletedAll = playerPuzzlesAllFoundCount,
            cpuCompletedAll = cpuPuzzlesAllFoundCount,
            playerInvalidAttempts = state.playerInvalidAttempts,
            playerPasses = state.playerPasses
        )

        state = state.copy(
            gamePhase = GamePhase.GAME_OVER,
            puzzlePhase = PuzzlePhase.GAME_OVER,
            result = result
        )

        return result
    }

    fun setCpuThinking(isThinking: Boolean) {
        state = state.copy(cpuThinking = isThinking)
    }

    fun setProcessing(isProcessing: Boolean) {
        state = state.copy(isProcessing = isProcessing)
    }

    companion object {
        fun buildWord(order: List<Int>, tiles: List<LetterTile>): String {
            val tileMap = tiles.associateBy { it.id }
            return order.mapNotNull { tileMap[it]?.letter }.joinToString("")
        }

        fun evaluateGameResult(
            playerScore: Int,
            cpuScore: Int,
            playerAnswersCount: Int,
            cpuAnswersCount: Int,
            playerLongestWord: String?,
            cpuLongestWord: String?,
            playerCompletedAll: Int,
            cpuCompletedAll: Int,
            playerInvalidAttempts: Int,
            playerPasses: Int
        ): GameResult {
            return when {
                playerScore > cpuScore -> {
                    if (playerInvalidAttempts == 0 && playerPasses == 0) {
                        GameResult.PERFECT_ANAGRAM
                    } else {
                        GameResult.PLAYER_WIN
                    }
                }

                cpuScore > playerScore -> GameResult.CPU_WIN

                else -> {
                    // Tiebreakers:
                    // 1. Compare total valid answers count
                    if (playerAnswersCount > cpuAnswersCount) {
                        return if (playerInvalidAttempts == 0 && playerPasses == 0) GameResult.PERFECT_ANAGRAM else GameResult.PLAYER_WIN
                    }
                    if (cpuAnswersCount > playerAnswersCount) {
                        return GameResult.CPU_WIN
                    }

                    // 2. Compare longest word length
                    val playerLen = playerLongestWord?.length ?: 0
                    val cpuLen = cpuLongestWord?.length ?: 0
                    if (playerLen > cpuLen) {
                        return if (playerInvalidAttempts == 0 && playerPasses == 0) GameResult.PERFECT_ANAGRAM else GameResult.PLAYER_WIN
                    }
                    if (cpuLen > playerLen) {
                        return GameResult.CPU_WIN
                    }

                    // 3. Compare puzzles completed with all answers
                    if (playerCompletedAll > cpuCompletedAll) {
                        return if (playerInvalidAttempts == 0 && playerPasses == 0) GameResult.PERFECT_ANAGRAM else GameResult.PLAYER_WIN
                    }
                    if (cpuCompletedAll > playerCompletedAll) {
                        return GameResult.CPU_WIN
                    }

                    // 4. Otherwise DRAW
                    GameResult.DRAW
                }
            }
        }
    }
}
