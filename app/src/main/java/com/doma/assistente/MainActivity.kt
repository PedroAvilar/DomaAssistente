package com.doma.assistente

import android.content.Context
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
import com.doma.assistente.audio.getAudioOutputDeviceNames
import com.doma.assistente.audio.AudioDeviceReceiver

//Classe para obter as saídas de áudio
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Conteúdo da interface
        setContent {
            MaterialTheme {
                val deviceNames = remember {mutableStateListOf<String>()}

                //Função para atualizar os dispositivos conectados
                fun updateDeviceList() {
                    deviceNames.clear()
                    deviceNames.addAll(getAudioOutputDeviceNames(this))
                }

                //Atualiza ao iniciar
                LaunchedEffect(Unit) {
                    updateDeviceList()
                }

                //Registra o receiver para ouvir alterações
                DisposableEffect(Unit) {
                    val receiver = AudioDeviceReceiver {updateDeviceList()}
                    val filter = receiver.getIntentFilter()
                    registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
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