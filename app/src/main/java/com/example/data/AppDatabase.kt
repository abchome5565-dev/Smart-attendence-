package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [Student::class, AttendanceRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_attendance_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.studentDao(), database.attendanceDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(studentDao: StudentDao, attendanceDao: AttendanceDao) {
            val initialStudents = listOf(
                Student(1, "101", "Alex Johnson", "Class 10-A", "alex.j@school.edu", "+1 555-0101", "1"),
                Student(2, "102", "Sophia Martinez", "Class 10-A", "sophia.m@school.edu", "+1 555-0102", "2"),
                Student(3, "103", "Ethan Williams", "Class 10-A", "ethan.w@school.edu", "+1 555-0103", "3"),
                Student(4, "104", "Emma Brown", "Class 10-A", "emma.b@school.edu", "+1 555-0104", "4"),
                Student(5, "105", "Liam Davis", "Class 10-A", "liam.d@school.edu", "+1 555-0105", "5"),
                Student(6, "106", "Olivia Miller", "Class 10-A", "olivia.m@school.edu", "+1 555-0106", "6"),

                Student(7, "201", "Noah Wilson", "Class 10-B", "noah.w@school.edu", "+1 555-0201", "7"),
                Student(8, "202", "Ava Anderson", "Class 10-B", "ava.a@school.edu", "+1 555-0202", "8"),
                Student(9, "203", "Lucas Thomas", "Class 10-B", "lucas.t@school.edu", "+1 555-0203", "9"),
                Student(10, "204", "Isabella Taylor", "Class 10-B", "isabella.t@school.edu", "+1 555-0204", "10"),

                Student(11, "301", "Mason Jackson", "CS-101", "mason.j@univ.edu", "+1 555-0301", "11"),
                Student(12, "302", "Mia White", "CS-101", "mia.w@univ.edu", "+1 555-0302", "12"),
                Student(13, "303", "Benjamin Harris", "CS-101", "ben.h@univ.edu", "+1 555-0303", "13"),
                Student(14, "304", "Charlotte Martin", "CS-101", "charlotte.m@univ.edu", "+1 555-0304", "14")
            )
            studentDao.insertStudents(initialStudents)

            // Populate sample attendance for the past 5 days
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            val statuses = listOf("PRESENT", "PRESENT", "PRESENT", "ABSENT", "PRESENT", "LATE")

            for (i in 0..6) {
                val dateStr = dateFormat.format(calendar.time)
                initialStudents.forEachIndexed { index, student ->
                    val status = when {
                        (index + i) % 7 == 0 -> "ABSENT"
                        (index + i) % 5 == 0 -> "LATE"
                        (index + i) % 11 == 0 -> "LEAVE"
                        else -> "PRESENT"
                    }
                    attendanceDao.insertOrUpdateRecord(
                        AttendanceRecord(
                            studentId = student.id,
                            date = dateStr,
                            status = status,
                            notes = if (status == "LATE") "15 mins late" else ""
                        )
                    )
                }
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
        }
    }
}
