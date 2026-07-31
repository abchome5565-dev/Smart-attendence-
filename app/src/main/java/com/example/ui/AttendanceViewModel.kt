package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AttendanceRecord
import com.example.data.AttendanceRepository
import com.example.data.Student
import com.example.data.StudentWithAttendance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UserSession(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val role: String = "Teacher / Administrator",
    val schoolName: String = "St. Jude Academy"
)

data class DashboardStats(
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val attendancePercentage: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AttendanceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AttendanceRepository(database.studentDao(), database.attendanceDao())
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateStr: String = dateFormat.format(Date())

    // User session
    private val _userSession = MutableStateFlow(UserSession(isLoggedIn = true, username = "Prof. Anderson"))
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    // UI state parameters
    private val _selectedDate = MutableStateFlow(todayDateStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedClass = MutableStateFlow("All Classes")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMonth = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // One-shot toast / snackbar messages
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Flow of Class List (adding "All Classes" to beginning)
    val classSections: StateFlow<List<String>> = repository.allClassSections
        .map { list -> listOf("All Classes") + list }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All Classes"))

    // Flow of Students list filtered by class & query
    val students: StateFlow<List<Student>> = combine(
        _selectedClass,
        _searchQuery
    ) { classSec, query ->
        Pair(classSec, query)
    }.flatMapLatest { (classSec, query) ->
        if (query.isNotBlank()) {
            repository.searchStudents(query)
        } else if (classSec != "All Classes" && classSec.isNotEmpty()) {
            repository.getStudentsByClass(classSec)
        } else {
            repository.allStudents
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow of Students with attendance status for current selected Date and Class
    val studentsWithAttendance: StateFlow<List<StudentWithAttendance>> = combine(
        _selectedClass,
        _selectedDate
    ) { classSec, date ->
        Pair(classSec, date)
    }.flatMapLatest { (classSec, date) ->
        repository.getStudentsWithAttendanceByClassAndDate(classSec, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Statistics for selected date
    val dashboardStats: StateFlow<DashboardStats> = _selectedDate.flatMapLatest { date ->
        combine(
            repository.studentCount,
            repository.getPresentCountForDate(date),
            repository.getAbsentCountForDate(date),
            repository.getLateCountForDate(date)
        ) { total, present, absent, late ->
            val percentage = if (total > 0) ((present + late).toFloat() / total.toFloat()) * 100f else 0f
            DashboardStats(
                totalStudents = total,
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                attendancePercentage = percentage
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Monthly attendance records
    val monthlyRecords: StateFlow<List<AttendanceRecord>> = _selectedMonth.flatMapLatest { month ->
        repository.getAttendanceForMonth(month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Working days in selected month
    val workingDaysCount: StateFlow<Int> = _selectedMonth.flatMapLatest { month ->
        repository.getWorkingDaysInMonth(month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Actions
    fun login(username: String, password: String): Boolean {
        return if (username.isNotBlank() && password.isNotBlank()) {
            _userSession.value = UserSession(isLoggedIn = true, username = username)
            viewModelScope.launch { _toastEvent.emit("Welcome back, $username!") }
            true
        } else {
            viewModelScope.launch { _toastEvent.emit("Please enter username and password") }
            false
        }
    }

    fun logout() {
        _userSession.value = UserSession(isLoggedIn = false)
        viewModelScope.launch { _toastEvent.emit("Logged out successfully") }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedClass(classSection: String) {
        _selectedClass.value = classSection
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
    }

    fun markAttendance(studentId: Long, status: String, notes: String = "") {
        viewModelScope.launch {
            val record = AttendanceRecord(
                studentId = studentId,
                date = _selectedDate.value,
                status = status,
                notes = notes
            )
            repository.saveAttendanceRecord(record)
            _toastEvent.emit("Attendance updated: $status")
        }
    }

    fun markAllPresent() {
        viewModelScope.launch {
            val currentList = studentsWithAttendance.value
            if (currentList.isEmpty()) {
                _toastEvent.emit("No students to mark!")
                return@launch
            }
            val records = currentList.map { student ->
                AttendanceRecord(
                    studentId = student.studentId,
                    date = _selectedDate.value,
                    status = "PRESENT",
                    notes = ""
                )
            }
            repository.saveAttendanceRecords(records)
            _toastEvent.emit("Marked ${records.size} students Present for ${_selectedDate.value}")
        }
    }

    fun addStudent(rollNo: String, name: String, classSection: String, email: String, phone: String) {
        viewModelScope.launch {
            if (rollNo.isBlank() || name.isBlank() || classSection.isBlank()) {
                _toastEvent.emit("Please fill Roll No, Name, and Class!")
                return@launch
            }
            val newStudent = Student(
                rollNumber = rollNo,
                name = name,
                classSection = classSection,
                email = email,
                phone = phone,
                avatarSeed = (1..15).random().toString()
            )
            repository.insertStudent(newStudent)
            _toastEvent.emit("Student $name added successfully!")
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            _toastEvent.emit("Student details updated")
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _toastEvent.emit("${student.name} deleted")
        }
    }

    suspend fun getStudentById(id: Long): Student? = repository.getStudentById(id)
    fun getStudentAttendanceHistory(id: Long): Flow<List<AttendanceRecord>> = repository.getAttendanceForStudent(id)
}

class AttendanceViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
