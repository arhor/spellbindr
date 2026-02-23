@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.character.sheet

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.feature.character.R
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.spells.CastSpellBottomSheetContent
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotPool
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Stateful entry point for the Character Sheet screen.
 *
 * Handles:
 * - ViewModel integration and state collection.
 * - Navigation callbacks (back, open detail, etc.).
 * - Side effects (like showing delete confirmation or handling returning from spell picker).
 * - Configuring the shared top bar.
 */
@Composable
fun CharacterSheetRoute(
    vm: CharacterSheetViewModel,
    savedStateHandle: SavedStateHandle,
    args: CharacterSheetRouteArgs,
    onOpenSpellDetail: (String) -> Unit,
    onAddSpells: (String) -> Unit,
    onOpenFullEditor: (String) -> Unit,
    onCharacterDeleted: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var overflowExpanded by remember(savedStateHandle) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(savedStateHandle) { mutableStateOf(false) }
    var showLongRestConfirmation by remember(savedStateHandle) { mutableStateOf(false) }
    var showShortRestConfirmation by remember(savedStateHandle) { mutableStateOf(false) }
    var pendingConcentrationReplacement by remember(savedStateHandle) { mutableStateOf<PendingCastRequest?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val castSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<CharacterSpellAssignment?>(
                CHARACTER_SPELL_SELECTION_RESULT_KEY,
                null,
            )
            .collectLatest { assignment ->
                if (assignment != null) {
                    vm.dispatch(CharacterSheetIntent.SpellsAssigned(listOf(assignment)))
                    savedStateHandle[CHARACTER_SPELL_SELECTION_RESULT_KEY] = null
                }
            }
    }

    LaunchedEffect(vm) {
        vm.effects.collectLatest {
            when (it) {
                CharacterSheetEffect.CharacterDeleted -> onCharacterDeleted()
            }
        }
    }

    val headerState = (state as? CharacterSheetUiState.Content)?.header
    val contentState = state as? CharacterSheetUiState.Content
    val castSpell = contentState?.castSpell
    val dispatch: CharacterSheetDispatch = { intent ->
        when (intent) {
            is CharacterSheetIntent.SpellSelected -> onOpenSpellDetail(intent.spellId)
            CharacterSheetIntent.AddSpellsClicked -> contentState?.characterId?.let(onAddSpells)
            CharacterSheetIntent.OpenFullEditorClicked -> contentState?.characterId?.let(onOpenFullEditor)
            CharacterSheetIntent.LongRestClicked -> showLongRestConfirmation = true
            CharacterSheetIntent.ShortRestClicked -> showShortRestConfirmation = true
            else -> vm.dispatch(intent)
        }
    }

    LaunchedEffect(castSpell) {
        if (castSpell == null) {
            pendingConcentrationReplacement = null
        }
    }

    fun dismissCastSheet() {
        pendingConcentrationReplacement = null
        coroutineScope
            .launch { castSheetState.hide() }
            .invokeOnCompletion { vm.dispatch(CharacterSheetIntent.CastSheetDismissed) }
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = AppTopBarConfig(
                title = headerState?.name ?: args.initialName,
                navigation = AppTopBarNavigation.Back(onBack),
                actions = {
                    val contentState = state as? CharacterSheetUiState.Content
                    CharacterSheetTopBarActions(
                        editMode = contentState?.editMode ?: SheetEditMode.View,
                        canEdit = contentState != null,
                        hasCharacter = contentState?.characterId != null,
                        onOverflowOpen = { overflowExpanded = true },
                        onEnterEdit = { dispatch(CharacterSheetIntent.EnterEditMode) },
                        onCancelEdit = { dispatch(CharacterSheetIntent.CancelEditMode) },
                        onSaveEdits = { dispatch(CharacterSheetIntent.SaveEditsClicked) },
                    )
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open full editor") },
                            onClick = {
                                overflowExpanded = false
                                dispatch(CharacterSheetIntent.OpenFullEditorClicked)
                            },
                            enabled = contentState?.characterId != null,
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete character",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                overflowExpanded = false
                                showDeleteConfirmation = true
                            },
                            enabled = contentState?.characterId != null,
                        )
                    }
                },
            ),
            overlays = {
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
                                dispatch(CharacterSheetIntent.DeleteCharacterClicked)
                            }) {
                                Text(
                                    text = "Delete",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirmation = false }) {
                                Text(text = "Cancel")
                            }
                        },
                    )
                }
                if (showLongRestConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showLongRestConfirmation = false },
                        title = { Text(text = stringResource(R.string.spells_long_rest)) },
                        text = { Text(text = stringResource(R.string.spells_long_rest_confirm_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showLongRestConfirmation = false
                                dispatch(CharacterSheetIntent.LongRestConfirmed)
                            }) {
                                Text(text = stringResource(R.string.spells_restore))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLongRestConfirmation = false }) {
                                Text(text = stringResource(R.string.spells_cancel))
                            }
                        },
                    )
                }
                if (showShortRestConfirmation) {
                    AlertDialog(
                        onDismissRequest = { showShortRestConfirmation = false },
                        title = { Text(text = stringResource(R.string.spells_short_rest_label)) },
                        text = { Text(text = stringResource(R.string.spells_short_rest_confirm_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showShortRestConfirmation = false
                                dispatch(CharacterSheetIntent.ShortRestConfirmed)
                            }) {
                                Text(text = stringResource(R.string.spells_restore))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showShortRestConfirmation = false }) {
                                Text(text = stringResource(R.string.spells_cancel))
                            }
                        },
                    )
                }

                val pendingCast = pendingConcentrationReplacement
                val currentConcentration = contentState?.spells?.concentration
                if (pendingCast != null && currentConcentration != null) {
                    AlertDialog(
                        onDismissRequest = { pendingConcentrationReplacement = null },
                        title = { Text(text = stringResource(R.string.spells_replace_concentration_title)) },
                        text = {
                            Text(
                                text = stringResource(
                                    R.string.spells_replace_concentration_message,
                                    currentConcentration.label,
                                ),
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                pendingConcentrationReplacement = null
                                coroutineScope
                                    .launch { castSheetState.hide() }
                                    .invokeOnCompletion {
                                        dispatch(
                                            CharacterSheetIntent.CastConfirmed(
                                                pool = pendingCast.pool,
                                                slotLevel = pendingCast.slotLevel,
                                                castAsRitual = pendingCast.castAsRitual,
                                            ),
                                        )
                                    }
                            }) {
                                Text(text = stringResource(R.string.spells_replace))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingConcentrationReplacement = null }) {
                                Text(text = stringResource(R.string.spells_cancel))
                            }
                        },
                    )
                }

                if (castSpell != null) {
                    ModalBottomSheet(
                        onDismissRequest = ::dismissCastSheet,
                        sheetState = castSheetState,
                    ) {
                        CastSpellBottomSheetContent(
                            castSpell = castSpell,
                            onCancel = ::dismissCastSheet,
                            onCast = { pool, slotLevel, castAsRitual ->
                                val needsConcentrationConfirm =
                                    castSpell.isConcentration &&
                                        currentConcentration != null &&
                                        currentConcentration.spellId != castSpell.spellId
                                if (needsConcentrationConfirm) {
                                    pendingConcentrationReplacement = PendingCastRequest(
                                        pool = pool,
                                        slotLevel = slotLevel,
                                        castAsRitual = castAsRitual,
                                    )
                                } else {
                                    coroutineScope
                                        .launch { castSheetState.hide() }
                                        .invokeOnCompletion {
                                            dispatch(
                                                CharacterSheetIntent.CastConfirmed(
                                                    pool = pool,
                                                    slotLevel = slotLevel,
                                                    castAsRitual = castAsRitual,
                                                ),
                                            )
                                        }
                                }
                            },
                        )
                    }
                }
            },
        ),
    ) {
        CharacterSheetScreen(
            state = state,
            dispatch = dispatch,
            modifier = modifier,
        )
    }
}

private data class PendingCastRequest(
    val pool: SpellSlotPool?,
    val slotLevel: Int?,
    val castAsRitual: Boolean,
)
