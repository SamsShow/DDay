package com.diettrackr.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import android.view.ViewGroup
import android.widget.LinearLayout
import com.diettrackr.app.data.db.AppDatabase

/**
 * Debug activity to help diagnose app crashes
 * This is a simple activity that doesn't use Jetpack Compose
 */
class DebugActivity : AppCompatActivity() {
    
    companion object {
        const val TAG = "DebugActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create layout programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Add title
        val titleText = TextView(this).apply {
            text = "Debug Activity"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 32)
            }
        }
        layout.addView(titleText)
        
        // Add database test button
        val testDbButton = Button(this).apply {
            text = "Test Database Connection"
            setOnClickListener {
                testDatabaseConnection()
            }
        }
        layout.addView(testDbButton)
        
        // Add launch main activity button
        val launchMainButton = Button(this).apply {
            text = "Launch Main Activity"
            setOnClickListener {
                try {
                    startActivity(Intent(this@DebugActivity, MainActivity::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "Error launchingMainActivity", e)
                    val resultText = TextView(this@DebugActivity).apply {
                        text = "Error: ${e.message}"
                        setTextColor(android.graphics.Color.RED)
                    }
                    layout.addView(resultText)
                }
            }
        }
        layout.addView(launchMainButton)
        
        // Status text view
        val statusText = TextView(this).apply {
            text = "Ready"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 32, 0, 0)
            }
            id = 1001
        }
        layout.addView(statusText)
        
        setContentView(layout)
    }
    
    private fun testDatabaseConnection() {
        val statusText = findViewById<TextView>(1001)
        
        try {
            val db = AppDatabase.getDatabase(this)
            statusText.text = "Database connection successful."
            statusText.setTextColor(android.graphics.Color.GREEN)
        } catch (e: Exception) {
            Log.e(TAG, "Database test failed", e)
            statusText.text = "Database error: ${e.message}"
            statusText.setTextColor(android.graphics.Color.RED)
        }
    }
} 