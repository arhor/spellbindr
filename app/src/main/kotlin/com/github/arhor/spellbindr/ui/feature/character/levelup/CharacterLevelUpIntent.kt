package com.github.arhor.spellbindr.ui.feature.character.levelup

import com.github.arhor.spellbindr.domain.model.AbilityScoreDecision
import com.github.arhor.spellbindr.domain.model.HitPointGain
import com.github.arhor.spellbindr.domain.model.SpellChanges

sealed interface CharacterLevelUpIntent {
    data class ClassSelected(val classId: String) : CharacterLevelUpIntent
    data class SubclassSelected(val subclassId: String) : CharacterLevelUpIntent
    data class ChoiceToggled(val requirementId: String, val optionId: String, val maximum: Int) : CharacterLevelUpIntent
    data class HitPointsSelected(val gain: HitPointGain) : CharacterLevelUpIntent
    data class AbilityScoreDecisionSelected(val decision: AbilityScoreDecision) : CharacterLevelUpIntent
    data class SpellChangesSelected(val changes: SpellChanges) : CharacterLevelUpIntent
    data class AcknowledgementChanged(val issueCode: String, val acknowledged: Boolean) : CharacterLevelUpIntent
    data object NextClicked : CharacterLevelUpIntent
    data object BackClicked : CharacterLevelUpIntent
    data object CancelClicked : CharacterLevelUpIntent
    data object ConfirmClicked : CharacterLevelUpIntent
    data object ReloadClicked : CharacterLevelUpIntent
}

typealias CharacterLevelUpDispatch = (CharacterLevelUpIntent) -> Unit
