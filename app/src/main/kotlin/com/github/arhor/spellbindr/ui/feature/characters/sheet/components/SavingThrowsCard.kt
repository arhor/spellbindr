package com.github.arhor.spellbindr.ui.feature.characters.sheet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.displayName
import com.github.arhor.spellbindr.ui.feature.characters.sheet.AbilityUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SavingThrowsCard(
    abilities: List<AbilityUiModel>,
    modifier: Modifier = Modifier,
) {
    val (leftColumn, rightColumn) = remember(abilities) {
        val abilityLookup = abilities.associateBy(AbilityUiModel::abilityId)
        val left = LEFT_ABILITY_ORDER.mapNotNull { abilityLookup[it] }
        val right = RIGHT_ABILITY_ORDER.mapNotNull { abilityLookup[it] }
        left to right
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        abilities.forEach { ability ->
            SavingThrowCard(
                abilityName = ability.abilityId.displayName(),
                bonus = ability.savingThrowBonus,
                proficient = ability.savingThrowProficient,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SavingThrowsCardPreview() {
    AppTheme {
        Surface {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SavingThrowsCard(
                    abilities = CharacterSheetPreviewData.overview.abilities,
                )
            }
        }
    }
}
