package com.yuki.talknote

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.FFmpeg
import com.yuki.talkmemo.Talk
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.lang.NumberFormatException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

private const val LOG_TAG = "MainActivity"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

private const val DELIMITER_MEAN_VOLUME = "mean_volume: "
private const val DELIMITER_MAX_VOLUME = "max_volume: "

class TalkRecordActivity : AppCompatActivity() {
    private var fileName: String = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    private lateinit var addTalkViewModel: AddTalkViewModel

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
            setOutputFormat(MediaRecorder.OutputFormat.AMR_WB)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            //setAudioSamplingRate(16000)

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
//        Timer().schedule(30000){
//            stopRecording()
//        }
        startRecording()
    } else {
        stopRecording()
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    private fun onPlay(start: Boolean) = if (start) {
        startPlaying()
    } else {
        stopPlaying()
    }

    private fun getVolume(delimiter: String,output:String):Double?{
        var volume: Double? = null
        if(Regex(delimiter).containsMatchIn(output)) {
            volume = try{
                output.split(delimiter)[1].split(" ")[0].toDouble()
            }catch (e: NumberFormatException){
                null
            }
        }
        return volume
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
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.awb"

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        val recordButton = findViewById<Button>(R.id.button_record)
        val ffmpegButton = findViewById<Button>(R.id.button_ffmpeg)
        val recognizeButton = findViewById<Button>(R.id.button_recognize)
        val playButton = findViewById<Button>(R.id.button_playing)

        val textMaxVolume = findViewById<TextView>(R.id.text_maxvolume)
        val textMeanVolume =  findViewById<TextView>(R.id.text_meanvolume)
        val textRecognition = findViewById<TextView>(R.id.text_recognition)
        var mStartRecording = true
        var mStartPlaying = true

        playButton.setOnClickListener{
            onPlay(mStartPlaying)
            playButton.text = when(mStartPlaying){
                true -> "Stop"
                false -> "Start"
            }
            mStartPlaying=!mStartPlaying
        }

        recordButton.setOnClickListener{

            onRecord(mStartRecording)
            recordButton.text = when(mStartRecording){
                true -> "Stop Recording"
                false -> "Start Recording"
            }
            mStartRecording=!mStartRecording
            //textFFmpeg.text = ffmpeg(fileName)
        }

        ffmpegButton.setOnClickListener{
            if (fileName.isNotEmpty()) {
                val output = ffmpeg(fileName)
                val maxVolume = getVolume(DELIMITER_MAX_VOLUME, output)
                val meanVolume = getVolume(DELIMITER_MEAN_VOLUME, output)

                textMaxVolume.text = maxVolume.toString()
                textMeanVolume.text = meanVolume.toString()
            }else{
                textMaxVolume.text = "file is not exist"
            }
        }

        recognizeButton.setOnClickListener{
            addTalkViewModel =  ViewModelProvider(this).get(AddTalkViewModel::class.java)
            val date:String = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
//            SpeechToTextClient(fileName).recognitionStart()?.let{
//                addTalkViewModel.insert(Talk(0,date,it))
//            }
            //mainスレッドで処理が重いと言われた
            thread {
                addTalkViewModel.insert(Talk(0,date,SpeechToTextClient(fileName).recognitionStart()))
            }
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