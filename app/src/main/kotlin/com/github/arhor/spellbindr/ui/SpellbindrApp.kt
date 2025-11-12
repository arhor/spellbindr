package com.github.arhor.spellbindr.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellbindrApp(
    onLoaded: () -> Unit,
    viewModel: SpellbindrAppViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val controller = rememberNavController()
    val backStackEntry by controller.currentBackStackEntryAsState()
    val topBarController = rememberAppTopBarController()
    val topBarConfig by topBarController.config

    LaunchedEffect(state.ready) {
        if (state.ready) {
            onLoaded()
        }
    }

    AppTheme {
        CompositionLocalProvider(LocalAppTopBarController provides topBarController) {
            Scaffold(
                topBar = { AppTopBar(topBarConfig) },
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
                    composable<AppDestination.Library> {
                        LibraryScreen(
                            onSpellSelected = { controller.navigate(AppDestination.SpellDetail(it)) },
                            onMonsterSelected = { controller.navigate(AppDestination.MonsterDetail(it)) },
                            onRuleSelected = { controller.navigate(AppDestination.RuleDetail(it)) },
                        )
                    }
                    composable<AppDestination.SpellDetail> {
                        val args = it.toRoute<AppDestination.SpellDetail>()
                        SpellDetailScreen(
                            spellId = args.spellId,
                            onBack = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.MonsterDetail> {
                        val args = it.toRoute<AppDestination.MonsterDetail>()
                        MonsterDetailScreen(
                            monsterId = args.monsterId,
                            onBack = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.RuleDetail> {
                        val args = it.toRoute<AppDestination.RuleDetail>()
                        RuleDetailScreen(
                            ruleId = args.ruleId,
                            onBack = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.Dice> {
                        DiceScreen()
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
