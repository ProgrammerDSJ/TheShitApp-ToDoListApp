package com.example.theshitapp.model

import java.util.Date

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

data class SubTask(
    val id: String,
    val title: String,
    var isCompleted: Boolean = false
)

// Days of the week for habit scheduling
enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val category: TaskCategory,
    val dueDate: Date,
    val dueTime: Long? = null, // Store time in milliseconds
    val createdDate: Date,
    val createdBy: User,
    val assignees: List<User>,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val reminderSet: Boolean = false,
    val subtasks: MutableList<SubTask> = mutableListOf(),
    val durationMinutes: Int = 60, // Default 1 hour duration
    val taskColor: String = "#3B82F6", // Default blue color
    val isHabit: Boolean = false, // Whether this task is a habit
    val habitDays: Set<DayOfWeek> = emptySet(), // Days when the habit should occur
    val habitDurationWeeks: Int = 0, // Duration in weeks for forming the habit
    val habitDurationMonths: Int = 0 // Duration in months for forming the habit
) 