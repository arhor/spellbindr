package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.CharacterEntity
import com.github.arhor.spellbindr.domain.model.Character

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
