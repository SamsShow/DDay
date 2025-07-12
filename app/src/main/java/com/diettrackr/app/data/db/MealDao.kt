package com.diettrackr.app.data.db

import androidx.room.*
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.data.models.MealComponent
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY time ASC")
    fun getAllMealsFlow(): Flow<List<Meal>>
    
    @Query("SELECT * FROM meals ORDER BY time ASC")
    suspend fun getAllMeals(): List<Meal>
    
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealById(id: Int): Meal?
    
    @Query("SELECT * FROM meals WHERE time BETWEEN :startTime AND :endTime")
    suspend fun getMealsInTimeRange(startTime: LocalTime, endTime: LocalTime): List<Meal>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long
    
    @Update
    suspend fun updateMeal(meal: Meal)
    
    @Delete
    suspend fun deleteMeal(meal: Meal)
    
    // Meal Components
    @Query("SELECT * FROM meal_components WHERE mealId = :mealId")
    suspend fun getMealComponents(mealId: Int): List<MealComponent>
    
    @Query("SELECT * FROM meal_components WHERE mealId = :mealId")
    fun getMealComponentsFlow(mealId: Int): Flow<List<MealComponent>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealComponent(component: MealComponent): Long
    
    @Update
    suspend fun updateMealComponent(component: MealComponent)
    
    @Delete
    suspend fun deleteMealComponent(component: MealComponent)
} 