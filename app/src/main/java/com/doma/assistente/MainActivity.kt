package com.doma.assistente

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Obtém o gerenciador de áudio do sistema
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        //Lista os dipositivos de saída de áudio conectados
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        //Mapeia os tipos de dispositivos para novos nomes
        val deviceNames = audioDevices.map {
            when (it.type) {
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Alto-falante embutido"
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Fone Bluetooth A2DP"
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Fone Bluetooth SCO"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Fones com fio"
                else -> "Outro: ${it.type}"
            }
        }

        //Conteúdo da interface
        setContent {
            MaterialTheme {
                //Lista rolável
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