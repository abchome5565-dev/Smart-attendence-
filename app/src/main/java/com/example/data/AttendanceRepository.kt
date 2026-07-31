package com.example.data

import kotlinx.coroutines.flow.Flow

class AttendanceRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val allClassSections: Flow<List<String>> = studentDao.getAllClassSections()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)
    fun getStudentsByClass(classSection: String): Flow<List<Student>> = studentDao.getStudentsByClass(classSection)
    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    suspend fun saveAttendanceRecord(record: AttendanceRecord) = attendanceDao.insertOrUpdateRecord(record)
    suspend fun saveAttendanceRecords(records: List<AttendanceRecord>) = attendanceDao.insertOrUpdateRecords(records)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> = attendanceDao.getAttendanceForDate(date)
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> = attendanceDao.getAttendanceForStudent(studentId)
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>> = attendanceDao.getAttendanceForMonth(monthPrefix)

    fun getPresentCountForDate(date: String): Flow<Int> = attendanceDao.getPresentCountForDate(date)
    fun getAbsentCountForDate(date: String): Flow<Int> = attendanceDao.getAbsentCountForDate(date)
    fun getLateCountForDate(date: String): Flow<Int> = attendanceDao.getLateCountForDate(date)

    fun getStudentsWithAttendanceByClassAndDate(classSection: String, date: String): Flow<List<StudentWithAttendance>> {
        return if (classSection.isEmpty() || classSection == "All Classes") {
            attendanceDao.getAllStudentsWithAttendanceForDate(date)
        } else {
            attendanceDao.getStudentsWithAttendanceByClassAndDate(classSection, date)
        }
    }

    fun getWorkingDaysInMonth(monthPrefix: String): Flow<Int> = attendanceDao.getWorkingDaysInMonth(monthPrefix)
}
