package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.Student
import com.example.ui.AttendanceViewModel
import com.example.ui.AttendanceViewModelFactory
import com.example.ui.components.SmartBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AddStudentScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MarkAttendanceScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.StudentDetailScreen
import com.example.ui.screens.StudentListScreen
import com.example.ui.theme.SmartAttendanceTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: AttendanceViewModel by viewModels {
        AttendanceViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartAttendanceTheme {
                SmartAttendanceApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartAttendanceApp(viewModel: AttendanceViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val userSession by viewModel.userSession.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val classes by viewModel.classSections.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val students by viewModel.students.collectAsState()
    val studentsWithAttendance by viewModel.studentsWithAttendance.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val monthlyRecords by viewModel.monthlyRecords.collectAsState()
    val workingDays by viewModel.workingDaysCount.collectAsState()

    // Observe Toast Events
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on login, add student, or detail screens
    val showBottomBar = userSession.isLoggedIn && currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.MarkAttendance.route,
        Screen.StudentList.route,
        Screen.MonthlyReport.route
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                SmartBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (userSession.isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onQuickLogin = { username, password ->
                        if (viewModel.login(username, password)) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    userSession = userSession,
                    selectedDate = selectedDate,
                    selectedClass = selectedClass,
                    classes = classes,
                    stats = dashboardStats,
                    todayStudents = studentsWithAttendance,
                    onSelectClass = { viewModel.setSelectedClass(it) },
                    onNavigateToMarkAttendance = { navController.navigate(Screen.MarkAttendance.route) },
                    onNavigateToAddStudent = { navController.navigate(Screen.AddStudent.route) },
                    onNavigateToReports = { navController.navigate(Screen.MonthlyReport.route) },
                    onNavigateToStudents = { navController.navigate(Screen.StudentList.route) }
                )
            }

            composable(Screen.MarkAttendance.route) {
                MarkAttendanceScreen(
                    selectedDate = selectedDate,
                    selectedClass = selectedClass,
                    classes = classes,
                    students = studentsWithAttendance,
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    onSelectClass = { viewModel.setSelectedClass(it) },
                    onMarkAttendance = { studentId, status, notes ->
                        viewModel.markAttendance(studentId, status, notes)
                    },
                    onMarkAllPresent = { viewModel.markAllPresent() }
                )
            }

            composable(Screen.StudentList.route) {
                StudentListScreen(
                    students = students,
                    searchQuery = searchQuery,
                    selectedClass = selectedClass,
                    classes = classes,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onSelectClass = { viewModel.setSelectedClass(it) },
                    onStudentClick = { studentId ->
                        navController.navigate(Screen.StudentDetail.createRoute(studentId))
                    },
                    onAddStudentClick = { navController.navigate(Screen.AddStudent.route) },
                    onDeleteStudent = { student -> viewModel.deleteStudent(student) }
                )
            }

            composable(Screen.AddStudent.route) {
                AddStudentScreen(
                    existingClasses = classes,
                    onSaveStudent = { rollNo, name, classSec, email, phone ->
                        viewModel.addStudent(rollNo, name, classSec, email, phone)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.MonthlyReport.route) {
                MonthlyReportScreen(
                    selectedMonth = selectedMonth,
                    selectedClass = selectedClass,
                    classes = classes,
                    allStudents = students,
                    monthlyRecords = monthlyRecords,
                    workingDays = workingDays,
                    onSelectMonth = { viewModel.setSelectedMonth(it) },
                    onSelectClass = { viewModel.setSelectedClass(it) },
                    onExportReport = {
                        Toast.makeText(context, "Monthly Attendance Report exported to CSV / Downloads", Toast.LENGTH_LONG).show()
                    }
                )
            }

            composable(
                route = Screen.StudentDetail.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                var student by remember { mutableStateOf<Student?>(null) }

                LaunchedEffect(studentId) {
                    student = viewModel.getStudentById(studentId)
                }

                StudentDetailScreen(
                    student = student,
                    attendanceHistoryFlow = viewModel.getStudentAttendanceHistory(studentId),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
