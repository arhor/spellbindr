package com.github.arhor.spellbindr.ui.feature.characters.sheet.components.tabs.spells

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.R
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.CharacterSheetPreviewData
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.model.SpellcastingClassUiModel
import com.github.arhor.spellbindr.ui.theme.AppTheme
import com.github.arhor.spellbindr.utils.signed

@Composable
internal fun SpellcastingClassHeader(
    spellcastingClass: SpellcastingClassUiModel,
    modifier: Modifier = Modifier,
) {
    val unknown = stringResource(R.string.spells_unknown_placeholder)
    val title = if (spellcastingClass.isUnassigned) {
        stringResource(R.string.spells_unassigned)
    } else {
        spellcastingClass.name.ifBlank { stringResource(R.string.spells_unassigned) }
    }
    val abilityText = spellcastingClass.spellcastingAbility ?: unknown
    val dcText = spellcastingClass.spellSaveDc?.toString() ?: unknown
    val attackText = spellcastingClass.spellAttackBonus?.let(::signed) ?: unknown

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpellcastingStat(text = stringResource(R.string.spells_ability_label, abilityText))
            SpellcastingStat(text = stringResource(R.string.spells_dc_label, dcText))
            SpellcastingStat(text = stringResource(R.string.spells_attack_label, attackText))
        }
    }
}

@Composable
private fun SpellcastingStat(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
internal fun SpellLevelHeader(
    spellLevel: SpellLevelUiModel,
    modifier: Modifier = Modifier,
) {
    val label = if (spellLevel.level == 0) {
        stringResource(R.string.spells_level_cantrips)
    } else {
        stringResource(R.string.spells_level_label, spellLevel.level)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun SpellcastingClassHeaderPreview() {
    AppTheme {
        SpellcastingClassHeader(spellcastingClass = CharacterSheetPreviewData.spells.spellcastingClasses.first())
    }
}
