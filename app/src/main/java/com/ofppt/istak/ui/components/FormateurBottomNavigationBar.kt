package com.ofppt.istak.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class FormateurBottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : FormateurBottomNavItem("formateur_dashboard", "Accueil", Icons.Default.Home)
    object Schedule : FormateurBottomNavItem("formateur_schedule", "Emploi", Icons.Default.DateRange)
    object News : FormateurBottomNavItem("formateur_news", "Actus", Icons.Default.Newspaper)
    object Profile : FormateurBottomNavItem("formateur_profile", "Profil", Icons.Default.Person)
}

@Composable
fun FormateurBottomNavigationBar(navController: NavController) {
    val items = listOf(
        FormateurBottomNavItem.Dashboard,
        FormateurBottomNavItem.Schedule,
        FormateurBottomNavItem.News,
        FormateurBottomNavItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo("formateur_dashboard") { saveState = false }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }
    }
}
