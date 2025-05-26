package com.example.gestiondeoptica

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnChangeLogo: Button
    private lateinit var ivCurrentLogoPreview: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    companion object {
        const val LOGO_PATH_KEY = "custom_logo_path"
        const val CUSTOM_LOGO_FILENAME = "custom_logo.png"
        const val ADMIN_USERNAME_KEY = "admin_username" // Added
        const val ADMIN_PASSWORD_KEY = "admin_password" // Added
    }

    private lateinit var etCurrentPassword // Added
            : android.widget.EditText
    private lateinit var etNewPassword // Added
            : android.widget.EditText
    private lateinit var etConfirmNewPassword // Added
            : android.widget.EditText
    private lateinit var btnChangePassword // Added
            : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = getString(R.string.settings_title)

        btnChangeLogo = findViewById(R.id.btn_change_logo)
        ivCurrentLogoPreview = findViewById(R.id.iv_current_logo_preview)

        // Password change UI
        etCurrentPassword = findViewById(R.id.et_current_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password)
        btnChangePassword = findViewById(R.id.btn_change_password)

        sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // Copy the image to internal storage
                val internalFile = copyUriToInternalStorage(it, CUSTOM_LOGO_FILENAME)
                internalFile?.let { file ->
                    // Save the path to the internal file
                    sharedPreferences.edit().putString(LOGO_PATH_KEY, file.absolutePath).apply()
                    // Load the image into the preview
                    ivCurrentLogoPreview.setImageURI(Uri.fromFile(file))
                    Toast.makeText(this, getString(R.string.settings_toast_logo_updated), Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, getString(R.string.settings_toast_logo_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnChangeLogo.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        btnChangePassword.setOnClickListener { // Added
            handleChangePassword()
        }

        loadCurrentLogo()
    }

    private fun copyUriToInternalStorage(uri: Uri, filename: String): File? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(filesDir, filename)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadCurrentLogo() {
        val logoPath = sharedPreferences.getString(LOGO_PATH_KEY, null)
        logoPath?.let {
            val logoFile = File(it)
            if (logoFile.exists()) {
                ivCurrentLogoPreview.setImageURI(Uri.fromFile(logoFile))
            }
        }
    }

    private fun handleChangePassword() { // Added
        val currentPasswordInput = etCurrentPassword.text.toString()
        val newPasswordInput = etNewPassword.text.toString()
        val confirmNewPasswordInput = etConfirmNewPassword.text.toString()

        val storedPassword = sharedPreferences.getString(ADMIN_PASSWORD_KEY, "admin") // Default if not set

        if (currentPasswordInput != storedPassword) {
            Toast.makeText(this, getString(R.string.settings_toast_current_password_incorrect), Toast.LENGTH_SHORT).show()
            return
        }

        if (newPasswordInput.isEmpty() || newPasswordInput.length < 6) {
            Toast.makeText(this, getString(R.string.settings_toast_new_password_min_length), Toast.LENGTH_SHORT).show()
            return
        }

        if (newPasswordInput != confirmNewPasswordInput) {
            Toast.makeText(this, getString(R.string.settings_toast_passwords_do_not_match), Toast.LENGTH_SHORT).show()
            return
        }

        // Save the new password
        sharedPreferences.edit().putString(ADMIN_PASSWORD_KEY, newPasswordInput).apply()
        Toast.makeText(this, getString(R.string.settings_toast_password_changed_successfully), Toast.LENGTH_SHORT).show()

        // Clear fields
        etCurrentPassword.text.clear()
        etNewPassword.text.clear()
        etConfirmNewPassword.text.clear()
    }
}
