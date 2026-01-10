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
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.CharacterSpellAssignment
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.ui.components.AppTopBarConfig
import com.github.arhor.spellbindr.ui.components.AppTopBarNavigation
import com.github.arhor.spellbindr.ui.components.ProvideTopBarState
import com.github.arhor.spellbindr.ui.components.TopBarState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetViewModel.CharacterSheetEffect
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetError
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetTopBarActions
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.WeaponCatalogDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SheetEditMode
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

    val config = AppTopBarConfig(
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
    }

    ProvideTopBarState(
        topBarState = TopBarState(
            config = config,
            overlays = overlays,
        ),
    ) {
        CharacterSheetScreen(
            state = state,
            onTabSelected = vm::selectTab,
            onAddSpellsClick = { (state as? CharacterSheetUiState.Content)?.characterId?.let(onAddSpells) },
            onSpellSelected = onOpenSpellDetail,
            onSpellRemoved = vm::removeSpell,
            onSpellSlotToggle = vm::toggleSpellSlot,
            onSpellSlotTotalChanged = vm::setSpellSlotTotal,
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

/**
 * Pure UI composable for the character sheet.
 * Renders Loading, Failure, or Content states.
 */
@Composable
private fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    onTabSelected: (CharacterSheetTab) -> Unit,
    onAddSpellsClick: () -> Unit,
    onSpellSelected: (String) -> Unit,
    onSpellRemoved: (String, String) -> Unit,
    onSpellSlotToggle: (Int, Int) -> Unit,
    onSpellSlotTotalChanged: (Int, Int) -> Unit,
    onAddWeaponClick: () -> Unit,
    onWeaponSelected: (String) -> Unit,
    onWeaponDeleted: (String) -> Unit,
    onWeaponEditorDismissed: () -> Unit,
    onWeaponNameChanged: (String) -> Unit,
    onWeaponAbilityChanged: (AbilityId) -> Unit,
    onWeaponUseAbilityForDamageChanged: (Boolean) -> Unit,
    onWeaponProficiencyChanged: (Boolean) -> Unit,
    onWeaponDiceCountChanged: (String) -> Unit,
    onWeaponDieSizeChanged: (String) -> Unit,
    onWeaponDamageTypeChanged: (DamageType) -> Unit,
    onWeaponSaved: () -> Unit,
    onWeaponCatalogOpened: () -> Unit,
    onWeaponCatalogClosed: () -> Unit,
    onWeaponCatalogItemSelected: (String) -> Unit,
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
                    onTabSelected = onTabSelected,
                    onAddSpellsClick = onAddSpellsClick,
                    onSpellSelected = onSpellSelected,
                    onSpellRemoved = onSpellRemoved,
                    onSpellSlotToggle = onSpellSlotToggle,
                    onSpellSlotTotalChanged = onSpellSlotTotalChanged,
                    onAddWeaponClick = onAddWeaponClick,
                    onWeaponSelected = onWeaponSelected,
                    modifier = modifier,
                )
                state.weaponEditorState?.let { editor ->
                    WeaponEditorDialog(
                        editorState = editor,
                        onDismiss = onWeaponEditorDismissed,
                        onNameChange = onWeaponNameChanged,
                        onCatalogOpen = onWeaponCatalogOpened,
                        onAbilityChange = onWeaponAbilityChanged,
                        onUseAbilityForDamageChange = onWeaponUseAbilityForDamageChanged,
                        onProficiencyChange = onWeaponProficiencyChanged,
                        onDiceCountChange = onWeaponDiceCountChanged,
                        onDieSizeChange = onWeaponDieSizeChanged,
                        onDamageTypeChange = onWeaponDamageTypeChanged,
                        onDelete = onWeaponDeleted,
                        onSave = onWeaponSaved,
                    )
                }
                if (state.isWeaponCatalogVisible) {
                    WeaponCatalogDialog(
                        catalog = state.weaponCatalog,
                        onDismiss = onWeaponCatalogClosed,
                        onItemSelected = onWeaponCatalogItemSelected,
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
            onTabSelected = {},
            onAddSpellsClick = {},
            onSpellSelected = {},
            onSpellRemoved = { _, _ -> },
            onSpellSlotToggle = { _, _ -> },
            onSpellSlotTotalChanged = { _, _ -> },
            onAddWeaponClick = {},
            onWeaponSelected = {},
            onWeaponDeleted = {},
            onWeaponEditorDismissed = {},
            onWeaponNameChanged = {},
            onWeaponAbilityChanged = {},
            onWeaponUseAbilityForDamageChanged = {},
            onWeaponProficiencyChanged = {},
            onWeaponDiceCountChanged = {},
            onWeaponDieSizeChanged = {},
            onWeaponDamageTypeChanged = {},
            onWeaponSaved = {},
            onWeaponCatalogOpened = {},
            onWeaponCatalogClosed = {},
            onWeaponCatalogItemSelected = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
