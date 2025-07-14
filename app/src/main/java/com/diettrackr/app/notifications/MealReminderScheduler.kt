package com.diettrackr.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.diettrackr.app.data.models.Meal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MealReminderScheduler(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun scheduleMealReminders(meals: List<Meal>) {
        // Cancel existing alarms first
        cancelAllMealReminders(meals)
        
        val today = LocalDate.now()
        
        meals.forEach { meal ->
            scheduleMealReminder(meal, today)
            
            // Also schedule for tomorrow to ensure continuity
            val tomorrow = today.plusDays(1)
            scheduleMealReminder(meal, tomorrow)
        }
        
        Log.d("MealReminderScheduler", "Scheduled ${meals.size} meal reminders for today and tomorrow")
    }
    
    private fun scheduleMealReminder(meal: Meal, date: LocalDate) {
        // Create the notification time (15 minutes before meal time)
        val mealDateTime = date.atTime(meal.time)
        val reminderDateTime = mealDateTime.minusMinutes(15)
        
        // Only schedule if the reminder time is in the future
        val now = LocalDateTime.now()
        if (reminderDateTime.isAfter(now)) {
            val reminderTimeMillis = reminderDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            val intent = Intent(context, MealReminderReceiver::class.java).apply {
                putExtra("MEAL_ID", meal.id)
                putExtra("MEAL_NAME", meal.name)
                putExtra("MEAL_TIME", meal.time.toString())
                putExtra("NOTIFICATION_DATE", date.toString())
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                generateUniqueRequestCode(meal.id, date),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
                
                Log.d("MealReminderScheduler", 
                    "Scheduled reminder for ${meal.name} at $reminderDateTime")
            } catch (e: SecurityException) {
                Log.e("MealReminderScheduler", 
                    "Failed to schedule alarm for ${meal.name}. Missing permission?", e)
            }
        }
    }
    
    fun cancelAllMealReminders(meals: List<Meal>) {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        
        meals.forEach { meal ->
            cancelMealReminder(meal, today)
            cancelMealReminder(meal, tomorrow)
        }
    }
    
    private fun cancelMealReminder(meal: Meal, date: LocalDate) {
        val intent = Intent(context, MealReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            generateUniqueRequestCode(meal.id, date),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    private fun generateUniqueRequestCode(mealId: Int, date: LocalDate): Int {
        // Generate unique request code combining meal ID and date
        val dateCode = date.dayOfYear * 1000 + date.year % 1000
        return mealId * 100000 + dateCode
    }
    
    fun rescheduleForTomorrow(meals: List<Meal>) {
        val tomorrow = LocalDate.now().plusDays(1)
        meals.forEach { meal ->
            scheduleMealReminder(meal, tomorrow)
        }
        Log.d("MealReminderScheduler", "Rescheduled ${meals.size} meals for tomorrow")
    }
} 