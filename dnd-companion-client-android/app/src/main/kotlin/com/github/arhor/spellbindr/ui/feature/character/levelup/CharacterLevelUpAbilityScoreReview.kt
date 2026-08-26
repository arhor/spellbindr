package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.displayName

/** Shows only the ability scores whose values changed in the level-up preview. */
@Composable
internal fun CharacterLevelUpAbilityScoreReview(state: CharacterLevelUpUiState.Content) {
    val before = state.preview.before.abilityScores
    val after = state.preview.after.abilityScores
    val changes = AbilityIds.standardOrder.mapNotNull { abilityId ->
        val beforeScore = before.scoreForReview(abilityId)
        val afterScore = after.scoreForReview(abilityId)
        if (beforeScore == afterScore) {
            null
        } else {
            AbilityScoreReviewChange(
                abilityId = abilityId,
                beforeScore = beforeScore,
                afterScore = afterScore,
                beforeModifier = before.modifierFor(abilityId),
                afterModifier = after.modifierFor(abilityId),
            )
        }
    }
    if (changes.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ability scores", style = MaterialTheme.typography.titleSmall)
        changes.forEach { change ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(change.abilityId.displayName())
                Text(
                    "${change.beforeScore} (${change.beforeModifier.asModifier()}) → " +
                        "${change.afterScore} (${change.afterModifier.asModifier()})",
                )
            }
        }
    }
}

private data class AbilityScoreReviewChange(
    val abilityId: String,
    val beforeScore: Int,
    val afterScore: Int,
    val beforeModifier: Int,
    val afterModifier: Int,
)

private fun AbilityScores.scoreForReview(abilityId: String): Int = when (abilityId) {
    AbilityIds.STR -> strength
    AbilityIds.DEX -> dexterity
    AbilityIds.CON -> constitution
    AbilityIds.INT -> intelligence
    AbilityIds.WIS -> wisdom
    AbilityIds.CHA -> charisma
    else -> error("Unknown ability id: $abilityId")
}

private fun Int.asModifier(): String = if (this >= 0) "+$this" else toString()
