package io.github.arhor.dnd.companion.ui.feature.character.levelup

import io.github.arhor.dnd.companion.domain.model.AbilityScoreDecision
import io.github.arhor.dnd.companion.domain.model.HitPointGain
import io.github.arhor.dnd.companion.domain.model.LevelUpPlan
import io.github.arhor.dnd.companion.domain.model.SpellChanges

sealed interface CharacterLevelUpIntent {
    data class ClassSelected(val classId: String) : CharacterLevelUpIntent
    data class SubclassSelected(val subclassId: String) : CharacterLevelUpIntent
    data class ChoiceToggled(val requirementId: String, val optionId: String, val maximum: Int) : CharacterLevelUpIntent
    data class HitPointsSelected(val gain: HitPointGain) : CharacterLevelUpIntent
    data object HitPointsCleared : CharacterLevelUpIntent
    data class AbilityScoreDecisionSelected(val decision: AbilityScoreDecision) : CharacterLevelUpIntent
    data class SpellChangesSelected(val changes: SpellChanges) : CharacterLevelUpIntent
    data class AcknowledgementChanged(val issueCode: String, val acknowledged: Boolean) : CharacterLevelUpIntent
    data object NextClicked : CharacterLevelUpIntent
    data object BackClicked : CharacterLevelUpIntent
    data object CancelClicked : CharacterLevelUpIntent
    data object ConfirmClicked : CharacterLevelUpIntent
    data object ReloadClicked : CharacterLevelUpIntent
    data object RetryClicked : CharacterLevelUpIntent
}

typealias CharacterLevelUpDispatch = (CharacterLevelUpIntent) -> Unit

internal fun LevelUpPlan.applySpellChangesSelection(
    intent: CharacterLevelUpIntent.SpellChangesSelected,
): LevelUpPlan = copy(selections = selections.copy(spellChanges = intent.changes))
