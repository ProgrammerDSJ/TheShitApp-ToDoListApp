package com.example.theshitapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.theshitapp.model.TaskCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCategoryDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<TaskCategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): TaskCategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TaskCategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<TaskCategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: TaskCategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: TaskCategoryEntity)
    
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String)
    
    @Query("UPDATE categories SET activeTasksCount = :taskCount WHERE id = :categoryId")
    suspend fun updateTaskCount(categoryId: String, taskCount: Int)
} 