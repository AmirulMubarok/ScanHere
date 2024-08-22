package com.example.scanhear

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback

class MainActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var mediaPlayer: MediaPlayer
    private val CAMERA_REQUEST_CODE = 101
    private val QR_CODE_TIMEOUT = 16000L // 16 seconds
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeoutRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Play opening audio
        playOpeningAudio()

        val scannerView: CodeScannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        codeScanner.decodeCallback = DecodeCallback { result ->
            runOnUiThread {
                handleQRCode(result.text)
            }
        }

        codeScanner.errorCallback = ErrorCallback { error ->
            runOnUiThread {
                error.printStackTrace()
            }
        }

        // Initialize timeout Runnable
        timeoutRunnable = Runnable {
            playWarningSoundAndCloseApp()
        }

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun playOpeningAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.opening) // Assume opening_audio is the audio file for the opening sound
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            stopAudio()
        }
    }

    private fun startScanning() {
        codeScanner.startPreview()
        startTimeout() // Start the timeout when scanning starts
    }

    private fun startTimeout() {
        handler.postDelayed(timeoutRunnable, QR_CODE_TIMEOUT)
    }

    private fun cancelTimeout() {
        handler.removeCallbacks(timeoutRunnable)
    }

    private fun playWarningSoundAndCloseApp() {
        mediaPlayer = MediaPlayer.create(this, R.raw.warning) // Assume warning_audio is the audio file for the warning sound
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            // Close the app after the warning sound finishes
            finishAffinity()
        }
    }

    private fun handleQRCode(result: String) {
        // Stop any currently playing audio
        stopAudio()

        // Cancel the timeout since a QR code was detected
        cancelTimeout()

        when (result) {
            "QR_CODE_1" -> {
                playAudio(R.raw.audio1)
            }
            "QR_CODE_2" -> {
                playAudio(R.raw.audio2)
            }
            else -> {
                // Handle unknown QR code
            }
        }
        // Restart preview if needed
        codeScanner.startPreview()
    }

    private fun playAudio(audioResId: Int) {
        mediaPlayer = MediaPlayer.create(this, audioResId)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            stopAudio()
            // Start timeout with 16 seconds delay after audio finishes
            startTimeout()
        }
    }

    private fun stopAudio() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
        startTimeout() // Start timeout when resuming
    }

    override fun onPause() {
        codeScanner.releaseResources()
        stopAudio()
        cancelTimeout() // Cancel timeout when pausing
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio() // Ensure audio is stopped when activity is destroyed
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }
}
