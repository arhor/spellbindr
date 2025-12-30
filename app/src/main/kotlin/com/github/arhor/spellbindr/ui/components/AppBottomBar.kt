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
            val isSelected = backStackEntry?.destination matches item.destination
            NavigationBarItem(
                selected = isSelected,
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
    if (this == null) return false

    return when (destination) {
        AppDestination.CompendiumSections -> hierarchy.any { navDestination ->
            navDestination.hasRoute(AppDestination.CompendiumSections::class) ||
                navDestination.hasRoute(AppDestination.CompendiumSpells::class) ||
                navDestination.hasRoute(AppDestination.CompendiumConditions::class) ||
                navDestination.hasRoute(AppDestination.CompendiumAlignments::class) ||
                navDestination.hasRoute(AppDestination.CompendiumRaces::class) ||
                navDestination.hasRoute(AppDestination.CompendiumTraits::class) ||
                navDestination.hasRoute(AppDestination.CompendiumFeatures::class) ||
                navDestination.hasRoute(AppDestination.CompendiumClasses::class) ||
                navDestination.hasRoute(AppDestination.CompendiumEquipment::class)
        }

        else -> hierarchy.any { it.hasRoute(destination::class) }
    }
}
