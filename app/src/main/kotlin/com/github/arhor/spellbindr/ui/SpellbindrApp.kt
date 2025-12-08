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
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorViewModel
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellPickerRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellPickerViewModel
import com.github.arhor.spellbindr.ui.feature.characters.CharactersListRoute
import com.github.arhor.spellbindr.ui.feature.characters.CharactersListViewModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetRoute
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.CompendiumRoute
import com.github.arhor.spellbindr.ui.feature.compendium.alignments.AlignmentsViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.conditions.ConditionsViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.races.RacesViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailRoute
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailsViewModel
import com.github.arhor.spellbindr.ui.feature.compendium.spells.search.SpellSearchViewModel
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerRoute
import com.github.arhor.spellbindr.ui.feature.dice.DiceRollerViewModel
import com.github.arhor.spellbindr.ui.feature.settings.SettingsScreen
import com.github.arhor.spellbindr.ui.feature.settings.SettingsViewModel
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

    AppTheme(isDarkTheme = state.isDarkTheme) {
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
                    composable<AppDestination.CharactersHome> { entry ->
                        val viewModel: CharactersListViewModel = hiltViewModel(entry)
                        val uiState by viewModel.uiState.collectAsState()

                        CharactersListRoute(
                            uiState = uiState,
                            onCharacterSelected = { characterId ->
                                controller.navigate(
                                    AppDestination.CharacterSheet(characterId = characterId)
                                )
                            },
                            onCreateCharacter = { controller.navigate(AppDestination.CharacterEditor()) },
                        )
                    }
                    composable<AppDestination.CharacterSheet> { entry ->
                        val viewModel: CharacterSheetViewModel = hiltViewModel(entry)

                        CharacterSheetRoute(
                            viewModel = viewModel,
                            savedStateHandle = entry.savedStateHandle,
                            onBack = { controller.navigateUp() },
                            onOpenSpellDetail = { controller.navigate(AppDestination.SpellDetail(it)) },
                            onAddSpells = { controller.navigate(AppDestination.CharacterSpellPicker(characterId = it)) },
                            onOpenFullEditor = { controller.navigate(AppDestination.CharacterEditor(characterId = it)) },
                            onCharacterDeleted = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.CharacterEditor> { entry ->
                        val viewModel: CharacterEditorViewModel = hiltViewModel(entry)

                        CharacterEditorRoute(
                            viewModel = viewModel,
                            onBack = { controller.navigateUp() },
                            onFinished = { controller.navigateUp() },
                        )
                    }
                    composable<AppDestination.Compendium> { entry ->
                        val spellSearchViewModel: SpellSearchViewModel = hiltViewModel(entry)
                        val conditionsViewModel: ConditionsViewModel = hiltViewModel(entry)
                        val alignmentsViewModel: AlignmentsViewModel = hiltViewModel(entry)
                        val racesViewModel: RacesViewModel = hiltViewModel(entry)

                        CompendiumRoute(
                            spellSearchViewModel = spellSearchViewModel,
                            conditionsViewModel = conditionsViewModel,
                            alignmentsViewModel = alignmentsViewModel,
                            racesViewModel = racesViewModel,
                            onSpellSelected = { controller.navigate(AppDestination.SpellDetail(it)) },
                        )
                    }
                    composable<AppDestination.SpellDetail> { entry ->
                        val args = entry.toRoute<AppDestination.SpellDetail>()
                        val viewModel: SpellDetailsViewModel = hiltViewModel(entry)
                        val state by viewModel.state.collectAsState()

                        LaunchedEffect(args.spellId) {
                            viewModel.loadSpell(args.spellId)
                        }

                        SpellDetailRoute(
                            state = state,
                            onBackClick = { controller.navigateUp() },
                            onToggleFavorite = viewModel::toggleFavorite,
                        )
                    }
                    composable<AppDestination.CharacterSpellPicker> { entry ->
                        val viewModel: CharacterSpellPickerViewModel = hiltViewModel(entry)
                        val spellSearchViewModel: SpellSearchViewModel = hiltViewModel(entry)

                        CharacterSpellPickerRoute(
                            viewModel = viewModel,
                            spellSearchViewModel = spellSearchViewModel,
                            onBack = { controller.navigateUp() },
                            onSpellSelected = { assignments ->
                                controller.previousBackStackEntry?.savedStateHandle?.set(
                                    CHARACTER_SPELL_SELECTION_RESULT_KEY,
                                    ArrayList(assignments),
                                )
                                controller.navigateUp()
                            },
                        )
                    }
                    composable<AppDestination.Dice> { entry ->
                        val viewModel: DiceRollerViewModel = hiltViewModel(entry)
                        val state by viewModel.state.collectAsState()

                        DiceRollerRoute(
                            state = state,
                            onIntent = viewModel::onIntent,
                        )
                    }
                    composable<AppDestination.Settings> {
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        val settingsState by settingsViewModel.state.collectAsState()

                        SettingsScreen(
                            state = settingsState,
                            onThemeSelected = settingsViewModel::onThemeModeSelected,
                        )
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
