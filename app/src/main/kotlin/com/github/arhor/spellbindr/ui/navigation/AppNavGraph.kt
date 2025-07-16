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
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.github.arhor.spellbindr.ui.screens.characters.creation.AppearanceScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.BackgroundDetailsScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.CharacterCreationViewModel
import com.github.arhor.spellbindr.ui.screens.characters.creation.ClassSelectionScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.EquipmentScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.NameAndBackgroundScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.RaceSelectionScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.SkillsScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.SpellsScreen
import com.github.arhor.spellbindr.ui.screens.characters.creation.SummaryScreen
import com.github.arhor.spellbindr.ui.screens.characters.details.CharacterDetailsScreen
import com.github.arhor.spellbindr.ui.screens.characters.search.CharacterListScreen
import com.github.arhor.spellbindr.ui.screens.library.conditions.details.ConditionDetailsScreen
import com.github.arhor.spellbindr.ui.screens.library.conditions.search.ConditionListScreen
import com.github.arhor.spellbindr.ui.screens.library.main.LibraryMainScreen
import com.github.arhor.spellbindr.ui.screens.library.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.screens.library.spells.search.SpellSearchScreen

@Composable
fun AppNavGraph() {
    val controller = rememberNavController()
    val stackEntry by controller.currentBackStackEntryAsState()
    val currentDest = stackEntry?.destination

    Scaffold(
        bottomBar = {
            AppNavBar(
                onItemClick = { controller navigateTo it },
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
                startDestination = AppRoute.Library,
                modifier = Modifier.padding(innerPadding),
            ) {
                libraryBaseNavGraph(controller)
                charactersNavGraph(controller)
            }
        }
    }
}

fun NavGraphBuilder.libraryBaseNavGraph(controller: NavController) {
    navigation<AppRoute.Library>(
        startDestination = AppRoute.Library.Main
    ) {
        composable<AppRoute.Library.Main> {
            LibraryMainScreen(
                onItemClick = { controller navigateTo it }
            )
        }
        spellsNavGraph(controller)
        conditionsNavGraph(controller)
    }
}

fun NavGraphBuilder.spellsNavGraph(controller: NavController) {
    navigation<AppRoute.Library.Spells>(
        startDestination = AppRoute.Library.Spells.Search
    ) {
        composable<AppRoute.Library.Spells.Search> {
            SpellSearchScreen(
                onSpellClick = { controller navigateTo AppRoute.Library.Spells.Details(it) },
            )
        }
        composable<AppRoute.Library.Spells.Details> {
            SpellDetailScreen(
                spellName = it.toRoute<AppRoute.Library.Spells.Details>().spellName,
                onBackClick = { controller.navigateUp() },
            )
        }
    }
}

fun NavGraphBuilder.conditionsNavGraph(controller: NavController) {
    navigation<AppRoute.Library.Conditions>(
        startDestination = AppRoute.Library.Conditions.Search
    ) {
        composable<AppRoute.Library.Conditions.Search> {
            ConditionListScreen(
                onConditionClick = { controller navigateTo AppRoute.Library.Conditions.Details(it) },
            )
        }
        composable<AppRoute.Library.Conditions.Details> {
            val details = it.toRoute<AppRoute.Library.Conditions.Details>()
            ConditionDetailsScreen(
                conditionName = details.conditionName,
                onBackClick = { controller.navigateUp() },
            )
        }
    }
}

fun NavGraphBuilder.charactersNavGraph(controller: NavController) {
    navigation<AppRoute.Characters>(
        startDestination = AppRoute.Characters.Search
    ) {
        composable<AppRoute.Characters.Search> {
            CharacterListScreen(
                navigateToCreate = { controller.navigate(route = AppRoute.Characters.Create) },
                navigateToDetails = { characterId ->
                    controller.navigate(route = AppRoute.Characters.Details(characterId = characterId))
                }
            )
        }
        composable<AppRoute.Characters.Details> {
            CharacterDetailsScreen(
                onBackClick = { controller.navigateUp() }
            )
        }
        characterCreationNavGraph(controller)
    }
}

fun NavGraphBuilder.characterCreationNavGraph(controller: NavController) {
    navigation<AppRoute.Characters.Create>(
        startDestination = AppRoute.Characters.Create.NameAndBackground
    ) {
        composable<AppRoute.Characters.Create.NameAndBackground> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            NameAndBackgroundScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.BackgroundDetails) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.BackgroundDetails> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            BackgroundDetailsScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Race) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Race> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            RaceSelectionScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Class) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Class> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            ClassSelectionScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Abilities) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Abilities> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            AbilitiesScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Skills) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Skills> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            SkillsScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Equipment) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Equipment> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            EquipmentScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Spells) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Spells> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            SpellsScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Appearance) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Appearance> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            AppearanceScreen(
                onNext = { controller.navigate(route = AppRoute.Characters.Create.Summary) },
                viewModel = viewModel
            )
        }
        composable<AppRoute.Characters.Create.Summary> {
            val creationGraphEntry = remember(it) {
                controller.getBackStackEntry(AppRoute.Characters.Create)
            }
            val viewModel: CharacterCreationViewModel = hiltViewModel(creationGraphEntry)
            SummaryScreen(
                onFinish = { controller.popBackStack(route = AppRoute.Characters, inclusive = false) },
                viewModel = viewModel
            )
        }
    }
}

private infix fun NavController.navigateTo(route: AppRoute) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private infix fun NavDestination?.inSameHierarchyWith(route: AppRoute): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(route::class) }
}
