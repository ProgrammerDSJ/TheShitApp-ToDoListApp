package com.example.theshitapp.model

data class TaskCategory(
    val id: String,
    val name: String,
    val members: List<User>,
    val activeTasksCount: Int = 0
) 