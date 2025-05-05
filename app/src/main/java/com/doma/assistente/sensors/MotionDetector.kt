package com.doma.assistente.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import com.doma.assistente.tts.TextToSpeechHelper
import com.doma.assistente.voice.SpeechRecognitionHelper
import com.doma.assistente.voice.VoiceCommandProcessor
import com.doma.assistente.emergency.EmergencyHelper
import kotlin.math.sqrt

//Classe para detectar movimentos bruscos, como queda, usando o acelerômetro
class MotionDetector (
    private val context: Context,
    private val ttsHelper: TextToSpeechHelper,
    private val voiceCommandProcessor: VoiceCommandProcessor,
    private val emergencyHelper: EmergencyHelper
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = null

    private val shakeThreshold = 25f  //Valor mínimo para considerar como movimento brusco
    private var lastAlertTime = 0L    //Armazena o timestamp do último alerta
    private val cooldownMs = 5000     //Tempo de espera entre alertas

    //Inicia o detectar e registra o sensor
    fun start() {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    //Encerra o detectar, removendo o listener do sensor
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    //Método chamado sempre que o sensor detecta uma mudança
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            //Calcula a magnitude da aceleração total
            val acceleration = sqrt(x * x + y * y + z * z)

            val currentTime = System.currentTimeMillis()

            //Verifica se o movimento já passou o tempo de espera
            if (acceleration > shakeThreshold && currentTime - lastAlertTime > cooldownMs) {
                lastAlertTime = currentTime

                //Alerta o usuário do movimento
                ttsHelper.speak("Movimento brusco detectado. Você está bem?")
                voiceCommandProcessor.isAwaitingFallResponse = true

                //Inicia reconhecimento após 2s
                Handler(Looper.getMainLooper()).postDelayed({
                    startListeningWithTimeout()
                }, 2000)
            }
        }
    }

    //Método obrigatório, não utilizado
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    //Função para o reconhecimento após 2s
    private fun startListeningWithTimeout() {
        val speechHelper = SpeechRecognitionHelper(
            context = context,
            onCommandRecognized = { command -> voiceCommandProcessor.process(command) },
            onError = { error -> ttsHelper.speak(error) }
        )
        speechHelper.startListening()

        Handler(Looper.getMainLooper()).postDelayed({
            if (voiceCommandProcessor.isAwaitingFallResponse) {
                ttsHelper.speak("Sem resposta. Iniciando alarme de emergência.")
                voiceCommandProcessor.isAwaitingFallResponse = false
                emergencyHelper.playAlarm()
            }
        }, 10000)
    }
}