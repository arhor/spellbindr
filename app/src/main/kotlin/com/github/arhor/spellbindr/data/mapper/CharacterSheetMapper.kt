package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AbilityScores as DataAbilityScores
import com.github.arhor.spellbindr.data.model.CharacterSheetSnapshot
import com.github.arhor.spellbindr.data.model.CharacterSpell as DataCharacterSpell
import com.github.arhor.spellbindr.data.model.DeathSaveState as DataDeathSaveState
import com.github.arhor.spellbindr.data.model.SavingThrowEntry as DataSavingThrowEntry
import com.github.arhor.spellbindr.data.model.SkillEntry as DataSkillEntry
import com.github.arhor.spellbindr.data.model.SpellSlotState as DataSpellSlotState
import com.github.arhor.spellbindr.data.model.Weapon as DataWeapon
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.CharacterSpell
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.SkillEntry
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.Weapon

fun CharacterSheetSnapshot.toDomain(id: String): CharacterSheet = CharacterSheet(
    id = id,
    name = name,
    level = level,
    className = className,
    race = race,
    background = background,
    alignment = alignment,
    experiencePoints = experiencePoints,
    abilityScores = abilityScores.toDomain(),
    proficiencyBonus = proficiencyBonus,
    inspiration = inspiration,
    maxHitPoints = maxHitPoints,
    currentHitPoints = currentHitPoints,
    temporaryHitPoints = temporaryHitPoints,
    armorClass = armorClass,
    initiative = initiative,
    speed = speed,
    hitDice = hitDice,
    deathSaves = deathSaves.toDomain(),
    spellSlots = spellSlots.map { it.toDomain() },
    savingThrows = savingThrows.map { it.toDomain() },
    skills = skills.map { it.toDomain() },
    senses = senses,
    languages = languages,
    proficiencies = proficiencies,
    attacksAndCantrips = attacksAndCantrips,
    featuresAndTraits = featuresAndTraits,
    equipment = equipment,
    personalityTraits = personalityTraits,
    ideals = ideals,
    bonds = bonds,
    flaws = flaws,
    notes = notes,
    characterSpells = characterSpells.map { it.toDomain() },
    weapons = weapons.map { it.toDomain() },
)

fun CharacterSheet.toSnapshot(): CharacterSheetSnapshot = CharacterSheetSnapshot(
    name = name,
    level = level,
    className = className,
    race = race,
    background = background,
    alignment = alignment,
    experiencePoints = experiencePoints,
    abilityScores = abilityScores.toData(),
    proficiencyBonus = proficiencyBonus,
    inspiration = inspiration,
    maxHitPoints = maxHitPoints,
    currentHitPoints = currentHitPoints,
    temporaryHitPoints = temporaryHitPoints,
    armorClass = armorClass,
    initiative = initiative,
    speed = speed,
    hitDice = hitDice,
    deathSaves = deathSaves.toData(),
    spellSlots = spellSlots.map { it.toData() },
    savingThrows = savingThrows.map { it.toData() },
    skills = skills.map { it.toData() },
    senses = senses,
    languages = languages,
    proficiencies = proficiencies,
    attacksAndCantrips = attacksAndCantrips,
    featuresAndTraits = featuresAndTraits,
    equipment = equipment,
    personalityTraits = personalityTraits,
    ideals = ideals,
    bonds = bonds,
    flaws = flaws,
    notes = notes,
    characterSpells = characterSpells.map { it.toData() },
    weapons = weapons.map { it.toData() },
)

private fun DataAbilityScores.toDomain(): AbilityScores = AbilityScores(
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma,
)

private fun AbilityScores.toData(): DataAbilityScores = DataAbilityScores(
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma,
)

private fun DataDeathSaveState.toDomain(): DeathSaveState = DeathSaveState(
    successes = successes,
    failures = failures,
)

private fun DeathSaveState.toData(): DataDeathSaveState = DataDeathSaveState(
    successes = successes,
    failures = failures,
)

private fun DataSpellSlotState.toDomain(): SpellSlotState = SpellSlotState(
    level = level,
    total = total,
    expended = expended,
)

private fun SpellSlotState.toData(): DataSpellSlotState = DataSpellSlotState(
    level = level,
    total = total,
    expended = expended,
)

private fun DataSavingThrowEntry.toDomain(): SavingThrowEntry = SavingThrowEntry(
    ability = ability.toDomain(),
    bonus = bonus,
    proficient = proficient,
)

private fun SavingThrowEntry.toData(): DataSavingThrowEntry = DataSavingThrowEntry(
    ability = ability.toData(),
    bonus = bonus,
    proficient = proficient,
)

private fun DataSkillEntry.toDomain(): SkillEntry = SkillEntry(
    skill = skill.toDomain(),
    bonus = bonus,
    proficient = proficient,
    expertise = expertise,
)

private fun SkillEntry.toData(): DataSkillEntry = DataSkillEntry(
    skill = skill.toData(),
    bonus = bonus,
    proficient = proficient,
    expertise = expertise,
)

private fun DataCharacterSpell.toDomain(): CharacterSpell = CharacterSpell(
    spellId = spellId,
    sourceClass = sourceClass,
)

private fun CharacterSpell.toData(): DataCharacterSpell = DataCharacterSpell(
    spellId = spellId,
    sourceClass = sourceClass,
)

private fun DataWeapon.toDomain(): Weapon = Weapon(
    id = id,
    name = name,
    ability = ability.toDomain(),
    proficient = proficient,
    damageDiceCount = damageDiceCount,
    damageDieSize = damageDieSize,
    useAbilityForDamage = useAbilityForDamage,
    damageType = damageType,
)

private fun Weapon.toData(): DataWeapon = DataWeapon(
    id = id,
    name = name,
    ability = ability.toData(),
    proficient = proficient,
    damageDiceCount = damageDiceCount,
    damageDieSize = damageDieSize,
    useAbilityForDamage = useAbilityForDamage,
    damageType = damageType,
)
