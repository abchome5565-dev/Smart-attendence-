package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecords(records: List<AttendanceRecord>)

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'PRESENT'")
    fun getPresentCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'ABSENT'")
    fun getAbsentCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'LATE'")
    fun getLateCountForDate(date: String): Flow<Int>

    @Query("""
        SELECT s.id AS studentId, s.rollNumber, s.name, s.classSection, s.email, s.phone, s.avatarSeed,
               a.status AS status, a.notes AS notes
        FROM students s
        LEFT JOIN attendance_records a ON s.id = a.studentId AND a.date = :date
        WHERE s.classSection = :classSection
        ORDER BY s.rollNumber ASC
    """)
    fun getStudentsWithAttendanceByClassAndDate(classSection: String, date: String): Flow<List<StudentWithAttendance>>

    @Query("""
        SELECT s.id AS studentId, s.rollNumber, s.name, s.classSection, s.email, s.phone, s.avatarSeed,
               a.status AS status, a.notes AS notes
        FROM students s
        LEFT JOIN attendance_records a ON s.id = a.studentId AND a.date = :date
        ORDER BY s.classSection ASC, s.rollNumber ASC
    """)
    fun getAllStudentsWithAttendanceForDate(date: String): Flow<List<StudentWithAttendance>>

    @Query("SELECT COUNT(DISTINCT date) FROM attendance_records WHERE date LIKE :monthPrefix || '%'")
    fun getWorkingDaysInMonth(monthPrefix: String): Flow<Int>
}
