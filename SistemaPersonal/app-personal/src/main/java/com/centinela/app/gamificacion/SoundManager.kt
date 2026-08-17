package com.centinela.app.gamificacion

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundManager {

    fun reproducirCelebracion(scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        scope.launch {
            var tg: ToneGenerator? = null
            try {
                tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                delay(150)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
                delay(150)
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 220)
                delay(240)
            } catch (_: Exception) {

            } finally {
                tg?.release()
            }
        }
    }

    fun reproducirTonoCorto(scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        scope.launch {
            var tg: ToneGenerator? = null
            try {
                tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 150)
                delay(160)
            } catch (_: Exception) {
            } finally {
                tg?.release()
            }
        }
    }
}
