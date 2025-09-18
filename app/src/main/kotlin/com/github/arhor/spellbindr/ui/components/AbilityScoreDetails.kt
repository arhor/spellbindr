package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.utils.PreviewScope
import com.github.arhor.spellbindr.utils.asCommaSeparatedString

@Composable
fun AbilityScoreDetails(
    abilityScores: Map<Ability, Int> = emptyMap(),
    racialBonuses: Map<Ability, Int> = emptyMap(),
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AbilityScoresGrid(
            abilityScores = abilityScores,
            racialBonuses = racialBonuses,
        )
        GradientDivider(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 25.dp,
            )
        )
        Text(
            text = "Racial bonuses: ${racialBonuses.asCommaSeparatedString()}",
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview
@Composable
private fun AbilityScoreDetailsPreview() {
    PreviewScope {
        AbilityScoreDetails(
            racialBonuses = mapOf(
                Ability.STR to +2,
                Ability.DEX to +1,
                Ability.INT to -3,
            )
        )
    }
}
