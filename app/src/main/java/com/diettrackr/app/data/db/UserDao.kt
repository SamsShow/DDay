package com.diettrackr.app.data.db

import androidx.room.*
import com.diettrackr.app.data.models.User
import com.diettrackr.app.data.models.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = 1")
    fun getUserFlow(): Flow<User?>
    
    @Query("SELECT * FROM user WHERE id = 1")
    suspend fun getUser(): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    // Weight Entries
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getWeightEntriesFlow(): Flow<List<WeightEntry>>
    
    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentWeightEntries(limit: Int): List<WeightEntry>
    
    @Query("SELECT * FROM weight_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getWeightEntriesInRange(startDate: LocalDate, endDate: LocalDate): List<WeightEntry>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(weightEntry: WeightEntry): Long
    
    @Delete
    suspend fun deleteWeightEntry(weightEntry: WeightEntry)
} 