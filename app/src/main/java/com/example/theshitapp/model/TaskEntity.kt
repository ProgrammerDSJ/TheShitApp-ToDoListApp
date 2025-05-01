package com.example.theshitapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.theshitapp.util.Converters
import java.util.Date

@Entity(tableName = "tasks")
@TypeConverters(Converters::class)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val dueDate: Date,
    val dueTime: Long? = null,
    val createdDate: Date,
    val createdById: String,
    val assigneeIds: List<String>,
    val isCompleted: Boolean = false,
    val priority: String, // Stored as string representation of TaskPriority enum
    val reminderSet: Boolean = false,
    val subtasks: List<SubTaskEntity> = emptyList(),
    val durationMinutes: Int = 60,
    val taskColor: String = "#3B82F6",
    val isHabit: Boolean = false,
    val habitDays: List<String> = emptyList(), // Stored as string representation of DayOfWeek enum
    val habitDurationWeeks: Int = 0,
    val habitDurationMonths: Int = 0
) {
    // Method to convert to domain model
    fun toTask(categoryMap: Map<String, TaskCategory>, userMap: Map<String, User>): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            category = categoryMap[categoryId] ?: error("Category not found for ID: $categoryId"),
            dueDate = dueDate,
            dueTime = dueTime,
            createdDate = createdDate,
            createdBy = userMap[createdById] ?: error("User not found for ID: $createdById"),
            assignees = assigneeIds.mapNotNull { userMap[it] },
            isCompleted = isCompleted,
            priority = TaskPriority.valueOf(priority),
            reminderSet = reminderSet,
            subtasks = subtasks.map { it.toSubTask() }.toMutableList(),
            durationMinutes = durationMinutes,
            taskColor = taskColor,
            isHabit = isHabit,
            habitDays = habitDays.map { DayOfWeek.valueOf(it) }.toSet(),
            habitDurationWeeks = habitDurationWeeks,
            habitDurationMonths = habitDurationMonths
        )
    }
    
    companion object {
        // Method to create from domain model
        fun fromTask(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                description = task.description,
                categoryId = task.category.id,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                createdDate = task.createdDate,
                createdById = task.createdBy.id,
                assigneeIds = task.assignees.map { it.id },
                isCompleted = task.isCompleted,
                priority = task.priority.name,
                reminderSet = task.reminderSet,
                subtasks = task.subtasks.map { SubTaskEntity.fromSubTask(it) },
                durationMinutes = task.durationMinutes,
                taskColor = task.taskColor,
                isHabit = task.isHabit,
                habitDays = task.habitDays.map { it.name },
                habitDurationWeeks = task.habitDurationWeeks,
                habitDurationMonths = task.habitDurationMonths
            )
        }
    }
}

data class SubTaskEntity(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
) {
    fun toSubTask(): SubTask {
        return SubTask(
            id = id,
            title = title,
            isCompleted = isCompleted
        )
    }
    
    companion object {
        fun fromSubTask(subTask: SubTask): SubTaskEntity {
            return SubTaskEntity(
                id = subTask.id,
                title = subTask.title,
                isCompleted = subTask.isCompleted
            )
        }
    }
} 