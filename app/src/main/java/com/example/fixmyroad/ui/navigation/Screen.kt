package com.example.fixmyroad.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Map : Screen("map")
    object ReportIssue : Screen("report_issue")
    object History : Screen("history")
    object ReportDetails : Screen("report_details/{ticketId}") {
        fun createRoute(ticketId: String) = "report_details/$ticketId"
    }
    object EditReport : Screen("edit_report/{ticketId}") {
        fun createRoute(ticketId: String) = "edit_report/$ticketId"
    }
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Support : Screen("support")
}
