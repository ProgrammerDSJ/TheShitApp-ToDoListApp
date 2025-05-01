package com.example.theshitapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String
) {
    // Convert to domain model
    fun toUser(): User {
        return User(
            id = id,
            name = name
        )
    }
    
    companion object {
        // Create from domain model
        fun fromUser(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                name = user.name
            )
        }
    }
} 