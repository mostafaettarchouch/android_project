package com.ofppt.istak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ofppt.istak.ui.screens.auth.LoginScreen
import com.ofppt.istak.ui.screens.formateur.FormateurDashboardScreen
import com.ofppt.istak.ui.screens.stagiaire.StagiaireDashboardScreen
import com.ofppt.istak.ui.theme.IstakTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val splashViewModel: com.ofppt.istak.viewmodel.SplashViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val darkModePref by splashViewModel.isDarkMode.collectAsState(initial = null)
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val useDarkMode = darkModePref ?: isSystemDark

            IstakTheme(darkTheme = useDarkMode) {
                val navigateTo = intent.getStringExtra("navigate_to")
                AppNavigation(viewModel = splashViewModel, initialNavigateTo = navigateTo)
            }
        }
    }
}

@OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val permissionState = com.google.accompanist.permissions.rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )

        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }

        if (permissionState.status.shouldShowRationale) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { },
                title = { Text("Notifications") },
                text = { Text("Activez les notifications pour recevoir les nouveaux messages de l'administration.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Activer")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { /* Do nothing, user refused */ }) {
                        Text("Plus tard")
                    }
                }
            )
        }
    }
}

@Composable
fun AppNavigation(
    viewModel: com.ofppt.istak.viewmodel.SplashViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    initialNavigateTo: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val startDestination by viewModel.startDestination.collectAsState()

    // Request Permissions
    RequestNotificationPermission()

    // Start WebSocket Service if logged in
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(startDestination) {
        if (startDestination != "login" && startDestination != null) {
            val intent = android.content.Intent(context, com.ofppt.istak.data.websocket.ReverbService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    // Handle Notification Click
    LaunchedEffect(startDestination, initialNavigateTo) {
        if (startDestination != null && initialNavigateTo == "messages") {
            // We can't easily open the dialog from here without passing state down.
            // But we can navigate to the dashboard which has the FAB.
            // Ideally, we would pass a flag to the dashboard to open the dialog automatically.
            // For now, navigating to dashboard is good enough, user sees the badge.
            // Or we can use a SavedStateHandle or a SharedViewModel to trigger the dialog.
        }
    }

    if (startDestination == null) {
        // Show Loading/Splash Screen
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Determine which BottomBar to show
        val isStagiaireRoute = currentRoute in listOf("stagiaire_dashboard", "schedule", "news", "exams", "profile")
        val isFormateurRoute = currentRoute in listOf("formateur_dashboard", "formateur_schedule", "formateur_profile", "absence_entry", "formateur_news")

        androidx.compose.material3.Scaffold(
            bottomBar = {
                if (isStagiaireRoute) {
                    com.ofppt.istak.ui.components.BottomNavigationBar(navController = navController)
                } else if (isFormateurRoute) {
                    com.ofppt.istak.ui.components.FormateurBottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination!!,
                modifier = androidx.compose.ui.Modifier.padding(innerPadding)
            ) {
                composable("login") {
                    LoginScreen(onLoginSuccess = { role ->
                        if (role == "stagiaire") {
                            navController.navigate("stagiaire_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else if (role == "formateur") {
                            navController.navigate("formateur_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    })
                }
                
                composable("maintenance") {
                    com.ofppt.istak.ui.screens.MaintenanceScreen(
                        onRetry = { viewModel.retry() }
                    )
                }
                
                // Stagiaire Routes
                composable("stagiaire_dashboard") {
                    StagiaireDashboardScreen(
                        onNavigateToSchedule = { navController.navigate("schedule") },
                        onNavigateToProfile = { navController.navigate("profile") }
                    )
                }
                composable("schedule") {
                    com.ofppt.istak.ui.screens.stagiaire.ScheduleScreen()
                }
                composable("news") {
                    com.ofppt.istak.ui.screens.news.NewsScreen()
                }
                composable("formateur_news") {
                    com.ofppt.istak.ui.screens.news.NewsScreen()
                }
                composable("exams") {
                    com.ofppt.istak.ui.screens.exams.ExamScheduleScreen()
                }
                composable("profile") {
                    com.ofppt.istak.ui.screens.profile.ProfileScreen(
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Formateur Routes
                composable("formateur_dashboard") {
                    // This is now the main Home/Absence Entry screen
                    com.ofppt.istak.ui.screens.formateur.FormateurDashboardScreen(
                        onNavigateToAbsenceEntry = { /* No longer needed as separate screen */ },
                        onNavigateToProfile = { navController.navigate("formateur_profile") }
                    )
                }
                composable("formateur_schedule") {
                    com.ofppt.istak.ui.screens.formateur.FormateurScheduleScreen()
                }
                composable("formateur_profile") {
                    com.ofppt.istak.ui.screens.formateur.FormateurProfileScreen(
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
