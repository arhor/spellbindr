package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.CharacterLevelRecord
import com.github.arhor.spellbindr.domain.model.CharacterProgression
import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.ProgressionOrigin
import com.github.arhor.spellbindr.domain.model.SpellChanges
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupUiState
import com.github.arhor.spellbindr.ui.feature.character.guided.GuidedCharacterSetupViewModel

internal fun buildGuidedCharacterProgression(
    content: GuidedCharacterSetupUiState.Content,
): CharacterProgression {
    val selection = content.selection
    val clazz = requireNotNull(content.classes.firstOrNull { it.id == selection.classId }) {
        "A selected class is required to build guided character progression."
    }
    val classFeatureIds = clazz.levels
        .firstOrNull { it.level == 1 }
        ?.features
        .orEmpty()
        .toSet()
    val featureChoices = selection.choiceSelections
        .asSequence()
        .filter { (key, _) -> key.startsWith(FEATURE_CHOICE_PREFIX) }
        .map { (key, optionIds) -> key.removePrefix(FEATURE_CHOICE_PREFIX) to optionIds }
        .filter { (featureId, _) -> featureId in classFeatureIds }
        .toMap(linkedMapOf())
    val learnedSpells = buildSet {
        selection.choiceSelections[GuidedCharacterSetupViewModel.spellCantripsChoiceKey()]
            .orEmpty()
            .mapTo(this) { spellId -> ClassSpellRef(classId = clazz.id, spellId = spellId) }
        selection.choiceSelections[GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()]
            .orEmpty()
            .mapTo(this) { spellId -> ClassSpellRef(classId = clazz.id, spellId = spellId) }
    }

    return CharacterProgression(
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = content.referenceDataVersion.toString(),
        origin = ProgressionOrigin.Guided,
        levels = listOf(
            CharacterLevelRecord(
                characterLevel = 1,
                classId = clazz.id,
                classLevel = 1,
                subclassId = selection.subclassId,
                hitPointGain = HitPointGain.Fixed(rolledValue = clazz.hitDie),
                featureChoices = featureChoices,
                spellChanges = SpellChanges(learned = learnedSpells),
            ),
        ),
    )
}

private const val FEATURE_CHOICE_PREFIX = "feature/"
