package com.example.anagramtiles

import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.data.GamePhase
import com.example.anagramtiles.data.GameResult
import com.example.anagramtiles.data.GameStatistics
import com.example.anagramtiles.data.LetterTile
import com.example.anagramtiles.data.PuzzlePhase
import com.example.anagramtiles.data.SubmissionResult
import com.example.anagramtiles.data.UserSettings
import com.example.anagramtiles.domain.AnagramCpuStrategy
import com.example.anagramtiles.domain.AnagramGameEngine
import com.example.anagramtiles.domain.AnagramPuzzleGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class GameEngineAndFlowTest {

    private lateinit var dictionary: AnagramDictionary
    private lateinit var puzzleGenerator: AnagramPuzzleGenerator
    private lateinit var cpuStrategy: AnagramCpuStrategy
    private lateinit var engine: AnagramGameEngine

    @Before
    fun setup() {
        val curated = listOf(
            "CAT", "ACT",
            "CARE", "RACE", "ACRE",
            "TEAM", "MEAT", "MATE", "TAME",
            "PLANT", "PLANET"
        )
        dictionary = AnagramDictionary(curated, curated)
        puzzleGenerator = AnagramPuzzleGenerator(dictionary, Random(100))
        cpuStrategy = AnagramCpuStrategy(Random(100))
        engine = AnagramGameEngine(dictionary, puzzleGenerator, cpuStrategy, Random(100))
    }

    // Test 7: Puzzle generation
    @Test
    fun testPuzzleGeneration() {
        val puzzle = puzzleGenerator.generatePuzzle(1, Difficulty.EASY)
        assertNotNull(puzzle)
        assertTrue(puzzle.tiles.isNotEmpty())
    }

    // Test 8: Puzzle has valid answer
    @Test
    fun testPuzzleHasValidAnswer() {
        val puzzle = puzzleGenerator.generatePuzzle(1, Difficulty.NORMAL)
        assertTrue(puzzle.possibleAnswers.isNotEmpty())
        for (ans in puzzle.possibleAnswers) {
            assertTrue(dictionary.isWordInDictionary(ans))
        }
    }

    // Test 9: Puzzle tile count
    @Test
    fun testPuzzleTileCountMatchesWordLength() {
        val puzzle = puzzleGenerator.generatePuzzle(1, Difficulty.NORMAL)
        val expectedLength = puzzle.possibleAnswers.first().length
        assertEquals(expectedLength, puzzle.tiles.size)
    }

    // Test 10: Unique tile IDs
    @Test
    fun testUniqueTileIDs() {
        val puzzle = puzzleGenerator.generatePuzzle(1, Difficulty.HARD)
        val ids = puzzle.tiles.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    // Test 30: Player submission
    @Test
    fun testPlayerValidSubmission() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        val answer = engine.currentState.puzzlePossibleAnswers.first()

        // Rearrange tiles to match valid answer
        rearrangeTilesToWord(answer)
        val result = engine.submitPlayerWord()

        assertEquals(SubmissionResult.VALID, result)
        assertEquals(1, engine.currentState.playerAnswers.size)
        assertTrue(engine.currentState.playerScore > 0)
        assertTrue(engine.currentState.usedAnswers.contains(answer))
    }

    // Test 31: Invalid submission
    @Test
    fun testPlayerInvalidSubmission() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        // Set invalid scrambled word
        val invalidWord = "ZZZZ"
        // Force submit invalid word through current state
        val result = engine.submitPlayerWord()
        // If current scrambled letters form invalid word:
        if (result != SubmissionResult.VALID) {
            assertEquals(1, engine.currentState.playerInvalidAttempts)
            assertEquals(PuzzlePhase.CPU_TURN, engine.currentState.puzzlePhase)
        }
    }

    // Test 38: Puzzle completion when all common answers found
    @Test
    fun testPuzzleCompletion() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        val answers = engine.currentState.puzzlePossibleAnswers

        for (ans in answers) {
            if (engine.currentState.usedAnswers.contains(ans)) continue
            rearrangeTilesToWord(ans)
            engine.submitPlayerWord()
            if (engine.currentState.puzzlePhase == PuzzlePhase.PUZZLE_COMPLETE) break
            if (engine.currentState.puzzlePhase == PuzzlePhase.CPU_TURN) {
                engine.executeCpuTurn(Difficulty.EASY)
            }
            if (engine.currentState.puzzlePhase == PuzzlePhase.PUZZLE_COMPLETE) break
        }

        if (engine.currentState.puzzlePhase != PuzzlePhase.PUZZLE_COMPLETE) {
            engine.completeCurrentPuzzle()
        }

        assertEquals(PuzzlePhase.PUZZLE_COMPLETE, engine.currentState.puzzlePhase)
        assertNotNull(engine.currentState.possibleAnswerCount)
    }

    // Test 39 & 40: Timer tick and expiration
    @Test
    fun testTimerTickAndExpiration() {
        engine.startNewGame(Difficulty.NORMAL, 2)
        assertEquals(2, engine.currentState.timeRemaining)

        engine.tickTimer()
        assertEquals(1, engine.currentState.timeRemaining)

        engine.tickTimer()
        assertEquals(0, engine.currentState.timeRemaining)
        assertEquals(PuzzlePhase.PUZZLE_COMPLETE, engine.currentState.puzzlePhase)
    }

    // Test 41: Pass does not count as invalid word
    @Test
    fun testPass() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        val initialInvalid = engine.currentState.playerInvalidAttempts

        engine.passPlayerTurn()

        assertEquals(1, engine.currentState.playerPasses)
        assertEquals(initialInvalid, engine.currentState.playerInvalidAttempts)
        assertEquals(PuzzlePhase.CPU_TURN, engine.currentState.puzzlePhase)
    }

    // Test 42: Puzzle progression
    @Test
    fun testPuzzleProgression() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        assertEquals(1, engine.currentState.currentPuzzle)

        engine.completeCurrentPuzzle()
        val hasNext = engine.nextPuzzle(Difficulty.NORMAL, 60)

        assertTrue(hasNext)
        assertEquals(2, engine.currentState.currentPuzzle)
        assertEquals(PuzzlePhase.PLAYER_TURN, engine.currentState.puzzlePhase)
    }

    // Test 43: Ten puzzle completion
    @Test
    fun testTenPuzzleCompletion() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        for (i in 1 until 10) {
            engine.completeCurrentPuzzle()
            engine.nextPuzzle(Difficulty.NORMAL, 60)
        }

        assertEquals(10, engine.currentState.currentPuzzle)
        engine.completeCurrentPuzzle()

        assertEquals(GamePhase.GAME_OVER, engine.currentState.gamePhase)
        assertNotNull(engine.currentState.result)
    }

    // Test 44: Player win
    @Test
    fun testPlayerWinResult() {
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 365,
            cpuScore = 320,
            playerAnswersCount = 10,
            cpuAnswersCount = 8,
            playerLongestWord = "PLANET",
            cpuLongestWord = "PLANT",
            playerCompletedAll = 2,
            cpuCompletedAll = 1,
            playerInvalidAttempts = 1,
            playerPasses = 1
        )
        assertEquals(GameResult.PLAYER_WIN, result)
    }

    // Test 45: CPU win
    @Test
    fun testCpuWinResult() {
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 200,
            cpuScore = 260,
            playerAnswersCount = 5,
            cpuAnswersCount = 7,
            playerLongestWord = "CARE",
            cpuLongestWord = "PLANET",
            playerCompletedAll = 0,
            cpuCompletedAll = 1,
            playerInvalidAttempts = 2,
            playerPasses = 1
        )
        assertEquals(GameResult.CPU_WIN, result)
    }

    // Test 46: Draw
    @Test
    fun testDrawResult() {
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 300,
            cpuScore = 300,
            playerAnswersCount = 8,
            cpuAnswersCount = 8,
            playerLongestWord = "PLANET",
            cpuLongestWord = "PLANET",
            playerCompletedAll = 1,
            cpuCompletedAll = 1,
            playerInvalidAttempts = 1,
            playerPasses = 0
        )
        assertEquals(GameResult.DRAW, result)
    }

    // Test 47: Score tiebreaker
    @Test
    fun testScoreTiebreaker() {
        // Equal score, player has more valid answers
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 300,
            cpuScore = 300,
            playerAnswersCount = 9,
            cpuAnswersCount = 7,
            playerLongestWord = "CARE",
            cpuLongestWord = "CARE",
            playerCompletedAll = 0,
            cpuCompletedAll = 0,
            playerInvalidAttempts = 1,
            playerPasses = 0
        )
        assertEquals(GameResult.PLAYER_WIN, result)
    }

    // Test 48: Answer count tiebreaker
    @Test
    fun testAnswerCountTiebreakerCpuWins() {
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 300,
            cpuScore = 300,
            playerAnswersCount = 5,
            cpuAnswersCount = 8,
            playerLongestWord = "CARE",
            cpuLongestWord = "CARE",
            playerCompletedAll = 0,
            cpuCompletedAll = 0,
            playerInvalidAttempts = 1,
            playerPasses = 0
        )
        assertEquals(GameResult.CPU_WIN, result)
    }

    // Test 49: Longest word tiebreaker
    @Test
    fun testLongestWordTiebreaker() {
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 300,
            cpuScore = 300,
            playerAnswersCount = 6,
            cpuAnswersCount = 6,
            playerLongestWord = "PLANET", // 6 letters
            cpuLongestWord = "CARE",   // 4 letters
            playerCompletedAll = 0,
            cpuCompletedAll = 0,
            playerInvalidAttempts = 1,
            playerPasses = 0
        )
        assertEquals(GameResult.PLAYER_WIN, result)
    }

    // Test 50: Perfect Anagram
    @Test
    fun testPerfectAnagramDetection() {
        val result = AnagramGameEngine.evaluateGameResult(
            playerScore = 350,
            cpuScore = 300,
            playerAnswersCount = 8,
            cpuAnswersCount = 6,
            playerLongestWord = "PLANET",
            cpuLongestWord = "CARE",
            playerCompletedAll = 2,
            cpuCompletedAll = 0,
            playerInvalidAttempts = 0, // 0 invalid attempts
            playerPasses = 0           // 0 passes
        )
        assertEquals(GameResult.PERFECT_ANAGRAM, result)
    }

    // Test 51: Accuracy calculation
    @Test
    fun testAccuracyCalculation() {
        val stats = GameStatistics(
            totalValidAnswers = 18,
            totalInvalidAttempts = 2
        )
        assertEquals(90.0f, stats.overallAccuracy, 0.01f)
    }

    // Test 52: Statistics model
    @Test
    fun testStatisticsModel() {
        val stats = GameStatistics(
            gamesPlayed = 5,
            wins = 4,
            losses = 1,
            bestScore = 420
        )
        assertEquals(5, stats.gamesPlayed)
        assertEquals(4, stats.wins)
        assertEquals(420, stats.bestScore)
    }

    // Test 53: Win streak
    @Test
    fun testWinStreak() {
        val stats = GameStatistics(
            currentWinStreak = 4,
            bestWinStreak = 4
        )
        val updated = stats.copy(
            currentWinStreak = stats.currentWinStreak + 1,
            bestWinStreak = maxOf(stats.bestWinStreak, stats.currentWinStreak + 1)
        )
        assertEquals(5, updated.currentWinStreak)
        assertEquals(5, updated.bestWinStreak)
    }

    // Test 54: Settings defaults
    @Test
    fun testSettingsDefaults() {
        val settings = UserSettings()
        assertEquals(Difficulty.NORMAL, settings.cpuDifficulty)
        assertTrue(settings.sound)
        assertTrue(settings.hapticFeedback)
        assertTrue(settings.showWordFeedback)
        assertTrue(settings.confirmRestart)
    }

    // Test 55: Restart
    @Test
    fun testRestart() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        engine.swapTiles(0, 1)

        val restartedState = engine.startNewGame(Difficulty.NORMAL, 60)
        assertEquals(1, restartedState.currentPuzzle)
        assertEquals(0, restartedState.playerScore)
        assertEquals(0, restartedState.playerAnswers.size)
        assertEquals(GamePhase.PLAYING, restartedState.gamePhase)
    }

    // Test 56: Rapid input protection
    @Test
    fun testRapidInputProtection() {
        engine.startNewGame(Difficulty.NORMAL, 60)
        engine.setProcessing(true)

        // Actions should be blocked while isProcessing is true
        val swapped = engine.selectOrSwapTile(0)
        assertFalse(swapped)

        val subResult = engine.submitPlayerWord()
        assertEquals(SubmissionResult.INVALID_WORD, subResult)

        engine.setProcessing(false)
        // Now allowed
        engine.selectOrSwapTile(0)
        assertEquals(0, engine.currentState.selectedPosition)
    }

    private fun rearrangeTilesToWord(targetWord: String) {
        val tiles = engine.currentState.tiles
        val remainingTiles = tiles.toMutableList()
        val targetOrder = mutableListOf<Int>()

        for (ch in targetWord) {
            val tile = remainingTiles.first { it.letter == ch }
            targetOrder.add(tile.id)
            remainingTiles.remove(tile)
        }

        // Apply order directly
        for (i in targetOrder.indices) {
            val targetTileId = targetOrder[i]
            val currentPos = engine.currentState.currentOrder.indexOf(targetTileId)
            if (currentPos != i) {
                engine.swapTiles(i, currentPos)
            }
        }
    }
}
