@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.navigation.AppDestination
import com.github.arhor.spellbindr.navigation.BottomNavItems
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharactersListRoute
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumScreen
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailScreen
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerScreen
import com.github.arhor.spellbindr.ui.theme.AppTheme

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

    AppTheme {
        AppTopBarControllerProvider { config ->
            Scaffold(
                topBar = createTopBar(config),
                bottomBar = createBottomBar(controller),
            ) { innerPadding ->
                NavHost(
                    navController = controller,
                    startDestination = AppDestination.CharactersHome,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable<AppDestination.CharactersHome> {
                        CharactersListRoute(
                            onCharacterSelected = { characterId ->
                                controller.navigate(
                                    AppDestination.CharacterSheet(characterId = characterId)
                                )
                            },
                            onCreateCharacter = { controller.navigate(AppDestination.CharacterEditor()) },
                        )
                    }
                    composable<AppDestination.CharacterSheet> {
                        CharacterSheetRoute(
                            onBack = { controller.navigateUp() },
                            onEditCharacter = { controller.navigate(AppDestination.CharacterEditor(characterId = it)) },
                        )
                    }
                    composable<AppDestination.CharacterEditor> {
                        CharacterEditorRoute(
                            onFinished = { controller.navigateUp() },
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

private fun createTopBar(config: AppTopBarConfig): @Composable () -> Unit = {
    if (config.visible) {
        TopAppBar(
            title = config.title,
            navigationIcon = config.navigation.asNavigationIcon(),
            actions = config.actions,
        )
    }
}

private fun createBottomBar(controller: NavHostController): @Composable () -> Unit = {
    val backStackEntry by controller.currentBackStackEntryAsState()

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
}

private infix fun NavDestination?.matches(destination: AppDestination): Boolean = when (this) {
    null -> false
    else -> hierarchy.any { it.hasRoute(destination::class) }
}
