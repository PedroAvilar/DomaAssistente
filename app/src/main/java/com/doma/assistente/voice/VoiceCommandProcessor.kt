package com.doma.assistente.voice

import com.doma.assistente.notification.NotificationListenerService
import com.doma.assistente.tts.TextToSpeechHelper

//Classe que processa os comandos de voz e executa ações
class VoiceCommandProcessor(
    private val ttsHelper: TextToSpeechHelper
) {
    fun process(command: String) {
        when {
            //Comando de ajuda
            command.contains("ajuda") -> {
                ttsHelper.speak("Você pode dizer: mensagens, status ou ajuda.")
            }
            //Comando de mensagens
            command.contains("mensagem") || command.contains("mensagens") -> {
                val ultimas = NotificationListenerService.getLastNotifications(3)
                if (ultimas.isEmpty()) {
                    ttsHelper.speak("Não há mensagens recentes.")
                } else {
                    ttsHelper.speak("Lendo suas últimas mensagens.")
                    ultimas.forEach { ttsHelper.speak(it) }
                }
            }
            //Comando para status
            command.contains("Status") -> {
                ttsHelper.speak("Seu assistente está funcionando corretamente.")
            }
            //Comando não reconhecido
            else -> {
                ttsHelper.speak("Comando não reconhecido. Tente novamente.")
            }
        }
    }
}