package com.github.arhor.spellbindr.ui.components

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

const val CHARACTER_LIST = "character-list"

@Composable
fun AppRouter() {
    val controller = rememberNavController()

    NavHost(controller, CHARACTER_LIST) {
        composable(route = CHARACTER_LIST) {
            CharacterListScreen()
        }
    }
}
