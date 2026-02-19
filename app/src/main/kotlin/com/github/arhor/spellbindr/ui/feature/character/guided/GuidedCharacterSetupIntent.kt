package com.github.arhor.spellbindr.ui.feature.character.guided

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.ui.feature.character.guided.model.AbilityScoreMethod
import com.github.arhor.spellbindr.ui.feature.character.guided.model.GuidedStep

/**
 * Represents user intents for the Guided Character Setup screen.
 */
sealed interface GuidedCharacterSetupIntent {
    data class NameChanged(val value: String) : GuidedCharacterSetupIntent
    data class ClassSelected(val classId: String) : GuidedCharacterSetupIntent
    data class SubclassSelected(val subclassId: String) : GuidedCharacterSetupIntent
    data class RaceSelected(val raceId: String) : GuidedCharacterSetupIntent
    data class SubraceSelected(val subraceId: String) : GuidedCharacterSetupIntent
    data class BackgroundSelected(val backgroundId: String) : GuidedCharacterSetupIntent
    data class AbilityMethodSelected(val method: AbilityScoreMethod) : GuidedCharacterSetupIntent
    data class StandardArrayAssigned(val abilityId: AbilityId, val score: Int?) : GuidedCharacterSetupIntent
    data class PointBuyIncrement(val abilityId: AbilityId) : GuidedCharacterSetupIntent
    data class PointBuyDecrement(val abilityId: AbilityId) : GuidedCharacterSetupIntent
    data class ChoiceToggled(val key: String, val optionId: String, val maxSelected: Int) : GuidedCharacterSetupIntent
    data object NextClicked : GuidedCharacterSetupIntent
    data object BackClicked : GuidedCharacterSetupIntent
    data object CreateClicked : GuidedCharacterSetupIntent
    data class GoToStep(val step: GuidedStep) : GuidedCharacterSetupIntent
}

/**
 * Dispatch function for [GuidedCharacterSetupIntent] events.
 */
typealias GuidedCharacterSetupDispatch = (GuidedCharacterSetupIntent) -> Unit
