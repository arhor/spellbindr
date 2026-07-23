package com.github.arhor.spellbindr.ui.feature.character.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.CharacterSheetContent
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.CharacterSheetError
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.weapons.WeaponCatalogDialog
import com.github.arhor.spellbindr.ui.feature.character.sheet.components.tabs.weapons.WeaponEditorDialog
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme

/**
 * Pure UI composable for the character sheet.
 * Renders Loading, Failure, or Content states.
 */
@Composable
internal fun CharacterSheetScreen(
    state: CharacterSheetUiState,
    modifier: Modifier = Modifier,
    dispatch: CharacterSheetDispatch = {},
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
                    onTabSelected = { dispatch(CharacterSheetIntent.TabSelected(it)) },
                    onAddSpellsClick = { dispatch(CharacterSheetIntent.AddSpellsClicked) },
                    onSpellSelected = { dispatch(CharacterSheetIntent.SpellSelected(it)) },
                    onSpellRemoved = { spellId, sourceClass ->
                        dispatch(CharacterSheetIntent.SpellRemoved(spellId, sourceClass))
                    },
                    onCastSpellClick = { dispatch(CharacterSheetIntent.CastSpellClicked(it)) },
                    onLongRestClick = { dispatch(CharacterSheetIntent.LongRestClicked) },
                    onShortRestClick = { dispatch(CharacterSheetIntent.ShortRestClicked) },
                    onConfigureSlotsClick = { dispatch(CharacterSheetIntent.ConfigureSlotsClicked) },
                    onSpellSlotToggle = { level, slotIndex ->
                        dispatch(CharacterSheetIntent.SpellSlotToggled(level, slotIndex))
                    },
                    onSpellSlotTotalChanged = { level, total ->
                        dispatch(CharacterSheetIntent.SpellSlotTotalChanged(level, total))
                    },
                    onPactSlotToggle = { dispatch(CharacterSheetIntent.PactSlotToggled(it)) },
                    onPactSlotTotalChanged = { dispatch(CharacterSheetIntent.PactSlotTotalChanged(it)) },
                    onPactSlotLevelChanged = { dispatch(CharacterSheetIntent.PactSlotLevelChanged(it)) },
                    onConcentrationClear = { dispatch(CharacterSheetIntent.ConcentrationCleared) },
                    onAddWeaponClick = { dispatch(CharacterSheetIntent.AddWeaponClicked) },
                    onWeaponSelected = { dispatch(CharacterSheetIntent.WeaponSelected(it)) },
                    modifier = modifier,
                )
                state.weaponEditorState?.let { editor ->
                    WeaponEditorDialog(
                        editorState = editor,
                        onDismiss = { dispatch(CharacterSheetIntent.WeaponEditorDismissed) },
                        onNameChange = { dispatch(CharacterSheetIntent.WeaponNameChanged(it)) },
                        onCatalogOpen = { dispatch(CharacterSheetIntent.WeaponCatalogOpened) },
                        onAbilityChange = { dispatch(CharacterSheetIntent.WeaponAbilityChanged(it)) },
                        onUseAbilityForDamageChange = {
                            dispatch(CharacterSheetIntent.WeaponUseAbilityForDamageChanged(it))
                        },
                        onProficiencyChange = { dispatch(CharacterSheetIntent.WeaponProficiencyChanged(it)) },
                        onDiceCountChange = { dispatch(CharacterSheetIntent.WeaponDiceCountChanged(it)) },
                        onDieSizeChange = { dispatch(CharacterSheetIntent.WeaponDieSizeChanged(it)) },
                        onDamageTypeChange = { dispatch(CharacterSheetIntent.WeaponDamageTypeChanged(it)) },
                        onDelete = { dispatch(CharacterSheetIntent.WeaponDeleted(it)) },
                        onSave = { dispatch(CharacterSheetIntent.WeaponSaved) },
                    )
                }
                if (state.isWeaponCatalogVisible) {
                    WeaponCatalogDialog(
                        catalog = state.weaponCatalog,
                        onDismiss = { dispatch(CharacterSheetIntent.WeaponCatalogClosed) },
                        onItemSelected = { dispatch(CharacterSheetIntent.WeaponCatalogItemSelected(it)) },
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
