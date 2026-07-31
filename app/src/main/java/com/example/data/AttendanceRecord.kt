package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String, // ISO date string: "YYYY-MM-DD"
    val status: String, // "PRESENT", "ABSENT", "LATE", "LEAVE"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class StudentWithAttendance(
    val studentId: Long,
    val rollNumber: String,
    val name: String,
    val classSection: String,
    val email: String,
    val phone: String,
    val avatarSeed: String,
    val status: String? = null, // null if attendance not yet marked for date
    val notes: String? = null
)
