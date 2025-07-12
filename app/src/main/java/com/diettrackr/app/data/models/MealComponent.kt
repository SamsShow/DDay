package com.diettrackr.app.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_components",
    foreignKeys = [
        ForeignKey(
            entity = Meal::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MealComponent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealId: Int,
    val name: String,
    val quantity: String,
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val isDefault: Boolean = true  // True if part of default component, False if substituted
) 