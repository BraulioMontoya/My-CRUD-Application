package com.example.myapplication.model

enum class Degree {
    LITC,
    ISC,
    ICI
}

data class Student(
    val id: Int,
    var name: String,
    var degree: Degree,
    var group: String
)
