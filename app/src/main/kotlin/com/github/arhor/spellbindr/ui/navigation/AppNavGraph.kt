package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.AppNavBar
import com.github.arhor.spellbindr.ui.screens.CharacterCreationWizardScreen
import com.github.arhor.spellbindr.ui.screens.CharacterListScreen
import com.github.arhor.spellbindr.ui.screens.FavoriteSpellsScreen
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
                    AppRoute.SpellSearch,
                    AppRoute.FavoriteSpells,
                    AppRoute.Characters,
                ),
                onItemClick = controller::navigate,
                isItemSelected = stackEntry::isSelected,
            )
        }
    ) { innerPadding ->
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_stars),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
            NavHost(
                navController = controller,
                startDestination = AppRoute.SpellSearch,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<AppRoute.SpellSearch> {
                    SpellSearchScreen(
                        onSpellClick = { controller.navigate(AppRoute.SpellDetails(it)) },
                    )
                }
                composable<AppRoute.SpellDetails> {
                    SpellDetailScreen(
                        spellName = it.toRoute<AppRoute.SpellDetails>().spellName,
                        onBackClicked = { controller.navigateUp() },
                    )
                }
                composable<AppRoute.FavoriteSpells> {
                    FavoriteSpellsScreen(
                        onSpellClick = { controller.navigate(AppRoute.SpellDetails(it)) },
                    )
                }
                composable<AppRoute.Characters> {
                    CharacterListScreen(
                        onCreateNewCharacter = { controller.navigate(AppRoute.CharacterCreate) }
                    )
                }
                composable<AppRoute.CharacterCreate> {
                    CharacterCreationWizardScreen()
                }
            }
        }
    }
}

private fun NavBackStackEntry?.isSelected(route: AppRoute): Boolean =
    when (this) {
        null -> false
        else -> destination.hasRoute(route::class)
    }
