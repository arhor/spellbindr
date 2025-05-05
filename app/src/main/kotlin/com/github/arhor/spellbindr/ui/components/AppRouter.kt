package com.github.arhor.spellbindr.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

const val SPELL_SEARCH = "spell-search"

@Composable
fun AppRouter() {
    val controller = rememberNavController()

    NavHost(navController = controller, startDestination = SPELL_SEARCH) {
        composable(route = SPELL_SEARCH) {
            SpellSearchScreen()
        }
    }
}
