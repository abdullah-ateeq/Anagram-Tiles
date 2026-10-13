package com.example.anagramtiles.ui.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Handles synthesized tones and device haptics offline without external media dependencies.
 */
class SoundHapticController(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun playTileSelect(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_PROP_BEEP, 40)
        if (haptic) vibrate(30, 80)
    }

    fun playTileSwap(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_PROP_ACK, 60)
        if (haptic) vibrate(40, 100)
    }

    fun playValidWord(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_PROP_PROMPT, 120)
        if (haptic) vibrate(70, 180)
    }

    fun playInvalidWord(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_PROP_NACK, 140)
        if (haptic) vibrate(60, 140)
    }

    fun playAllFound(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 250)
        if (haptic) vibratePattern(longArrayOf(0, 80, 60, 120))
    }

    fun playCpuAnswer(sound: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_PROP_BEEP2, 70)
    }

    fun playVictory(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
        if (haptic) vibratePattern(longArrayOf(0, 100, 80, 150))
    }

    fun playDefeat(sound: Boolean, haptic: Boolean) {
        if (sound) playTone(ToneGenerator.TONE_CDMA_LOW_L, 250)
        if (haptic) vibrate(120, 160)
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            // Ignore audio output errors safely
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors safely
        }
    }

    private fun vibratePattern(timings: LongArray) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(timings, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(timings, -1)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors safely
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            // Safe release
        }
    }
}
