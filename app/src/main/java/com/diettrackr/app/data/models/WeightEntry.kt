package com.diettrackr.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weight: Float,
    val date: LocalDate = LocalDate.now(),
    val notes: String = ""
) 