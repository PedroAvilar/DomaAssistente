package com.doma.assistente.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.doma.assistente.tts.TextToSpeechHelper

//Composable de inicialização do TTS e navegação para MainScreen
@Composable
fun AppRoot() {
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    var ttsFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(10000)
        if (!ttsReady) {
            ttsFailed = true
        }
    }
    //Inicia o TTS
    val ttsHelper = remember {
        TextToSpeechHelper(context) {
            ttsReady = true
        }
    }
    when {
        //Quando falha
        ttsFailed -> {
            Box(
                Modifier.fillMaxSize().padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Falha ao iniciar o TTS. Verifique permissões ou reinicie o app.", fontSize = 16.sp)
            }
            return
        }
        //Quando ainda não está pronto
        !ttsReady -> {
            Box(
                Modifier.fillMaxSize().padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Aguarde. Tentando iniciar o Text To Speech.", fontSize = 16.sp)
            }
            return
        }
        else -> MainScreen(ttsHelper)
    }
}