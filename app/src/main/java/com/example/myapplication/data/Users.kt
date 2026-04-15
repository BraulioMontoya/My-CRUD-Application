package com.example.myapplication.data

import com.example.myapplication.model.Role
import com.example.myapplication.model.User

class Users {
    private var _id = 1
    private val _usersList = mutableListOf<User>()

    fun insertUser(
        username: String,
        password: String,
        role: Role
    ) {
        _usersList.add(User(
            _id,
            username,
            password,
            role
        ))

        _id++
    }

    fun consultUser(
        username: String,
        password: String
    ): User? {
        _usersList.forEach { user ->
            if(user.username == username && user.password == password) {
                return user
            }
        }

        return null
    }
}