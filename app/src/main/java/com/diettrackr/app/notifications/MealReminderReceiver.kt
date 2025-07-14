package com.diettrackr.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.diettrackr.app.R
import com.diettrackr.app.data.db.AppDatabase
import com.diettrackr.app.data.models.DailyLog
import com.diettrackr.app.data.models.MealStatus
import com.diettrackr.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MealReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mealId = intent.getIntExtra("MEAL_ID", -1)
        val mealName = intent.getStringExtra("MEAL_NAME") ?: "Meal"
        val mealTimeString = intent.getStringExtra("MEAL_TIME") ?: ""
        val notificationDate = intent.getStringExtra("NOTIFICATION_DATE") ?: ""
        
        if (mealId == -1) {
            Log.e("MealReminderReceiver", "Invalid meal ID received")
            return
        }
        
        Log.d("MealReminderReceiver", "Received reminder for meal: $mealName")
        
        // Check if meal is already completed for today
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val date = LocalDate.parse(notificationDate)
                val log = database.dailyLogDao().getLogForMealAndDate(mealId, date)
                
                // Only show notification if meal is not completed or skipped
                if (log == null || log.status == MealStatus.PENDING) {
                    showMealReminder(context, mealId, mealName, mealTimeString)
                    
                    // Create a log entry if it doesn't exist
                    if (log == null) {
                        database.dailyLogDao().insertDailyLog(
                            DailyLog(
                                mealId = mealId,
                                date = date,
                                status = MealStatus.PENDING
                            )
                        )
                    }
                } else {
                    Log.d("MealReminderReceiver", "Meal $mealName already completed, skipping notification")
                }
            } catch (e: Exception) {
                Log.e("MealReminderReceiver", "Error checking meal status", e)
                // Show notification anyway if we can't check status
                showMealReminder(context, mealId, mealName, mealTimeString)
            }
        }
        
        // Schedule notifications for tomorrow
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val meals = database.mealDao().getAllMeals()
                val scheduler = MealReminderScheduler(context)
                scheduler.rescheduleForTomorrow(meals)
            } catch (e: Exception) {
                Log.e("MealReminderReceiver", "Error rescheduling for tomorrow", e)
            }
        }
    }
    
    private fun showMealReminder(context: Context, mealId: Int, mealName: String, mealTimeString: String) {
        try {
            val mealTime = LocalTime.parse(mealTimeString)
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
            val formattedTime = mealTime.format(timeFormatter)
            
            // Create intent for the notification
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("OPEN_MEAL_ID", mealId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 
                mealId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Build notification
            val builder = NotificationCompat.Builder(context, "MEAL_NOTIFICATIONS")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🍽️ $mealName Reminder")
                .setContentText("Your $mealName is scheduled at $formattedTime. Time to prepare!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Your $mealName is scheduled at $formattedTime. Tap to view your meal plan and track your progress.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            
            // Show notification if permissions are granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context, 
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(context).notify(
                        "meal_reminder_$mealId".hashCode(), 
                        builder.build()
                    )
                    Log.d("MealReminderReceiver", "Notification shown for $mealName")
                } else {
                    Log.w("MealReminderReceiver", "Notification permission not granted")
                }
            } else {
                NotificationManagerCompat.from(context).notify(
                    "meal_reminder_$mealId".hashCode(), 
                    builder.build()
                )
                Log.d("MealReminderReceiver", "Notification shown for $mealName")
            }
            
        } catch (e: Exception) {
            Log.e("MealReminderReceiver", "Error showing notification", e)
        }
    }
} 