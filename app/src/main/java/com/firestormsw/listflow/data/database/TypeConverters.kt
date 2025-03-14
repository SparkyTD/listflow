package com.firestormsw.listflow.data.database

import androidx.room.TypeConverter
import java.util.Date

class TypeConverters {

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(time: Long): Date {
        return Date(time)
    }

}