package com.example.theshitapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.theshitapp.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    fun getActiveTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    fun getCompletedTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId AND isCompleted = 0")
    fun getTasksForCategory(categoryId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId AND isCompleted = 0")
    suspend fun getTaskCountForCategory(categoryId: String): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    suspend fun getTotalTaskCountForCategory(categoryId: String): Int
    
    @Query("SELECT * FROM tasks WHERE dueDate >= :startDate AND dueDate < :endDate AND isCompleted = 0")
    suspend fun getTasksForDateRange(startDate: Date, endDate: Date): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE dueDate >= :startDate AND dueDate < :endDate AND isCompleted = 0 ORDER BY dueTime ASC, priority DESC")
    suspend fun getTasksForDateRangeSorted(startDate: Date, endDate: Date): List<TaskEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    @Query("DELETE FROM tasks WHERE categoryId = :categoryId")
    suspend fun deleteTasksByCategory(categoryId: String)
    
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletionStatus(taskId: String, isCompleted: Boolean)
    
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
} 