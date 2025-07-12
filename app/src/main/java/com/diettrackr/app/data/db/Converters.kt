package com.diettrackr.app.data.db

import androidx.room.TypeConverter
import com.diettrackr.app.data.models.MealStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // LocalTime converters
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    @TypeConverter
    fun toLocalTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME)
    }

    // MealStatus converters
    @TypeConverter
    fun fromMealStatus(status: MealStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMealStatus(statusString: String): MealStatus {
        return MealStatus.valueOf(statusString)
    }
} 