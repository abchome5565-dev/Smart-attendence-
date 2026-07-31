package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object MarkAttendance : Screen("mark_attendance", "Mark Attendance", Icons.Default.CheckCircle)
    object StudentList : Screen("students", "Students", Icons.Default.People)
    object AddStudent : Screen("add_student", "Add Student", Icons.Default.PersonAdd)
    object MonthlyReport : Screen("reports", "Reports", Icons.Default.Assessment)
    object StudentDetail : Screen("student_detail/{studentId}", "Student Details") {
        fun createRoute(studentId: Long) = "student_detail/$studentId"
    }
}

val navBarItems = listOf(
    Screen.Dashboard,
    Screen.MarkAttendance,
    Screen.StudentList,
    Screen.MonthlyReport
)
