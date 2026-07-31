package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY classSection ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE classSection = :classSection ORDER BY rollNumber ASC")
    fun getStudentsByClass(classSection: String): Flow<List<Student>>

    @Query("SELECT DISTINCT classSection FROM students ORDER BY classSection ASC")
    fun getAllClassSections(): Flow<List<String>>

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%' OR classSection LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}
