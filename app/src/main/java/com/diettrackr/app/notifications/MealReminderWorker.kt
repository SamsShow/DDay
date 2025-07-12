package com.diettrackr.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diettrackr.app.R
import com.diettrackr.app.data.db.AppDatabase
import com.diettrackr.app.data.models.DailyLog
import com.diettrackr.app.data.models.MealStatus
import com.diettrackr.app.ui.MainActivity
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MealReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Get the database instance
            val database = AppDatabase.getDatabase(context)
            val mealDao = database.mealDao()
            val dailyLogDao = database.dailyLogDao()
            
            // Get today's date
            val today = LocalDate.now()
            
            // Get current time
            val currentTime = LocalTime.now()
            
            // Get all meals
            val allMeals = mealDao.getAllMeals()
            
            // Find upcoming meals (in the next 30 minutes)
            val upcomingMeals = allMeals.filter { meal ->
                val mealTime = meal.time
                val minutesUntilMeal = ChronoUnit.MINUTES.between(currentTime, mealTime)
                
                // If meal time is in the future and less than 30 minutes away
                minutesUntilMeal in 1..30
            }
            
            // For each upcoming meal, check if there's already a log for today
            upcomingMeals.forEach { meal ->
                val log = dailyLogDao.getLogForMealAndDate(meal.id, today)
                
                // If no log exists or the status is still PENDING, show a notification
                if (log == null || log.status == MealStatus.PENDING) {
                    // Create a log entry if it doesn't exist
                    if (log == null) {
                        dailyLogDao.insertDailyLog(
                            DailyLog(
                                mealId = meal.id,
                                date = today,
                                status = MealStatus.PENDING
                            )
                        )
                    }
                    
                    // Show notification
                    showMealReminder(meal.id, meal.name, meal.time)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun showMealReminder(mealId: Int, mealName: String, mealTime: LocalTime) {
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val formattedTime = mealTime.format(timeFormatter)
        
        // Create intent for the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_MEAL_ID", mealId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, mealId, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val builder = NotificationCompat.Builder(context, "MEAL_NOTIFICATIONS")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming Meal: $mealName")
            .setContentText("Time to prepare for your $mealName scheduled at $formattedTime")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show notification if permissions are granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context, 
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(mealId, builder.build())
            }
        } else {
            NotificationManagerCompat.from(context).notify(mealId, builder.build())
        }
    }
} 