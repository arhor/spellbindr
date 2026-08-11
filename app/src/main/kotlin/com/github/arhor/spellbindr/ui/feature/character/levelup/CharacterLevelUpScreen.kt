package com.github.arhor.spellbindr.ui.feature.character.levelup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.LevelUpChoiceCategory
import com.github.arhor.spellbindr.domain.model.LevelUpRequirement
import com.github.arhor.spellbindr.domain.model.LevelUpSpellOption
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.domain.model.SpellReplacement

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
                        if (requirement.category == LevelUpChoiceCategory.Feat) {
                            return@forEach
                        }
                        SectionTitle("${requirement.label} · choose ${requirement.choice.choose}")
                        choiceOptions(requirement).forEach { option ->
                            FilterChip(
                                selected = option.id in requirement.selectedOptionIds,
                                onClick = {
                                    dispatch(CharacterLevelUpIntent.ChoiceToggled(
                                        requirement.id,
                                        option.id,
                                        requirement.choice.choose,
                                    ))
                                },
                                label = { Text(option.label) },
                            )
                        }
                    }
                    else -> Unit
                }
            }
            CharacterLevelUpStep.HitPoints -> state.preview.requirements.filterIsInstance<LevelUpRequirement.HitPoints>().firstOrNull()?.let { hp ->
                Text("Choose hit points for d${hp.hitDie}. Your Constitution modifier is applied during materialization.")
                var rolledText by rememberSaveable(hp.id) {
                    mutableStateOf((hp.selectedGain as? HitPointGain.Rolled)?.rolledValue?.toString().orEmpty())
                }
                var manualText by rememberSaveable(hp.id) {
                    mutableStateOf((hp.selectedGain as? HitPointGain.Manual)?.rolledValue?.toString().orEmpty())
                }
                ChoiceRow("Fixed (${hp.fixedGain})", hp.selectedGain == HitPointGain.Fixed(hp.fixedGain)) {
                    rolledText = ""
                    manualText = ""
                    dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Fixed(hp.fixedGain)))
                }
                val rolledValue = rolledText.toIntOrNull()
                OutlinedTextField(
                    value = rolledText,
                    onValueChange = { value ->
                        rolledText = value.filter(Char::isDigit)
                        manualText = ""
                        rolledText.toIntOrNull()?.takeIf { it in 1..hp.hitDie }?.let {
                            dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Rolled(it)))
                        } ?: dispatch(CharacterLevelUpIntent.HitPointsCleared)
                    },
                    label = { Text("Rolled result (1-${hp.hitDie})") },
                    supportingText = if (
                        rolledText.isNotEmpty() && (rolledValue == null || rolledValue !in 1..hp.hitDie)
                    ) {
                        { Text("Enter a roll from 1 to ${hp.hitDie}.") }
                    } else {
                        null
                    },
                    isError = rolledText.isNotEmpty() && (rolledValue == null || rolledValue !in 1..hp.hitDie),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                val manualValue = manualText.toIntOrNull()
                OutlinedTextField(
                    value = manualText,
                    onValueChange = { value ->
                        manualText = value.filter(Char::isDigit)
                        rolledText = ""
                        manualText.toIntOrNull()?.takeIf { it > 0 }?.let {
                            dispatch(CharacterLevelUpIntent.HitPointsSelected(HitPointGain.Manual(it)))
                        } ?: dispatch(CharacterLevelUpIntent.HitPointsCleared)
                    },
                    label = { Text("Manual result (positive)") },
                    supportingText = if (manualText.isNotEmpty() && (manualValue == null || manualValue <= 0)) {
                        { Text("Enter a positive hit point gain.") }
                    } else {
                        null
                    },
                    isError = manualText.isNotEmpty() && (manualValue == null || manualValue <= 0),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            CharacterLevelUpStep.AbilityScore -> state.preview.requirements.filterIsInstance<LevelUpRequirement.AbilityScoreImprovement>().firstOrNull()?.let { asi ->
                Text("Choose an ability score improvement or a feat.")
                SectionTitle("Increase one ability by +${asi.abilityPoints}")
                AbilityIds.standardOrder.filter { ability ->
                    state.preview.before.abilityScores.scoreForLevelUp(ability) + asi.abilityPoints <=
                        asi.maximumAbilityScore
                }.forEach { ability ->
                    val current = state.preview.before.abilityScores.scoreForLevelUp(ability)
                    val decision = AbilityScoreDecision.Increase(mapOf(ability to asi.abilityPoints))
                    ChoiceRow(
                        "+${asi.abilityPoints} ${ability.uppercase()} ($current → ${current + asi.abilityPoints})",
                        asi.selectedDecision == decision,
                    ) {
                        dispatch(CharacterLevelUpIntent.AbilityScoreDecisionSelected(decision))
                    }
                }
                if (asi.abilityPoints == 2) {
                    SectionTitle("Increase two abilities by +1")
                    val splitDecision = (asi.selectedDecision as? AbilityScoreDecision.Increase)
                        ?.increases
                        ?.takeIf { increases -> increases.values.all { it == 1 } }
                        .orEmpty()
                    AbilityIds.standardOrder.filter { ability ->
                        state.preview.before.abilityScores.scoreForLevelUp(ability) < asi.maximumAbilityScore
                    }.forEach { ability ->
                        val selected = ability in splitDecision
                        FilterChip(
                            selected = selected,
                            enabled = selected || splitDecision.size < asi.abilityPoints,
                            onClick = {
                                val updated = splitDecision.keys.toMutableSet().apply {
                                    if (!remove(ability)) add(ability)
                                }
                                dispatch(CharacterLevelUpIntent.AbilityScoreDecisionSelected(
                                    AbilityScoreDecision.Increase(updated.associateWith { 1 }),
                                ))
                            },
                            label = { Text("+1 ${ability.uppercase()}") },
                        )
                    }
                }
                if (asi.allowsFeat) {
                    SectionTitle("Choose a feat")
                    val eligibleFeatIds = asi.eligibleFeatIds.toSet()
                    val eligibleFeats = state.feats.filter { it.id in eligibleFeatIds }
                    if (eligibleFeats.isEmpty()) {
                        Text("No feats meet the current prerequisites and ability score caps.")
                    }
                    eligibleFeats.forEach { feat ->
                        ChoiceRow(
                            "Feat: ${feat.name}",
                            asi.selectedDecision == AbilityScoreDecision.Feat(feat.id),
                        ) {
                            dispatch(CharacterLevelUpIntent.AbilityScoreDecisionSelected(
                                AbilityScoreDecision.Feat(feat.id),
                            ))
                        }
                    }
                    asi.featEligibility.filterNot { it.eligible }.forEach { eligibility ->
                        val featName = state.feats.firstOrNull { it.id == eligibility.featId }?.name
                            ?: eligibility.featId
                        Text(
                            "Unavailable: $featName — ${eligibility.reasons.joinToString(" ")}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    state.preview.requirements.filterIsInstance<LevelUpRequirement.ChoiceSelection>()
                        .filter { it.category == LevelUpChoiceCategory.Feat }
                        .forEach { requirement ->
                            SectionTitle("${requirement.label} · choose ${requirement.choice.choose}")
                            choiceOptions(requirement).forEach { option ->
                                FilterChip(
                                    selected = option.id in requirement.selectedOptionIds,
                                    onClick = {
                                        dispatch(CharacterLevelUpIntent.ChoiceToggled(
                                            requirement.id,
                                            option.id,
                                            requirement.choice.choose,
                                        ))
                                    },
                                    label = { Text(option.label) },
                                )
                            }
                        }
                }
            }
            CharacterLevelUpStep.Spells -> {
                state.preview.requirements.filterIsInstance<LevelUpRequirement.SpellDecisions>().firstOrNull()
                    ?.let { requirement -> SpellDecisions(requirement, dispatch) }
            }
            CharacterLevelUpStep.Review -> {
                state.staleMessage?.let {
                    WarningCard(it)
                    Button(
                        enabled = !state.isSaving,
                        onClick = { dispatch(CharacterLevelUpIntent.ReloadClicked) },
                    ) { Text("Reload draft") }
                }
                state.persistenceMessage?.let { WarningCard(it) }
                CharacterLevelUpClassProgressionReview(state)
                CharacterLevelUpAbilityScoreReview(state)
                CharacterLevelUpSpellChangesReview(state)
                ReviewRow("Proficiency bonus", "+${state.preview.before.proficiencyBonus}", "+${state.preview.after.proficiencyBonus}")
                CharacterLevelUpDurabilityReview(state)
                CharacterLevelUpValidationReview(state, dispatch)
                state.blockingIssues.forEach { issue ->
                    WarningCard(issue.message)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            OutlinedButton(
                enabled = !state.isSaving,
                onClick = { dispatch(CharacterLevelUpIntent.CancelClicked) },
            ) { Text("Cancel") }
            if (state.currentStepIndex > 0) {
                OutlinedButton(
                    enabled = !state.isSaving,
                    onClick = { dispatch(CharacterLevelUpIntent.BackClicked) },
                ) { Text("Back") }
            }
            if (state.isReview) Button(enabled = state.canConfirm, onClick = { dispatch(CharacterLevelUpIntent.ConfirmClicked) }) { Text(if (state.isSaving) "Saving…" else "Confirm level up") }
            else Button(
                enabled = state.canAdvance,
                onClick = { dispatch(CharacterLevelUpIntent.NextClicked) },
            ) { Text("Next") }
        }
    }
}

@Composable
private fun SpellDecisions(
    requirement: LevelUpRequirement.SpellDecisions,
    dispatch: CharacterLevelUpDispatch,
) {
    Text(
        "${requirement.policyId.replaceFirstChar { it.uppercase() }} spell decisions for " +
            "class level ${requirement.classLevel}.",
    )
    if (requirement.requiredCantripCount > 0) {
        SpellSelection(
            title = "Choose ${requirement.requiredCantripCount} new cantrip(s)",
            options = requirement.cantripCandidates,
            selectedSpellIds = requirement.changes.learned
                .filter { it.classId == requirement.classId }
                .mapTo(hashSetOf()) { it.spellId },
        ) { spellId ->
            dispatch(CharacterLevelUpIntent.SpellChangesSelected(
                requirement.changes.toggleLearned(
                    classId = requirement.classId,
                    spellId = spellId,
                    candidates = requirement.cantripCandidates,
                    maximum = requirement.requiredCantripCount,
                ),
            ))
        }
    }
    if (requirement.requiredKnownSpellCount > 0) {
        SpellSelection(
            title = "Choose ${requirement.requiredKnownSpellCount} new known spell(s)",
            options = requirement.knownSpellCandidates,
            selectedSpellIds = requirement.changes.learned
                .filter { it.classId == requirement.classId }
                .mapTo(hashSetOf()) { it.spellId },
        ) { spellId ->
            dispatch(CharacterLevelUpIntent.SpellChangesSelected(
                requirement.changes.toggleLearned(
                    classId = requirement.classId,
                    spellId = spellId,
                    candidates = requirement.knownSpellCandidates,
                    maximum = requirement.requiredKnownSpellCount,
                ),
            ))
        }
    }
    requirement.featureSpellGrants.forEach { grant ->
        SpellSelection(
            title = "${grant.label}: choose ${grant.requiredCount} spell(s) from any class",
            options = grant.candidates,
            selectedSpellIds = grant.selectedSpellIds,
        ) { spellId ->
            dispatch(CharacterLevelUpIntent.SpellChangesSelected(
                requirement.changes.toggleFeatureGrant(
                    classId = requirement.classId,
                    featureId = grant.featureId,
                    spellId = spellId,
                    candidates = grant.candidates,
                    maximum = grant.requiredCount,
                ),
            ))
        }
    }
    if (requirement.requiredSpellbookAdditionCount > 0) {
        SpellSelection(
            title = "Add ${requirement.requiredSpellbookAdditionCount} spell(s) to your spellbook",
            options = requirement.spellbookCandidates,
            selectedSpellIds = requirement.changes.addedToSpellbook
                .filter { it.classId == requirement.classId }
                .mapTo(hashSetOf()) { it.spellId },
        ) { spellId ->
            dispatch(CharacterLevelUpIntent.SpellChangesSelected(
                requirement.changes.toggleSpellbookAddition(
                    classId = requirement.classId,
                    spellId = spellId,
                    candidates = requirement.spellbookCandidates,
                    maximum = requirement.requiredSpellbookAdditionCount,
                ),
            ))
        }
    }
    requirement.replacement?.let { replacement ->
        SectionTitle("Replace one known spell (optional)")
        if (replacement.sourceCandidates.isEmpty()) {
            Text("No currently known class spell is eligible for replacement.")
        } else {
            Text("Spell to replace")
            replacement.sourceCandidates.forEach { option ->
                FilterChip(
                    selected = option.spellId == replacement.selectedSourceSpellId,
                    onClick = {
                        val updated = if (option.spellId == replacement.selectedSourceSpellId) {
                            requirement.changes.copy(replaced = emptySet(), replacementSourceSpellId = null)
                        } else {
                            requirement.changes.copy(
                                replaced = emptySet(),
                                replacementSourceSpellId = option.spellId,
                            )
                        }
                        dispatch(CharacterLevelUpIntent.SpellChangesSelected(updated))
                    },
                    label = { Text(option.displayLabel()) },
                )
            }
            replacement.selectedSourceSpellId?.let { sourceSpellId ->
                Text("Replacement spell")
                replacement.replacementCandidates.forEach { option ->
                    FilterChip(
                        selected = option.spellId == replacement.selectedReplacementSpellId,
                        onClick = {
                            val updated = if (option.spellId == replacement.selectedReplacementSpellId) {
                                requirement.changes.copy(
                                    replaced = emptySet(),
                                    replacementSourceSpellId = sourceSpellId,
                                )
                            } else {
                                requirement.changes.copy(
                                    replaced = setOf(SpellReplacement(
                                        classId = requirement.classId,
                                        removedSpellId = sourceSpellId,
                                        learnedSpellId = option.spellId,
                                    )),
                                    replacementSourceSpellId = null,
                                )
                            }
                            dispatch(CharacterLevelUpIntent.SpellChangesSelected(updated))
                        },
                        label = { Text(option.displayLabel()) },
                    )
                }
            }
        }
    }
    requirement.preparationCapacity?.let { capacity ->
        Text("You may prepare up to $capacity spell(s) after leveling. Prepared spells remain mutable play state.")
    }
}

@Composable
private fun SpellSelection(
    title: String,
    options: List<LevelUpSpellOption>,
    selectedSpellIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    SectionTitle(title)
    if (options.isEmpty()) {
        Text("No legal spells are available from the bundled catalog.")
    }
    options.forEach { option ->
        FilterChip(
            selected = option.spellId in selectedSpellIds,
            onClick = { onToggle(option.spellId) },
            label = { Text(option.displayLabel()) },
        )
    }
}

private fun LevelUpSpellOption.displayLabel(): String =
    if (level == 0) name else "$name (level $level)"

private fun SpellChanges.toggleLearned(
    classId: String,
    spellId: String,
    candidates: List<LevelUpSpellOption>,
    maximum: Int,
): SpellChanges {
    val candidateIds = candidates.mapTo(hashSetOf()) { it.spellId }
    val updated = learned.toMutableSet()
    val ref = ClassSpellRef(classId, spellId)
    if (!updated.remove(ref)) {
        val selectedInGroup = updated.filter { it.classId == classId && it.spellId in candidateIds }
        if (selectedInGroup.size >= maximum) updated.remove(selectedInGroup.first())
        updated.add(ref)
    }
    return copy(learned = updated)
}

private fun SpellChanges.toggleSpellbookAddition(
    classId: String,
    spellId: String,
    candidates: List<LevelUpSpellOption>,
    maximum: Int,
): SpellChanges {
    val candidateIds = candidates.mapTo(hashSetOf()) { it.spellId }
    val updated = addedToSpellbook.toMutableSet()
    val ref = ClassSpellRef(classId, spellId)
    if (!updated.remove(ref)) {
        val selectedInGroup = updated.filter { it.classId == classId && it.spellId in candidateIds }
        if (selectedInGroup.size >= maximum) updated.remove(selectedInGroup.first())
        updated.add(ref)
    }
    return copy(addedToSpellbook = updated)
}

private fun SpellChanges.toggleFeatureGrant(
    classId: String,
    featureId: String,
    spellId: String,
    candidates: List<LevelUpSpellOption>,
    maximum: Int,
): SpellChanges {
    val candidateIds = candidates.mapTo(hashSetOf()) { it.spellId }
    val updatedGrants = featureLearned.toMutableMap()
    val selected = updatedGrants[featureId].orEmpty().toMutableSet()
    val ref = ClassSpellRef(classId, spellId)
    if (!selected.remove(ref)) {
        val selectedInGroup = selected.filter { it.classId == classId && it.spellId in candidateIds }
        if (selectedInGroup.size >= maximum) selected.remove(selectedInGroup.first())
        selected.add(ref)
    }
    updatedGrants[featureId] = selected
    return copy(featureLearned = updatedGrants)
}

@Composable private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) = Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected, onClick = onClick); Text(label) } }
@Composable private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleSmall)
@Composable private fun WarningCard(text: String) = Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) { Text(text, Modifier.fillMaxWidth().padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer) }
@Composable private fun ReviewRow(label: String, before: String, after: String) = Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text("$before → $after") }
@Composable private fun MessagePane(text: String, modifier: Modifier) = Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { Text(text) }

