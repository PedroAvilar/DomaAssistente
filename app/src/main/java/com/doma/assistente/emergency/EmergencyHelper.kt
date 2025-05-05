package com.doma.assistente.emergency

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.doma.assistente.R

//Classe para tocar e parar o alarme de emergência
class EmergencyHelper(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    //Inicia a reprodução do alarme
    fun playAlarm() {
        setMediaVolumeToMax()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.emergency_alarm)
            mediaPlayer?.isLooping = true
        }
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    //Para o alarme e libera os recursos
    fun stopAlarm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    //Verifica se o alarme está tocando
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    //Ajusta o volume de mídia para o máximo
    private fun setMediaVolumeToMax() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
    }
}