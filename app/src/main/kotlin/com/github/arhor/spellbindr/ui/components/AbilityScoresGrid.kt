package com.github.arhor.spellbindr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.utils.PreviewScope

private const val ITEMS_PER_ROW = 3
private const val ABILITY_SCORE_DEFAULT_VALUE = 8
private const val ABILITY_SCORE_DEFAULT_BONUS = 0

@Composable
fun AbilityScoresGrid(
    abilityScores: Map<Ability, Int> = emptyMap(),
    racialBonuses: Map<Ability, Int> = emptyMap(),
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (abilityScoresChunk in Ability.entries.chunked(ITEMS_PER_ROW)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (abilityScore in abilityScoresChunk) {
                    val value = abilityScores[abilityScore] ?: ABILITY_SCORE_DEFAULT_VALUE
                    val bonus = racialBonuses[abilityScore] ?: ABILITY_SCORE_DEFAULT_BONUS

                    AbilityScoreCard(
                        name = abilityScore.name,
                        value = value + bonus,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AbilityScoresGridPreview() {
    PreviewScope {
        AbilityScoresGrid(
            racialBonuses = mapOf(
                Ability.STR to +2,
                Ability.DEX to +1,
                Ability.INT to -3,
            )
        )
    }
}
