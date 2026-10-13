package com.example.anagramtiles.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("anagram_tiles_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): UserSettings {
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val diffName = prefs.getString(KEY_CPU_DIFFICULTY, Difficulty.NORMAL.name) ?: Difficulty.NORMAL.name
        val timerSec = prefs.getInt(KEY_TIMER_SECONDS, 60)

        return UserSettings(
            theme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.SYSTEM },
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            cpuDifficulty = try { Difficulty.valueOf(diffName) } catch (e: Exception) { Difficulty.NORMAL },
            sound = prefs.getBoolean(KEY_SOUND, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            animations = prefs.getBoolean(KEY_ANIMATIONS, true),
            timer = TimerOption.fromSeconds(timerSec),
            showWordFeedback = prefs.getBoolean(KEY_SHOW_FEEDBACK, true),
            confirmRestart = prefs.getBoolean(KEY_CONFIRM_RESTART, true)
        )
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.theme.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putString(KEY_CPU_DIFFICULTY, settings.cpuDifficulty.name)
            .putBoolean(KEY_SOUND, settings.sound)
            .putBoolean(KEY_HAPTIC, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animations)
            .putInt(KEY_TIMER_SECONDS, settings.timer.seconds)
            .putBoolean(KEY_SHOW_FEEDBACK, settings.showWordFeedback)
            .putBoolean(KEY_CONFIRM_RESTART, settings.confirmRestart)
            .apply()
    }

    fun resetSettings(): UserSettings {
        val defaults = UserSettings()
        saveSettings(defaults)
        return defaults
    }

    fun loadStatistics(): GameStatistics {
        return GameStatistics(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            wins = prefs.getInt(KEY_WINS, 0),
            losses = prefs.getInt(KEY_LOSSES, 0),
            draws = prefs.getInt(KEY_DRAWS, 0),
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            longestWord = prefs.getString(KEY_LONGEST_WORD, "") ?: "",
            bestPuzzle = prefs.getInt(KEY_BEST_PUZZLE, 0),
            bestAccuracy = prefs.getFloat(KEY_BEST_ACCURACY, 0f),
            currentWinStreak = prefs.getInt(KEY_WIN_STREAK, 0),
            bestWinStreak = prefs.getInt(KEY_BEST_WIN_STREAK, 0),
            perfectAnagramCount = prefs.getInt(KEY_PERFECT_ANAGRAMS, 0),
            totalAnswers = prefs.getInt(KEY_TOTAL_ANSWERS, 0),
            totalValidAnswers = prefs.getInt(KEY_TOTAL_VALID, 0),
            totalInvalidAttempts = prefs.getInt(KEY_TOTAL_INVALID, 0),
            totalPasses = prefs.getInt(KEY_TOTAL_PASSES, 0),
            totalPuzzles = prefs.getInt(KEY_TOTAL_PUZZLES, 0)
        )
    }

    fun saveStatistics(stats: GameStatistics) {
        prefs.edit()
            .putInt(KEY_GAMES_PLAYED, stats.gamesPlayed)
            .putInt(KEY_WINS, stats.wins)
            .putInt(KEY_LOSSES, stats.losses)
            .putInt(KEY_DRAWS, stats.draws)
            .putInt(KEY_BEST_SCORE, stats.bestScore)
            .putString(KEY_LONGEST_WORD, stats.longestWord)
            .putInt(KEY_BEST_PUZZLE, stats.bestPuzzle)
            .putFloat(KEY_BEST_ACCURACY, stats.bestAccuracy)
            .putInt(KEY_WIN_STREAK, stats.currentWinStreak)
            .putInt(KEY_BEST_WIN_STREAK, stats.bestWinStreak)
            .putInt(KEY_PERFECT_ANAGRAMS, stats.perfectAnagramCount)
            .putInt(KEY_TOTAL_ANSWERS, stats.totalAnswers)
            .putInt(KEY_TOTAL_VALID, stats.totalValidAnswers)
            .putInt(KEY_TOTAL_INVALID, stats.totalInvalidAttempts)
            .putInt(KEY_TOTAL_PASSES, stats.totalPasses)
            .putInt(KEY_TOTAL_PUZZLES, stats.totalPuzzles)
            .apply()
    }

    fun resetStatistics(): GameStatistics {
        val empty = GameStatistics()
        saveStatistics(empty)
        return empty
    }

    companion object {
        private const val KEY_THEME = "pref_theme"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_CPU_DIFFICULTY = "pref_cpu_difficulty"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_HAPTIC = "pref_haptic"
        private const val KEY_ANIMATIONS = "pref_animations"
        private const val KEY_TIMER_SECONDS = "pref_timer_seconds"
        private const val KEY_SHOW_FEEDBACK = "pref_show_feedback"
        private const val KEY_CONFIRM_RESTART = "pref_confirm_restart"

        private const val KEY_GAMES_PLAYED = "stat_games_played"
        private const val KEY_WINS = "stat_wins"
        private const val KEY_LOSSES = "stat_losses"
        private const val KEY_DRAWS = "stat_draws"
        private const val KEY_BEST_SCORE = "stat_best_score"
        private const val KEY_LONGEST_WORD = "stat_longest_word"
        private const val KEY_BEST_PUZZLE = "stat_best_puzzle"
        private const val KEY_BEST_ACCURACY = "stat_best_accuracy"
        private const val KEY_WIN_STREAK = "stat_win_streak"
        private const val KEY_BEST_WIN_STREAK = "stat_best_win_streak"
        private const val KEY_PERFECT_ANAGRAMS = "stat_perfect_anagrams"
        private const val KEY_TOTAL_ANSWERS = "stat_total_answers"
        private const val KEY_TOTAL_VALID = "stat_total_valid"
        private const val KEY_TOTAL_INVALID = "stat_total_invalid"
        private const val KEY_TOTAL_PASSES = "stat_total_passes"
        private const val KEY_TOTAL_PUZZLES = "stat_total_puzzles"
    }
}
