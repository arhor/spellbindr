package com.github.arhor.spellbindr.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.navigation.BottomNavItems

@Composable
fun AppBottomBar(controller: NavHostController) {
    val backStackEntry by controller.currentBackStackEntryAsState()

    NavigationBar {
        BottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = backStackEntry?.destination matches item.destination,
                onClick = {
                    controller.navigate(item.destination) {
                        popUpTo(controller.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}

private infix fun NavDestination?.matches(destination: AppDestination): Boolean {
    if (this == null) {
        return false
    }
    return when (destination) {
        AppDestination.CompendiumSections -> hierarchy.any {
            it.hasRoute<AppDestination.CompendiumSections>() ||
                it.hasRoute<AppDestination.Spells>() ||
                it.hasRoute<AppDestination.Conditions>() ||
                it.hasRoute<AppDestination.Alignments>() ||
                it.hasRoute<AppDestination.Races>()
        }

        else -> hierarchy.any { it.hasRoute(destination::class) }
    }
}
