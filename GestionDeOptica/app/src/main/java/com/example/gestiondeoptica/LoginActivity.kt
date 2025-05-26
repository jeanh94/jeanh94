package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Context // Added
import android.content.SharedPreferences // Added

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val usernameInput = etUsername.text.toString()
            val passwordInput = etPassword.text.toString()

            val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            // Get stored credentials. Use "admin" as default only if not found (first launch scenario)
            val storedUsername = sharedPreferences.getString(SettingsActivity.ADMIN_USERNAME_KEY, "admin")
            val storedPassword = sharedPreferences.getString(SettingsActivity.ADMIN_PASSWORD_KEY, "admin")

            // If it's the very first launch and defaults were used to fetch, ensure they are saved.
            if (!sharedPreferences.contains(SettingsActivity.ADMIN_USERNAME_KEY)) {
                sharedPreferences.edit()
                    .putString(SettingsActivity.ADMIN_USERNAME_KEY, storedUsername)
                    .putString(SettingsActivity.ADMIN_PASSWORD_KEY, storedPassword)
                    .apply()
            }


            if (usernameInput == storedUsername && passwordInput == storedPassword) {
                // Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Finish LoginActivity so user can't navigate back to it
            } else {
                Toast.makeText(this, getString(R.string.login_credentials_incorrect), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
