package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.characters.CharacterCreationScreen
import com.github.arhor.spellbindr.characters.CharacterLevelUpScreen
import com.github.arhor.spellbindr.characters.CharacterSheetScreen
import com.github.arhor.spellbindr.characters.CharactersListScreen
import com.github.arhor.spellbindr.navigation.AppDestination
import com.github.arhor.spellbindr.navigation.BottomNavItems
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumScreen
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerScreen
import com.github.arhor.spellbindr.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellbindrApp(
    onLoaded: () -> Unit,
    viewModel: SpellbindrAppViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val controller = rememberNavController()
    val backStackEntry by controller.currentBackStackEntryAsState()

    LaunchedEffect(state.ready) {
        if (state.ready) {
            onLoaded()
        }
    }

    AppTheme {
        AppTopBarControllerProvider { config ->
            Scaffold(
                topBar = {
                    if (config.visible) {
                        TopAppBar(
                            title = config.title,
                            navigationIcon = config.navigation.asNavigationIcon(),
                            actions = config.actions,
                        )
                    }
                },
                bottomBar = {
                    NavigationBar {
                        BottomNavItems.forEach { item ->
                            val isSelected = backStackEntry?.destination matches item.destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    controller.navigate(item.destination) {
                                        popUpTo(controller.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                    )
                                },
                                label = { Text(item.label) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = controller,
                    startDestination = AppDestination.CharactersHome,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable<AppDestination.CharactersHome> {
                        CharactersListScreen(
                            onCharacterSelected = { summary ->
                                controller.navigate(
                                    AppDestination.CharacterSheet(characterId = summary.id)
                                )
                            },
                            onCreateCharacter = { controller.navigate(AppDestination.CharacterCreate) },
                        )
                    }
                    composable<AppDestination.CharacterSheet> {
                        val args = it.toRoute<AppDestination.CharacterSheet>()
                        CharacterSheetScreen(
                            characterId = args.characterId,
                            onBack = { controller.navigateUp() },
                            onLevelUp = { characterId ->
                                controller.navigate(
                                    AppDestination.CharacterLevelUp(characterId = characterId)
                                )
                            },
                        )
                    }
                    composable<AppDestination.CharacterCreate> {
                        CharacterCreationScreen(onBack = { controller.navigateUp() })
                    }
                    composable<AppDestination.CharacterLevelUp> {
                        val args = it.toRoute<AppDestination.CharacterLevelUp>()
                        CharacterLevelUpScreen(
                            characterId = args.characterId,
                            onBack = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.Compendium> {
                        CompendiumScreen(
                            onSpellSelected = { controller.navigate(AppDestination.SpellDetail(it)) },
                        )
                    }
                    composable<AppDestination.SpellDetail> {
                        val args = it.toRoute<AppDestination.SpellDetail>()
                        SpellDetailScreen(
                            spellId = args.spellId,
                            onBackClick = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.Dice> {
                        DiceRollerScreen()
                    }
                }
            }
        }
    }
}

private infix fun NavDestination?.matches(destination: AppDestination): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(destination::class) }
}
