package com.example.theshitapp.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.theshitapp.util.Converters

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
@TypeConverters(Converters::class)
data class TaskCategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val members: List<String>, // Store user IDs
    val activeTasksCount: Int = 0
) {
    // Method to convert to domain model
    fun toTaskCategory(userMap: Map<String, User>): TaskCategory {
        return TaskCategory(
            id = id,
            name = name,
            members = members.mapNotNull { userMap[it] },
            activeTasksCount = activeTasksCount
        )
    }
    
    companion object {
        // Method to create from domain model
        fun fromTaskCategory(taskCategory: TaskCategory): TaskCategoryEntity {
            return TaskCategoryEntity(
                id = taskCategory.id,
                name = taskCategory.name,
                members = taskCategory.members.map { it.id },
                activeTasksCount = taskCategory.activeTasksCount
            )
        }
    }
} 