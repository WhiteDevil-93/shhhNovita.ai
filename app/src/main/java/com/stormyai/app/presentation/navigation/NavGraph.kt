package com.stormyai.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stormyai.app.presentation.generate.GenerateRoute
import com.stormyai.app.presentation.history.HistoryRoute
import com.stormyai.app.presentation.settings.SettingsRoute

@Composable
fun StormyAiNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val screens = listOf(Screen.Generate, Screen.History, Screen.Settings)
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(screen.label) },
                        icon = { Icon(screen.icon, contentDescription = screen.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Generate.route,
            modifier = modifier
        ) {
            composable(Screen.Generate.route) { GenerateRoute(paddingValues) }
            composable(Screen.History.route) { HistoryRoute(paddingValues) }
            composable(Screen.Settings.route) { SettingsRoute(paddingValues) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StormyAiNavGraphPreview() {
    StormyAiNavGraph()
}
