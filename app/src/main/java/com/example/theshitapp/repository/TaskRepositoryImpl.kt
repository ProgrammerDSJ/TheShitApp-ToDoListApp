package com.example.theshitapp.repository

import android.content.Context
import com.example.theshitapp.db.AppDatabase
import com.example.theshitapp.model.DayOfWeek
import com.example.theshitapp.model.SubTask
import com.example.theshitapp.model.Task
import com.example.theshitapp.model.TaskCategory
import com.example.theshitapp.model.TaskCategoryEntity
import com.example.theshitapp.model.TaskEntity
import com.example.theshitapp.model.TaskPriority
import com.example.theshitapp.model.User
import com.example.theshitapp.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class TaskRepositoryImpl(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val taskDao = database.taskDao()
    private val categoryDao = database.taskCategoryDao()
    private val userDao = database.userDao()
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    
    // Users cache to avoid repeated database fetches
    private var usersCache: Map<String, User> = emptyMap()
    
    // Categories cache to avoid repeated database fetches
    private var categoriesCache: Map<String, TaskCategory> = emptyMap()
    
    // Initialize database with sample data if needed
    suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            // Initialize users if none exist
            val users = TaskRepository.users.map { UserEntity.fromUser(it) }
            userDao.insertUsers(users)
            
            // Initialize categories if none exist
            val categories = TaskRepository.taskCategories.map { 
                TaskCategoryEntity.fromTaskCategory(it) 
            }
            categoryDao.insertCategories(categories)
            
            // Initialize tasks if none exist
            val tasks = TaskRepository.tasks.map { TaskEntity.fromTask(it) }
            taskDao.insertTasks(tasks)
            
            // Refresh caches
            refreshUserCache()
            refreshCategoryCache()
        }
    }
    
    // Refresh the users cache
    private suspend fun refreshUserCache() {
        withContext(Dispatchers.IO) {
            val userEntities = userDao.getAllUsers().map { it.toList() }.firstOrNull() ?: emptyList()
            usersCache = userEntities.associate { it.id to it.toUser() }
        }
    }
    
    // Refresh the categories cache
    private suspend fun refreshCategoryCache() {
        withContext(Dispatchers.IO) {
            // Ensure user cache is updated
            if (usersCache.isEmpty()) {
                refreshUserCache()
            }
            
            val categoryEntities = categoryDao.getAllCategories().map { it.toList() }.firstOrNull() ?: emptyList()
            categoriesCache = categoryEntities.associate { 
                it.id to it.toTaskCategory(usersCache)
            }
        }
    }
    
    // Get all task categories
    fun getTaskCategories(): Flow<List<TaskCategory>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toTaskCategory(usersCache) }
        }
    }
    
    // Get tasks for a specific category
    fun getTasksForCategory(categoryId: String): Flow<List<Task>> {
        return taskDao.getTasksForCategory(categoryId).map { entities ->
            entities.map { it.toTask(categoriesCache, usersCache) }
        }
    }
    
    // Get all active tasks
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities.map { it.toTask(categoriesCache, usersCache) }
        }
    }
    
    // Get task by ID
    suspend fun getTaskById(taskId: String): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskById(taskId)?.toTask(categoriesCache, usersCache)
        }
    }
    
    // Add a new task
    suspend fun addTask(
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
        // Refresh caches if needed
        if (usersCache.isEmpty()) refreshUserCache()
        if (categoriesCache.isEmpty()) refreshCategoryCache()
        
        val category = categoriesCache[categoryId]
            ?: throw IllegalArgumentException("Invalid category ID")
        
        val taskAssignees = assigneeIds.mapNotNull { usersCache[it] }
        
        val newTask = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            category = category,
            dueDate = dueDate,
            dueTime = dueTime,
            createdDate = Calendar.getInstance().time,
            createdBy = usersCache.values.firstOrNull() ?: throw IllegalStateException("No users available"),
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
        
        withContext(Dispatchers.IO) {
            // Insert the main task
            taskDao.insertTask(TaskEntity.fromTask(newTask))
            
            // Update task count for the category
            val taskCount = taskDao.getTaskCountForCategory(categoryId)
            categoryDao.updateTaskCount(categoryId, taskCount)
            
            // If it's a habit, generate recurring tasks for the schedule
            if (isHabit && habitDays.isNotEmpty()) {
                addHabitToSchedule(newTask)
            }
        }
        
        return newTask
    }
    
    // Add habit tasks to weekly schedule
    private suspend fun addHabitToSchedule(habitTask: Task) {
        withContext(Dispatchers.IO) {
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
                        taskDao.insertTask(TaskEntity.fromTask(newTask))
                    }
                }
                
                // Move to the next day
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
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
    
    // Mark a task as complete
    suspend fun completeTask(taskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                taskDao.updateTaskCompletionStatus(taskId, true)
                
                // Update task count for the category
                val taskCount = taskDao.getTaskCountForCategory(task.categoryId)
                categoryDao.updateTaskCount(task.categoryId, taskCount)
                
                true
            } else {
                false
            }
        }
    }
    
    // Date formatting helper
    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
    
    // Calculate remaining time until due date
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
    suspend fun getTasksForDate(date: Date): List<Task> {
        return withContext(Dispatchers.IO) {
            // Refresh caches if needed
            if (usersCache.isEmpty()) refreshUserCache()
            if (categoriesCache.isEmpty()) refreshCategoryCache()
            
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endDate = calendar.time
            
            val taskEntities = taskDao.getTasksForDateRangeSorted(startDate, endDate)
            taskEntities.map { it.toTask(categoriesCache, usersCache) }
        }
    }
    
    // Get all tasks for the week starting from the given date
    suspend fun getTasksForWeek(startDate: Date): Map<Date, List<Task>> {
        return withContext(Dispatchers.IO) {
            val result = mutableMapOf<Date, List<Task>>()
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            
            // Get all days of the week
            for (i in 0 until 7) {
                val currentDate = calendar.time
                result[currentDate] = getTasksForDate(currentDate)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            result
        }
    }
    
    // Add a new category
    suspend fun addCategory(name: String): TaskCategory {
        return withContext(Dispatchers.IO) {
            // Refresh user cache if needed
            if (usersCache.isEmpty()) refreshUserCache()
            
            val id = UUID.randomUUID().toString()
            val currentUser = usersCache.values.firstOrNull() 
                ?: throw IllegalStateException("No users available")
                
            val category = TaskCategory(id, name, listOf(currentUser), 0)
            
            // Insert into database
            categoryDao.insertCategory(TaskCategoryEntity.fromTaskCategory(category))
            
            // Refresh category cache
            refreshCategoryCache()
            
            category
        }
    }
    
    // Delete a category and all its tasks
    suspend fun deleteCategory(categoryId: String): Boolean {
        return withContext(Dispatchers.IO) {
            // Delete all tasks for this category first
            taskDao.deleteTasksByCategory(categoryId)
            
            // Then delete the category
            categoryDao.deleteCategoryById(categoryId)
            
            // Refresh category cache
            refreshCategoryCache()
            
            true
        }
    }
    
    // Add a subtask to a task
    suspend fun addSubtask(taskId: String, subtaskTitle: String): Boolean {
        return withContext(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId) ?: return@withContext false
            
            // Create new subtask
            val subtask = SubTask(
                id = UUID.randomUUID().toString(),
                title = subtaskTitle
            )
            
            // Convert to domain model, add subtask, and convert back to entity
            val taskWithSubtask = task.toTask(categoriesCache, usersCache).let { domainTask ->
                domainTask.subtasks.add(subtask)
                TaskEntity.fromTask(domainTask)
            }
            
            // Update in database
            taskDao.updateTask(taskWithSubtask)
            
            true
        }
    }
    
    // Toggle subtask completion status
    suspend fun toggleSubtaskStatus(taskId: String, subtaskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId) ?: return@withContext false
            
            // Convert to domain model
            val domainTask = task.toTask(categoriesCache, usersCache)
            
            // Find and toggle the subtask
            val subtask = domainTask.subtasks.find { it.id == subtaskId } ?: return@withContext false
            subtask.isCompleted = !subtask.isCompleted
            
            // Convert back to entity and update in database
            taskDao.updateTask(TaskEntity.fromTask(domainTask))
            
            true
        }
    }
    
    // Delete a subtask
    suspend fun deleteSubtask(taskId: String, subtaskId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val task = taskDao.getTaskById(taskId) ?: return@withContext false
            
            // Convert to domain model
            val domainTask = task.toTask(categoriesCache, usersCache)
            
            // Remove the subtask
            val removed = domainTask.subtasks.removeIf { it.id == subtaskId }
            if (!removed) return@withContext false
            
            // Convert back to entity and update in database
            taskDao.updateTask(TaskEntity.fromTask(domainTask))
            
            true
        }
    }
} 