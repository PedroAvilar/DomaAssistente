package com.doma.assistente.audio

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager

//Função que retorna os nomes dos dispositivos de saída de áudio
fun getAudioOutputDeviceNames(context: Context): List<String> {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

    return devices.map {
        when (it.type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Alto-falante embutido"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Fone Bluetooth A2DP"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Fone Bluetooth SCO"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Fones com fio"
            else -> "Outro: ${it.type}"
        }
    }
}

//Classe que escuta eventos de conexão e desconexão
class AudioDeviceReceiver(private val onDevicesChanged: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY ||
            intent?.action == BluetoothDevice.ACTION_ACL_CONNECTED ||
            intent?.action == BluetoothDevice.ACTION_ACL_DISCONNECTED
        ) {
            onDevicesChanged()
        }
    }

    //Retorna o filtro com os eventos
    fun getIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
    }
}