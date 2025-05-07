package com.doma.assistente.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Text
import com.doma.assistente.audio.AudioDeviceReceiver
import com.doma.assistente.audio.getAudioOutputDeviceNames
import com.doma.assistente.emergency.EmergencyHelper
import com.doma.assistente.sensors.MotionDetector
import com.doma.assistente.tts.TextToSpeechHelper
import com.doma.assistente.voice.SpeechRecognitionHelper
import com.doma.assistente.voice.VoiceCommandProcessor
import kotlin.text.contains

//Função para verificar se a permissão de notificação está ativada
private fun isNotificationPermissionGranted(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    val packageName = context.packageName
    return enabledListeners != null && enabledListeners.contains(packageName)
}

//Composable com a UI principal e lógica de interação
@Composable
fun MainScreen(ttsHelper: TextToSpeechHelper) {
    val context = LocalContext.current

    //Inicia os módulos de emergência, comandos de voz e sensores
    val emergencyHelper = remember { EmergencyHelper(context) }
    val processor = remember { VoiceCommandProcessor(context, ttsHelper, emergencyHelper) }
    val motionDetector = remember { MotionDetector(context, ttsHelper, processor, emergencyHelper) }
    val speechHelper = remember {
        SpeechRecognitionHelper(
            context = context,
            onCommandRecognized = { cmd -> processor.process(cmd) },
            onError = { err -> ttsHelper.speak(err) }
        )
    }
    //Dispositivos de áudio
    val deviceNames = remember { mutableStateListOf<String>() }
    fun updateDeviceList() {
        deviceNames.clear()
        deviceNames.addAll(getAudioOutputDeviceNames(context))
    }
    //Permissões
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var audioPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val requestAudioPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        audioPermissionGranted = granted
        if (granted) {
            ttsHelper.speak("Permissão de microfone concedida.")
        } else {
            ttsHelper.speak("Permissão de microfone negada.")
        }
    }
    //Inicia os serviços
    LaunchedEffect(Unit) {
        updateDeviceList()
        motionDetector.start()
        hasNotificationPermission = isNotificationPermissionGranted(context)
        ttsHelper.speak("Doma Assistente iniciado.")
        if (!hasNotificationPermission) {
            ttsHelper.speak("Permissão de notificação não concedida. Toque para ativar.")
        }
        if (!audioPermissionGranted) {
            ttsHelper.speak("Solicitando permissão de microfone.")
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    //Observa o retorno ao app e revalida a permissão de notificação
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = isNotificationPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    //Registra mudanças
    DisposableEffect(Unit) {
        val receiver = AudioDeviceReceiver { updateDeviceList() }
        val filter = receiver.getIntentFilter()
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        //Libera recursos
        onDispose {
            context.unregisterReceiver(receiver)
            ttsHelper.shutdown()
            motionDetector.stop()
            speechHelper.stop()
        }
    }
    //Área principal da interface
    Box(
        Modifier
            .fillMaxSize()
            .clickable {
                //Abre configurações de permissão de notificação
                if (!hasNotificationPermission) {
                    ttsHelper.speak("Abrindo configurações de notificação.")
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (e: Exception) {
                        ttsHelper.speak("Não foi possível abrir as configurações")
                    }
                } else {
                    if (audioPermissionGranted) {
                        ttsHelper.speak("Diga o comando.") {
                            Handler(Looper.getMainLooper()).post {
                                speechHelper.startListening()
                            }
                        }
                    } else {
                        ttsHelper.speak("Permissão de microfone não concedida.")
                    }
                }
            }
    ) {
        //Lista de dispositivos de aúdio
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                Text("Dispositivos de saída:", fontSize = 16.sp)
            }
            items(deviceNames) { name ->
                Text("• $name", fontSize = 14.sp)
            }
        }
    }
}