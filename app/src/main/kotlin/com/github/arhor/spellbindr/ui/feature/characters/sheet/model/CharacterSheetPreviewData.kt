package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.DamageType
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.abbreviation
import com.github.arhor.spellbindr.ui.feature.characters.sheet.CharacterSheetUiState

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
        abilities = AbilityIds.standardOrder.mapIndexed { index, abilityId ->
            AbilityUiModel(
                abilityId = abilityId,
                label = abilityId.abbreviation(),
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
                abilityAbbreviation = skill.abilityAbbreviation,
                totalBonus = index,
                proficient = index % 2 == 0,
                expertise = index == 0,
            )
        }
    )

    val spells = SpellsTabState(
        spellcastingClasses = listOf(
            SpellcastingClassUiModel(
                sourceKey = "wizard",
                name = "Wizard",
                spellcastingAbilityLabel = "INT",
                spellSaveDcLabel = "DC 15",
                spellAttackBonusLabel = "ATK +7",
                spellLevels = listOf(
                    SpellLevelUiModel(
                        level = 0,
                        label = "Cantrips",
                        spells = listOf(
                            CharacterSpellUiModel(
                                spellId = "minor_illusion",
                                name = "Minor Illusion",
                                level = 0,
                                school = "Illusion",
                                castingTime = "1 action",
                                sourceClass = "Wizard",
                                sourceLabel = "Wizard",
                                sourceKey = "wizard",
                            )
                        ),
                    ),
                    SpellLevelUiModel(
                        level = 1,
                        label = "Level 1",
                        spells = listOf(
                            CharacterSpellUiModel(
                                spellId = "magic_missile",
                                name = "Magic Missile",
                                level = 1,
                                school = "Evocation",
                                castingTime = "1 action",
                                sourceClass = "Wizard",
                                sourceLabel = "Wizard",
                                sourceKey = "wizard",
                            ),
                        ),
                    ),
                ),
            ),
            SpellcastingClassUiModel(
                sourceKey = "paladin",
                name = "Paladin",
                spellcastingAbilityLabel = "CHA",
                spellSaveDcLabel = "DC 13",
                spellAttackBonusLabel = "ATK +5",
                spellLevels = listOf(
                    SpellLevelUiModel(
                        level = 1,
                        label = "Level 1",
                        spells = listOf(
                            CharacterSpellUiModel(
                                spellId = "divine_favor",
                                name = "Divine Favor",
                                level = 1,
                                school = "Evocation",
                                castingTime = "1 action",
                                sourceClass = "Paladin",
                                sourceLabel = "Paladin",
                                sourceKey = "paladin",
                            ),
                        ),
                    ),
                ),
            ),
            SpellcastingClassUiModel(
                sourceKey = "warlock",
                name = "Warlock",
                spellcastingAbilityLabel = "CHA",
                spellSaveDcLabel = "DC 14",
                spellAttackBonusLabel = "ATK +6",
                spellLevels = listOf(
                    SpellLevelUiModel(
                        level = 1,
                        label = "Level 1",
                        spells = listOf(
                            CharacterSpellUiModel(
                                spellId = "hex",
                                name = "Hex",
                                level = 1,
                                school = "Enchantment",
                                castingTime = "1 bonus action",
                                sourceClass = "Warlock",
                                sourceLabel = "Warlock",
                                sourceKey = "warlock",
                            ),
                        ),
                    ),
                ),
            ),
        ),
        canAddSpells = true,
        sharedSlots = listOf(
            SpellSlotUiModel(level = 1, total = 4, expended = 1),
            SpellSlotUiModel(level = 2, total = 3, expended = 2),
            SpellSlotUiModel(level = 3, total = 2, expended = 0),
            SpellSlotUiModel(level = 4, total = 0, expended = 0),
            SpellSlotUiModel(level = 5, total = 0, expended = 0),
        ),
        pactSlots = PactSlotUiModel(
            slotLevel = 2,
            total = 2,
            expended = 1,
            isConfigured = true,
        ),
        concentration = ConcentrationUiModel(
            spellId = "hex",
            label = "Hex",
        ),
    )

    val weapons = WeaponsTabState(
        weapons = listOf(
            WeaponUiModel(
                id = "w1",
                name = "Longsword",
                attackBonusLabel = "ATK +7",
                damageLabel = "DMG 1d8+4",
                damageType = DamageType.SLASHING,
            ),
            WeaponUiModel(
                id = "w2",
                name = "Shortbow",
                attackBonusLabel = "ATK +5",
                damageLabel = "DMG 1d6+3",
                damageType = DamageType.PIERCING,
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

    val uiState = CharacterSheetUiState.Content(
        characterId = "preview",
        selectedTab = CharacterSheetTab.Overview,
        editMode = SheetEditMode.View,
        header = header,
        overview = overview,
        skills = skills,
        spells = spells,
        weapons = weapons,
        weaponCatalog = emptyList(),
        isWeaponCatalogVisible = false,
        editingState = editingState,
        weaponEditorState = null,
        errorMessage = null,
    )
}
