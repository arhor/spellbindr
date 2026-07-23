package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.AbilityScores
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.DeathSaveState
import com.github.arhor.spellbindr.domain.model.SavingThrowEntry
import com.github.arhor.spellbindr.domain.model.Skill
import com.github.arhor.spellbindr.domain.model.abbreviation
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.AbilityUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterHeaderUiState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSheetEditingState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.DeathSaveUiState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.HitPointSummary
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.OverviewTabState
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SkillUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SkillsTabState

internal fun CharacterSheet.toHeaderState(): CharacterHeaderUiState {
    val subtitleParts = buildList {
        val levelLabel =
            if (className.isBlank()) "Level $level" else "Level $level ${className.trim()}"
        add(levelLabel)
        race.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
        background.takeIf { it.isNotBlank() }?.let { add(it.trim()) }
    }
    return CharacterHeaderUiState(
        name = name.ifBlank { "Unnamed hero" },
        subtitle = subtitleParts.joinToString(" • "),
        hitPoints = HitPointSummary(
            max = maxHitPoints,
            current = currentHitPoints,
            temporary = temporaryHitPoints,
        ),
        armorClass = armorClass,
        initiative = initiative,
        speed = speed.ifBlank { "—" },
        proficiencyBonus = proficiencyBonus,
        inspiration = inspiration,
    )
}

internal fun CharacterSheet.toOverviewState(): OverviewTabState {
    val savingThrowLookup = savingThrows.associateBy(SavingThrowEntry::abilityId)
    val abilityModels = AbilityIds.standardOrder.map { abilityId ->
        val modifier = abilityScores.modifierFor(abilityId)
        val entry = savingThrowLookup[abilityId]
        val proficiencyBonusValue = if (entry?.proficient == true) proficiencyBonus else 0
        val computedBonus = modifier + proficiencyBonusValue
        val resolvedBonus = when {
            entry == null -> modifier
            entry.bonus != 0 || entry.proficient -> entry.bonus
            else -> computedBonus
        }
        AbilityUiModel(
            abilityId = abilityId,
            label = abilityId.abbreviation(),
            score = abilityScores.scoreFor(abilityId),
            modifier = modifier,
            savingThrowBonus = resolvedBonus,
            savingThrowProficient = entry?.proficient ?: false,
        )
    }

    return OverviewTabState(
        abilities = abilityModels,
        hitDice = hitDice.ifBlank { "—" },
        senses = senses,
        languages = languages,
        proficiencies = proficiencies,
        equipment = equipment,
        background = background,
        race = race,
        alignment = alignment,
        deathSaves = DeathSaveUiState(
            successes = deathSaves.successes.coerceIn(0, 3),
            failures = deathSaves.failures.coerceIn(0, 3),
        ),
    )
}

internal fun CharacterSheet.toSkillsState(): SkillsTabState {
    val entries = skills.associateBy { it.skill }
    val models = Skill.entries.map { skill ->
        val entry = entries[skill]
        val proficiencyBonus = when {
            entry?.expertise == true -> this.proficiencyBonus * 2
            entry?.proficient == true -> this.proficiencyBonus
            else -> 0
        }
        val computedBonus = abilityScores.modifierFor(skill.abilityId) + proficiencyBonus
        val resolvedBonus = when {
            entry == null -> abilityScores.modifierFor(skill.abilityId)
            entry.bonus != 0 || entry.proficient || entry.expertise -> entry.bonus
            else -> computedBonus
        }
        SkillUiModel(
            id = skill,
            name = skill.displayName,
            abilityAbbreviation = skill.abilityAbbreviation,
            totalBonus = resolvedBonus,
            proficient = entry?.proficient ?: false,
            expertise = entry?.expertise ?: false,
        )
    }
    return SkillsTabState(models)
}

internal fun CharacterSheet.applyInlineEdits(edits: CharacterSheetEditingState): CharacterSheet {
    val newMaxHp = edits.maxHp.toIntOrNull()?.coerceAtLeast(1) ?: maxHitPoints
    val newCurrentHp = edits.currentHp.toIntOrNull()?.coerceIn(0, newMaxHp) ?: currentHitPoints.coerceIn(0, newMaxHp)
    val newTempHp = edits.tempHp.toIntOrNull()?.coerceAtLeast(0) ?: temporaryHitPoints
    return copy(
        maxHitPoints = newMaxHp,
        currentHitPoints = newCurrentHp,
        temporaryHitPoints = newTempHp,
        speed = edits.speed.trim(),
        hitDice = edits.hitDice.trim(),
        senses = edits.senses.trim(),
        languages = edits.languages.trim(),
        proficiencies = edits.proficiencies.trim(),
        equipment = edits.equipment.trim(),
    )
}

internal fun AbilityScores.scoreFor(abilityId: AbilityId): Int = when (abilityId.lowercase()) {
    AbilityIds.STR -> strength
    AbilityIds.DEX -> dexterity
    AbilityIds.CON -> constitution
    AbilityIds.INT -> intelligence
    AbilityIds.WIS -> wisdom
    AbilityIds.CHA -> charisma
    else -> 0
}

internal fun CharacterSheet.clearDeathSavesIfConscious(): CharacterSheet {
    return if (currentHitPoints > 0 && (deathSaves.successes != 0 || deathSaves.failures != 0)) {
        copy(deathSaves = DeathSaveState())
    } else {
        this
    }
}
