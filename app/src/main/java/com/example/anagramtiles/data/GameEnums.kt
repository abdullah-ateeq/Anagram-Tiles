package com.example.anagramtiles.data

enum class PuzzlePhase {
    PLAYER_TURN,
    CPU_TURN,
    RESOLVING,
    PUZZLE_COMPLETE,
    GAME_OVER
}

enum class GamePhase {
    PLAYING,
    PUZZLE_TRANSITION,
    GAME_OVER
}

enum class GameResult {
    PLAYER_WIN,
    CPU_WIN,
    DRAW,
    PERFECT_ANAGRAM
}

enum class SubmissionResult {
    VALID,
    TOO_SHORT,
    TOO_LONG,
    INVALID_WORD,
    WRONG_TILES,
    WORD_ALREADY_FOUND
}

enum class Difficulty {
    EASY,
    NORMAL,
    HARD
}

enum class TimerOption(val seconds: Int) {
    OFF(0),
    SECONDS_30(30),
    SECONDS_60(60),
    SECONDS_90(90);

    companion object {
        fun fromSeconds(sec: Int): TimerOption = when (sec) {
            0 -> OFF
            30 -> SECONDS_30
            90 -> SECONDS_90
            else -> SECONDS_60
        }
    }
}

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}
