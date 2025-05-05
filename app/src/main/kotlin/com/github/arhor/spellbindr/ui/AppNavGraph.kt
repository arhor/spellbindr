package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.screens.SettingsScreen
import com.github.arhor.spellbindr.ui.screens.SpellSearchScreen

private const val SPELL_SEARCH = "spell-search"
private const val SETTINGS = "settings"

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val currentRoute = controller.currentBackStackEntryFlow
        .collectAsState(initial = SPELL_SEARCH)
        .value
        .toString()

    Scaffold(
        bottomBar = { AppNavBar(controller) }
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = SPELL_SEARCH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = SPELL_SEARCH) {
                SpellSearchScreen()
            }
            composable(route = SETTINGS) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun AppNavBar(controller: NavController) {
    val currentBackStackEntry by controller.currentBackStackEntryAsState()

    NavigationBar {
        for ((route, label) in NAV_ITEMS) {
            NavigationBarItem(
                selected = currentBackStackEntry.isSelected(route),
                onClick = { controller.navigate(route) },
                label = { Text(label) },
                icon = { }
            )
        }
    }
}

private val NAV_ITEMS = listOf(
    SPELL_SEARCH to "Spells",
    SETTINGS to "Settings",
)

private fun NavBackStackEntry?.isSelected(route: String): Boolean =
    when (this) {
        null -> false
        else -> destination.hierarchy.any { it.route == route }
    }
