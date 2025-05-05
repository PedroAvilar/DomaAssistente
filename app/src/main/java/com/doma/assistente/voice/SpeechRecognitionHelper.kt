package com.doma.assistente.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

//Classe auxiliar para reconhecimento de voz
class SpeechRecognitionHelper(
    private val context: Context,
    //Callback para quando um comando for recebido
    private val onCommandRecognized: (String) -> Unit,
    //Callback para quando ocorrer um erro
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    //Inicia o reconhecimento de voz
    fun startListening() {
        //Caso não esteja disponível no dispositivo
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Reconhecimento de voz não disponível.")
            return
        }

        //Cria o objeto de reconhecimento de voz e define o listener de eventos
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {

                //Quando os resultados são recebidos com sucesso
                override fun onResults(results: Bundle) {
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val command = matches?.firstOrNull()?.lowercase() ?: ""
                    onCommandRecognized(command)
                }

                //Caso de erro no reconhecimento
                override fun onError(error: Int) {
                    onError("Erro ao reconhecer fala. Código: $error")
                }

                //Métodos obrigatórios não utilizados
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        //Cria a intent para reconhecimento em português do Brasil
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
        }

        //Inicia e escuta a fala
        speechRecognizer?.startListening(intent)
    }

    //Libera recursos ao final do uso
    fun stop() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
    }
}