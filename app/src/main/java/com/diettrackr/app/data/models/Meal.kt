package com.diettrackr.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val time: LocalTime,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val calories: Int,
    val description: String = "",
    val isDefault: Boolean = true  // True if part of default plan, False if custom added
) 