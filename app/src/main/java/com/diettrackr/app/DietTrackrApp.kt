package com.diettrackr.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.diettrackr.app.notifications.MealReminderWorker
import java.util.concurrent.TimeUnit
import android.util.Log
import android.widget.Toast

class DietTrackrApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupDailyMealReminders()
        
        // Set up global exception handler
        setupExceptionHandler()
        
        Log.d("DietTrackrApp", "Application started")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Meal Reminders"
            val descriptionText = "Notifications for upcoming meals"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MEAL_NOTIFICATIONS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupDailyMealReminders() {
        val mealReminderRequest = PeriodicWorkRequestBuilder<MealReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MEAL_REMINDERS",
            ExistingPeriodicWorkPolicy.KEEP,
            mealReminderRequest
        )
    }

    private fun setupExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("DietTrackrApp", "Uncaught exception", throwable)
                Toast.makeText(
                    applicationContext,
                    "Error: ${throwable.message}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e("DietTrackrApp", "Error in exception handler", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
} 