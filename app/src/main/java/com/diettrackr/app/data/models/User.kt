package com.diettrackr.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int = 1,  // Single user, always ID 1
    val name: String = "",
    val currentWeight: Float = 0f,
    val goalWeight: Float = 0f,
    val height: Float = 0f,
    val age: Int = 0,
    val gender: String = "",
    val activityLevel: String = ""
) 