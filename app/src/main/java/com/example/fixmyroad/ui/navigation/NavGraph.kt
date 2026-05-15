package com.example.fixmyroad.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.fixmyroad.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
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
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
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
                onNavigateToSupport = { navController.navigate(Screen.Support.route) }
            )
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

