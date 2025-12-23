package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.local.db.CharacterEntity
import com.github.arhor.spellbindr.domain.model.Character

/**
 * Maps the persistence entity [CharacterEntity] to the domain model [Character].
 */
fun CharacterEntity.toDomain(): Character = Character(
    id = id,
    name = name,
    race = race,
    subrace = subrace,
    classes = classes,
    background = background,
    abilityScores = abilityScores,
    proficiencies = proficiencies,
    equipment = equipment,
    inventory = inventory,
    spells = spells,
)

/**
 * Maps the domain model [Character] back to the persistence entity [CharacterEntity].
 * Note: This mapping does NOT populate [CharacterEntity.manualSheet], which is handled separately
 * by the repository when saving character sheets.
 */
fun Character.toEntity(): CharacterEntity = CharacterEntity(
    id = id,
    name = name,
    race = race,
    subrace = subrace,
    classes = classes,
    background = background,
    abilityScores = abilityScores,
    proficiencies = proficiencies,
    equipment = equipment,
    inventory = inventory,
    spells = spells,
)
