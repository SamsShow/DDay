package com.diettrackr.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diettrackr.app.data.db.AppDatabase
import kotlinx.coroutines.coroutineScope

class MealReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Get the database instance and reschedule reminders for today and tomorrow
            val database = AppDatabase.getDatabase(context)
            val meals = database.mealDao().getAllMeals()
            
            // Use the new scheduler to ensure reminders are set up
            val scheduler = MealReminderScheduler(context)
            scheduler.scheduleMealReminders(meals)
            
            android.util.Log.d("MealReminderWorker", "Rescheduled ${meals.size} meal reminders")
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MealReminderWorker", "Error in daily reminder work", e)
            Result.failure()
        }
    }
} 