private data class ChoiceUiOption(val id: String, val label: String = id)

private fun choiceOptions(requirement: LevelUpRequirement.ChoiceSelection): List<ChoiceUiOption> =
    requirement.options.takeIf { it.isNotEmpty() }
        ?.map { ChoiceUiOption(it.id, it.label) }
        ?: choiceOptions(requirement.choice)

private fun choiceOptions(choice: Choice): List<ChoiceUiOption> = when (choice) {
    is Choice.FavoredEnemyChoice -> choice.from.map(::ChoiceUiOption)
    is Choice.TerrainTypeChoice -> choice.from.map(::ChoiceUiOption)
    is Choice.ProficiencyChoice -> choice.from.map(::ChoiceUiOption)
    is Choice.FeatureChoice -> choice.from.map(::ChoiceUiOption)
    is Choice.OptionsArrayChoice -> choice.from.map(::ChoiceUiOption)
    is Choice.EquipmentChoice -> choice.from.map(::ChoiceUiOption)
    is Choice.AbilityBonusChoice -> choice.from.flatMap { option ->
        option.map { (ability, increase) -> ChoiceUiOption(ability, "+$increase ${ability.uppercase()}") }
    }
    is Choice.ResourceListChoice, is Choice.EquipmentCategoriesChoice, is Choice.NestedChoice,
    is Choice.FromAllChoice, is Choice.IdealChoice -> emptyList()
}

private fun com.github.arhor.spellbindr.domain.model.AbilityScores.scoreForLevelUp(abilityId: String): Int =
    when (abilityId) {
        AbilityIds.STR -> strength
        AbilityIds.DEX -> dexterity
        AbilityIds.CON -> constitution
        AbilityIds.INT -> intelligence
        AbilityIds.WIS -> wisdom
        AbilityIds.CHA -> charisma
        else -> Int.MIN_VALUE
    }
