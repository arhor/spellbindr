package com.github.arhor.spellbindr.ui.screens.characters.creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.AbilityScore
import com.github.arhor.spellbindr.ui.components.GradientDivider
import com.github.arhor.spellbindr.ui.theme.Accent
import com.github.arhor.spellbindr.utils.PreviewScope
import com.github.arhor.spellbindr.utils.signed

@Composable
fun AbilityScoresScreen(
    abilityScores: Map<AbilityScore, Int> = emptyMap(),
    racialBonuses: Map<AbilityScore, Int> = emptyMap(),
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AbilityScoresGrid(abilityScores, racialBonuses)

        GradientDivider(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 25.dp,
            )
        )

        Text(
            text = "Racial bonuses: ${
                racialBonuses
                    .filterValues { it != 0 }
                    .map { (ability, bonus) -> "$ability: ${signed(bonus)}" }
                    .joinToString()
            }",
            color = Accent,
        )
    }
}

@Preview
@Composable
private fun AbilityScoresScreenPreview() {
    PreviewScope {
        AbilityScoresScreen(
            racialBonuses = mapOf(
                AbilityScore.STR to +2,
                AbilityScore.DEX to +1,
                AbilityScore.INT to -3,
            )
        )
    }
}
