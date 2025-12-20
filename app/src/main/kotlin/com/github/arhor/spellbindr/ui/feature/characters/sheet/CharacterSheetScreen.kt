package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarTitle
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetError
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

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
    val state by vm.uiState.collectAsState()
    var overflowExpanded by remember(savedStateHandle) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(savedStateHandle) { mutableStateOf(false) }

    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<List<CharacterSpellAssignment>?>(
                CHARACTER_SPELL_SELECTION_RESULT_KEY,
                null,
            )
            .collectLatest { assignments ->
                if (!assignments.isNullOrEmpty()) {
                    vm.addSpells(assignments)
                    savedStateHandle[CHARACTER_SPELL_SELECTION_RESULT_KEY] = null
                }
            }
    }

    val callbacks = CharacterSheetCallbacks(
        onTabSelected = vm::onTabSelected,
        onEnterEdit = vm::enterEditMode,
        onCancelEdit = vm::cancelEditMode,
        onSaveEdits = vm::saveInlineEdits,
        onAdjustHp = vm::adjustCurrentHp,
        onTempHpChanged = vm::setTemporaryHp,
        onMaxHpEdited = vm::onMaxHpEdited,
        onCurrentHpEdited = vm::onCurrentHpEdited,
        onTempHpEdited = vm::onTemporaryHpEdited,
        onSpeedEdited = vm::onSpeedEdited,
        onHitDiceEdited = vm::onHitDiceEdited,
        onSensesEdited = vm::onSensesEdited,
        onLanguagesEdited = vm::onLanguagesEdited,
        onProficienciesEdited = vm::onProficienciesEdited,
        onEquipmentEdited = vm::onEquipmentEdited,
        onDeathSaveSuccessesChanged = vm::setDeathSaveSuccesses,
        onDeathSaveFailuresChanged = vm::setDeathSaveFailures,
        onSpellSlotToggle = vm::toggleSpellSlot,
        onSpellSlotTotalChanged = vm::setSpellSlotTotal,
        onSpellRemoved = vm::removeSpell,
        onSpellSelected = onOpenSpellDetail,
        onAddSpellsClicked = { state.characterId?.let(onAddSpells) },
        onAddWeaponClicked = vm::onAddWeaponClicked,
        onWeaponSelected = vm::onWeaponSelected,
        onWeaponDeleted = vm::onWeaponDeleted,
        onWeaponEditorDismissed = vm::onWeaponEditorDismissed,
        onWeaponNameChanged = vm::onWeaponNameChanged,
        onWeaponAbilityChanged = vm::onWeaponAbilityChanged,
        onWeaponUseAbilityForDamageChanged = vm::onWeaponUseAbilityForDamageChanged,
        onWeaponProficiencyChanged = vm::onWeaponProficiencyChanged,
        onWeaponDiceCountChanged = vm::onWeaponDiceCountChanged,
        onWeaponDieSizeChanged = vm::onWeaponDieSizeChanged,
        onWeaponDamageTypeChanged = vm::onWeaponDamageTypeChanged,
        onWeaponSaved = vm::onWeaponSaved,
        onOpenFullEditor = { state.characterId?.let(onOpenFullEditor) },
        onDeleteCharacter = { vm.deleteCharacter(onCharacterDeleted) },
    )

    val config = AppTopBarConfig(
        visible = true,
        title = {
            CharacterSheetTopBarTitle(
                name = state.header?.name ?: args.initialName,
                subtitle = state.header?.subtitle ?: args.initialSubtitle,
            )
        },
        navigation = AppTopBarNavigation.Back(onBack),
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
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = config,
            overlays = overlays,
        ),
    ) {
        CharacterSheetScreen(
            state = state,
            callbacks = callbacks,
            modifier = modifier,
        )
    }
}

@Composable
private fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    callbacks: CharacterSheetCallbacks = CharacterSheetCallbacks(),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.errorMessage != null -> {
                CharacterSheetError(
                    message = state.errorMessage,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                )
            }

            state.header != null &&
                state.overview != null &&
                state.skills != null &&
                state.spells != null &&
                state.weapons != null -> {
                CharacterSheetContent(
                    header = state.header,
                    state = state,
                    callbacks = callbacks,
                    modifier = Modifier.fillMaxSize(),
                )
                state.weaponEditorState?.let { editorState ->
                    WeaponEditorDialog(
                        editorState = editorState,
                        onDismiss = callbacks.onWeaponEditorDismissed,
                        onNameChange = callbacks.onWeaponNameChanged,
                        onAbilityChange = callbacks.onWeaponAbilityChanged,
                        onUseAbilityForDamageChange = callbacks.onWeaponUseAbilityForDamageChanged,
                        onProficiencyChange = callbacks.onWeaponProficiencyChanged,
                        onDiceCountChange = callbacks.onWeaponDiceCountChanged,
                        onDieSizeChange = callbacks.onWeaponDieSizeChanged,
                        onDamageTypeChange = callbacks.onWeaponDamageTypeChanged,
                        onDelete = callbacks.onWeaponDeleted,
                        onSave = callbacks.onWeaponSaved,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CharacterSheetPreview() {
    AppTheme(isDarkTheme = false) {
        Box {
            CharacterSheetScreen(
                state = CharacterSheetPreviewData.uiState,
            )
        }
    }
}
