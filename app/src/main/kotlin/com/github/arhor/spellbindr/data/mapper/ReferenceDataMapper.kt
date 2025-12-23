package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.CharacterRace
import com.github.arhor.spellbindr.domain.model.Alignment
import com.github.arhor.spellbindr.domain.model.EntityRef
import com.github.arhor.spellbindr.domain.model.Race
import com.github.arhor.spellbindr.domain.model.Trait
import com.github.arhor.spellbindr.data.model.Alignment as DataAlignment
import com.github.arhor.spellbindr.data.model.Trait as DataTrait

fun DataAlignment.toDomain(): Alignment = Alignment(
    id = id,
    name = name,
    desc = desc,
    abbr = abbr,
)

fun DataTrait.toDomain(): Trait = Trait(
    id = id,
    name = name,
    desc = desc,
    effects = effects,
    spellChoice = spellChoice,
    languageChoice = languageChoice,
    proficiencyChoice = proficiencyChoice,
    abilityBonusChoice = abilityBonusChoice,
    draconicAncestryChoice = draconicAncestryChoice,
)

fun CharacterRace.toDomain(): Race = Race(
    id = id,
    name = name,
    traits = traits.map { it.toDomain() },
    subraces = subraces.map { it.toDomain() },
)

private fun CharacterRace.Subrace.toDomain(): Race.Subrace = Race.Subrace(
    id = id,
    name = name,
    desc = desc,
    traits = traits.map { it.toDomain() },
)

private fun EntityRef.toDomain(): EntityRef = EntityRef(id)
