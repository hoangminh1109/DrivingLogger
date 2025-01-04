package com.hoangminh.drivinglogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var recyclerView: RecyclerView
    private val logEntries = mutableListOf<String>()
    private lateinit var adapter: LogAdapter
    private val settingsFile by lazy { File(filesDir, "settings.txt") }
    private var maxLogFiles = 1000
    private lateinit var buttonContainer: LinearLayout
    private val defaultMaxFile = 1000
    private val defaultButtonList = "Traffic Light, Dangerous, Slow, Violation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        recyclerView = findViewById(R.id.recyclerView)
        adapter = LogAdapter(logEntries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load settings
        val settings = loadSettings()

        val buttonList = settings.optString("buttonList", defaultButtonList)
        buttonContainer = findViewById(R.id.buttonContainer)
        createButtons(buttonList)

        maxLogFiles = settings.optInt("maxFiles", defaultMaxFile)
        loadLogs()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_logs -> {
                clearLogs() // Call the existing clearLogs method
                true
            }
            R.id.action_settings -> {
                // Open settings activity (if needed)
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logTimeAndLocation(logType: String = "Other") {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val locationString = location?.let {
                "${it.latitude}, ${it.longitude}" // Google Maps GPS format
            } ?: "0, 0"
            val logEntry = "$timestamp\n$locationString\n$logType"

            val logFile = File(filesDir, "log.txt")
            val logs = if (logFile.exists()) logFile.readLines().toMutableList() else mutableListOf()

            // Add the new entry to the top of the file
            logs.add(0, logEntry) // Add the new entry at the start
            if (logs.size > maxLogFiles * 3) { // Each entry is 3 lines
                logs.removeAt(logs.size - 1) // Remove the oldest entry (line 3 of the oldest log)
                logs.removeAt(logs.size - 1) // Remove the oldest entry (line 2 of the oldest log)
                logs.removeAt(logs.size - 1) // Remove the oldest entry (line 1 of the oldest log)
            }

            // Write back to the file
            logFile.writeText(logs.joinToString("\n"))

            // Update the in-memory list and UI
            logEntries.add(0, logEntry) // Add the new entry at the top of the list
            adapter.notifyItemInserted(0) // Notify the adapter
            recyclerView.scrollToPosition(0) // Scroll to the top
        }
    }

    private fun loadLogs() {
        val logFile = File(filesDir, "log.txt")
        logEntries.clear() // Clear the in-memory list initially

        if (logFile.exists()) {
            val logs = logFile.readLines()

            // Validate the log file content
            val isValid = logs.chunked(3).all { chunk ->
                chunk.size == 3 && isValidTimestamp(chunk[0]) && isValidLocation(chunk[1])
            }

            if (!isValid) {
                // Clear the file if invalid
                logFile.writeText("")
                Toast.makeText(getApplicationContext(), "Invalid log file content. File has been cleared!", Toast.LENGTH_SHORT).show();
                Log.w("MainActivity", "Invalid log file content. File has been cleared.")
            } else {
                logEntries.addAll(logs.chunked(3).map { it.joinToString("\n") }) // Add valid entries
            }
        }

        adapter.notifyDataSetChanged() // Refresh the RecyclerView
    }

    private fun clearLogs() {
        val logFile = File(filesDir, "log.txt")

        // Clear the log file content
        if (logFile.exists()) {
            logFile.writeText("") // Erase content of the file
        }

        // Clear the in-memory list
        logEntries.clear()

        // Notify the adapter to refresh the RecyclerView
        adapter.notifyDataSetChanged()

        // Show a toast notification
        Toast.makeText(this, "Logs cleared successfully", Toast.LENGTH_SHORT).show()
    }

    private fun isValidTimestamp(timestamp: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.isLenient = false
            format.parse(timestamp) != null // Check if the timestamp can be parsed
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidLocation(location: String): Boolean {
        val regex = """-?\d+(\.\d+)?,\s*-?\d+(\.\d+)?""".toRegex() // Matches latitude, longitude
        return location.matches(regex)
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
    private fun createButtons(buttonList: String) {
        buttonContainer.removeAllViews()

        // Split the button list into individual button texts
        val buttons = buttonList.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        // Create buttons dynamically
        for (buttonText in buttons) {
            val button = Button(this).apply {
                text = buttonText
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.button_width),
                    resources.getDimensionPixelSize(R.dimen.button_height),
                    1f
                )
                gravity = Gravity.CENTER
                setOnClickListener {
                    logTimeAndLocation(buttonText) // Pass the button text to the log function
                }
            }
            buttonContainer.addView(button)
        }

        // Add the fixed "Other" button
        val otherButton = Button(this).apply {
            text = "Other"
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.button_width),
                resources.getDimensionPixelSize(R.dimen.button_height),
                1f
            )
            gravity = Gravity.CENTER
            setOnClickListener {
                logTimeAndLocation()
            }
        }
        buttonContainer.addView(otherButton)
    }

}
