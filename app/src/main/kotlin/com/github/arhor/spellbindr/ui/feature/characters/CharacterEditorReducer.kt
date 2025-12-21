package com.github.arhor.spellbindr.ui.feature.characters

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase

sealed interface CharacterEditorAction {
    data class NameChanged(val value: String) : CharacterEditorAction
    data class ClassChanged(val value: String) : CharacterEditorAction
    data class LevelChanged(val value: String) : CharacterEditorAction
    data class RaceChanged(val value: String) : CharacterEditorAction
    data class BackgroundChanged(val value: String) : CharacterEditorAction
    data class AlignmentChanged(val value: String) : CharacterEditorAction
    data class ExperienceChanged(val value: String) : CharacterEditorAction
    data class AbilityChanged(val ability: Ability, val value: String) : CharacterEditorAction
    data class ProficiencyBonusChanged(val value: String) : CharacterEditorAction
    data class InspirationChanged(val value: Boolean) : CharacterEditorAction
    data class MaxHpChanged(val value: String) : CharacterEditorAction
    data class CurrentHpChanged(val value: String) : CharacterEditorAction
    data class TemporaryHpChanged(val value: String) : CharacterEditorAction
    data class ArmorClassChanged(val value: String) : CharacterEditorAction
    data class InitiativeChanged(val value: String) : CharacterEditorAction
    data class SpeedChanged(val value: String) : CharacterEditorAction
    data class HitDiceChanged(val value: String) : CharacterEditorAction
    data class SavingThrowProficiencyChanged(val ability: Ability, val value: Boolean) : CharacterEditorAction
    data class SkillProficiencyChanged(val skill: Skill, val value: Boolean) : CharacterEditorAction
    data class SkillExpertiseChanged(val skill: Skill, val value: Boolean) : CharacterEditorAction
    data class SensesChanged(val value: String) : CharacterEditorAction
    data class LanguagesChanged(val value: String) : CharacterEditorAction
    data class ProficienciesChanged(val value: String) : CharacterEditorAction
    data class AttacksChanged(val value: String) : CharacterEditorAction
    data class FeaturesChanged(val value: String) : CharacterEditorAction
    data class EquipmentChanged(val value: String) : CharacterEditorAction
    data class PersonalityTraitsChanged(val value: String) : CharacterEditorAction
    data class IdealsChanged(val value: String) : CharacterEditorAction
    data class BondsChanged(val value: String) : CharacterEditorAction
    data class FlawsChanged(val value: String) : CharacterEditorAction
    data class NotesChanged(val value: String) : CharacterEditorAction
}

fun reduceCharacterEditorState(
    state: CharacterEditorUiState,
    action: CharacterEditorAction,
    computeDerivedBonusesUseCase: ComputeDerivedBonusesUseCase,
): CharacterEditorUiState {
    val updated = when (action) {
        is CharacterEditorAction.NameChanged -> state.copy(name = action.value, nameError = null)
        is CharacterEditorAction.ClassChanged -> state.copy(className = action.value)
        is CharacterEditorAction.LevelChanged -> state.copy(level = action.value, levelError = null)
        is CharacterEditorAction.RaceChanged -> state.copy(race = action.value)
        is CharacterEditorAction.BackgroundChanged -> state.copy(background = action.value)
        is CharacterEditorAction.AlignmentChanged -> state.copy(alignment = action.value)
        is CharacterEditorAction.ExperienceChanged -> state.copy(experiencePoints = action.value)
        is CharacterEditorAction.AbilityChanged -> state.copy(
            abilities = state.abilities.map { field ->
                if (field.ability == action.ability) field.copy(score = action.value, error = null) else field
            },
        )
        is CharacterEditorAction.ProficiencyBonusChanged -> state.copy(proficiencyBonus = action.value)
        is CharacterEditorAction.InspirationChanged -> state.copy(inspiration = action.value)
        is CharacterEditorAction.MaxHpChanged -> state.copy(maxHitPoints = action.value, maxHitPointsError = null)
        is CharacterEditorAction.CurrentHpChanged -> state.copy(currentHitPoints = action.value)
        is CharacterEditorAction.TemporaryHpChanged -> state.copy(temporaryHitPoints = action.value)
        is CharacterEditorAction.ArmorClassChanged -> state.copy(armorClass = action.value)
        is CharacterEditorAction.InitiativeChanged -> state.copy(initiative = action.value)
        is CharacterEditorAction.SpeedChanged -> state.copy(speed = action.value)
        is CharacterEditorAction.HitDiceChanged -> state.copy(hitDice = action.value)
        is CharacterEditorAction.SavingThrowProficiencyChanged -> state.copy(
            savingThrows = state.savingThrows.map { entry ->
                if (entry.ability == action.ability) entry.copy(proficient = action.value) else entry
            },
        )
        is CharacterEditorAction.SkillProficiencyChanged -> state.copy(
            skills = state.skills.map { entry ->
                if (entry.skill == action.skill) entry.copy(proficient = action.value) else entry
            },
        )
        is CharacterEditorAction.SkillExpertiseChanged -> state.copy(
            skills = state.skills.map { entry ->
                if (entry.skill == action.skill) entry.copy(expertise = action.value) else entry
            },
        )
        is CharacterEditorAction.SensesChanged -> state.copy(senses = action.value)
        is CharacterEditorAction.LanguagesChanged -> state.copy(languages = action.value)
        is CharacterEditorAction.ProficienciesChanged -> state.copy(proficiencies = action.value)
        is CharacterEditorAction.AttacksChanged -> state.copy(attacksAndCantrips = action.value)
        is CharacterEditorAction.FeaturesChanged -> state.copy(featuresAndTraits = action.value)
        is CharacterEditorAction.EquipmentChanged -> state.copy(equipment = action.value)
        is CharacterEditorAction.PersonalityTraitsChanged -> state.copy(personalityTraits = action.value)
        is CharacterEditorAction.IdealsChanged -> state.copy(ideals = action.value)
        is CharacterEditorAction.BondsChanged -> state.copy(bonds = action.value)
        is CharacterEditorAction.FlawsChanged -> state.copy(flaws = action.value)
        is CharacterEditorAction.NotesChanged -> state.copy(notes = action.value)
    }

    return if (action.requiresDerivedBonuses()) {
        updated.withDerivedBonuses(computeDerivedBonusesUseCase(updated.toDomainInput()))
    } else {
        updated
    }
}

private fun CharacterEditorAction.requiresDerivedBonuses(): Boolean = when (this) {
    is CharacterEditorAction.AbilityChanged,
    is CharacterEditorAction.ProficiencyBonusChanged,
    is CharacterEditorAction.SavingThrowProficiencyChanged,
    is CharacterEditorAction.SkillProficiencyChanged,
    is CharacterEditorAction.SkillExpertiseChanged -> true
    else -> false
}
