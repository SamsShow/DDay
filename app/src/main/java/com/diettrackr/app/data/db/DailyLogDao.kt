package com.diettrackr.app.data.db

import androidx.room.*
import com.diettrackr.app.data.models.DailyLog
import com.diettrackr.app.data.models.MealStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE date = :date ORDER BY id ASC")
    fun getDailyLogsForDateFlow(date: LocalDate): Flow<List<DailyLog>>
    
    @Query("SELECT * FROM daily_logs WHERE date = :date ORDER BY id ASC")
    suspend fun getDailyLogsForDate(date: LocalDate): List<DailyLog>
    
    @Query("SELECT * FROM daily_logs WHERE date = :date AND mealId = :mealId LIMIT 1")
    suspend fun getLogForMealAndDate(mealId: Int, date: LocalDate): DailyLog?
    
    @Query("SELECT COUNT(*) FROM daily_logs WHERE date = :date AND status = :status")
    suspend fun countMealsWithStatus(date: LocalDate, status: MealStatus): Int
    
    @Query("SELECT * FROM daily_logs WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getLogsInDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyLog>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(dailyLog: DailyLog): Long
    
    @Update
    suspend fun updateDailyLog(dailyLog: DailyLog)
    
    @Delete
    suspend fun deleteDailyLog(dailyLog: DailyLog)
} 