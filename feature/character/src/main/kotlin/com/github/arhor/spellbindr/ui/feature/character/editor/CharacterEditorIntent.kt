package com.github.arhor.spellbindr.ui.feature.character.editor

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.Skill

/**
 * Represents user intents for the Character Editor screen.
 */
sealed interface CharacterEditorIntent {
    data object SaveClicked : CharacterEditorIntent
    data class NameChanged(val value: String) : CharacterEditorIntent
    data class ClassChanged(val value: String) : CharacterEditorIntent
    data class LevelChanged(val value: String) : CharacterEditorIntent
    data class RaceChanged(val value: String) : CharacterEditorIntent
    data class BackgroundChanged(val value: String) : CharacterEditorIntent
    data class AlignmentChanged(val value: String) : CharacterEditorIntent
    data class ExperienceChanged(val value: String) : CharacterEditorIntent
    data class AbilityChanged(val abilityId: AbilityId, val value: String) : CharacterEditorIntent
    data class ProficiencyBonusChanged(val value: String) : CharacterEditorIntent
    data class InspirationChanged(val value: Boolean) : CharacterEditorIntent
    data class MaxHpChanged(val value: String) : CharacterEditorIntent
    data class CurrentHpChanged(val value: String) : CharacterEditorIntent
    data class TemporaryHpChanged(val value: String) : CharacterEditorIntent
    data class ArmorClassChanged(val value: String) : CharacterEditorIntent
    data class InitiativeChanged(val value: String) : CharacterEditorIntent
    data class SpeedChanged(val value: String) : CharacterEditorIntent
    data class HitDiceChanged(val value: String) : CharacterEditorIntent
    data class SavingThrowProficiencyChanged(val abilityId: AbilityId, val value: Boolean) : CharacterEditorIntent
    data class SkillProficiencyChanged(val skill: Skill, val value: Boolean) : CharacterEditorIntent
    data class SkillExpertiseChanged(val skill: Skill, val value: Boolean) : CharacterEditorIntent
    data class SensesChanged(val value: String) : CharacterEditorIntent
    data class LanguagesChanged(val value: String) : CharacterEditorIntent
    data class ProficienciesChanged(val value: String) : CharacterEditorIntent
    data class AttacksChanged(val value: String) : CharacterEditorIntent
    data class FeaturesChanged(val value: String) : CharacterEditorIntent
    data class EquipmentChanged(val value: String) : CharacterEditorIntent
    data class PersonalityTraitsChanged(val value: String) : CharacterEditorIntent
    data class IdealsChanged(val value: String) : CharacterEditorIntent
    data class BondsChanged(val value: String) : CharacterEditorIntent
    data class FlawsChanged(val value: String) : CharacterEditorIntent
    data class NotesChanged(val value: String) : CharacterEditorIntent
}

/**
 * Dispatch function for [CharacterEditorIntent] events.
 */
typealias CharacterEditorDispatch = (CharacterEditorIntent) -> Unit
