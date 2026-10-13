package com.example.anagramtiles.data

data class UserSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val cpuDifficulty: Difficulty = Difficulty.NORMAL,
    val sound: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val timer: TimerOption = TimerOption.SECONDS_60,
    val showWordFeedback: Boolean = true,
    val confirmRestart: Boolean = true
)
