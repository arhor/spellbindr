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
import com.github.arhor.spellbindr.ui.AppTopBarConfig
import com.github.arhor.spellbindr.ui.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.WithAppTopBar
import com.github.arhor.spellbindr.ui.feature.characters.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.feature.characters.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetError
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarTitle
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CharacterSheetRoute(
    viewModel: CharacterSheetViewModel,
    savedStateHandle: SavedStateHandle,
    onBack: () -> Unit,
    onOpenSpellDetail: (String) -> Unit,
    onAddSpells: (String) -> Unit,
    onOpenFullEditor: (String) -> Unit,
    onCharacterDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<List<CharacterSpellAssignment>?>(
                CHARACTER_SPELL_SELECTION_RESULT_KEY,
                null,
            )
            .collectLatest { assignments ->
                if (!assignments.isNullOrEmpty()) {
                    viewModel.addSpells(assignments)
                    savedStateHandle[CHARACTER_SPELL_SELECTION_RESULT_KEY] = null
                }
            }
    }

    val callbacks = CharacterSheetCallbacks(
        onTabSelected = viewModel::onTabSelected,
        onEnterEdit = viewModel::enterEditMode,
        onCancelEdit = viewModel::cancelEditMode,
        onSaveEdits = viewModel::saveInlineEdits,
        onAdjustHp = viewModel::adjustCurrentHp,
        onTempHpChanged = viewModel::setTemporaryHp,
        onMaxHpEdited = viewModel::onMaxHpEdited,
        onCurrentHpEdited = viewModel::onCurrentHpEdited,
        onTempHpEdited = viewModel::onTemporaryHpEdited,
        onSpeedEdited = viewModel::onSpeedEdited,
        onHitDiceEdited = viewModel::onHitDiceEdited,
        onSensesEdited = viewModel::onSensesEdited,
        onLanguagesEdited = viewModel::onLanguagesEdited,
        onProficienciesEdited = viewModel::onProficienciesEdited,
        onEquipmentEdited = viewModel::onEquipmentEdited,
        onDeathSaveSuccessesChanged = viewModel::setDeathSaveSuccesses,
        onDeathSaveFailuresChanged = viewModel::setDeathSaveFailures,
        onSpellSlotToggle = viewModel::toggleSpellSlot,
        onSpellSlotTotalChanged = viewModel::setSpellSlotTotal,
        onSpellRemoved = viewModel::removeSpell,
        onSpellSelected = onOpenSpellDetail,
        onAddSpellsClicked = { state.characterId?.let(onAddSpells) },
        onAddWeaponClicked = viewModel::onAddWeaponClicked,
        onWeaponSelected = viewModel::onWeaponSelected,
        onWeaponDeleted = viewModel::onWeaponDeleted,
        onWeaponEditorDismissed = viewModel::onWeaponEditorDismissed,
        onWeaponNameChanged = viewModel::onWeaponNameChanged,
        onWeaponAttackAbilityChanged = viewModel::onWeaponAttackAbilityChanged,
        onWeaponDamageAbilityChanged = viewModel::onWeaponDamageAbilityChanged,
        onWeaponProficiencyChanged = viewModel::onWeaponProficiencyChanged,
        onWeaponDiceCountChanged = viewModel::onWeaponDiceCountChanged,
        onWeaponDieSizeChanged = viewModel::onWeaponDieSizeChanged,
        onWeaponDamageTypeChanged = viewModel::onWeaponDamageTypeChanged,
        onWeaponSaved = viewModel::onWeaponSaved,
        onOpenFullEditor = { state.characterId?.let(onOpenFullEditor) },
        onDeleteCharacter = { viewModel.deleteCharacter(onCharacterDeleted) },
    )

    CharacterSheetScreen(
        state = state,
        onBack = onBack,
        callbacks = callbacks,
        modifier = modifier,
    )
}

@Composable
private fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    onBack: () -> Unit,
    callbacks: CharacterSheetCallbacks = CharacterSheetCallbacks(),
    modifier: Modifier = Modifier,
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val headerState = state.header

    WithAppTopBar(
        AppTopBarConfig(
            visible = true,
            title = { CharacterSheetTopBarTitle(header = headerState) },
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
                            onAttackAbilityChange = callbacks.onWeaponAttackAbilityChanged,
                            onDamageAbilityChange = callbacks.onWeaponDamageAbilityChanged,
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

@Preview(showBackground = true)
@Composable
private fun CharacterSheetPreview() {
    AppTheme(isDarkTheme = false) {
        Box {
            CharacterSheetScreen(
                state = CharacterSheetPreviewData.uiState,
                onBack = {},
            )
        }
    }
}
