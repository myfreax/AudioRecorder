package com.myfreax.audiorecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.activity.result.ActivityResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AudioRecordService : Service() {
    companion object {
        private lateinit var activityResult: ActivityResult
        const val TAG = "AudioRecordService"
        const val NOTIFICATION_ID = 441823
        const val NOTIFICATION_CHANNEL_ID = "com.myfreax.webrtc.app"
        const val NOTIFICATION_CHANNEL_NAME = "com.myfreax.webrtc.app"
        fun start(context: Context, mediaProjectionActivityResult: ActivityResult) {
            activityResult = mediaProjectionActivityResult
            val intent = Intent(context, AudioRecordService::class.java)
            context.startForegroundService(intent)
        }
    }

    private val audioRecordingTask by lazy {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = mediaProjectionManager.getMediaProjection(
            activityResult.resultCode,
            activityResult.data!!
        )
        AudioRecordingTask(this, mediaProjection)
    }

    private val fileOutputStream by lazy {
        val audioCapturesDirectory = File(getExternalFilesDir(null), "/AudioCaptures")
        if (!audioCapturesDirectory.exists()) {
            audioCapturesDirectory.mkdirs()
        }
        val timestamp = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(Date())
        val fileName = "Capture-$timestamp.pcm"
        File(audioCapturesDirectory.absolutePath + "/" + fileName)
        FileOutputStream(File(audioCapturesDirectory.absolutePath + "/" + fileName))
    }

    override fun onDestroy() {
        audioRecordingTask.cancel()
        fileOutputStream.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        startRecording()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        createNotificationChannel(this)
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_record_voice_over_24)
            .setContentTitle(this.getString(R.string.app_name))
            .setContentText(this.getString(R.string.recording))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setShowWhen(true)
            .build()
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            NOTIFICATION_ID,
            notification
        )
        startForeground(
            NOTIFICATION_ID,
            notification,
        )
    }

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun startRecording() {
        audioRecordingTask.execute(fileOutputStream)
    }
}