package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.screens.SettingsScreen
import com.github.arhor.spellbindr.ui.screens.SpellDetailScreen
import com.github.arhor.spellbindr.ui.screens.SpellSearchScreen

private const val SPELL_SEARCH = "spell-search"
private const val SPELL_DETAIL = "spell-detail/{spellName}"
private const val SETTINGS = "settings"

private val NAV_ITEMS = listOf(
    SPELL_SEARCH to "Spells",
    SETTINGS to "Settings",
)

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val stackEntry by controller.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            AppNavBar(
                items = NAV_ITEMS,
                onItemClick = controller::navigate,
                isItemSelected = stackEntry::isSelected,
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = SPELL_SEARCH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = SPELL_SEARCH) {
                SpellSearchScreen(onSpellClick = {
                    controller.navigate("spell-detail/$it")
                })
            }
            composable(route = SPELL_DETAIL) {
                SpellDetailScreen(spellName = it.arguments?.getString("spellName"))
            }
            composable(route = SETTINGS) {
                SettingsScreen()
            }
        }
    }
}

private fun NavBackStackEntry?.isSelected(route: String): Boolean =
    when (this) {
        null -> false
        else -> destination.hierarchy.any { it.route == route }
    }
