package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.CharacterSheetError
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.weapons.WeaponCatalogDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.weapons.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetTab
import com.github.arhor.spellbindr.ui.theme.AppTheme

/**
 * Pure UI composable for the character sheet.
 * Renders Loading, Failure, or Content states.
 */
@Composable
internal fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    modifier: Modifier = Modifier,
    onTabSelected: (CharacterSheetTab) -> Unit = {},
    onAddSpellsClick: () -> Unit = {},
    onSpellSelected: (String) -> Unit = {},
    onSpellRemoved: (String, String) -> Unit = { _, _ -> },
    onSpellSlotToggle: (Int, Int) -> Unit = { _, _ -> },
    onSpellSlotTotalChanged: (Int, Int) -> Unit = { _, _ -> },
    onPactSlotToggle: (Int) -> Unit = {},
    onPactSlotTotalChanged: (Int) -> Unit = {},
    onConcentrationClear: () -> Unit = {},
    onAddWeaponClick: () -> Unit = {},
    onWeaponSelected: (String) -> Unit = {},
    onWeaponDeleted: (String) -> Unit = {},
    onWeaponEditorDismissed: () -> Unit = {},
    onWeaponNameChanged: (String) -> Unit = {},
    onWeaponAbilityChanged: (AbilityId) -> Unit = {},
    onWeaponUseAbilityForDamageChanged: (Boolean) -> Unit = {},
    onWeaponProficiencyChanged: (Boolean) -> Unit = {},
    onWeaponDiceCountChanged: (String) -> Unit = {},
    onWeaponDieSizeChanged: (String) -> Unit = {},
    onWeaponDamageTypeChanged: (DamageType) -> Unit = {},
    onWeaponSaved: () -> Unit = {},
    onWeaponCatalogOpened: () -> Unit = {},
    onWeaponCatalogClosed: () -> Unit = {},
    onWeaponCatalogItemSelected: (String) -> Unit = {},
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

        is CharacterSheetUiState.Failure -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                CharacterSheetError(message = state.errorMessage)
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
                    onPactSlotToggle = onPactSlotToggle,
                    onPactSlotTotalChanged = onPactSlotTotalChanged,
                    onConcentrationClear = onConcentrationClear,
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

@Composable
@PreviewLightDark
private fun CharacterSheetScreenPreview() {
    AppTheme {
        CharacterSheetScreen(
            state = CharacterSheetPreviewData.uiState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
