package com.github.arhor.spellbindr.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.screens.SpellSearchScreen

private const val SPELL_SEARCH = "spell-search"

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()

    NavHost(navController = controller, startDestination = SPELL_SEARCH) {
        composable(route = SPELL_SEARCH) {
            SpellSearchScreen()
        }
    }
}
