package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpValidationSeverity

@Composable
fun CharacterLevelUpScreen(
    state: CharacterLevelUpUiState,
    dispatch: CharacterLevelUpDispatch,
    modifier: Modifier = Modifier,
) {
    when (state) {
        CharacterLevelUpUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is CharacterLevelUpUiState.Failure -> MessagePane(state.message, modifier)
        is CharacterLevelUpUiState.Unavailable -> MessagePane("${state.title}\n\n${state.explanation}", modifier)
        is CharacterLevelUpUiState.Content -> Content(state, dispatch, modifier)
    }
}

@Composable
private fun Content(state: CharacterLevelUpUiState.Content, dispatch: CharacterLevelUpDispatch, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("${state.characterName}: level ${state.preview.before.totalLevel} → ${state.preview.after.totalLevel}", style = MaterialTheme.typography.titleMedium)
        when (state.step) {
            CharacterLevelUpStep.Class -> {
                val requirement = state.preview.requirements
                    .filterIsInstance<LevelUpRequirement.ClassSelection>().firstOrNull()
                state.classes.forEach { clazz ->
                    val eligibility = requirement?.eligibility?.firstOrNull { it.classId == clazz.id }
                    val label = buildString {
                        append(clazz.name)
                        eligibility?.reasons?.takeIf { it.isNotEmpty() }?.let { append(" — "); append(it.joinToString(" ")) }
                    }
                    ChoiceRow(label, state.plan.selectedClassId == clazz.id) { dispatch(CharacterLevelUpIntent.ClassSelected(clazz.id)) }
                }
            }
            CharacterLevelUpStep.Choices -> state.preview.requirements.forEach { requirement ->
                when (requirement) {
                    is LevelUpRequirement.SubclassSelection -> {
                        SectionTitle("Choose subclass")
                        requirement.options.forEach { option -> ChoiceRow(option.label, requirement.selectedSubclassId == option.id) { dispatch(CharacterLevelUpIntent.SubclassSelected(option.id)) } }
                    }
                    is LevelUpRequirement.ChoiceSelection -> {
                        SectionTitle("${requirement.label} · choose ${requirement.choice.choose}")
                        choiceOptions(requirement.choice).forEach { option ->
                            FilterChip(
                                selected = option in requirement.selectedOptionIds,
                                onClick = { dispatch(CharacterLevelUpIntent.ChoiceToggled(requirement.id, option, requirement.choice.choose)) },
                                label = { Text(option) },
                            )
                        }
                    }
                    else -> Unit
                }
            }
            CharacterLevelUpStep.HitPoints -> state.preview.requirements.filterIsInstance<LevelUpRequirement.HitPoints>().firstOrNull()?.let { hp ->
                Text("Choose hit points for d${hp.hitDie}. Your Constitution modifier is applied during materialization.")
                ChoiceRow("Fixed (${hp.hitDie / 2 + 1})", hp.selectedGain is HitPointGain.Fixed) { dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Fixed(hp.hitDie / 2 + 1))) }
                (1..hp.hitDie).forEach { value ->
                    FilterChip(selected = hp.selectedGain == HitPointGain.Rolled(value), onClick = { dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Rolled(value))) }, label = { Text("Rolled $value") })
                }
                Text("A manual result can be recorded after choosing the closest rolled value.", style = MaterialTheme.typography.bodySmall)
            }
            CharacterLevelUpStep.AbilityScore -> state.preview.requirements.filterIsInstance<LevelUpRequirement.AbilityScoreImprovement>().firstOrNull()?.let { asi ->
                Text("Choose an ability score improvement or a feat.")
                AbilityIds.standardOrder.forEach { ability ->
                    ChoiceRow("+${asi.abilityPoints} ${ability.uppercase()}", asi.selectedDecision == AbilityScoreDecision.Increase(mapOf(ability to asi.abilityPoints))) {
                        dispatch(CharacterLevelUpIntent.AbilityScoreDecisionSelected(AbilityScoreDecision.Increase(mapOf(ability to asi.abilityPoints))))
                    }
                }
                if (asi.allowsFeat) state.feats.forEach { feat -> ChoiceRow("Feat: ${feat.name}", asi.selectedDecision == AbilityScoreDecision.Feat(feat.id)) { dispatch(CharacterLevelUpIntent.AbilityScoreDecisionSelected(AbilityScoreDecision.Feat(feat.id))) } }
            }
            CharacterLevelUpStep.Spells -> {
                val spellRequirement = state.preview.requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().firstOrNull()
                Text("${spellRequirement?.policyId ?: "Spellcasting"} decisions are validated for this class level.")
                spellRequirement?.preparationCapacity?.let { Text("You may prepare up to $it spells after leveling. Prepared spells remain mutable play state.") }
                Text("Learned, replaced, and spellbook changes will be listed here when required by the class policy.")
            }
            CharacterLevelUpStep.Review -> {
                state.staleMessage?.let { WarningCard(it); Button(onClick = { dispatch(CharacterLevelUpIntent.ReloadClicked) }) { Text("Reload draft") } }
                state.persistenceMessage?.let(::WarningCard)
                ReviewRow("Total level", state.preview.before.totalLevel.toString(), state.preview.after.totalLevel.toString())
                ReviewRow("Class", state.preview.before.classDisplayName, state.preview.after.classDisplayName)
                ReviewRow("Proficiency bonus", "+${state.preview.before.proficiencyBonus}", "+${state.preview.after.proficiencyBonus}")
                ReviewRow("Maximum HP", state.preview.before.maximumHitPoints.toString(), state.preview.after.maximumHitPoints.toString())
                state.preview.validations.forEach { issue ->
                    if (issue.severity == LevelUpValidationSeverity.Overrideable) {
                        val checked = issue.acknowledgementId in state.plan.selections.acknowledgedIssueCodes
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked, onCheckedChange = { dispatch(CharacterLevelUpIntent.AcknowledgementChanged(issue.acknowledgementId, it)) })
                            Text(issue.message)
                        }
                    } else WarningCard(issue.message)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            OutlinedButton(onClick = { dispatch(CharacterLevelUpIntent.CancelClicked) }) { Text("Cancel") }
            if (state.currentStepIndex > 0) OutlinedButton(onClick = { dispatch(CharacterLevelUpIntent.BackClicked) }) { Text("Back") }
            if (state.isReview) Button(enabled = state.canConfirm, onClick = { dispatch(CharacterLevelUpIntent.ConfirmClicked) }) { Text(if (state.isSaving) "Saving…" else "Confirm level up") }
            else Button(onClick = { dispatch(CharacterLevelUpIntent.NextClicked) }) { Text("Next") }
        }
    }
}

@Composable private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) = Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected, onClick = onClick); Text(label) } }
@Composable private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleSmall)
@Composable private fun WarningCard(text: String) = Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) { Text(text, Modifier.fillMaxWidth().padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer) }
@Composable private fun ReviewRow(label: String, before: String, after: String) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text("$before → $after") }
@Composable private fun MessagePane(text: String, modifier: Modifier) = Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { Text(text) }

private fun choiceOptions(choice: Choice): List<String> = when (choice) {
    is Choice.FavoredEnemyChoice -> choice.from
    is Choice.TerrainTypeChoice -> choice.from
    is Choice.ProficiencyChoice -> choice.from
    is Choice.FeatureChoice -> choice.from
    is Choice.OptionsArrayChoice -> choice.from
    is Choice.EquipmentChoice -> choice.from
    is Choice.AbilityBonusChoice -> choice.from.map { option -> option.entries.joinToString { "${it.key} +${it.value}" } }
    is Choice.ResourceListChoice, is Choice.EquipmentCategoriesChoice, is Choice.NestedChoice,
    is Choice.FromAllChoice, is Choice.IdealChoice -> emptyList()
}
