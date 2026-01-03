package com.stormyai.app.presentation.navigation

sealed class Screen(val route: String, val label: String) {
    data object Generate : Screen("generate", "Generate")
    data object History : Screen("history", "History")
    data object Settings : Screen("settings", "Settings")
}
