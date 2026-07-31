package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.Student
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusLateAmber
import com.example.ui.theme.StatusLateBg
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen

data class StudentMonthlySummary(
    val student: Student,
    val totalWorkingDays: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val percentage: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    selectedMonth: String,
    selectedClass: String,
    classes: List<String>,
    allStudents: List<Student>,
    monthlyRecords: List<AttendanceRecord>,
    workingDays: Int,
    onSelectMonth: (String) -> Unit,
    onSelectClass: (String) -> Unit,
    onExportReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val months = listOf("2026-07", "2026-06", "2026-05", "2026-04")

    val filteredStudents = if (selectedClass != "All Classes" && selectedClass.isNotEmpty()) {
        allStudents.filter { it.classSection == selectedClass }
    } else {
        allStudents
    }

    val studentSummaries = filteredStudents.map { student ->
        val studentRecords = monthlyRecords.filter { it.studentId == student.id }
        val present = studentRecords.count { it.status == "PRESENT" }
        val absent = studentRecords.count { it.status == "ABSENT" }
        val late = studentRecords.count { it.status == "LATE" }
        val total = studentRecords.size
        val pct = if (total > 0) ((present + late).toFloat() / total.toFloat()) * 100f else 0f
        StudentMonthlySummary(
            student = student,
            totalWorkingDays = total,
            presentCount = present,
            absentCount = absent,
            lateCount = late,
            percentage = pct
        )
    }

    val totalRecordsCount = monthlyRecords.size
    val totalPresents = monthlyRecords.count { it.status == "PRESENT" }
    val totalAbsents = monthlyRecords.count { it.status == "ABSENT" }
    val totalLates = monthlyRecords.count { it.status == "LATE" }
    val classAveragePct = if (totalRecordsCount > 0) ((totalPresents + totalLates).toFloat() / totalRecordsCount.toFloat()) * 100f else 0f

    val lowAttendanceStudents = studentSummaries.filter { it.totalWorkingDays > 0 && it.percentage < 75f }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Attendance Report", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = onExportReport,
                        modifier = Modifier.testTag("export_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Report")
                    }
                }
            )
        },
        modifier = modifier.testTag("monthly_report_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Filter Section Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Select Month:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(months) { monthStr ->
                                FilterChip(
                                    selected = selectedMonth == monthStr,
                                    onClick = { onSelectMonth(monthStr) },
                                    label = { Text(monthStr) },
                                    modifier = Modifier.testTag("month_chip_$monthStr")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Filter Class:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(classes) { classSec ->
                                FilterChip(
                                    selected = selectedClass == classSec,
                                    onClick = { onSelectClass(classSec) },
                                    label = { Text(classSec) },
                                    modifier = Modifier.testTag("report_class_chip_$classSec")
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Overview Summary Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$selectedMonth Summary",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "$workingDays Working Days Recorded",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            Text(
                                text = "%.1f%%".format(classAveragePct),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (classAveragePct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReportMiniBadge(label = "Total Logs", count = totalRecordsCount, color = MaterialTheme.colorScheme.primary)
                            ReportMiniBadge(label = "Presents", count = totalPresents, color = StatusPresentGreen)
                            ReportMiniBadge(label = "Absents", count = totalAbsents, color = StatusAbsentRed)
                            ReportMiniBadge(label = "Lates", count = totalLates, color = StatusLateAmber)
                        }
                    }
                }
            }

            // Low Attendance Warning Banner
            if (lowAttendanceStudents.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusAbsentBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = StatusAbsentRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${lowAttendanceStudents.size} Students below 75% Threshold",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = StatusAbsentRed
                                    )
                                )
                                Text(
                                    text = "Needs attention: ${lowAttendanceStudents.joinToString { it.student.name.split(" ").first() }}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StatusAbsentRed.copy(alpha = 0.8f))
                                )
                            }
                        }
                    }
                }
            }

            // Export Action Button
            item {
                Button(
                    onClick = onExportReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_report_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Report (CSV / PDF)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Student Monthly Breakdown List
            item {
                Text(
                    text = "Individual Student Performance",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (studentSummaries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available for selected filter.")
                    }
                }
            } else {
                items(studentSummaries) { summary ->
                    StudentMonthlyRowCard(summary = summary)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ReportMiniBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StudentMonthlyRowCard(summary: StudentMonthlySummary) {
    val isLow = summary.totalWorkingDays > 0 && summary.percentage < 75f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLow) StatusAbsentBg.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${summary.student.rollNumber} - ${summary.student.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (isLow) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low",
                            tint = StatusAbsentRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "${summary.student.classSection} • ${summary.presentCount} Present / ${summary.totalWorkingDays} Sessions",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Surface(
                color = if (summary.percentage >= 85f) StatusPresentBg else if (summary.percentage >= 75f) StatusLateBg else StatusAbsentBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "%.0f%%".format(summary.percentage),
                    color = if (summary.percentage >= 85f) StatusPresentGreen else if (summary.percentage >= 75f) StatusLateAmber else StatusAbsentRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
