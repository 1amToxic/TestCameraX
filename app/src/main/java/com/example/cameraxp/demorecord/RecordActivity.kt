package com.example.cameraxp.demorecord

import android.Manifest
import android.R.attr.data
import android.content.pm.PackageManager
import android.media.*
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.cameraxp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.log


class RecordActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var btnStartRecord: FloatingActionButton
    lateinit var btnStopRecord: FloatingActionButton
    lateinit var btnPlayRecord: FloatingActionButton
    var myRecorder: MediaRecorder? = null
    lateinit var outputFile: String
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val samplingRate = 11025  /* in Hz*/
    private val channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize =
        AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat)
    private val sampleNumBits = 16
    private val numChannels = 1
    private fun analysisAudio(){
        var data = ShortArray(100000)
        val recorder =
            AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize)
        recorder.startRecording()
        val isRecording = true

//        val audioPlayer = AudioTrack(
//            AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//            AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM
//        )
//
//        if (audioPlayer.playState != AudioTrack.PLAYSTATE_PLAYING) audioPlayer.play()
        var readBytes :Int
        var writtenBytes = 0
        do {
            readBytes = recorder.read(data, 0, bufferSize/4)
            Log.d("AppLog","read " + readBytes.toString())
            if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
//                writtenBytes += audioPlayer.write(data, 0, readBytes)
                Log.d("AppLog","write "+ writtenBytes.toString())
            }
        } while (isRecording)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
        btnStartRecord = findViewById(R.id.fab_record)
        btnStopRecord = findViewById(R.id.fab_stop)
        btnPlayRecord = findViewById(R.id.fab_play)
        btnPlayRecord.setOnClickListener(this)
        btnStartRecord.setOnClickListener(this)
        btnStopRecord.setOnClickListener(this)
        btnStopRecord.visibility = View.INVISIBLE
        btnPlayRecord.visibility = View.INVISIBLE
        outputFile =
            this.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath + "/recording.3gp"
        myRecorder = MediaRecorder()
        myRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        myRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        myRecorder!!.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        myRecorder!!.setOutputFile(outputFile)

    }
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
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab_record -> {
                try {
//                    myRecorder!!.prepare()
//                    myRecorder!!.start()
                    analysisAudio()
                } catch (ex: Exception) {

                }
                btnStartRecord.visibility = View.INVISIBLE
                btnStopRecord.visibility = View.VISIBLE
                Log.d("AppLog", "Record start")
            }
            R.id.fab_stop -> {
                myRecorder!!.stop()
                myRecorder!!.release()
                myRecorder = null
                btnStartRecord.visibility = View.VISIBLE
                btnStopRecord.visibility = View.INVISIBLE
                btnPlayRecord.visibility = View.VISIBLE
                Log.d("AppLog", "Record stop")
            }
            R.id.fab_play -> {
                val mediaPlayer: MediaPlayer? = MediaPlayer()
                try {
                    mediaPlayer!!.setDataSource(outputFile)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    Log.d("AppLog","Play Start")

                } catch (ex: Exception){

                }
            }
        }
    }
}
