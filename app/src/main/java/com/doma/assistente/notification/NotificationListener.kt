package com.doma.assistente.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.doma.assistente.tts.TextToSpeechHelper

//Classe que acptura as notificações, processa e envia ao TTS
class NotificationListenerService : NotificationListenerService() {

    private var ttsHelper: TextToSpeechHelper? = null

    //Método chamado quando o serviço é iniciado
    override fun onListenerConnected() {
        super.onListenerConnected()
        ttsHelper = TextToSpeechHelper(applicationContext) {
            ttsHelper?.speak("Assistente de notificação ativo.")
        }
    }

    //Lê em voz alta o conteúdo da notificação recebida
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        //Evita notificações vazias ou irrelevantes
        if (title.isNotBlank() || text.isNotBlank()) {
            val message = "$title: $text"
            ttsHelper?.speak(message)
        }
    }

    //Método chamado quando uma notificação é removida
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    //Libera recursos ao finalizar
    override fun onDestroy() {
        super.onDestroy()
        ttsHelper?.shutdown()
    }
}