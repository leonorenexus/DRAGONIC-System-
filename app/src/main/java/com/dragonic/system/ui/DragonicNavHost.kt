package com.dragonic.system.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dragonic.system.ui.screens.*

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Setup : Screen("setup")
    object Logs : Screen("logs")
    object FaceEnroll : Screen("face_enroll")
    object Settings : Screen("settings")
}

@Composable
fun DragonicNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.Setup.route) {
            SetupScreen(navController)
        }
        composable(Screen.Logs.route) {
            LogsScreen(navController)
        }
        composable(Screen.FaceEnroll.route) {
            FaceEnrollScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}
