package com.example.anagramtiles.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.anagramtiles.data.AnagramDictionary
import com.example.anagramtiles.data.AnagramGameState
import com.example.anagramtiles.data.Difficulty
import com.example.anagramtiles.data.GamePhase
import com.example.anagramtiles.data.GameResult
import com.example.anagramtiles.data.GameStatistics
import com.example.anagramtiles.data.PreferencesManager
import com.example.anagramtiles.data.PuzzlePhase
import com.example.anagramtiles.data.SubmissionResult
import com.example.anagramtiles.data.TimerOption
import com.example.anagramtiles.data.UserSettings
import com.example.anagramtiles.domain.AnagramCpuStrategy
import com.example.anagramtiles.domain.AnagramGameEngine
import com.example.anagramtiles.domain.AnagramPuzzleGenerator
import com.example.anagramtiles.ui.audio.SoundHapticController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnagramViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = PreferencesManager(application)
    private val soundHaptic = SoundHapticController(application)
    private val dictionary = AnagramDictionary.loadFromAssets(application)
    private val puzzleGenerator = AnagramPuzzleGenerator(dictionary)
    private val cpuStrategy = AnagramCpuStrategy()
    private val gameEngine = AnagramGameEngine(dictionary, puzzleGenerator, cpuStrategy)

    private val _settings = MutableStateFlow(prefsManager.loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _statistics = MutableStateFlow(prefsManager.loadStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private val _gameState = MutableStateFlow(gameEngine.currentState)
    val gameState: StateFlow<AnagramGameState> = _gameState.asStateFlow()

    private val _showHowToPlaySheet = MutableStateFlow(false)
    val showHowToPlaySheet: StateFlow<Boolean> = _showHowToPlaySheet.asStateFlow()

    private val _showAnswersSheet = MutableStateFlow(false)
    val showAnswersSheet: StateFlow<Boolean> = _showAnswersSheet.asStateFlow()

    private val _showRestartDialog = MutableStateFlow(false)
    val showRestartDialog: StateFlow<Boolean> = _showRestartDialog.asStateFlow()

    private var timerJob: Job? = null
    private var cpuJob: Job? = null

    init {
        // Start first game automatically with saved settings
        startNewGame(_settings.value.cpuDifficulty)
    }

    fun startNewGame(difficulty: Difficulty? = null) {
        timerJob?.cancel()
        cpuJob?.cancel()

        val activeDifficulty = difficulty ?: _settings.value.cpuDifficulty
        val timerSec = _settings.value.timer.seconds

        val newState = gameEngine.startNewGame(activeDifficulty, timerSec)
        _gameState.value = newState

        startTimerIfNeeded()
    }

    fun selectOrSwapTile(position: Int) {
        val s = _gameState.value
        if (s.puzzlePhase != PuzzlePhase.PLAYER_TURN || s.isProcessing) return

        val hadSelection = s.selectedPosition != null
        val swapped = gameEngine.selectOrSwapTile(position)
        _gameState.value = gameEngine.currentState

        if (swapped) {
            soundHaptic.playTileSwap(_settings.value.sound, _settings.value.hapticFeedback)
        } else if (!hadSelection || s.selectedPosition != position) {
            soundHaptic.playTileSelect(_settings.value.sound, _settings.value.hapticFeedback)
        }
    }

    fun shuffleTiles() {
        val s = _gameState.value
        if (s.puzzlePhase != PuzzlePhase.PLAYER_TURN || s.isProcessing) return
        gameEngine.shuffleTiles()
        _gameState.value = gameEngine.currentState
        soundHaptic.playTileSwap(_settings.value.sound, _settings.value.hapticFeedback)
    }

    fun resetTiles() {
        val s = _gameState.value
        if (s.puzzlePhase != PuzzlePhase.PLAYER_TURN || s.isProcessing) return
        gameEngine.resetTiles()
        _gameState.value = gameEngine.currentState
        soundHaptic.playTileSelect(_settings.value.sound, _settings.value.hapticFeedback)
    }

    fun checkWord() {
        val s = _gameState.value
        if (s.puzzlePhase != PuzzlePhase.PLAYER_TURN || s.isProcessing) return

        val result = gameEngine.submitPlayerWord()
        _gameState.value = gameEngine.currentState

        when (result) {
            SubmissionResult.VALID -> {
                val state = gameEngine.currentState
                if (state.lastFeedback?.startsWith("ALL FOUND") == true) {
                    soundHaptic.playAllFound(_settings.value.sound, _settings.value.hapticFeedback)
                } else {
                    soundHaptic.playValidWord(_settings.value.sound, _settings.value.hapticFeedback)
                }
                checkIfGameOrPuzzleFinished()
                if (gameEngine.currentState.puzzlePhase == PuzzlePhase.CPU_TURN) {
                    triggerCpuTurn()
                }
            }

            else -> {
                soundHaptic.playInvalidWord(_settings.value.sound, _settings.value.hapticFeedback)
                if (gameEngine.currentState.puzzlePhase == PuzzlePhase.CPU_TURN) {
                    triggerCpuTurn()
                }
            }
        }
    }

    fun passTurn() {
        val s = _gameState.value
        if (s.puzzlePhase != PuzzlePhase.PLAYER_TURN || s.isProcessing) return

        gameEngine.passPlayerTurn()
        _gameState.value = gameEngine.currentState
        soundHaptic.playTileSelect(_settings.value.sound, _settings.value.hapticFeedback)

        checkIfGameOrPuzzleFinished()
        if (gameEngine.currentState.puzzlePhase == PuzzlePhase.CPU_TURN) {
            triggerCpuTurn()
        }
    }

    fun nextPuzzle() {
        timerJob?.cancel()
        cpuJob?.cancel()

        val timerSec = _settings.value.timer.seconds
        val hasNext = gameEngine.nextPuzzle(_settings.value.cpuDifficulty, timerSec)
        _gameState.value = gameEngine.currentState

        if (hasNext) {
            startTimerIfNeeded()
        } else {
            checkIfGameOrPuzzleFinished()
        }
    }

    fun requestRestartGame() {
        if (_settings.value.confirmRestart) {
            _showRestartDialog.value = true
        } else {
            startNewGame()
        }
    }

    fun confirmRestart() {
        _showRestartDialog.value = false
        startNewGame()
    }

    fun dismissRestartDialog() {
        _showRestartDialog.value = false
    }

    fun setHowToPlayVisible(visible: Boolean) {
        _showHowToPlaySheet.value = visible
    }

    fun setAnswersSheetVisible(visible: Boolean) {
        _showAnswersSheet.value = visible
    }

    fun updateSettings(newSettings: UserSettings) {
        _settings.value = newSettings
        prefsManager.saveSettings(newSettings)
    }

    fun resetSettings() {
        val defaults = prefsManager.resetSettings()
        _settings.value = defaults
    }

    fun resetStatistics() {
        val empty = prefsManager.resetStatistics()
        _statistics.value = empty
    }

    private fun triggerCpuTurn() {
        cpuJob?.cancel()
        cpuJob = viewModelScope.launch {
            gameEngine.setCpuThinking(true)
            _gameState.value = gameEngine.currentState

            val delayMs = cpuStrategy.getThinkingDelayMs(_settings.value.cpuDifficulty)
            delay(delayMs)

            val answer = gameEngine.executeCpuTurn(_settings.value.cpuDifficulty)
            _gameState.value = gameEngine.currentState

            if (answer != null) {
                soundHaptic.playCpuAnswer(_settings.value.sound)
            }

            checkIfGameOrPuzzleFinished()
        }
    }

    private fun startTimerIfNeeded() {
        timerJob?.cancel()
        if (_settings.value.timer == TimerOption.OFF) return

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val s = _gameState.value
                if (s.gamePhase == GamePhase.PLAYING && s.puzzlePhase != PuzzlePhase.PUZZLE_COMPLETE) {
                    gameEngine.tickTimer()
                    _gameState.value = gameEngine.currentState
                    if (gameEngine.currentState.puzzlePhase == PuzzlePhase.PUZZLE_COMPLETE) {
                        checkIfGameOrPuzzleFinished()
                        break
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun checkIfGameOrPuzzleFinished() {
        val s = _gameState.value
        if (s.gamePhase == GamePhase.GAME_OVER) {
            timerJob?.cancel()
            cpuJob?.cancel()

            // Update stats
            recordCompletedGameStats(s)

            // Play final audio / haptics
            when (s.result) {
                GameResult.PLAYER_WIN, GameResult.PERFECT_ANAGRAM ->
                    soundHaptic.playVictory(_settings.value.sound, _settings.value.hapticFeedback)
                GameResult.CPU_WIN ->
                    soundHaptic.playDefeat(_settings.value.sound, _settings.value.hapticFeedback)
                else ->
                    soundHaptic.playValidWord(_settings.value.sound, _settings.value.hapticFeedback)
            }
        }
    }

    private fun recordCompletedGameStats(state: AnagramGameState) {
        val currentStats = _statistics.value

        val isWin = state.result == GameResult.PLAYER_WIN || state.result == GameResult.PERFECT_ANAGRAM
        val isLoss = state.result == GameResult.CPU_WIN
        val isDraw = state.result == GameResult.DRAW

        val newStreak = if (isWin) currentStats.currentWinStreak + 1 else if (isLoss) 0 else currentStats.currentWinStreak
        val bestStreak = maxOf(currentStats.bestWinStreak, newStreak)

        val newBestScore = maxOf(currentStats.bestScore, state.playerScore)

        val longest = if ((state.playerLongestWord?.length ?: 0) > currentStats.longestWord.length) {
            state.playerLongestWord ?: currentStats.longestWord
        } else {
            currentStats.longestWord
        }

        // Max anagrams found in a single puzzle
        val currentMaxPuzzleAnagrams = state.playerAnswers.size
        val bestPuzzle = maxOf(currentStats.bestPuzzle, currentMaxPuzzleAnagrams)

        val totalValid = currentStats.totalValidAnswers + state.playerAnswers.size
        val totalInvalid = currentStats.totalInvalidAttempts + state.playerInvalidAttempts
        val accuracy = if (totalValid + totalInvalid > 0) {
            (totalValid.toFloat() / (totalValid + totalInvalid)) * 100f
        } else 0f

        val updatedStats = currentStats.copy(
            gamesPlayed = currentStats.gamesPlayed + 1,
            wins = currentStats.wins + (if (isWin) 1 else 0),
            losses = currentStats.losses + (if (isLoss) 1 else 0),
            draws = currentStats.draws + (if (isDraw) 1 else 0),
            bestScore = newBestScore,
            longestWord = longest,
            bestPuzzle = bestPuzzle,
            bestAccuracy = maxOf(currentStats.bestAccuracy, accuracy),
            currentWinStreak = newStreak,
            bestWinStreak = bestStreak,
            perfectAnagramCount = currentStats.perfectAnagramCount + (if (state.result == GameResult.PERFECT_ANAGRAM) 1 else 0),
            totalAnswers = currentStats.totalAnswers + state.playerAnswers.size + state.cpuAnswers.size,
            totalValidAnswers = totalValid,
            totalInvalidAttempts = totalInvalid,
            totalPasses = currentStats.totalPasses + state.playerPasses,
            totalPuzzles = currentStats.totalPuzzles + state.currentPuzzle
        )

        _statistics.value = updatedStats
        prefsManager.saveStatistics(updatedStats)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        cpuJob?.cancel()
        soundHaptic.release()
    }
}
