package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.components.AbilityTokenData
import com.github.arhor.spellbindr.ui.components.AbilityTokensGrid

@Composable
fun OverviewTab(
    header: CharacterHeaderUiState,
    overview: OverviewTabState,
    editMode: SheetEditMode,
    editingState: CharacterSheetEditingState?,
    callbacks: CharacterSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    var showHpAdjustDialog by remember { mutableStateOf(false) }
    val onHitPointsClick = if (editMode == SheetEditMode.View) {
        { showHpAdjustDialog = true }
    } else {
        null
    }

    if (showHpAdjustDialog) {
        HitPointAdjustDialog(
            hitPoints = header.hitPoints,
            onAdjustHp = callbacks.onAdjustHp,
            onTempHpChanged = callbacks.onTempHpChanged,
            onDismiss = { showHpAdjustDialog = false },
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            val isAtOrBelowZeroHp = header.hitPoints.current <= 0
            if (isAtOrBelowZeroHp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CombatOverviewCard(
                        header = header,
                        onDamageClick = { onHitPointsClick?.invoke() },
                        onHealClick = { onHitPointsClick?.invoke() },
                    )
                    DeathSavesCard(
                        state = overview.deathSaves,
                        onSuccessChanged = callbacks.onDeathSaveSuccessesChanged,
                        onFailureChanged = callbacks.onDeathSaveFailuresChanged,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                CombatOverviewCard(
                    header = header,
                    onDamageClick = { onHitPointsClick?.invoke() },
                    onHealClick = { onHitPointsClick?.invoke() },
                )
            }
        }
        item {
            AbilityTokensGrid(
                abilities = overview.abilities.map { ability ->
                    AbilityTokenData(
                        abbreviation = ability.label,
                        score = ability.score,
                        modifier = ability.modifier,
                        savingThrowBonus = ability.savingThrowBonus,
                        proficient = ability.savingThrowProficient,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            DetailCard(
                title = "Core details",
                lines = listOfNotBlank(
                    overview.race.takeIf { it.isNotBlank() }?.let { "Race: $it" },
                    overview.background.takeIf { it.isNotBlank() }?.let { "Background: $it" },
                    overview.alignment.takeIf { it.isNotBlank() }?.let { "Alignment: $it" },
                ),
            )
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                EditableDetailCard(
                    title = "Senses & Languages",
                    primaryLabel = "Senses",
                    primaryValue = editingState.senses,
                    onPrimaryChanged = callbacks.onSensesEdited,
                    secondaryLabel = "Languages",
                    secondaryValue = editingState.languages,
                    onSecondaryChanged = callbacks.onLanguagesEdited,
                )
            } else {
                DetailCard(
                    title = "Senses & Languages",
                    lines = listOfNotBlank(
                        overview.senses.takeIf { it.isNotBlank() }?.let { "Senses: $it" },
                        overview.languages.takeIf { it.isNotBlank() }?.let { "Languages: $it" },
                    ),
                )
            }
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                EditableDetailCard(
                    title = "Proficiencies & Equipment",
                    primaryLabel = "Proficiencies",
                    primaryValue = editingState.proficiencies,
                    onPrimaryChanged = callbacks.onProficienciesEdited,
                    secondaryLabel = "Equipment",
                    secondaryValue = editingState.equipment,
                    onSecondaryChanged = callbacks.onEquipmentEdited,
                )
            } else {
                DetailCard(
                    title = "Proficiencies & Equipment",
                    lines = listOfNotBlank(
                        overview.proficiencies.takeIf { it.isNotBlank() }?.let { "Proficiencies:\n$it" },
                        overview.equipment.takeIf { it.isNotBlank() }?.let { "Equipment:\n$it" },
                    ),
                )
            }
        }
        item {
            if (editMode == SheetEditMode.Editing && editingState != null) {
                InlineTextField(
                    label = "Hit Dice",
                    value = editingState.hitDice,
                    onValueChanged = callbacks.onHitDiceEdited,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                DetailCard(
                    title = "Hit Dice",
                    lines = listOf(overview.hitDice.ifBlank { "—" }),
                )
            }
        }
    }
}

@Preview
@Composable
fun OverviewTabPreview() {

}
