package com.diettrackr.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.diettrackr.app.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime

@Database(
    entities = [
        User::class,
        WeightEntry::class,
        Meal::class,
        MealComponent::class,
        DailyLog::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun mealDao(): MealDao
    abstract fun dailyLogDao(): DailyLogDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to daily_logs table
                database.execSQL("ALTER TABLE daily_logs ADD COLUMN manualCalories INTEGER")
                database.execSQL("ALTER TABLE daily_logs ADD COLUMN manualProtein INTEGER")
                database.execSQL("ALTER TABLE daily_logs ADD COLUMN manualCarbs INTEGER")
                database.execSQL("ALTER TABLE daily_logs ADD COLUMN manualFats INTEGER")
                database.execSQL("ALTER TABLE daily_logs ADD COLUMN hasManualEntry INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diet_tracker_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(AppDatabaseCallback())
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class AppDatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateDatabase(database)
                }
            }
        }
        
        private suspend fun prepopulateDatabase(database: AppDatabase) {
            // Create default user
            database.userDao().insertUser(User(id = 1))
            
            // Create default diet plan based on provided image
            val mealDao = database.mealDao()
            
            // 1) Midday Lunch Block
            val lunchMealId = mealDao.insertMeal(
                Meal(
                    name = "Lunch Block",
                    time = LocalTime.of(13, 0),  // 1:00 PM
                    protein = 54,
                    carbs = 100,
                    fats = 20,
                    calories = 700,
                    description = "Main lunch meal with balanced macros"
                )
            ).toInt()
            
            // Add components for lunch
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = lunchMealId,
                    name = "Cooked white rice",
                    quantity = "150g",
                    protein = 0,
                    carbs = 100,
                    fats = 0
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = lunchMealId,
                    name = "Dal (cooked lentils)",
                    quantity = "1 cup (200ml)",
                    protein = 12,
                    carbs = 0,
                    fats = 6
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = lunchMealId,
                    name = "Mixed veggies with sarso oil",
                    quantity = "100g veggies + 1 tbsp oil",
                    protein = 0,
                    carbs = 0,
                    fats = 14
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = lunchMealId,
                    name = "Lassi",
                    quantity = "200ml",
                    protein = 15,
                    carbs = 0,
                    fats = 0
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = lunchMealId,
                    name = "Whey protein",
                    quantity = "1 scoop in water",
                    protein = 27,
                    carbs = 0,
                    fats = 0
                )
            )
            
            // 2) Afternoon Snack
            val snackMealId = mealDao.insertMeal(
                Meal(
                    name = "Afternoon Snack",
                    time = LocalTime.of(16, 0),  // 4:00 PM
                    protein = 15,
                    carbs = 70,
                    fats = 10,
                    calories = 350,
                    description = "Afternoon energy boost"
                )
            ).toInt()
            
            // Add components for snack
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = snackMealId,
                    name = "Oats (water-cooked)",
                    quantity = "50g",
                    protein = 5,
                    carbs = 30,
                    fats = 0
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = snackMealId,
                    name = "Small banana",
                    quantity = "1 banana",
                    protein = 0,
                    carbs = 25,
                    fats = 0
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = snackMealId,
                    name = "Peanut butter",
                    quantity = "1 tbsp",
                    protein = 4,
                    carbs = 0,
                    fats = 8
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = snackMealId,
                    name = "Medjool dates",
                    quantity = "3 dates",
                    protein = 0,
                    carbs = 15,
                    fats = 0
                )
            )
            
            // 3) Pre/Post Workout
            val workoutMealId = mealDao.insertMeal(
                Meal(
                    name = "Pre/Post Workout",
                    time = LocalTime.of(18, 15),  // 6:15 PM
                    protein = 27,
                    carbs = 70,
                    fats = 1,
                    calories = 300,
                    description = "Pre: 6:15 PM, Post: 9:15 PM"
                )
            ).toInt()
            
            // Add components for workout meal
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = workoutMealId,
                    name = "Pre: Banana + dates + creatine + caffeine",
                    quantity = "1 banana + 3 dates + 5g creatine + 200mg caffeine",
                    protein = 0,
                    carbs = 40,
                    fats = 0
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = workoutMealId,
                    name = "Post: Whey protein + boiled potato",
                    quantity = "1 scoop whey + 150g potato",
                    protein = 27,
                    carbs = 30,
                    fats = 1
                )
            )
            
            // 4) Dinner
            val dinnerMealId = mealDao.insertMeal(
                Meal(
                    name = "Dinner",
                    time = LocalTime.of(22, 30),  // 10:30 PM
                    protein = 55,
                    carbs = 100,
                    fats = 25,
                    calories = 650,
                    description = "Final meal of the day"
                )
            ).toInt()
            
            // Add components for dinner
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = dinnerMealId,
                    name = "Cooked rice + chapati",
                    quantity = "100g rice + 1 chapati",
                    protein = 3,
                    carbs = 100,
                    fats = 0
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = dinnerMealId,
                    name = "Eggs scrambled or fish/tofu",
                    quantity = "4 whole eggs OR 150g fish/tofu",
                    protein = 24,
                    carbs = 0,
                    fats = 20
                )
            )
            
            mealDao.insertMealComponent(
                MealComponent(
                    mealId = dinnerMealId,
                    name = "Greens with oil",
                    quantity = "1 cup greens + 1 tsp oil",
                    protein = 0,
                    carbs = 0,
                    fats = 5
                )
            )
        }
    }
} 