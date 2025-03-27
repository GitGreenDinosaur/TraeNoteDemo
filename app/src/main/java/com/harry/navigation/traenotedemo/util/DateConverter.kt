package com.harry.navigation.traenotedemo.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * 日期转换器
 * 用于Room数据库中Date类型与Long类型的相互转换
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}