package io.github.arhor.dnd.companion.domain.usecase

import io.github.arhor.dnd.companion.domain.model.CharacterEditorInput
import io.github.arhor.dnd.companion.domain.model.CharacterSheet
import io.github.arhor.dnd.companion.domain.model.SavingThrowEntry
import io.github.arhor.dnd.companion.domain.model.SkillEntry
import io.github.arhor.dnd.companion.domain.usecase.internal.proficiencyBonusFor
import io.github.arhor.dnd.companion.domain.usecase.internal.resolveAbilityScores
import io.github.arhor.dnd.companion.domain.usecase.internal.resolveProficiency
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildCharacterSheetFromInputsUseCase @Inject constructor() {

    operator fun invoke(input: CharacterEditorInput, base: CharacterSheet?): CharacterSheet {
        val ensuredId = input.characterId ?: base?.id ?: UUID.randomUUID().toString()
        val abilityScores = input.resolveAbilityScores()
        val proficiencyValue = input.resolveProficiency(defaultValue = 2)
        val baseline = base ?: CharacterSheet(id = ensuredId)

        return baseline.copy(
            id = ensuredId,
            name = input.name.trim(),
            level = input.level.toIntOrNull() ?: 1,
            className = input.className.trim(),
            race = input.race.trim(),
            background = input.background.trim(),
            alignment = input.alignment.trim(),
            experiencePoints = input.experiencePoints.toIntOrNull(),
            abilityScores = abilityScores,
            proficiencyBonus = proficiencyValue,
            inspiration = input.inspiration,
            maxHitPoints = input.maxHitPoints.toIntOrNull() ?: baseline.maxHitPoints,
            currentHitPoints = input.currentHitPoints.toIntOrNull() ?: baseline.currentHitPoints,
            temporaryHitPoints = input.temporaryHitPoints.toIntOrNull() ?: baseline.temporaryHitPoints,
            armorClass = input.armorClass.toIntOrNull() ?: 10,
            initiative = input.initiative.toIntOrNull() ?: 0,
            speed = input.speed.trim(),
            hitDice = input.hitDice.trim(),
            savingThrows = input.savingThrows.map { entry ->
                SavingThrowEntry(
                    abilityId = entry.abilityId,
                    bonus = abilityScores.modifierFor(entry.abilityId) + entry.proficiencyBonusFor(proficiencyValue),
                    proficient = entry.proficient,
                )
            },
            skills = input.skills.map { entry ->
                SkillEntry(
                    skill = entry.skill,
                    bonus = abilityScores.modifierFor(entry.skill.abilityId) + entry.proficiencyBonusFor(
                        proficiencyValue
                    ),
                    proficient = entry.proficient,
                    expertise = entry.expertise,
                )
            },
            senses = input.senses,
            languages = input.languages,
            proficiencies = input.proficiencies,
            attacksAndCantrips = input.attacksAndCantrips,
            featuresAndTraits = input.featuresAndTraits,
            equipment = input.equipment,
            personalityTraits = input.personalityTraits,
            ideals = input.ideals,
            bonds = input.bonds,
            flaws = input.flaws,
            notes = input.notes,
        )
    }
}
