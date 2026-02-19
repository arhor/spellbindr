package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterEditorDerivedBonuses
import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.SavingThrowBonus
import com.github.arhor.spellbindr.domain.model.SkillBonus
import com.github.arhor.spellbindr.domain.usecase.internal.proficiencyBonusFor
import com.github.arhor.spellbindr.domain.usecase.internal.resolveAbilityScores
import com.github.arhor.spellbindr.domain.usecase.internal.resolveProficiency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComputeDerivedBonusesUseCase @Inject constructor() {
    operator fun invoke(input: CharacterEditorInput): CharacterEditorDerivedBonuses {
        val abilityScores = input.resolveAbilityScores()
        val proficiencyValue = input.resolveProficiency(defaultValue = 0)

        return CharacterEditorDerivedBonuses(
            savingThrows = input.savingThrows.map { entry ->
                SavingThrowBonus(
                    abilityId = entry.abilityId,
                    bonus = abilityScores.modifierFor(entry.abilityId) + entry.proficiencyBonusFor(proficiencyValue),
                )
            },
            skills = input.skills.map { entry ->
                SkillBonus(
                    skill = entry.skill,
                    bonus = abilityScores.modifierFor(entry.skill.abilityId) + entry.proficiencyBonusFor(
                        proficiencyValue
                    ),
                )
            },
        )
    }
}
