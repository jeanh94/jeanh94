package com.example.gestiondeoptica

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.net.Uri
import java.io.File

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivSplashLogo = findViewById<ImageView>(R.id.iv_splash_logo)
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val logoPath = sharedPreferences.getString(SettingsActivity.LOGO_PATH_KEY, null)

        logoPath?.let {
            val logoFile = File(it)
            if (logoFile.exists()) {
                ivSplashLogo.setImageURI(Uri.fromFile(logoFile))
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // Intent to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 2 seconds delay
    }
}
