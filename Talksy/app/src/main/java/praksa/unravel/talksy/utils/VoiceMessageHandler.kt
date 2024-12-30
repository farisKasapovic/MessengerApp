package praksa.unravel.talksy.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

class VoiceMessageHandler(
    private val context: Context,
    private val onDurationUpdate: (Int) -> Unit,
    private val onPlaybackUpdate: (Int) -> Unit
) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String = ""
    private var duration = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateDurationRunnable = object : Runnable {
        override fun run() {
            duration++
            onDurationUpdate(duration)
            handler.postDelayed(this, 1000)
        }
    }


    fun startRecording(): String {
        Log.d("Notify","VMH prije try")
        val audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        audioFilePath = audioFile.absolutePath
        duration = 0
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }
            handler.post(updateDurationRunnable) // Start tracking duration
        } catch (e: Exception) {
            Log.e("VoiceMessageHandler", "Error starting recording: ${e.message}")
            throw e
        }

        return audioFilePath
    }

    // Stop recording
    fun stopRecording(): String {
        handler.removeCallbacks(updateDurationRunnable) 
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceMessageHandler", "Error stopping recording: ${e.message}")
        }
        mediaRecorder = null
        return audioFilePath
    }




}
