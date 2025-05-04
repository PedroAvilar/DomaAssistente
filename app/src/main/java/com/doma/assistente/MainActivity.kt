package com.doma.assistente

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.items

//Classe para obter as saídas de áudio
class MainActivity : ComponentActivity() {

    //Classe interna para escutar eventos de conexão e desconexão
    inner class AudioDeviceReceiver(private val onDevicesChanged: () -> Unit) : BroadcastReceiver () {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY ||
                intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED ||
                intent?.action == BluetoothDevice.ACTION_ACL_DISCONNECTED
            ) {
                onDevicesChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Obtém o gerenciador de áudio do sistema
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //Conteúdo da interface
        setContent {
            MaterialTheme {
                val deviceNames = remember {mutableStateListOf<String>()}

                //Função para atualizar os dispositivos conectados
                fun updateDeviceList() {
                    val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                    deviceNames.clear()
                    deviceNames.addAll(audioDevices.map {
                        when (it.type) {
                            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Alto-falante embutido"
                            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Fone Bluetooth A2DP"
                            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Fone Bluetooth SCO"
                            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Fones com fio"
                            else -> "Outro: ${it.type}"
                        }
                    })
                }

                //Atualiza a lista
                LaunchedEffect(Unit) {
                    updateDeviceList()
                }

                //Registra o BroadcastReceiver
                DisposableEffect(Unit) {
                    val receiver = AudioDeviceReceiver {updateDeviceList()}
                    val filter = IntentFilter().apply {
                        addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                    }
                    registerReceiver(receiver, filter)
                    onDispose {
                        unregisterReceiver(receiver)
                    }
                }

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
                }
            }
        }
    }
}