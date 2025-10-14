package com.github.arhor.spellbindr.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.screens.characters.creation.AbilitiesScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationViewModel
import com.github.arhor.spellbindr.ui.screens.characters.creation.ClassSelectionScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.NameAndBackgroundScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.RaceSelectionScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.SkillsScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.SpellsScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.SummaryScreen
import com.github.arhor.spellbindr.ui.screens.characters.details.CharacterDetailsScreen
import com.github.arhor.spellbindr.ui.screens.characters.search.CharacterListScreen
import com.github.arhor.spellbindr.ui.screens.compendium.CompendiumMainScreen
import com.github.arhor.spellbindr.ui.screens.compendium.alignments.AlignmentsScreen
import com.github.arhor.spellbindr.ui.screens.compendium.conditions.ConditionsScreen
import com.github.arhor.spellbindr.ui.screens.compendium.races.RacesScreen
import com.github.arhor.spellbindr.ui.screens.compendium.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.screens.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.utils.AppRoute

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val stackEntry by controller.currentBackStackEntryAsState()
    val currentDest = stackEntry?.destination

    Scaffold(
        bottomBar = {
            AppNavBar(
                onItemClick = {
                    controller.navigate(route = it) {
                        popUpTo(controller.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isItemSelected = { currentDest inSameHierarchyWith it },
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
                startDestination = Compendium,
                modifier = Modifier.padding(innerPadding),
            ) {
                compendiumNavGraph(controller)
                charactersNavGraph(controller)
            }
        }
    }
}

fun NavGraphBuilder.compendiumNavGraph(controller: NavController) {
    navigation<Compendium>(startDestination = Compendium.Main) {
        composable<Compendium.Main> {
            CompendiumMainScreen(
                onItemClick = { controller.navigate(it) }
            )
        }
        composable<Conditions> {
            ConditionsScreen()
        }
        composable<Alignments> {
            AlignmentsScreen()
        }
        composable<Races> {
            RacesScreen()
        }
        navigation<Spells>(startDestination = Spells.Search) {
            composable<Spells.Search> {
                SpellSearchScreen(
                    onSpellClick = { controller.navigate(route = Spells.Details(it)) },
                )
            }
            composable<Spells.Details> {
                SpellDetailScreen(
                    spellName = it.toRoute<Spells.Details>().spellName,
                    onBackClick = { controller.navigateUp() },
                )
            }
        }
    }
}

fun NavGraphBuilder.charactersNavGraph(controller: NavController) {
    navigation<Characters>(startDestination = Characters.Search) {
        composable<Characters.Search> {
            CharacterListScreen(
                navigateToCreate = { controller.navigate(route = Characters.Create) },
                navigateToDetails = { characterId ->
                    controller.navigate(route = Characters.Details(characterId = characterId))
                }
            )
        }
        composable<Characters.Details> {
            CharacterDetailsScreen(
                onBackClick = { controller.navigateUp() }
            )
        }
        characterCreationNavGraph(controller)
    }
}

fun NavGraphBuilder.characterCreationNavGraph(controller: NavController) {
    navigation<Characters.Create>(
        startDestination = Characters.Create.NameAndBackground
    ) {
        composable<Characters.Create.NameAndBackground> {
            NameAndBackgroundScreen(
                onNext = { controller.navigate(route = Characters.Create.Race) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
        composable<Characters.Create.Race> {
            RaceSelectionScreen(
                onPrev = { controller.navigateUp() },
                onNext = { controller.navigate(route = Characters.Create.Class) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
        composable<Characters.Create.Class> {
            ClassSelectionScreen(
                onPrev = { controller.navigateUp() },
                onNext = { controller.navigate(route = Characters.Create.Abilities) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
        composable<Characters.Create.Abilities> {
            AbilitiesScreen(
                onPrev = { controller.navigateUp() },
                onNext = { controller.navigate(route = Characters.Create.Skills) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
        composable<Characters.Create.Skills> {
            SkillsScreen(
                onPrev = { controller.navigateUp() },
                onNext = { controller.navigate(route = Characters.Create.Spells) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
        composable<Characters.Create.Spells> {
            SpellsScreen(
                onPrev = { controller.navigateUp() },
                onNext = { controller.navigate(route = Characters.Create.Summary) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
        composable<Characters.Create.Summary> {
            SummaryScreen(
                onPrev = { controller.navigateUp() },
                onFinish = { controller.popBackStack(route = Characters, inclusive = false) },
                viewModel = useCharacterCreationViewModel(it, controller)
            )
        }
    }
}

@Composable
private fun useCharacterCreationViewModel(
    entry: NavBackStackEntry,
    controller: NavController
): CharacterCreationViewModel {
    val creationGraphEntry = remember(entry) {
        controller.getBackStackEntry(Characters.Create)
    }
    return hiltViewModel(creationGraphEntry)
}

private infix fun NavDestination?.inSameHierarchyWith(route: AppRoute): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(route::class) }
}
