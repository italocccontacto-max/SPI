package com.centinela.app.contract

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class ContractTtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var onDone: (() -> Unit)? = null

    fun init(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "MX")
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(0.92f)
                onReady()
            }
        }
    }

    fun speak(text: String, onFinished: () -> Unit) {
        onDone = onFinished
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone?.invoke() }
            override fun onError(utteranceId: String?) { onDone?.invoke() }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CONTRACT_READ")
    }

    fun shutdown() { tts?.shutdown() }
}
