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
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*

class RecordActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var btnStartRecord: FloatingActionButton
    lateinit var btnStopRecord: FloatingActionButton
    lateinit var btnPlayRecord: FloatingActionButton
    var myRecorder: MediaRecorder? = null
    lateinit var outputFile: String
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    //setup for record and playing then
    private var isRun = false
    var bufferSize : Int = 0
    private val SAMPLE_RATE = 44100
    lateinit var mRecord : AudioRecord
    lateinit var mTrack : AudioTrack
    private val encoding_pcm = AudioFormat.ENCODING_PCM_16BIT
    var buffer = ByteArray(44200)
    private fun analysisAudio(){
        try {
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_CONFIGURATION_MONO,
                                    encoding_pcm)
            if (bufferSize <= SAMPLE_RATE) {
                bufferSize = SAMPLE_RATE;
            }
            mRecord = AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE,
                                    AudioFormat.CHANNEL_IN_MONO,
                                    encoding_pcm,
                                    bufferSize*1)
            mTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                                SAMPLE_RATE,AudioFormat.CHANNEL_OUT_MONO,
                                encoding_pcm,
                                bufferSize * 1,
                                AudioTrack.MODE_STREAM)
            mTrack.playbackRate = SAMPLE_RATE
        }catch (ex: Exception){

        }
        mRecord.startRecording()
        Log.d("AppLog","StartRecording")
        mTrack.play()
        Log.d("AppLog","StartPlaying")
        isRun = true
        var readBytes :  Int = 0
        var writeBytes : Int = 0
        do {
            Observable.just("buffer")
                .observeOn(Schedulers.io())
                .subscribe(
                    {readBytes = mRecord.read(buffer,0,SAMPLE_RATE)},
                    { error -> Log.d("AppLog",error.toString())},
                    {if (AudioRecord.ERROR_INVALID_OPERATION != readBytes) {
                        writeBytes += mTrack.write(buffer, 0, readBytes)
                    }}
                )
        }while (isRun)
    }
    private fun do_loopback(){
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
        outputFile = this.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath + "/recordings.3gp"
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
//                    myRecorder = MediaRecorder()
//                    myRecorder!!.apply {
//                        setAudioSource(MediaRecorder.AudioSource.MIC)
//                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                        setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
//                        setOutputFile(outputFile)
//                    }
                    isRun = true
//                    myRecorder!!.prepare(                                                                                                                    )
//                    myRecorder!!.start()
                    analysisAudio()
//                    do_loopback()
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
