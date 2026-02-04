@file:OptIn(ExperimentalMaterial3Api::class)

package com.github.arhor.spellbindr.ui.feature.characters.sheet

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
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetViewModel.CharacterSheetEffect
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells.CastSpellBottomSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellSlotPool
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.navigation.CHARACTER_SPELL_SELECTION_RESULT_KEY
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
    args: AppDestination.CharacterSheet,
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
                    vm.addSpells(listOf(assignment))
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

    LaunchedEffect(castSpell) {
        if (castSpell == null) {
            pendingConcentrationReplacement = null
        }
    }

    fun dismissCastSheet() {
        pendingConcentrationReplacement = null
        coroutineScope
            .launch { castSheetState.hide() }
            .invokeOnCompletion { vm.dismissCasting() }
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
                        onEnterEdit = vm::enterEditMode,
                        onCancelEdit = vm::cancelEditMode,
                        onSaveEdits = vm::saveInlineEdits,
                    )
                    DropdownMenu(
                        expanded = overflowExpanded,
                        onDismissRequest = { overflowExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open full editor") },
                            onClick = {
                                overflowExpanded = false
                                contentState?.characterId?.let(onOpenFullEditor)
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
                                vm.deleteCharacter()
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
                                vm.longRest()
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
                                vm.shortRest()
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
                                        vm.confirmCast(
                                            pool = pendingCast.pool,
                                            slotLevel = pendingCast.slotLevel,
                                            castAsRitual = pendingCast.castAsRitual,
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
                                            vm.confirmCast(
                                                pool = pool,
                                                slotLevel = slotLevel,
                                                castAsRitual = castAsRitual,
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
            onTabSelected = vm::selectTab,
            onAddSpellsClick = { (state as? CharacterSheetUiState.Content)?.characterId?.let(onAddSpells) },
            onSpellSelected = onOpenSpellDetail,
            onSpellRemoved = vm::removeSpell,
            onCastSpellClick = vm::startCasting,
            onLongRestClick = { showLongRestConfirmation = true },
            onShortRestClick = { showShortRestConfirmation = true },
            onConfigureSlotsClick = vm::enterEditMode,
            onSpellSlotToggle = vm::toggleSpellSlot,
            onSpellSlotTotalChanged = vm::setSpellSlotTotal,
            onPactSlotToggle = vm::togglePactSlot,
            onPactSlotTotalChanged = vm::setPactSlotTotal,
            onPactSlotLevelChanged = vm::setPactSlotLevel,
            onConcentrationClear = vm::clearConcentration,
            onAddWeaponClick = vm::startNewWeapon,
            onWeaponSelected = vm::selectWeapon,
            onWeaponDeleted = vm::deleteWeapon,
            onWeaponEditorDismissed = vm::dismissWeaponEditor,
            onWeaponNameChanged = vm::setWeaponName,
            onWeaponAbilityChanged = vm::setWeaponAbility,
            onWeaponUseAbilityForDamageChanged = vm::setWeaponUseAbilityForDamage,
            onWeaponProficiencyChanged = vm::setWeaponProficiency,
            onWeaponDiceCountChanged = vm::setWeaponDiceCount,
            onWeaponDieSizeChanged = vm::setWeaponDieSize,
            onWeaponDamageTypeChanged = vm::setWeaponDamageType,
            onWeaponSaved = vm::saveWeapon,
            onWeaponCatalogOpened = vm::openWeaponCatalog,
            onWeaponCatalogClosed = vm::closeWeaponCatalog,
            onWeaponCatalogItemSelected = vm::selectWeaponFromCatalog,
            modifier = modifier,
        )
    }
}

private data class PendingCastRequest(
    val pool: SpellSlotPool?,
    val slotLevel: Int?,
    val castAsRitual: Boolean,
)
