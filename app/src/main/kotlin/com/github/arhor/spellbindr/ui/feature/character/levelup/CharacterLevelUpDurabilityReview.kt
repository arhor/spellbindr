package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement

@Composable
internal fun CharacterLevelUpDurabilityReview(state: CharacterLevelUpUiState.Content) {
    val before = state.preview.before
    val after = state.preview.after
    val hitPointRequirement = state.preview.requirements
        .filterIsInstance<LevelUpRequirement.HitPoints>()
        .firstOrNull()
    val hitPointGain = hitPointRequirement?.selectedGain ?: state.plan.selections.hitPointGain
    val constitutionModifier = after.abilityScores.modifierFor(AbilityIds.CON)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Durability", style = MaterialTheme.typography.titleSmall)
            DurabilityReviewRow(
                label = "Maximum HP",
                before = before.maximumHitPoints.toString(),
                after = after.maximumHitPoints.toString(),
            )
            hitPointGain?.let { gain ->
                DurabilityValueRow(
                    label = "New level HP",
                    value = (gain.rolledValue + constitutionModifier).signed(),
                )
                DurabilityValueRow(
                    label = "HP method",
                    value = gain.reviewLabel(hitPointRequirement?.hitDie, constitutionModifier),
                )
            }

            val beforePools = before.hitDicePools.associate { it.dieSize to it.total }
            val afterPools = after.hitDicePools.associate { it.dieSize to it.total }
            (beforePools.keys + afterPools.keys).toSet().sorted().forEach { dieSize ->
                DurabilityReviewRow(
                    label = "d$dieSize hit dice",
                    before = beforePools[dieSize].orZero().toString(),
                    after = afterPools[dieSize].orZero().toString(),
                )
            }
        }
    }
}

@Composable
private fun DurabilityReviewRow(label: String, before: String, after: String) {
    DurabilityValueRow(label, "$before → $after")
}

@Composable
private fun DurabilityValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text(value)
    }
}

private fun HitPointGain.reviewLabel(hitDie: Int?, constitutionModifier: Int): String {
    val method = when (this) {
        is HitPointGain.Fixed -> "Fixed"
        is HitPointGain.Rolled -> "Rolled"
        is HitPointGain.Manual -> "Manual"
    }
    val base = when {
        this is HitPointGain.Manual || hitDie == null -> rolledValue.toString()
        else -> "$rolledValue on d$hitDie"
    }
    return "$method ($base) + CON ${constitutionModifier.signed()}"
}

private fun Int.signed(): String = if (this >= 0) "+$this" else toString()

private fun Int?.orZero(): Int = this ?: 0
