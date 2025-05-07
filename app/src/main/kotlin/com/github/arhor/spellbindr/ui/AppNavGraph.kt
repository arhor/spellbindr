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

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val stackEntry by controller.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            AppNavBar(
                items = listOf(
                    Routes.SPELL_SEARCH to "Spells",
                    Routes.APP_SETTINGS to "Settings",
                ),
                onItemClick = controller::navigate,
                isItemSelected = stackEntry::isSelected,
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = Routes.SPELL_SEARCH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(route = Routes.SPELL_SEARCH) {
                SpellSearchScreen(
                    onSpellClick = { controller.navigate("$SPELL_DETAIL_ROUTE$it") },
                )
            }
            composable(route = Routes.SPELL_DETAIL) {
                SpellDetailScreen(
                    spellName = it.arguments?.getString(SPELL_DETAIL_VALUE),
                )
            }
            composable(route = Routes.APP_SETTINGS) {
                SettingsScreen()
            }
        }
    }
}

private const val SPELL_DETAIL_ROUTE = "spell-detail/"
private const val SPELL_DETAIL_VALUE = "spell-name"

private object Routes {
    const val SPELL_SEARCH = "spell-search"
    const val SPELL_DETAIL = "$SPELL_DETAIL_ROUTE{$SPELL_DETAIL_VALUE}"
    const val APP_SETTINGS = "settings"
}

private fun NavBackStackEntry?.isSelected(route: String): Boolean =
    when (this) {
        null -> false
        else -> destination.hierarchy.any { it.route == route }
    }
