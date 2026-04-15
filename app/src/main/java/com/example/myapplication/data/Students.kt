package com.example.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import com.example.myapplication.model.Degree
import com.example.myapplication.model.Student

class Students {
    private var _id = 1
    private val _studentsList = mutableStateListOf<Student>()

    fun insertStudent(
        name: String,
        degree: Degree,
        group: String
    ) {
        _studentsList.add(Student(
            _id,
            name,
            degree,
            group
        ))

        _id++
    }

    fun consultStudents(): MutableList<Student> = _studentsList

    fun updateStudent(
        id: Int,
        newName: String,
        newDegree: Degree,
        newGroup: String
    ) {
        val index = _studentsList.indexOfFirst { it.id == id }

        if (index != -1) {
            _studentsList[index] = Student(id, newName, newDegree, newGroup)
        }
    }

    fun deleteStudent(id: Int) {
        _studentsList.removeIf { it.id == id }
    }
}