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
@Composable
internal fun CharacterLevelUpSpellSlotReview(state: CharacterLevelUpUiState.Content) {
    val before = state.preview.before
    val after = state.preview.after
    val changedSharedSlots = (before.sharedSpellSlots.keys + after.sharedSpellSlots.keys)
        .toSortedSet()
        .filter { level -> before.sharedSpellSlots[level].orZero() != after.sharedSpellSlots[level].orZero() }
    val pactCountChanged = before.pactMagic?.slots.orZero() != after.pactMagic?.slots.orZero()
    val pactLevelChanged = before.pactMagic?.slotLevel != after.pactMagic?.slotLevel

    if (changedSharedSlots.isEmpty() && !pactCountChanged && !pactLevelChanged) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Spell slots", style = MaterialTheme.typography.titleSmall)
            changedSharedSlots.forEach { level ->
                SpellSlotReviewRow(
                    label = "${level.ordinalLabel()} shared slots",
                    before = before.sharedSpellSlots[level].orZero().toString(),
                    after = after.sharedSpellSlots[level].orZero().toString(),
                )
            }
            if (pactCountChanged || pactLevelChanged) {
                Text("Pact Magic", style = MaterialTheme.typography.bodyMedium)
                if (pactCountChanged) {
                    SpellSlotReviewRow(
                        label = "Pact Magic slots",
                        before = before.pactMagic?.slots.orZero().toString(),
                        after = after.pactMagic?.slots.orZero().toString(),
                    )
                }
                if (pactLevelChanged) {
                    SpellSlotReviewRow(
                        label = "Pact Magic slot level",
                        before = before.pactMagic?.slotLevel?.ordinalLabel() ?: "N/A",
                        after = after.pactMagic?.slotLevel?.ordinalLabel() ?: "N/A",
                    )
                }
            }
        }
    }
}

@Composable
private fun SpellSlotReviewRow(label: String, before: String, after: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label)
        Text("$before → $after")
    }
}

private fun Int?.orZero(): Int = this ?: 0

private fun Int.ordinalLabel(): String = when (this % 100) {
    11, 12, 13 -> "${this}th-level"
    else -> when (this % 10) {
        1 -> "${this}st-level"
        2 -> "${this}nd-level"
        3 -> "${this}rd-level"
        else -> "${this}th-level"
    }
}
