package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.github.arhor.spellbindr.dice.DiceScreen
import com.github.arhor.spellbindr.library.LibraryScreen
import com.github.arhor.spellbindr.library.MonsterDetailScreen
import com.github.arhor.spellbindr.library.RuleDetailScreen
import com.github.arhor.spellbindr.library.SpellDetailScreen
import com.github.arhor.spellbindr.navigation.AppDestination
import com.github.arhor.spellbindr.navigation.BottomNavItems
import com.github.arhor.spellbindr.navigation.CharacterCreateDestination
import com.github.arhor.spellbindr.navigation.CharacterLevelUpDestination
import com.github.arhor.spellbindr.navigation.CharacterSheetDestination
import com.github.arhor.spellbindr.navigation.CharactersHomeDestination
import com.github.arhor.spellbindr.navigation.DiceDestination
import com.github.arhor.spellbindr.navigation.LibraryDestination
import com.github.arhor.spellbindr.navigation.MonsterDetailDestination
import com.github.arhor.spellbindr.navigation.RuleDetailDestination
import com.github.arhor.spellbindr.navigation.SpellDetailDestination
import com.github.arhor.spellbindr.ui.theme.AppTheme

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
        Scaffold(
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
                startDestination = CharactersHomeDestination,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable<CharactersHomeDestination> {
                    CharactersListScreen(
                        onCharacterSelected = { summary ->
                            controller.navigate(
                                CharacterSheetDestination(characterId = summary.id)
                            )
                        },
                        onCreateCharacter = { controller.navigate(CharacterCreateDestination) },
                    )
                }
                composable<CharacterSheetDestination> {
                    val args = it.toRoute<CharacterSheetDestination>()
                    CharacterSheetScreen(
                        characterId = args.characterId,
                        onBack = { controller.navigateUp() },
                        onLevelUp = { characterId ->
                            controller.navigate(
                                CharacterLevelUpDestination(characterId = characterId)
                            )
                        },
                    )
                }
                composable<CharacterCreateDestination> {
                    CharacterCreationScreen(onBack = { controller.navigateUp() })
                }
                composable<CharacterLevelUpDestination> {
                    val args = it.toRoute<CharacterLevelUpDestination>()
                    CharacterLevelUpScreen(
                        characterId = args.characterId,
                        onBack = { controller.navigateUp() },
                    )
                }
                composable<LibraryDestination> {
                    LibraryScreen(
                        onSpellSelected = { controller.navigate(SpellDetailDestination(it)) },
                        onMonsterSelected = { controller.navigate(MonsterDetailDestination(it)) },
                        onRuleSelected = { controller.navigate(RuleDetailDestination(it)) },
                    )
                }
                composable<SpellDetailDestination> {
                    val args = it.toRoute<SpellDetailDestination>()
                    SpellDetailScreen(
                        spellId = args.spellId,
                        onBack = { controller.navigateUp() },
                    )
                }
                composable<MonsterDetailDestination> {
                    val args = it.toRoute<MonsterDetailDestination>()
                    MonsterDetailScreen(
                        monsterId = args.monsterId,
                        onBack = { controller.navigateUp() },
                    )
                }
                composable<RuleDetailDestination> {
                    val args = it.toRoute<RuleDetailDestination>()
                    RuleDetailScreen(
                        ruleId = args.ruleId,
                        onBack = { controller.navigateUp() },
                    )
                }
                composable<DiceDestination> {
                    DiceScreen()
                }
            }
        }
    }
}

private infix fun NavDestination?.matches(destination: AppDestination): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(destination::class) }
}
