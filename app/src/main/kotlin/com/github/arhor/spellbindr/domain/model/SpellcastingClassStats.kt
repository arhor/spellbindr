package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/** Derived spellcasting values owned by one character class. */
@Serializable
data class SpellcastingClassStats(
    val abilityId: AbilityId,
    val spellSaveDc: Int,
    val spellAttackBonus: Int,
)

/**
 * Calculates per-class spellcasting values from a level-up snapshot.
 *
 * Multiclass characters intentionally keep one entry per spellcasting class so different casting abilities never
 * bleed into one another. Classes are included only after their configured spellcasting acquisition level.
 */
fun LevelUpSnapshot.calculateSpellcastingClassStats(
    classes: List<CharacterClass>,
): Map<String, SpellcastingClassStats> {
    val classesById = classes.associateBy(CharacterClass::id)
    return classLevels.toSortedMap().mapNotNull { (classId, classLevel) ->
        val spellcasting = classesById[classId]?.spellcasting ?: return@mapNotNull null
        if (classLevel < spellcasting.level) return@mapNotNull null
        val abilityId = spellcasting.spellcastingAbility.id.trim().lowercase()
            .takeIf { it in AbilityIds.standardOrder }
            ?: return@mapNotNull null
        val abilityModifier = abilityScores.modifierFor(abilityId)
        classId to SpellcastingClassStats(
            abilityId = abilityId,
            spellSaveDc = 8 + proficiencyBonus + abilityModifier,
            spellAttackBonus = proficiencyBonus + abilityModifier,
        )
    }.toMap(linkedMapOf())
}
