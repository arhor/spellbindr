package com.github.arhor.spellbindr.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.arhor.spellbindr.ui.components.AppBottomBar
import com.github.arhor.spellbindr.ui.components.AppTopBar
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorViewModel
import com.github.arhor.spellbindr.ui.feature.characters.EditorMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetViewModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarTitle
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.compendium.spells.details.SpellDetailsViewModel
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.navigation.SpellbindrAppNavGraph
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
fun SpellbindrApp(
    onLoaded: () -> Unit,
    vm: SpellbindrAppViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val controller = rememberNavController()

    LaunchedEffect(state.ready) {
        if (state.ready) {
            onLoaded()
        }
    }

    val topBarState = rememberTopBarState(controller)

    AppTheme(isDarkTheme = state.isDarkTheme) {
        Scaffold(
            topBar = { AppTopBar(topBarState.config) },
            bottomBar = { AppBottomBar(controller) },
        ) { innerPadding ->
            SpellbindrAppNavGraph(
                controller = controller,
                innerPadding = innerPadding,
            )
            topBarState.overlays()
        }
    }
}

private data class TopBarState(
    val config: AppTopBarConfig = AppTopBarConfig.None,
    val overlays: @Composable () -> Unit = {},
)

@Composable
private fun rememberTopBarState(controller: NavHostController): TopBarState {
    val backStackEntry by controller.currentBackStackEntryAsState()
    val entry = backStackEntry
    val destination = entry?.destination

    return when {
        destination matches AppDestination.CharactersHome::class -> TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Characters") },
            ),
        )

        destination matches AppDestination.Compendium::class -> TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Compendium") },
            ),
        )

        destination matches AppDestination.Dice::class -> TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Dice Roller") },
                actions = {
                    IconButton(onClick = { /* Stub: future history action */ }) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "History",
                        )
                    }
                },
            ),
        )

        destination matches AppDestination.Settings::class -> TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Settings") },
            ),
        )

        destination matches AppDestination.CharacterSpellPicker::class -> TopBarState(
            config = AppTopBarConfig(
                visible = true,
                title = { Text(text = "Add Spells") },
                navigation = AppTopBarNavigation.Back(controller::navigateUp),
            ),
        )

        destination matches AppDestination.SpellDetail::class && entry != null -> {
            spellDetailTopBarState(controller, entry)
        }

        destination matches AppDestination.CharacterEditor::class && entry != null -> {
            characterEditorTopBarState(controller, entry)
        }

        destination matches AppDestination.CharacterSheet::class && entry != null -> {
            characterSheetTopBarState(controller, entry)
        }

        else -> TopBarState()
    }
}

@Composable
private fun characterEditorTopBarState(
    controller: NavHostController,
    backStackEntry: NavBackStackEntry,
): TopBarState {
    val vm: CharacterEditorViewModel = hiltViewModel(backStackEntry)
    val state by vm.uiState.collectAsState()

    val title = if (state.mode == EditorMode.Create) "New Character" else "Edit Character"

    return TopBarState(
        config = AppTopBarConfig(
            visible = true,
            title = { Text(text = title) },
            navigation = AppTopBarNavigation.Back(controller::navigateUp),
            actions = {
                TextButton(
                    onClick = vm::onSaveClicked,
                    enabled = !state.isSaving && !state.isLoading,
                ) {
                    Text("Save")
                }
            },
        ),
    )
}

@Composable
private fun characterSheetTopBarState(
    controller: NavHostController,
    backStackEntry: NavBackStackEntry,
): TopBarState {
    val vm: CharacterSheetViewModel = hiltViewModel(backStackEntry)
    val state by vm.uiState.collectAsState()
    val args = backStackEntry.toRoute<AppDestination.CharacterSheet>()
    var overflowExpanded by remember(backStackEntry.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(backStackEntry.id) { mutableStateOf(false) }

    val callbacks = CharacterSheetCallbacks(
        onEnterEdit = vm::enterEditMode,
        onCancelEdit = vm::cancelEditMode,
        onSaveEdits = vm::saveInlineEdits,
        onOpenFullEditor = {
            state.characterId?.let { characterId ->
                controller.navigate(AppDestination.CharacterEditor(characterId))
            }
        },
        onDeleteCharacter = {
            vm.deleteCharacter {
                controller.navigateUp()
            }
        },
    )

    val config = AppTopBarConfig(
        visible = true,
        title = {
            CharacterSheetTopBarTitle(
                name = state.header?.name ?: args.initialName,
                subtitle = state.header?.subtitle ?: args.initialSubtitle,
            )
        },
        navigation = AppTopBarNavigation.Back(controller::navigateUp),
        actions = {
            CharacterSheetTopBarActions(
                state = state,
                callbacks = callbacks,
                onOverflowOpen = { overflowExpanded = true },
            )
            DropdownMenu(
                expanded = overflowExpanded,
                onDismissRequest = { overflowExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Open full editor") },
                    onClick = {
                        overflowExpanded = false
                        callbacks.onOpenFullEditor()
                    },
                    enabled = state.characterId != null,
                )
                DropdownMenuItem(
                    text = { Text("Delete character", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        overflowExpanded = false
                        showDeleteConfirmation = true
                    },
                    enabled = state.characterId != null,
                )
            }
        },
    )

    val overlays: @Composable () -> Unit = {
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(text = "Delete character") },
                text = {
                    Text(
                        text = "This will permanently remove the character and all of its data. This action cannot be undone.",
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirmation = false
                        callbacks.onDeleteCharacter()
                    }) {
                        Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text(text = "Cancel")
                    }
                },
            )
        }
    }

    return TopBarState(
        config = config,
        overlays = overlays,
    )
}

@Composable
private fun spellDetailTopBarState(
    controller: NavHostController,
    backStackEntry: NavBackStackEntry,
): TopBarState {
    val vm: SpellDetailsViewModel = hiltViewModel(backStackEntry)
    val state by vm.state.collectAsState()
    val args = backStackEntry.toRoute<AppDestination.SpellDetail>()

    return TopBarState(
        config = AppTopBarConfig(
            visible = true,
            title = { Text(state.spell?.name ?: args.initialName ?: "Spell Details") },
            navigation = AppTopBarNavigation.Back(controller::navigateUp),
            actions = {
                IconButton(
                    onClick = vm::toggleFavorite,
                    enabled = state.spell != null,
                ) {
                    if (state.isFavorite) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Remove from favorites",
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Add to favorites",
                        )
                    }
                }
            },
        ),
    )
}

private infix fun NavDestination?.matches(destination: kotlin.reflect.KClass<out AppDestination>): Boolean =
    when (this) {
        null -> false
        else -> hierarchy.any { it.hasRoute(destination) }
}
