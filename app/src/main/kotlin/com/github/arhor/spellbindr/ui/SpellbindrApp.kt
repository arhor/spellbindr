package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.ui.feature.characters.creation.AbilitiesScreen
import com.github.arhor.spellbindr.ui.feature.characters.creation.CharacterCreationViewModel
import com.github.arhor.spellbindr.ui.feature.characters.creation.ClassSelectionScreen
import com.github.arhor.spellbindr.ui.feature.characters.creation.NameAndBackgroundScreen
import com.github.arhor.spellbindr.ui.feature.characters.creation.RaceSelectionScreen
import com.github.arhor.spellbindr.ui.feature.characters.creation.SkillsScreen
import com.github.arhor.spellbindr.ui.feature.characters.creation.SpellsScreen
import com.github.arhor.spellbindr.ui.feature.characters.creation.SummaryScreen
import com.github.arhor.spellbindr.ui.feature.characters.details.CharacterDetailsScreen
import com.github.arhor.spellbindr.ui.feature.characters.search.CharacterListScreen
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumScreen
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsScreen
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsScreen
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesScreen
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchScreen
import com.github.arhor.spellbindr.ui.feature.diceRoller.DiceRollerScreen
import com.github.arhor.spellbindr.ui.theme.SpellbindrTheme

@Composable
fun SpellbindrApp(
    onLoaded: () -> Unit,
    viewModel: SpellbindrAppViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val controller = rememberNavController()

    LaunchedEffect(state.ready) {
        if (state.ready) {
            onLoaded()
        }
    }

    SpellbindrTheme {
        Scaffold(
            content = { innerPadding ->
                Box {
                    NavHost(
                        navController = controller,
                        startDestination = Compendium,
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        compendiumNavGraph(controller)
                        charactersNavGraph(controller)
                        composable<DiceRoller> { DiceRollerScreen() }
                    }
                }
            },
            bottomBar = {
                NavigationBar {
                    NavBarItem(route = Compendium, controller)
                    NavBarItem(route = Characters, controller)
                    NavBarItem(route = DiceRoller, controller)
                }
            },
        )
    }
}

private fun NavGraphBuilder.compendiumNavGraph(controller: NavController) {
    navigation<Compendium>(startDestination = Compendium.Main) {
        composable<Compendium.Main> {
            CompendiumScreen(
                onItemClick = { controller.navigate(it) }
            )
        }
        composable<Compendium.Conditions> {
            ConditionsScreen()
        }
        composable<Compendium.Alignments> {
            AlignmentsScreen()
        }
        composable<Compendium.Races> {
            RacesScreen()
        }
        navigation<Compendium.Spells>(startDestination = Compendium.Spells.Search) {
            composable<Compendium.Spells.Search> {
                SpellSearchScreen(
                    onSpellClick = { controller.navigate(route = Compendium.Spells.Details(it)) },
                )
            }
            composable<Compendium.Spells.Details> {
                SpellDetailScreen(
                    spellName = it.toRoute<Compendium.Spells.Details>().spellName,
                    onBackClick = { controller.navigateUp() },
                )
            }
        }
    }
}

private fun NavGraphBuilder.charactersNavGraph(controller: NavController) {
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

private fun NavGraphBuilder.characterCreationNavGraph(controller: NavController) {
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

@Composable
private fun RowScope.NavBarItem(
    route: AppRoute,
    controller: NavHostController,
) {
    val stackEntry by controller.currentBackStackEntryAsState()

    NavigationBarItem(
        selected = stackEntry?.destination inSameHierarchyWith route,
        onClick = {
            controller.navigate(route = route) {
                popUpTo(controller.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        label = { Text(route.title) },
        icon = { }
    )
}

private infix fun NavDestination?.inSameHierarchyWith(route: AppRoute): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(route::class) }
}

