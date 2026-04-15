package com.example.myapplication.model

enum class Role {
    ADMIN,
    EDITOR,
    CONSULTOR
}

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val role: Role
)
