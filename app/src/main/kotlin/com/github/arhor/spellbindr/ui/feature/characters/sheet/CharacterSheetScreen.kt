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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetViewModel.CharacterSheetEffect
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetViewModel.CharacterSheetUiAction
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetError
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarTitle
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.WeaponCatalogDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetCallbacks
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.navigation.AppDestination
import com.github.arhor.spellbindr.ui.navigation.CHARACTER_SPELL_SELECTION_RESULT_KEY
import com.github.arhor.spellbindr.ui.theme.AppTheme
import kotlinx.coroutines.flow.collectLatest

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

    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            .getStateFlow<List<CharacterSpellAssignment>?>(
                CHARACTER_SPELL_SELECTION_RESULT_KEY,
                null,
            )
            .collectLatest { assignments ->
                if (!assignments.isNullOrEmpty()) {
                    vm.onAction(CharacterSheetUiAction.AddSpells(assignments))
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

    val callbacks = CharacterSheetCallbacks(
        onTabSelected = { vm.onAction(CharacterSheetUiAction.TabSelected(it)) },
        onEnterEdit = { vm.onAction(CharacterSheetUiAction.EnterEdit) },
        onCancelEdit = { vm.onAction(CharacterSheetUiAction.CancelEdit) },
        onSaveEdits = { vm.onAction(CharacterSheetUiAction.SaveInlineEdits) },
        onAdjustHp = { vm.onAction(CharacterSheetUiAction.AdjustCurrentHp(it)) },
        onTempHpChanged = { vm.onAction(CharacterSheetUiAction.TempHpChanged(it)) },
        onMaxHpEdited = { vm.onAction(CharacterSheetUiAction.MaxHpEdited(it)) },
        onCurrentHpEdited = { vm.onAction(CharacterSheetUiAction.CurrentHpEdited(it)) },
        onTempHpEdited = { vm.onAction(CharacterSheetUiAction.TemporaryHpEdited(it)) },
        onSpeedEdited = { vm.onAction(CharacterSheetUiAction.SpeedEdited(it)) },
        onHitDiceEdited = { vm.onAction(CharacterSheetUiAction.HitDiceEdited(it)) },
        onSensesEdited = { vm.onAction(CharacterSheetUiAction.SensesEdited(it)) },
        onLanguagesEdited = { vm.onAction(CharacterSheetUiAction.LanguagesEdited(it)) },
        onProficienciesEdited = { vm.onAction(CharacterSheetUiAction.ProficienciesEdited(it)) },
        onEquipmentEdited = { vm.onAction(CharacterSheetUiAction.EquipmentEdited(it)) },
        onDeathSaveSuccessesChanged = { vm.onAction(CharacterSheetUiAction.DeathSaveSuccessesChanged(it)) },
        onDeathSaveFailuresChanged = { vm.onAction(CharacterSheetUiAction.DeathSaveFailuresChanged(it)) },
        onSpellSlotToggle = { level, index -> vm.onAction(CharacterSheetUiAction.SpellSlotToggled(level, index)) },
        onSpellSlotTotalChanged = { level, total ->
            vm.onAction(
                CharacterSheetUiAction.SpellSlotTotalChanged(
                    level,
                    total
                )
            )
        },
        onSpellRemoved = { spellId, sourceClass ->
            vm.onAction(
                CharacterSheetUiAction.SpellRemoved(
                    spellId,
                    sourceClass
                )
            )
        },
        onSpellSelected = onOpenSpellDetail,
        onAddSpellsClicked = { state.characterId?.let(onAddSpells) },
        onAddWeaponClicked = { vm.onAction(CharacterSheetUiAction.AddWeaponClicked) },
        onWeaponSelected = { vm.onAction(CharacterSheetUiAction.WeaponSelected(it)) },
        onWeaponDeleted = { vm.onAction(CharacterSheetUiAction.WeaponDeleted(it)) },
        onWeaponEditorDismissed = { vm.onAction(CharacterSheetUiAction.WeaponEditorDismissed) },
        onWeaponNameChanged = { vm.onAction(CharacterSheetUiAction.WeaponNameChanged(it)) },
        onWeaponAbilityChanged = { vm.onAction(CharacterSheetUiAction.WeaponAbilityChanged(it)) },
        onWeaponUseAbilityForDamageChanged = { vm.onAction(CharacterSheetUiAction.WeaponUseAbilityForDamageChanged(it)) },
        onWeaponProficiencyChanged = { vm.onAction(CharacterSheetUiAction.WeaponProficiencyChanged(it)) },
        onWeaponDiceCountChanged = { vm.onAction(CharacterSheetUiAction.WeaponDiceCountChanged(it)) },
        onWeaponDieSizeChanged = { vm.onAction(CharacterSheetUiAction.WeaponDieSizeChanged(it)) },
        onWeaponDamageTypeChanged = { vm.onAction(CharacterSheetUiAction.WeaponDamageTypeChanged(it)) },
        onWeaponSaved = { vm.onAction(CharacterSheetUiAction.WeaponSaved) },
        onWeaponCatalogOpened = { vm.onAction(CharacterSheetUiAction.WeaponCatalogOpened) },
        onWeaponCatalogClosed = { vm.onAction(CharacterSheetUiAction.WeaponCatalogClosed) },
        onWeaponCatalogItemSelected = { vm.onAction(CharacterSheetUiAction.WeaponCatalogItemSelected(it)) },
        onOpenFullEditor = { state.characterId?.let(onOpenFullEditor) },
        onDeleteCharacter = { vm.onAction(CharacterSheetUiAction.DeleteCharacter) },
    )

    val headerState = (state as? CharacterSheetUiState.Content)?.header

    val config = AppTopBarConfig(
        visible = true,
        title = {
            CharacterSheetTopBarTitle(
                name = headerState?.name ?: args.initialName,
                subtitle = headerState?.subtitle ?: args.initialSubtitle,
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

/**
 * Pure UI composable for the character sheet.
 * Renders Loading, Error, or Content states.
 */
@Composable
private fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is CharacterSheetUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is CharacterSheetUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CharacterSheetError(message = state.message)
            }
        }

        is CharacterSheetUiState.Content -> {
            if (state.errorMessage != null) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CharacterSheetError(message = state.errorMessage)
                }
            } else {
                CharacterSheetContent(
                    state = state,
                    header = state.header,
                    callbacks = callbacks,
                    modifier = modifier,
                )
                state.weaponEditorState?.let { editor ->
                    WeaponEditorDialog(
                        editorState = editor,
                        onDismiss = callbacks.onWeaponEditorDismissed,
                        onNameChange = callbacks.onWeaponNameChanged,
                        onCatalogOpen = callbacks.onWeaponCatalogOpened,
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
                if (state.isWeaponCatalogVisible) {
                    WeaponCatalogDialog(
                        catalog = state.weaponCatalog,
                        onDismiss = callbacks.onWeaponCatalogClosed,
                        onItemSelected = callbacks.onWeaponCatalogItemSelected,
                        isLoading = state.weaponCatalog.isEmpty(),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CharacterSheetScreenPreview() {
    AppTheme {
        CharacterSheetScreen(
            state = CharacterSheetPreviewData.uiState,
            callbacks = CharacterSheetCallbacks(),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
