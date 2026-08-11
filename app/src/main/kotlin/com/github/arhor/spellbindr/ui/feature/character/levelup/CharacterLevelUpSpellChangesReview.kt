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
import com.github.arhor.spellbindr.domain.model.SpellChanges

/** Summarizes permanent spell-list changes before a level-up is confirmed. */
@Composable
internal fun CharacterLevelUpSpellChangesReview(state: CharacterLevelUpUiState.Content) {
    val changes = state.plan.selections.spellChanges
    if (changes.isEmpty()) return

    fun className(id: String): String = state.classes.firstOrNull { it.id == id }?.name ?: id
    fun spellName(id: String): String = state.spells.firstOrNull { it.id == id }?.name ?: id
    fun spellLabel(id: String): String {
        val spell = state.spells.firstOrNull { it.id == id }
        return when {
            spell == null -> id
            spell.level == 0 -> "${spell.name} (cantrip)"
            else -> "${spell.name} (level ${spell.level})"
        }
    }
    fun learnedLabel(id: String): String {
        val spell = state.spells.firstOrNull { it.id == id }
        return if (spell?.level == 0) "Learned cantrip: ${spellLabel(id)}" else "Learned spell: ${spellLabel(id)}"
    }

    val grouped = buildMap<String, MutableList<String>> {
        changes.learned.forEach { ref ->
            getOrPut(ref.classId) { mutableListOf() }.add(learnedLabel(ref.spellId))
        }
        changes.featureLearned.values.flatten().forEach { ref ->
            getOrPut(ref.classId) { mutableListOf() }.add("Granted automatically: ${spellLabel(ref.spellId)}")
        }
        changes.addedToSpellbook.forEach { ref ->
            getOrPut(ref.classId) { mutableListOf() }.add("Added to spellbook: ${spellLabel(ref.spellId)}")
        }
        changes.replaced.forEach { replacement ->
            getOrPut(replacement.classId) { mutableListOf() }.add(
                "Replaced: ${spellName(replacement.removedSpellId)} → ${spellName(replacement.learnedSpellId)}",
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Spell changes", style = MaterialTheme.typography.titleSmall)
            grouped.toSortedMap().forEach { (classId, entries) ->
                Text(className(classId), style = MaterialTheme.typography.bodyMedium)
                entries.forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth()) { Text(entry) }
                }
            }
        }
    }
}

private fun SpellChanges.isEmpty(): Boolean =
    learned.isEmpty() && replaced.isEmpty() && addedToSpellbook.isEmpty() && featureLearned.values.all { it.isEmpty() }
