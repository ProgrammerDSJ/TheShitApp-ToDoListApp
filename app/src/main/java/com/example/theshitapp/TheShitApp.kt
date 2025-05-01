package com.example.theshitapp

import android.app.Application
import com.example.theshitapp.repository.TaskRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TheShitApp : Application() {
    // Application scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Lazy initialize repository
    val repository by lazy { TaskRepositoryImpl(this) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with sample data
        applicationScope.launch {
            repository.initializeDatabase()
        }
    }
    
    companion object {
        private var instance: TheShitApp? = null
        
        fun getInstance(): TheShitApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
    
    init {
        instance = this
    }
} 