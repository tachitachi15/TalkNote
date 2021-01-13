package com.yuki.talknote

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.FFmpeg
import java.io.IOException

private const val LOG_TAG = "MainActivity"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class TalkRecordActivity : AppCompatActivity() {
    private var fileName: String = ""
    private var recorder: MediaRecorder? = null

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private fun startRecording() {

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "recording prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            reset()
            release()
        }
        recorder = null
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun ffmpeg(filename:String):String{
        FFmpeg.execute("-i $filename -af volumedetect -f null NULL")
        val rc = FFmpeg.getLastReturnCode()
        val output = FFmpeg.getLastCommandOutput()

        return when(rc) {
            FFmpeg.RETURN_CODE_SUCCESS -> "FFmpeg Success!\n $output"
            FFmpeg.RETURN_CODE_CANCEL -> "FFmpeg Cancel\n $output"
            else -> "FFmpeg Error\n $output"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk_record)

        volumeControlStream = AudioManager.STREAM_MUSIC
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val recordButton = findViewById<Button>(R.id.button_record)
        var mStartRecording = true

        recordButton.setOnClickListener{
            onRecord(mStartRecording)
            recordButton.text = when(mStartRecording){
                true -> "Stop Recording"
                false -> "Start Recording"
            }
            mStartRecording=!mStartRecording
            ffmpeg(fileName)
        }

    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
//        player?.release()
//        player = null
    }
}