package com.doma.assistente.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

//Classe para facilitar o uso de Text-to-Speech
class TextToSpeechHelper(context: Context) : TextToSpeech.OnInitListener {
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
        }
    }

    //Fala o texto passado
    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    //Libera recursos ao final do uso
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}