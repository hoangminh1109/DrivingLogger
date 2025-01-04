package com.hoangminh.drivinglogger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private val settingsFile by lazy { File(filesDir, "settings.txt") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val editTextMaxFiles = findViewById<EditText>(R.id.editTextMaxFiles)
        val buttonSaveSettings = findViewById<Button>(R.id.buttonSaveSettings)

        // Load the current max log value
        val currentMaxLogs = if (settingsFile.exists()) {
            settingsFile.readText().toIntOrNull() ?: 1000 // Default to 1000
        } else {
            1000
        }
        editTextMaxFiles.setText(currentMaxLogs.toString())

        buttonSaveSettings.setOnClickListener {
            val maxFiles = editTextMaxFiles.text.toString().toIntOrNull()
            if (maxFiles != null && maxFiles > 0) {
                settingsFile.writeText(maxFiles.toString())
                Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
