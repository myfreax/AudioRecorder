package com.myfreax.audiorecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

class AudioRecordingTask(context: Context, mediaProjection: MediaProjection) : CoroutineScope {

    companion object {
        const val TAG = "AudioRecordingTask"
    }
    private var running:Boolean = false
    private var audioRecord: AudioRecord? = null
    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    init {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            audioRecord =
                AudioRecord.Builder()
                    .setAudioFormat(
                        AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(44100)
                            .setChannelMask(
                                AudioFormat.CHANNEL_IN_MONO
                            )
                            .build()
                    )
                    .setBufferSizeInBytes(2 * 1024 * 1024)
                    .setAudioPlaybackCaptureConfig(
                        AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                            .addMatchingUsage(AudioAttributes.USAGE_GAME)
                            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                            .build()
                    )
                    .build()
        } else {
            throw Exception("AudioPlaybackCapture: Permission Deny")
        }
    }

    fun cancel() {
        running = false
        job.cancel()
        audioRecord = null
    }

    fun execute(fileOutputStream: FileOutputStream) = launch {
        Log.d(TAG, "AudioRecord Start Recording")
        running = true
        audioRecord?.startRecording()
        withContext(Dispatchers.IO) {
            while (running) {
                val byteArray = ByteArray(1024)
                audioRecord?.read(byteArray, 0, byteArray.size)
                fileOutputStream.write(byteArray, 0, byteArray.size)
            }
        }
    }
}