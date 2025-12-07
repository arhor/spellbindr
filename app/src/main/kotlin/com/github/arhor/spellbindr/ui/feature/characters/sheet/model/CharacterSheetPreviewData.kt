package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.DamageType
import com.github.arhor.spellbindr.data.model.predefined.Skill
import com.github.arhor.spellbindr.ui.feature.characters.sheet.AbilityUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterHeaderUiState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetEditingState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetUiState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.DeathSaveUiState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.HitPointSummary
import com.github.arhor.spellbindr.ui.feature.characters.sheet.OverviewTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SkillUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SkillsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.SpellsTabState
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponUiModel
import com.github.arhor.spellbindr.ui.feature.characters.sheet.WeaponsTabState

internal object CharacterSheetPreviewData {

    val header = CharacterHeaderUiState(
        name = "Astra Moonshadow",
        subtitle = "Level 7 Wizard • Half-elf",
        hitPoints = HitPointSummary(max = 38, current = 13, temporary = 5),
        armorClass = 16,
        initiative = 2,
        speed = "30 ft",
        proficiencyBonus = 3,
        inspiration = true,
    )

    val overview = OverviewTabState(
        abilities = Ability.entries.mapIndexed { index, ability ->
            AbilityUiModel(
                ability = ability,
                label = ability.name,
                score = 10 + index * 2,
                modifier = index - 1,
                savingThrowBonus = index + 2,
                savingThrowProficient = index % 2 == 0,
            )
        },
        hitDice = "7d6",
        senses = "Darkvision 60 ft",
        languages = "Common, Elvish",
        proficiencies = "Arcana, History, Insight",
        equipment = "Quarterstaff, Spellbook",
        background = "Sage",
        race = "Half-elf",
        alignment = "Chaotic Good",
        deathSaves = DeathSaveUiState(successes = 1, failures = 0),
    )

    val skills = SkillsTabState(
        skills = Skill.entries.take(6).mapIndexed { index, skill ->
            SkillUiModel(
                id = skill,
                name = skill.displayName,
                abilityAbbreviation = skill.ability.name,
                totalBonus = index,
                proficient = index % 2 == 0,
                expertise = index == 0,
            )
        }
    )

    val spells = SpellsTabState(
        spellLevels = listOf(
            SpellLevelUiModel(
                level = 0,
                label = "Cantrips",
                spellSlot = null,
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "minor_illusion",
                        name = "Minor Illusion",
                        level = 0,
                        school = "Illusion",
                        castingTime = "1 action",
                        sourceClass = "Wizard",
                    )
                ),
            ),
            SpellLevelUiModel(
                level = 1,
                label = "Level 1",
                spellSlot = SpellSlotUiModel(level = 1, total = 4, expended = 1),
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "magic_missile",
                        name = "Magic Missile",
                        level = 1,
                        school = "Evocation",
                        castingTime = "1 action",
                        sourceClass = "Wizard",
                    )
                ),
            ),
            SpellLevelUiModel(
                level = 2,
                label = "Level 2",
                spellSlot = SpellSlotUiModel(level = 2, total = 3, expended = 2),
                spells = emptyList(),
            ),
        ),
        canAddSpells = true,
    )

    val weapons = WeaponsTabState(
        weapons = listOf(
            WeaponUiModel(
                id = "w1",
                name = "Longsword",
                attackBonusLabel = "ATK +7",
                damageLabel = "DMG 1d8+4",
                damageType = DamageType.SLASHING,
                ability = Ability.STR,
            ),
            WeaponUiModel(
                id = "w2",
                name = "Shortbow",
                attackBonusLabel = "ATK +5",
                damageLabel = "DMG 1d6+3",
                damageType = DamageType.PIERCING,
                ability = Ability.DEX,
            ),
        ),
    )

    val editingState = CharacterSheetEditingState(
        maxHp = header.hitPoints.max.toString(),
        currentHp = header.hitPoints.current.toString(),
        tempHp = header.hitPoints.temporary.toString(),
        speed = header.speed,
        hitDice = overview.hitDice,
        senses = overview.senses,
        languages = overview.languages,
        proficiencies = overview.proficiencies,
        equipment = overview.equipment,
    )

    val uiState = CharacterSheetUiState(
        characterId = "preview",
        selectedTab = CharacterSheetTab.Overview,
        header = header,
        overview = overview,
        skills = skills,
        spells = spells,
        weapons = weapons,
        editingState = editingState,
    )
}
