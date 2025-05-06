package com.doma.assistente.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

//Classe para facilitar o uso de Text-to-Speech
class TextToSpeechHelper(context: Context, private val onTtsReady: () -> Unit) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false

    //Inicializa e define o listener
    init {
        tts = TextToSpeech(context, this)
    }

    //Callback quando o mecanismo estiver pronto
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("pt", "BR")
            isReady = true
            onTtsReady()
        } else {
            Log.e("TextToSpeech", "Falha ao inicializar o TTS")
        }
    }

    //Fala o texto passado, sem callback
    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SIMPLE")
        } else {
            Log.e("TextToSpeech", "TTS ainda não está pronto")
        }
    }

    //Fala enfileirando frases em fila
    fun speakAdd(text: String) {
        if (!isReady) {
            Log.e("TextToSpeech", "TTS ainda não está pronto")
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
    }

    //Fala e chama onDone() quando terminar
    fun speak(text: String, onDone: () -> Unit) {
        if (!isReady) {
            Log.e("TextToSpeech","TTS não pronto")
            return
        }
        val id = UUID.randomUUID().toString()
        tts?.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                if (utteranceId == id) {
                    onDone()
                }
            }
            override fun onError(utteranceId: String) { /* opcional */ }
            override fun onStart(utteranceId: String) { }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
    }

    //Libera recursos ao final do uso
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}