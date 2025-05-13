package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.github.arhor.spellbindr.ui.screens.SpellDetailScreen
import com.github.arhor.spellbindr.ui.screens.SpellSearchScreen
import com.github.arhor.spellbindr.ui.screens.FavoriteSpellsScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.screens.CharacterCreationWizardScreen
import com.github.arhor.spellbindr.ui.screens.CharacterListScreen
import com.github.arhor.spellbindr.viewmodel.SpellListViewModel

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val stackEntry by controller.currentBackStackEntryAsState()
    val spellListViewModel = hiltViewModel<SpellListViewModel>()
    val spellListViewState by spellListViewModel.state.collectAsState()

    Scaffold(
        bottomBar = {
            AppNavBar(
                items = listOf(
                    Routes.SPELL_SEARCH to "Spells",
                    Routes.SPELL_LISTS to "Favorites",
                    Routes.CHARACTERS to "Characters",
                ),
                onItemClick = controller::navigate,
                isItemSelected = stackEntry::isSelected,
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier) {
            Image(
                painter = painterResource(id = R.drawable.bg_stars),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
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
                        onBackClicked = { controller.navigateUp() },
                        onLikeClicked = { name -> name?.let { spellListViewModel.toggleFavorite(it) } },
                        spellListViewModel = spellListViewModel
                    )
                }
                composable(route = Routes.SPELL_LISTS) {
                    FavoriteSpellsScreen(
                        onSpellClick = { controller.navigate("${SPELL_DETAIL_ROUTE}$it") },
                        spellListViewModel = spellListViewModel
                    )
                }
                composable(route = Routes.CHARACTERS) {
                    CharacterListScreen(onCreateNewCharacter = {
                        controller.navigate(Routes.CHARACTERS_CREATE)
                    })
                }
                composable(route = Routes.CHARACTERS_CREATE) {
                    CharacterCreationWizardScreen()
                }
            }
        }
    }
}

private const val SPELL_DETAIL_ROUTE = "spell-detail/"
private const val SPELL_DETAIL_VALUE = "spell-name"

private object Routes {
    const val SPELL_SEARCH = "spell-search"
    const val SPELL_DETAIL = "$SPELL_DETAIL_ROUTE{$SPELL_DETAIL_VALUE}"
    const val SPELL_LISTS = "spell-lists"
    const val CHARACTERS = "characters"
    const val CHARACTERS_CREATE = "characters/create"
}

private fun NavBackStackEntry?.isSelected(route: String): Boolean =
    when (this) {
        null -> false
        else -> destination.hierarchy.any { it.route == route }
    }
