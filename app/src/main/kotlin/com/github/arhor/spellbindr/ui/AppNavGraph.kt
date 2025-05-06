package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.github.arhor.spellbindr.ui.screens.SplashScreen

private const val SPLASH_SCREEN = "splash-screen"
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
    var showNavBar by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showNavBar && stackEntry?.destination?.route != SPLASH_SCREEN) {
                AppNavBar(
                    items = NAV_ITEMS,
                    onItemClick = controller::navigate,
                    isItemSelected = stackEntry::isSelected,
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = controller,
            startDestination = SPLASH_SCREEN,
        ) {
            composable(route = SPLASH_SCREEN) {
                SplashScreen(
                    onTimeout = { controller.navigate(SPELL_SEARCH) },
                )
            }
            composable(route = SPELL_SEARCH) {
                SpellSearchScreen(
                    modifier = Modifier.padding(innerPadding),
                    onSpellClick = { controller.navigate("spell-detail/$it") },
                    onScreedLoad = { showNavBar = true },
                )
            }
            composable(route = SPELL_DETAIL) {
                SpellDetailScreen(
                    modifier = Modifier.padding(innerPadding),
                    spellName = it.arguments?.getString("spellName"),
                )
            }
            composable(route = SETTINGS) {
                SettingsScreen(
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

private fun NavBackStackEntry?.isSelected(route: String): Boolean =
    when (this) {
        null -> false
        else -> destination.hierarchy.any { it.route == route }
    }
