package com.example.theshitapp.util

import androidx.room.TypeConverter
import com.example.theshitapp.model.SubTaskEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()
    
    // Date converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    // String List converters
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
    
    // SubTaskEntity List converters
    @TypeConverter
    fun fromSubTaskList(value: List<SubTaskEntity>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toSubTaskList(value: String): List<SubTaskEntity> {
        val listType = object : TypeToken<List<SubTaskEntity>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
} 