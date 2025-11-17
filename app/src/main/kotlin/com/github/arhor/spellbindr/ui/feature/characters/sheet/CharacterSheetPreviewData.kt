package com.github.arhor.spellbindr.ui.feature.characters.sheet

import com.github.arhor.spellbindr.data.model.predefined.Ability
import com.github.arhor.spellbindr.data.model.predefined.Skill

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
        spellcastingGroups = listOf(
            SpellcastingGroupUiModel(
                sourceKey = "Wizard",
                sourceLabel = "Wizard",
                spells = listOf(
                    CharacterSpellUiModel(
                        spellId = "fireball",
                        name = "Fireball",
                        level = 3,
                        school = "Evocation",
                        castingTime = "1 action",
                    )
                )
            )
        ),
        spellSlots = listOf(
            SpellSlotUiModel(level = 1, total = 4, expended = 1),
            SpellSlotUiModel(level = 2, total = 3, expended = 2),
        ),
        canAddSpells = true,
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
        editingState = editingState,
    )
}
