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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationWizardScreen
import com.github.arhor.spellbindr.ui.screens.characters.search.CharacterListScreen
import com.github.arhor.spellbindr.ui.screens.spells.favorites.FavoriteSpellsScreen
import com.github.arhor.spellbindr.ui.screens.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.screens.spells.search.SpellSearchScreen

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
                isItemSelected = { it.isCurrent(stackEntry) },
            )
        }
    ) { innerPadding ->
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_stars),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                contentDescription = "Global background",
            )
            NavHost(
                navController = controller,
                startDestination = AppRoute.SpellSearch,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<AppRoute.SpellSearch> {
                    SpellSearchScreen(
                        onSpellClick = { controller.navigate(route = AppRoute.SpellDetails(it)) },
                    )
                }
                composable<AppRoute.FavoriteSpells> {
                    FavoriteSpellsScreen(
                        onSpellClick = { controller.navigate(route = AppRoute.SpellDetails(it)) },
                    )
                }
                composable<AppRoute.SpellDetails> {
                    SpellDetailScreen(
                        spellName = it.toRoute<AppRoute.SpellDetails>().spellName,
                        onBackClicked = { controller.navigateUp() },
                    )
                }
                composable<AppRoute.Characters> {
                    CharacterListScreen(
                        onCreateNewCharacter = { controller.navigate(route = AppRoute.CharacterCreate) }
                    )
                }
                composable<AppRoute.CharacterCreate> {
                    CharacterCreationWizardScreen()
                }
            }
        }
    }
}
