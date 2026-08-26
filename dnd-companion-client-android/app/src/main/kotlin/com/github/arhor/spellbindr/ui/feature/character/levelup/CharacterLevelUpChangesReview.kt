package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.Skill

/** Shows only the permanent grants introduced by the pending level-up. */
@Composable
internal fun CharacterLevelUpChangesReview(state: CharacterLevelUpUiState.Content) {
    val before = state.preview.before
    val after = state.preview.after
    val addedProficiencies = after.proficiencyIds - before.proficiencyIds
    val addedSaves = after.savingThrowAbilityIds - before.savingThrowAbilityIds
    val addedLanguages = after.languageIds - before.languageIds
    val addedFeatures = after.featureIds - before.featureIds
    val selectedOptions = state.preview.requirements
        .filterIsInstance<LevelUpRequirement.ChoiceSelection>()
        .flatMap { requirement ->
            requirement.options.filter { it.id in requirement.selectedOptionIds }
                .map { option -> requirement to option.label }
        }
    if (addedProficiencies.isEmpty() && addedSaves.isEmpty() && addedLanguages.isEmpty() &&
        addedFeatures.isEmpty() && selectedOptions.isEmpty()
    ) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("New gains", style = MaterialTheme.typography.titleSmall)
            ProficiencyGroup("Skills", addedProficiencies.filter { it.startsWith("skill-") }.map(::skillName))
            ProficiencyGroup("Saving throws", addedSaves.map { it.uppercase() })
            ProficiencyGroup("Armor", addedProficiencies.filter { it.startsWith("armor-") }.map(::idName))
            ProficiencyGroup("Weapons", addedProficiencies.filter { it.startsWith("weapon-") }.map(::idName))
            ProficiencyGroup("Tools", addedProficiencies.filter { it.startsWith("tool-") }.map(::idName))
            ProficiencyGroup("Languages", addedLanguages.map(::idName))

            val selectedClass = state.classes.firstOrNull { it.id == state.plan.selectedClassId }
            val subclass = selectedClass?.subclasses?.firstOrNull {
                it.id == state.plan.selections.subclassId
            }
            val subclassFeatureIds = subclass?.levels.orEmpty().flatMap { it.features }.toSet()
            val featureNames = addedFeatures.map { id ->
                val name = state.features.firstOrNull { it.id == id }?.name ?: idName(id)
                when {
                    id in subclassFeatureIds && subclass != null -> "$name (${subclass.name})"
                    selectedClass != null -> "$name (${selectedClass.name})"
                    else -> name
                }
            }
            ProficiencyGroup("Class or subclass features", featureNames)
            if (selectedOptions.isNotEmpty()) {
                Text("Selected feature options", style = MaterialTheme.typography.labelLarge)
                selectedOptions.forEach { (requirement, label) ->
                    val kind = when (requirement.category) {
                        LevelUpChoiceCategory.Feature -> "Feature"
                        LevelUpChoiceCategory.Proficiency -> "Proficiency"
                        LevelUpChoiceCategory.Feat -> "Feat"
                    }
                    Text("$label ($kind)")
                }
            }
        }
    }
}

@Composable
private fun ProficiencyGroup(title: String, values: List<String>) {
    if (values.isEmpty()) return
    Text(title, style = MaterialTheme.typography.labelLarge)
    values.distinct().sorted().forEach { Text(it) }
}

private fun skillName(id: String): String {
    val skillId = id.removePrefix("skill-")
    return Skill.entries.firstOrNull { it.name.equals(skillId.replace('-', '_'), ignoreCase = true) }
        ?.displayName ?: idName(id)
}

private fun idName(id: String): String = id.substringAfter('-').replace('-', ' ')
    .replaceFirstChar { it.uppercase() }
