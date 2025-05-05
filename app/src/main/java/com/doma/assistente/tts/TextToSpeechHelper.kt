package com.doma.assistente.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.util.Log

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

    //Fala o texto passado
    fun speak(text: String) {
        if (isReady) {
            Log.d("TextToSpeech", "Falando: $text")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TextToSpeech", "TTS ainda não está pronto")
        }
    }

    //Libera recursos ao final do uso
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}