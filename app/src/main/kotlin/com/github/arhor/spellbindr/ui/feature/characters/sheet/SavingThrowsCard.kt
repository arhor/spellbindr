package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SavingThrowsCard(
    abilities: List<AbilityUiModel>,
    modifier: Modifier = Modifier,
) {
    val abilityLookup = abilities.associateBy(AbilityUiModel::ability)
    val leftColumn = LEFT_ABILITY_ORDER.mapNotNull { abilityLookup[it] }
    val rightColumn = RIGHT_ABILITY_ORDER.mapNotNull { abilityLookup[it] }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Saving Throws",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SavingThrowsGrid(
                leftColumn = leftColumn,
                rightColumn = rightColumn,
            )
        }
    }
}

@Composable
private fun SavingThrowsGrid(
    leftColumn: List<AbilityUiModel>,
    rightColumn: List<AbilityUiModel>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SavingThrowColumn(
            abilities = leftColumn,
            modifier = Modifier.weight(1f),
        )
        SavingThrowColumn(
            abilities = rightColumn,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SavingThrowColumn(
    abilities: List<AbilityUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        abilities.forEach { ability ->
            SavingThrowCard(
                abilityName = ability.label,
                bonus = ability.savingThrowBonus,
                proficient = ability.savingThrowProficient,
            )
        }
    }
}

@Preview
@Composable
private fun SavingThrowsCardPreview() {
    AppTheme {
        SavingThrowsCard(
            abilities = CharacterSheetPreviewData.overview.abilities,
        )
    }
}
