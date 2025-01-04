package com.hoangminh.drivinglogger

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import org.json.JSONObject
import android.app.AlertDialog

class SettingsActivity : AppCompatActivity() {

    private val settingsFile by lazy { File(filesDir, "settings.txt") }
    private val defaultMaxFile = 1000
    private val defaultButtonList = "Traffic Light, Dangerous, Crawling, Violation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val editTextMaxFiles = findViewById<EditText>(R.id.editTextMaxFiles)
        val editTextButtonList = findViewById<EditText>(R.id.editTextButtons)
        val buttonSaveSettings = findViewById<Button>(R.id.buttonSaveSettings)
        val buttonDefault = findViewById<Button>(R.id.buttonDefault)

        // Load existing settings
        val settings = loadSettings()
        editTextMaxFiles.setText(settings.optInt("maxFiles", defaultMaxFile).toString()) // Default to 1000
        editTextButtonList.setText(settings.optString("buttonList", defaultButtonList))

        buttonDefault.setOnClickListener {
            editTextMaxFiles.setText(defaultMaxFile.toString())
            editTextButtonList.setText(defaultButtonList)
        }

        // Save settings
        buttonSaveSettings.setOnClickListener {
            showConfirmDialog(
                title = "Save Settings",
                message = "Are you sure you want to save new settings? This action cannot be undone.",
                onConfirm = {
                    val maxFiles = editTextMaxFiles.text.toString().toIntOrNull()
                    val buttonList = editTextButtonList.text.toString()

                    if (maxFiles != null && maxFiles > 0) {
                        // Save to settings file
                        val newSettings = JSONObject().apply {
                            put("maxFiles", maxFiles)
                            put("buttonList", buttonList)
                        }
                        settingsFile.writeText(newSettings.toString())

                        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please enter a valid setting", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

    }

    private fun loadSettings(): JSONObject {
        val defaultSettings = JSONObject().apply {
            put("maxFiles", defaultMaxFile)
            put("buttonList", defaultButtonList)
        }
        return if (settingsFile.exists()) {
            try {
                JSONObject(settingsFile.readText())
            } catch (e: Exception) {
                settingsFile.writeText(defaultSettings.toString())
                JSONObject(settingsFile.readText())
            }
        } else {
            settingsFile.writeText(defaultSettings.toString())
            JSONObject(settingsFile.readText())
        }
    }

    private fun showConfirmDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
