package com.example.theshitapp.repository

import com.example.theshitapp.model.Task
import com.example.theshitapp.model.TaskCategory
import com.example.theshitapp.model.TaskPriority
import com.example.theshitapp.model.User
import com.example.theshitapp.model.SubTask
import com.example.theshitapp.model.DayOfWeek
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

object TaskRepository {
    private val users = listOf(
        User("1", "Matt"),
        User("2", "John"),
        User("3", "Sarah"),
        User("4", "Emily"),
        User("5", "Alex")
    )

    private val taskCategories = mutableListOf(
        TaskCategory("1", "Myself", listOf(users[0]), 2),
        TaskCategory("2", "Sweet Home", listOf(users[0], users[1]), 2),
        TaskCategory("3", "Work", listOf(users[0], users[1], users[2], users[3], users[4]), 3)
    )

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val today = Calendar.getInstance().time
    private val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time
    private val nextWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 7) }.time
    private val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time
    private val lastWeek = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }.time

    private val tasks = mutableListOf(
        Task(
            id = "1", 
            title = "Grocery Shopping", 
            description = "We have to buy some fresh bread, fruit, and vegetables. Supply of water is running out.", 
            category = taskCategories[1], 
            dueDate = tomorrow, 
            dueTime = getMorningTimeMillis(10, 0), // 10:00 AM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0], users[1]),
            isCompleted = false,
            priority = TaskPriority.MEDIUM
        ),
        Task(
            id = "2", 
            title = "Finish Project Report", 
            description = "Complete the quarterly project report with all metrics and KPIs.", 
            category = taskCategories[2], 
            dueDate = nextWeek,
            dueTime = getMorningTimeMillis(14, 0), // 2:00 PM
            createdDate = yesterday, 
            createdBy = users[2], 
            assignees = listOf(users[0], users[2], users[3]),
            isCompleted = false,
            priority = TaskPriority.HIGH
        ),
        Task(
            id = "3", 
            title = "Gym Workout", 
            description = "Complete 30 minutes of cardio and strength training.", 
            category = taskCategories[0], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(6, 0), // 6:00 AM
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.LOW
        ),
        Task(
            id = "4", 
            title = "House Cleaning", 
            description = "Vacuum living room, clean kitchen and bathroom.", 
            category = taskCategories[1], 
            dueDate = nextWeek,
            dueTime = getMorningTimeMillis(9, 0), // 9:00 AM
            createdDate = yesterday, 
            createdBy = users[1], 
            assignees = listOf(users[0], users[1]),
            isCompleted = false,
            priority = TaskPriority.MEDIUM
        ),
        Task(
            id = "5", 
            title = "Team Meeting Prep", 
            description = "Prepare slides and agenda for Monday's team meeting.", 
            category = taskCategories[2], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(8, 30), // 8:30 AM
            createdDate = yesterday, 
            createdBy = users[0], 
            assignees = listOf(users[0], users[2]),
            isCompleted = false,
            priority = TaskPriority.HIGH
        ),
        Task(
            id = "6", 
            title = "Doctor Appointment", 
            description = "Annual checkup at Dr. Smith's office.", 
            category = taskCategories[0], 
            dueDate = nextWeek,
            dueTime = getMorningTimeMillis(11, 30), // 11:30 AM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.LOW
        ),
        Task(
            id = "7", 
            title = "Client Proposal", 
            description = "Finalize proposal for new client project.", 
            category = taskCategories[2], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(17, 0), // 5:00 PM
            createdDate = lastWeek, 
            createdBy = users[4], 
            assignees = listOf(users[2], users[3], users[4]),
            isCompleted = false,
            priority = TaskPriority.HIGH
        ),
        // Tasks with same deadline but different priorities
        Task(
            id = "8", 
            title = "Email Reply - High Priority", 
            description = "Reply to urgent client email about project timeline.", 
            category = taskCategories[2], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(15, 0), // 3:00 PM
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.HIGH
        ),
        Task(
            id = "9", 
            title = "Email Reply - Medium Priority", 
            description = "Reply to team email about weekly progress.", 
            category = taskCategories[2], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(15, 0), // 3:00 PM (Same time as previous)
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.MEDIUM
        ),
        Task(
            id = "10", 
            title = "Email Reply - Low Priority", 
            description = "Reply to newsletter subscription request.", 
            category = taskCategories[2], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(15, 0), // 3:00 PM (Same time as previous)
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.LOW
        ),
        // Two HIGH priority tasks with the same time - will need user conflict resolution
        Task(
            id = "11", 
            title = "Study for Exam", 
            description = "Study for the upcoming certification exam.", 
            category = taskCategories[0], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(19, 0), // 7:00 PM
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.HIGH
        ),
        Task(
            id = "12", 
            title = "Prepare Notes", 
            description = "Prepare detailed notes for the team meeting.", 
            category = taskCategories[0], 
            dueDate = tomorrow,
            dueTime = getMorningTimeMillis(19, 0), // 7:00 PM (Same time as Study for Exam)
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.HIGH
        ),
        // Two MEDIUM priority tasks with the same time - will need user conflict resolution
        Task(
            id = "13", 
            title = "Review Documentation", 
            description = "Review product documentation for accuracy.", 
            category = taskCategories[2], 
            dueDate = nextWeek,
            dueTime = getMorningTimeMillis(10, 30), // 10:30 AM
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0], users[2]),
            isCompleted = false,
            priority = TaskPriority.MEDIUM
        ),
        Task(
            id = "14", 
            title = "Update Website", 
            description = "Update company website with new product information.", 
            category = taskCategories[2], 
            dueDate = nextWeek,
            dueTime = getMorningTimeMillis(10, 30), // 10:30 AM (Same time as Review Documentation)
            createdDate = today, 
            createdBy = users[0], 
            assignees = listOf(users[0], users[4]),
            isCompleted = false,
            priority = TaskPriority.MEDIUM
        ),
        // Add some completed tasks for testing
        Task(
            id = "101", 
            title = "Read Book Chapter", 
            description = "Complete reading chapter 5 of the book.", 
            category = taskCategories[0], 
            dueDate = yesterday,
            dueTime = getMorningTimeMillis(10, 0), // 10:00 AM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = true,
            priority = TaskPriority.MEDIUM
        ),
        Task(
            id = "102", 
            title = "Morning Run", 
            description = "Go for a 30 minute run.", 
            category = taskCategories[0], 
            dueDate = yesterday,
            dueTime = getMorningTimeMillis(7, 0), // 7:00 AM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = true,
            priority = TaskPriority.LOW
        ),
        Task(
            id = "103", 
            title = "Team Call", 
            description = "Weekly team status call.", 
            category = taskCategories[2], 
            dueDate = yesterday,
            dueTime = getMorningTimeMillis(15, 0), // 3:00 PM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0], users[2], users[3], users[4]),
            isCompleted = true,
            priority = TaskPriority.HIGH
        ),
        Task(
            id = "104", 
            title = "Laundry", 
            description = "Do the weekly laundry.", 
            category = taskCategories[1], 
            dueDate = lastWeek,
            dueTime = getMorningTimeMillis(14, 0), // 2:00 PM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0], users[1]),
            isCompleted = true,
            priority = TaskPriority.MEDIUM
        ),
        Task(
            id = "105", 
            title = "Update Resume", 
            description = "Update resume with recent achievements.", 
            category = taskCategories[0], 
            dueDate = lastWeek,
            dueTime = getMorningTimeMillis(16, 0), // 4:00 PM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = true,
            priority = TaskPriority.MEDIUM
        ),
        // Incomplete task from yesterday to test scoring
        Task(
            id = "106", 
            title = "Evening Meditation", 
            description = "15 minutes of evening meditation.", 
            category = taskCategories[0], 
            dueDate = yesterday,
            dueTime = getMorningTimeMillis(21, 0), // 9:00 PM
            createdDate = lastWeek, 
            createdBy = users[0], 
            assignees = listOf(users[0]),
            isCompleted = false,
            priority = TaskPriority.LOW
        )
    )

    // Helper function to create time in milliseconds for the specified hour and minute
    private fun getMorningTimeMillis(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTaskCategories(): List<TaskCategory> = taskCategories

    fun getTasksForCategory(categoryId: String): List<Task> {
        return tasks.filter { it.category.id == categoryId && !it.isCompleted }
    }

    fun getAllTasks(): List<Task> = tasks.filter { !it.isCompleted }

    fun getTaskById(taskId: String): Task? {
        return tasks.find { it.id == taskId }
    }

    fun addTask(
        title: String,
        description: String,
        categoryId: String,
        dueDate: Date,
        dueTime: Long? = null,
        assigneeIds: List<String>,
        priority: TaskPriority = TaskPriority.MEDIUM,
        reminderSet: Boolean = false,
        durationMinutes: Int = 60,
        taskColor: String = "#3B82F6",
        isHabit: Boolean = false,
        habitDays: Set<DayOfWeek> = emptySet(),
        habitDurationWeeks: Int = 0,
        habitDurationMonths: Int = 0
    ): Task {
        val category = taskCategories.find { it.id == categoryId }
            ?: throw IllegalArgumentException("Invalid category ID")
        
        val taskAssignees = users.filter { user -> assigneeIds.contains(user.id) }
        
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            category = category,
            dueDate = dueDate,
            dueTime = dueTime,
            createdDate = Calendar.getInstance().time,
            createdBy = users[0], // Current user
            assignees = taskAssignees,
            priority = priority,
            reminderSet = reminderSet,
            durationMinutes = durationMinutes,
            taskColor = taskColor,
            isHabit = isHabit,
            habitDays = habitDays,
            habitDurationWeeks = habitDurationWeeks,
            habitDurationMonths = habitDurationMonths
        )
        
        tasks.add(newTask)
        
        // If it's a habit, generate recurring tasks for the schedule
        if (isHabit && habitDays.isNotEmpty()) {
            addHabitToSchedule(newTask)
        }
        
        return newTask
    }

    // Add habit tasks to weekly schedule
    private fun addHabitToSchedule(habitTask: Task) {
        // Calculate end date based on habit duration
        val endDate = calculateHabitEndDate(habitTask)
        
        // Start from the due date of the original task
        val calendar = Calendar.getInstance()
        calendar.time = habitTask.dueDate
        
        while (calendar.time.before(endDate)) {
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> DayOfWeek.MONDAY
                Calendar.TUESDAY -> DayOfWeek.TUESDAY
                Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
                Calendar.THURSDAY -> DayOfWeek.THURSDAY
                Calendar.FRIDAY -> DayOfWeek.FRIDAY
                Calendar.SATURDAY -> DayOfWeek.SATURDAY
                Calendar.SUNDAY -> DayOfWeek.SUNDAY
                else -> null
            }
            
            // If this day is part of the habit days, create a task for it
            if (dayOfWeek != null && habitTask.habitDays.contains(dayOfWeek)) {
                // Skip the first occurrence if it's the same as the original task
                if (calendar.time != habitTask.dueDate) {
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = habitTask.title,
                        description = habitTask.description,
                        category = habitTask.category,
                        dueDate = calendar.time,
                        dueTime = habitTask.dueTime,
                        createdDate = habitTask.createdDate,
                        createdBy = habitTask.createdBy,
                        assignees = habitTask.assignees,
                        priority = habitTask.priority,
                        reminderSet = habitTask.reminderSet,
                        durationMinutes = habitTask.durationMinutes,
                        taskColor = habitTask.taskColor,
                        isHabit = false // This is a generated occurrence, not the habit definition
                    )
                    tasks.add(newTask)
                }
            }
            
            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
    
    // Calculate end date for a habit based on its duration
    private fun calculateHabitEndDate(habitTask: Task): Date {
        val calendar = Calendar.getInstance()
        calendar.time = habitTask.dueDate
        
        if (habitTask.habitDurationWeeks > 0) {
            calendar.add(Calendar.WEEK_OF_YEAR, habitTask.habitDurationWeeks)
        } else if (habitTask.habitDurationMonths > 0) {
            calendar.add(Calendar.MONTH, habitTask.habitDurationMonths)
        }
        
        return calendar.time
    }

    fun completeTask(taskId: String): Boolean {
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val task = tasks[taskIndex]
            tasks[taskIndex] = task.copy(isCompleted = true)
            return true
        }
        return false
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getRemainingTime(dueDate: Date): String {
        val currentTime = Calendar.getInstance().time
        val diff = dueDate.time - currentTime.time
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            else -> "${minutes}m"
        }
    }

    // Get tasks for a specific date
    fun getTasksForDate(date: Date): List<Task> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endDate = calendar.time
        
        return tasks.filter { 
            !it.isCompleted && it.dueDate >= startDate && it.dueDate < endDate
        }.sortedWith(compareBy<Task> { 
            it.dueTime ?: Long.MAX_VALUE 
        }.thenByDescending { 
            it.priority.ordinal  // Reverse order to get HIGH (2) > MEDIUM (1) > LOW (0)
        })
    }
    
    // Get all tasks for the week starting from the given date
    fun getTasksForWeek(startDate: Date): Map<Date, List<Task>> {
        val result = mutableMapOf<Date, List<Task>>()
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        // Get all days of the week
        for (i in 0 until 7) {
            val currentDate = calendar.time
            result[currentDate] = getTasksForDate(currentDate)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return result
    }
    
    // Get tasks that need reminders for a given timestamp
    fun getTasksForReminder(timestamp: Long): List<Task> {
        return tasks.filter { 
            !it.isCompleted && 
            it.reminderSet && 
            it.dueTime != null && 
            it.dueTime <= timestamp 
        }
    }
    
    fun getTasksByPriority(priority: TaskPriority): List<Task> {
        return tasks.filter { !it.isCompleted && it.priority == priority }
    }
    
    // Format time (hours and minutes)
    fun formatTime(timeInMillis: Long): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date(timeInMillis))
    }

    // Add new function to add a subtask to a task
    fun addSubtask(taskId: String, subtaskTitle: String): Boolean {
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        if (taskIndex != -1) {
            val subtask = SubTask(
                id = UUID.randomUUID().toString(),
                title = subtaskTitle
            )
            tasks[taskIndex].subtasks.add(subtask)
            return true
        }
        return false
    }
    
    // Add new function to toggle subtask completion status
    fun toggleSubtaskStatus(taskId: String, subtaskId: String): Boolean {
        val task = tasks.find { it.id == taskId } ?: return false
        val subtask = task.subtasks.find { it.id == subtaskId } ?: return false
        subtask.isCompleted = !subtask.isCompleted
        return true
    }
    
    // Add new function to delete subtask
    fun deleteSubtask(taskId: String, subtaskId: String): Boolean {
        val task = tasks.find { it.id == taskId } ?: return false
        return task.subtasks.removeIf { it.id == subtaskId }
    }
    
    // Add new function to create a category
    fun addCategory(name: String): TaskCategory {
        val id = UUID.randomUUID().toString()
        val category = TaskCategory(id, name, listOf(users[0]), 1)
        taskCategories.add(category)
        return category
    }

    // Check if there are any tasks on a specific date
    fun hasTasksOnDate(date: Date): Boolean {
        return getTasksForDate(date).isNotEmpty()
    }

    // Get tasks for a category sorted by due time and priority
    fun getTasksForCategorySorted(categoryId: String): List<Task> {
        return tasks.filter { it.category.id == categoryId && !it.isCompleted }
            .sortedWith(compareBy<Task> { 
                it.dueDate.time 
            }.thenBy { 
                it.dueTime ?: Long.MAX_VALUE 
            }.thenByDescending { 
                it.priority.ordinal  // Reverse order to get HIGH (2) > MEDIUM (1) > LOW (0)
            })
    }
    
    // Count habit tasks in a category
    fun getHabitTasksCountForCategory(categoryId: String): Int {
        return tasks.count { it.category.id == categoryId && !it.isCompleted && it.isHabit }
    }

    // Count non-habit tasks in a category
    fun getRegularTasksCountForCategory(categoryId: String): Int {
        return tasks.count { it.category.id == categoryId && !it.isCompleted && !it.isHabit }
    }
    
    // Get all completed tasks
    fun getCompletedTasks(): List<Task> = tasks.filter { it.isCompleted }
    
    // Get completed tasks grouped by date
    fun getCompletedTasksByDate(): Map<Date, List<Task>> {
        val completedTasks = getCompletedTasks()
        val groupedTasks = mutableMapOf<Date, MutableList<Task>>()
        
        // Group tasks by normalized date (without time component)
        completedTasks.forEach { task ->
            val normalizedDate = getNormalizedDate(task.dueDate)
            if (!groupedTasks.containsKey(normalizedDate)) {
                groupedTasks[normalizedDate] = mutableListOf()
            }
            groupedTasks[normalizedDate]?.add(task)
        }
        
        return groupedTasks
    }
    
    // Calculate completion score for a specific date
    fun getCompletionScoreForDate(date: Date): Pair<Int, Int> {
        val normalizedDate = getNormalizedDate(date)
        
        // Get all tasks due on this date (both completed and not completed)
        val allTasksForDate = tasks.filter { 
            getNormalizedDate(it.dueDate) == normalizedDate
        }
        
        val completedTasksCount = allTasksForDate.count { it.isCompleted }
        val totalTasksCount = allTasksForDate.size
        
        return Pair(completedTasksCount, totalTasksCount)
    }
    
    // Helper function to normalize a date by removing the time component
    private fun getNormalizedDate(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
    
    // Format date with day name for completed tasks view
    fun formatDateWithDayName(date: Date): String {
        val dayFormat = SimpleDateFormat("EEEE", Locale.US)
        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.US)
        return "${dayFormat.format(date)}, ${dateFormat.format(date)}"
    }

    // Add new function to delete a category and all its tasks
    fun deleteCategory(categoryId: String): Boolean {
        val index = taskCategories.indexOfFirst { it.id == categoryId }
        if (index != -1) {
            // Remove the category
            taskCategories.removeAt(index)
            
            // Remove all tasks in this category
            tasks.removeIf { it.category.id == categoryId }
            
            return true
        }
        return false
    }
} 