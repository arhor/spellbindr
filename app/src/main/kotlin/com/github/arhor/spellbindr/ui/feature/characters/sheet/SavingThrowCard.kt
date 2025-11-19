package com.github.arhor.spellbindr.ui.feature.characters.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.ui.theme.AppTheme

@Composable
internal fun SavingThrowCard(
    abilityName: String,
    bonus: Int,
    proficient: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 6.dp, end = 16.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = proficient,
                onClick = null,
                enabled = false,
                colors = RadioButtonDefaults.colors(
                    disabledSelectedColor = MaterialTheme.colorScheme.primary,
                    disabledUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = abilityName,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = formatBonus(bonus),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SavingThrowCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val abilities = CharacterSheetPreviewData.overview.abilities
            SavingThrowCard(
                abilityName = abilities[0].label,
                bonus = abilities[0].savingThrowBonus,
                proficient = abilities[0].savingThrowProficient,
            )
            SavingThrowCard(
                abilityName = abilities[1].label,
                bonus = abilities[1].savingThrowBonus,
                proficient = abilities[1].savingThrowProficient,
            )
        }
    }
}
