package com.diettrackr.app.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Represents the status of a meal on a specific date
 */
@Entity(
    tableName = "daily_logs",
    foreignKeys = [
        ForeignKey(
            entity = Meal::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealId: Int,
    val date: LocalDate = LocalDate.now(),
    val status: MealStatus = MealStatus.PENDING,
    val notes: String = ""
)

enum class MealStatus {
    PENDING,
    COMPLETED,
    SKIPPED,
    MODIFIED  // Meal was eaten but with modifications
} 