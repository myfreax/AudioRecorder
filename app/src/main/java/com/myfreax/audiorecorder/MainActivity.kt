package com.myfreax.audiorecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private var recorderRunning: Boolean = false
    private val recorderSwitcher by lazy {
        findViewById<Button>(R.id.record_switch)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestAudioPermissions()
        recorderSwitcher.setOnClickListener {
            if (recorderRunning) stopRecorder() else startRecorder()
        }
    }

    private fun startRecorder(){
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        resultLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
    }

    private fun stopRecorder() {
        switchButtonStyle(false)
        val intent = Intent(this, AudioRecordService::class.java)
        stopService(intent)
    }

    private fun switchButtonStyle(boolean: Boolean){
        if (boolean){
            recorderRunning = true
            recorderSwitcher.text = getString(R.string.stop_recording)
        }else{
            recorderRunning = false
            recorderSwitcher.text = getString(R.string.start_recording)
        }
    }


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "Media projection permission granted")
            switchButtonStyle(true)
            AudioRecordService.start(this.applicationContext, it)
        }

    private fun requestAudioPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }
}