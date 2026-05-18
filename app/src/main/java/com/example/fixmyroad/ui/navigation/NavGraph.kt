package com.example.fixmyroad.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.fixmyroad.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    isAdminSession: Boolean,
    onAdminSessionChanged: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 300 }) },
        exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { -300 }) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { -300 }) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { 300 }) }
    ) {
        composable(Screen.Splash.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            SplashScreen(
                onNavigateToNext = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = { isAdmin ->
                    onAdminSessionChanged(isAdmin)
                    val destination = if (isAdmin) {
                        Screen.AdminDashboard.route
                    } else {
                        Screen.Dashboard.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToReportIssue = { navController.navigate(Screen.ReportIssue.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    onAdminSessionChanged(false)
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.AdminDashboard.route) {
            if (isAdminSession) {
                AdminDashboardScreen(
                    onLogout = {
                        onAdminSessionChanged(false)
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToDetails = { ticketId ->
                        navController.navigate(Screen.ReportDetails.createRoute(ticketId))
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            }
        }
        composable(Screen.ReportIssue.route) {
            ReportIssueScreen(
                onBack = { navController.popBackStack() },
                onSubmitSuccess = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetails = { ticketId ->
                    navController.navigate(Screen.ReportDetails.createRoute(ticketId))
                }
            )
        }
        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToDetails = { ticketId ->
                    navController.navigate(Screen.ReportDetails.createRoute(ticketId))
                }
            )
        }
        composable(
            route = Screen.ReportDetails.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) {
            ReportDetailsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEdit = { ticketId ->
                    navController.navigate(Screen.EditReport.createRoute(ticketId))
                },
                onDeleteSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.EditReport.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) {
            EditReportScreen(
                onBack = { navController.popBackStack() },
                onUpdateSuccess = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.Support.route) {
            SupportScreen(onBack = { navController.popBackStack() })
        }
    }
}

