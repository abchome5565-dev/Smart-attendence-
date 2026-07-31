package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rollNumber: String,
    val name: String,
    val classSection: String, // e.g., "Class 10-A", "CS-101", "Grade 11-B"
    val email: String = "",
    val phone: String = "",
    val avatarSeed: String = "1", // Avatar identifier or photo placeholder
    val createdAt: Long = System.currentTimeMillis()
)
