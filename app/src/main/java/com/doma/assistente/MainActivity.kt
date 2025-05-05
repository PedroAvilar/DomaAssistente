package com.doma.assistente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.doma.assistente.audio.getAudioOutputDeviceNames
import com.doma.assistente.audio.AudioDeviceReceiver
import com.doma.assistente.tts.TextToSpeechHelper
import com.doma.assistente.voice.SpeechRecognitionHelper
import com.doma.assistente.voice.VoiceCommandProcessor

//Função para verificar se a permissão de notificação está ativa
private fun isNotificationPermissionGranted(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    val packageName = context.packageName
    return enabledListeners != null && enabledListeners.contains(packageName)
}

//Classe principal que exibe dispositivos e leitura da tela
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Conteúdo da interface
        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val ttsHelper = remember {
                    TextToSpeechHelper(
                        context = context,
                        //Callback chamado quando o TTS está pronto
                        onTtsReady = { }
                    )
                }
                val deviceNames = remember { mutableStateListOf<String>() }
                val hasNotificationPermission = remember { mutableStateOf(false) }

                //Função para atualizar os dispositivos conectados
                fun updateDeviceList() {
                    deviceNames.clear()
                    deviceNames.addAll(getAudioOutputDeviceNames(this))
                }

                //Verifica permissão ao iniciar
                LaunchedEffect(Unit) {
                    updateDeviceList()
                    val granted = isNotificationPermissionGranted(context)
                    hasNotificationPermission.value = granted

                    ttsHelper.speak("Doma Assistente iniciado.")

                    if (!granted) {
                        ttsHelper.speak("Permissão de notificação não concedida. Toque na tela para ativar.")
                    }
                }

                //Atualiza e fala ao detectar mudanças nos dispositivos
                LaunchedEffect(deviceNames.size) {
                    if (deviceNames.isNotEmpty()) {
                        ttsHelper.speak("Foram encontrados ${deviceNames.size} dispositivos de saída de áudio.")
                    }
                }

                //Registra o receiver para ouvir alterações
                DisposableEffect(Unit) {
                    val receiver = AudioDeviceReceiver {updateDeviceList()}
                    val filter = receiver.getIntentFilter()
                    registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)

                    //Libera ao sair
                    onDispose {
                        unregisterReceiver(receiver)
                        ttsHelper.shutdown()
                    }
                }

                //Exibe conteúdo com interação por toque para acessibilidade
                Box (
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable{
                            if (!hasNotificationPermission.value) {
                                ttsHelper.speak("Abrindo configurações para conceder permissão de notificação.")
                                context.startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                )
                            } else {
                                ttsHelper.speak("Diga o seu comando.")
                                val processor = VoiceCommandProcessor(ttsHelper)
                                val speechHelper = SpeechRecognitionHelper(
                                    context = context,
                                    onCommandRecognized = {command -> processor.process(command)},
                                    onError = {error -> ttsHelper.speak(error)}
                                )
                                speechHelper.startListening()
                            }
                        }
                ) {

                    //Exibe a lista
                    ScalingLazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 20.dp)
                    ) {
                        //Título da lista
                        item {
                            Text(
                                text = "Dispositivos de saída:",
                                fontSize = 16.sp
                            )
                        }
                        //Cada nome de dispositivo como item da lista
                        items(deviceNames) { name ->
                            Text("• $name", fontSize = 14.sp)
                        }
                        Log.d("MainDeviceNames", "Dispositivos detectados: $deviceNames")
                    }
                }
            }
        }
    }
}