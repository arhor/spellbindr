package io.github.arhor.dnd.companion.ui.feature.character.guided.internal

import io.github.arhor.dnd.companion.domain.model.CharacterLevelRecord
import io.github.arhor.dnd.companion.domain.model.CharacterProgression
import io.github.arhor.dnd.companion.domain.model.HitPointGain
import io.github.arhor.dnd.companion.domain.model.ProgressionOrigin
import io.github.arhor.dnd.companion.domain.model.ProficiencyChoiceSelection
import io.github.arhor.dnd.companion.ui.feature.character.guided.GuidedCharacterSetupUiState
import io.github.arhor.dnd.companion.ui.feature.character.guided.GuidedCharacterSetupViewModel

internal fun buildGuidedCharacterProgression(
    content: GuidedCharacterSetupUiState.Content,
): CharacterProgression {
    val selection = content.selection
    val clazz = requireNotNull(content.classes.firstOrNull { it.id == selection.classId }) {
        "A selected class is required to build guided character progression."
    }
    val levelOneFeatureIds = findGuidedLevelOneFeatureChoices(
        clazz = clazz,
        subclassId = selection.subclassId,
        featuresById = content.featuresById,
    )
        .mapTo(linkedSetOf()) { (featureId, _) -> featureId }
        .toSet()
    val featureChoices = selection.choiceSelections
        .asSequence()
        .filter { (key, _) -> key.startsWith(FEATURE_CHOICE_PREFIX) }
        .map { (key, optionIds) -> key.removePrefix(FEATURE_CHOICE_PREFIX) to optionIds }
        .filter { (featureId, _) -> featureId in levelOneFeatureIds }
        .toMap(linkedMapOf())
    val proficiencyChoices = clazz.proficiencyChoices.mapIndexedNotNull { index, _ ->
        val selected = selection.choiceSelections[
            GuidedCharacterSetupViewModel.classProficiencyChoiceKey(index)
        ].orEmpty()
        selected.takeIf { it.isNotEmpty() }?.let {
            ProficiencyChoiceSelection(
                choiceId = "class/${clazz.id}/starting-proficiency/${index + 1}",
                selectedProficiencyIds = it,
            )
        }
    }
    val spellChanges = buildGuidedLevelOneSpellChanges(
        classId = clazz.id,
        cantripSpellIds = selection.choiceSelections[
            GuidedCharacterSetupViewModel.spellCantripsChoiceKey()
        ].orEmpty(),
        levelOneSpellIds = selection.choiceSelections[
            GuidedCharacterSetupViewModel.spellLevel1ChoiceKey()
        ].orEmpty(),
    )

    return CharacterProgression(
        rulesetId = CharacterProgression.SUPPORTED_RULESET_ID,
        referenceDataVersion = content.referenceDataVersion,
        origin = ProgressionOrigin.Guided,
        levels = listOf(
            CharacterLevelRecord(
                characterLevel = 1,
                classId = clazz.id,
                classLevel = 1,
                subclassId = selection.subclassId,
                hitPointGain = HitPointGain.Fixed(rolledValue = clazz.hitDie),
                featureChoices = featureChoices,
                proficiencyChoices = proficiencyChoices,
                spellChanges = spellChanges,
            ),
        ),
    )
}

private const val FEATURE_CHOICE_PREFIX = "feature/"
