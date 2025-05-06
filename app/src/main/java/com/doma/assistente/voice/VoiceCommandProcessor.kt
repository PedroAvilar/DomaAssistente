package com.doma.assistente.voice

import com.doma.assistente.emergency.EmergencyHelper
import com.doma.assistente.notification.DomaNotificationListener
import com.doma.assistente.tts.TextToSpeechHelper

//Classe que processa os comandos de voz e executa ações
class VoiceCommandProcessor(
    private val ttsHelper: TextToSpeechHelper,
    private val emergencyHelper: EmergencyHelper
) {
    var isAwaitingFallResponse = false

    fun process(command: String) {
        when {
            //Comando de ajuda
            command.contains("ajuda", ignoreCase = true) -> {
                ttsHelper.speak("Você pode dizer: mensagens, status ou ajuda.")
            }
            //Comando de mensagens
            command.contains("mensagem", ignoreCase = true) || command.contains("mensagens", ignoreCase = true) -> {
                val ultimas = DomaNotificationListener.getLastNotifications(3)
                if (ultimas.isEmpty()) {
                    ttsHelper.speak("Não há mensagens recentes.")
                } else {
                    ttsHelper.speak("Lendo suas últimas mensagens.")
                    ultimas.forEach { ttsHelper.speak(it) }
                }
            }
            //Comando para status
            command.contains("Status", ignoreCase = true) -> {
                ttsHelper.speak("Seu assistente está funcionando corretamente.")
            }

            //Queda detectada e esperando resposta
            isAwaitingFallResponse && command.contains("sim", ignoreCase = true) -> {
                isAwaitingFallResponse = false
                ttsHelper.speak("Que bom que está tudo bem.")
            }
            isAwaitingFallResponse && command.contains("não", ignoreCase = true) -> {
                isAwaitingFallResponse = false
                ttsHelper.speak("Iniciando alarme de emergência.")
                emergencyHelper.playAlarm()
            }
            //Comando para parar alarme
            command.contains("parar", ignoreCase = true) -> {
                if (emergencyHelper.isPlaying()) {
                    emergencyHelper.stopAlarm()
                    ttsHelper.speak("Fim do alarme de emergência.")
                } else {
                    ttsHelper.speak("Nenhum alarme está tocando.")
                }
            }

            //Comando não reconhecido
            else -> {
                ttsHelper.speak("Comando não reconhecido. Tente novamente.")
            }
        }
    }
}