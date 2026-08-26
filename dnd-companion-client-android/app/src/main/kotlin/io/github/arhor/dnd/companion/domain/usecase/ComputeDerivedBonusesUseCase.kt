package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterEditorDerivedBonuses
import io.github.arhor.dnd.companion.domain.model.CharacterEditorInput
import io.github.arhor.dnd.companion.domain.model.SavingThrowBonus
import io.github.arhor.dnd.companion.domain.model.SkillBonus
import io.github.arhor.dnd.companion.domain.usecase.internal.proficiencyBonusFor
import io.github.arhor.dnd.companion.domain.usecase.internal.resolveAbilityScores
import io.github.arhor.dnd.companion.domain.usecase.internal.resolveProficiency
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
