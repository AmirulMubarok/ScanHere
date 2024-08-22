package com.example.scanhear

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler


class SplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Setup MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.welcome) // Ganti "your_audio_file" dengan nama file audio Anda
        mediaPlayer.start()

        // Durasi splash screen
        val splashScreenDuration = 4000L // 4 detik

        Handler().postDelayed({
            // Stop the audio when moving to the next activity
            mediaPlayer.stop()
            mediaPlayer.release()

            // Pindah ke aktivitas utama
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, splashScreenDuration)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Pastikan media player di-release jika aktivitas dihancurkan
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}