package com.github.arhor.spellbindr.ui.feature.character.sheet

import com.github.arhor.spellbindr.domain.model.AbilityId
import com.github.arhor.spellbindr.domain.model.AbilityIds
import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Spell
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.abbreviation
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CastSlotOptionUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.CharacterSpellUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.ConcentrationUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.PactSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellCastUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellLevelUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotPool
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellSlotUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellcastingClassUiModel
import com.github.arhor.spellbindr.ui.feature.character.sheet.model.SpellsTabState
import java.util.Locale

internal fun CharacterSheet.toSpellsState(
    allSpells: List<Spell>,
    spellcastingClasses: List<CharacterClass>,
): SpellsTabState {
    val spellLookup = allSpells.associateBy(Spell::id)
    val normalizedSlots = spellSlots.ifEmpty { defaultSpellSlots() }
    val hasConfiguredSharedSlots = normalizedSlots.any { it.total > 0 }
    val allSharedSlots = normalizedSlots
        .sortedBy { it.level }
        .map { slot ->
            SpellSlotUiModel(
                level = slot.level,
                total = slot.total,
                expended = slot.expended.coerceIn(0, slot.total.coerceAtLeast(0)),
            )
        }

    val spellEntries = characterSpells.map { stored ->
        val spell = spellLookup[stored.spellId]
        val sourceClass = stored.sourceClass.trim()
        CharacterSpellUiModel(
            spellId = stored.spellId,
            name = spell?.name ?: stored.spellId,
            level = spell?.level ?: 0,
            school = spell?.school?.prettyString().orEmpty(),
            castingTime = spell?.castingTime ?: "",
            range = spell?.range.orEmpty(),
            components = spell?.components.orEmpty(),
            ritual = spell?.ritual == true,
            concentration = spell?.concentration == true,
            sourceClass = sourceClass,
            sourceLabel = formatSourceLabel(sourceClass),
            sourceKey = normalizeSourceKey(sourceClass),
        )
    }

    val spellsBySource = spellEntries.groupBy { it.sourceKey }
    val classModels = spellsBySource
        .map { (sourceKey, spells) ->
            val abilityId = spellcastingClasses.resolveSpellcastingAbility(sourceKey)
            val abilityLabel = abilityId?.abbreviation()
            val abilityModifier = abilityId?.let(abilityScores::modifierFor)
            val spellAttack = abilityModifier?.let { proficiencyBonus + it }
            val spellDc = abilityModifier?.let { 8 + proficiencyBonus + it }

            val spellsByLevel = spells.groupBy { it.level }
            val spellLevels = buildList {
                spellsByLevel[0]?.let { cantrips ->
                    add(
                        SpellLevelUiModel(
                            level = 0,
                            spells = cantrips.sortedBy { it.name.lowercase(Locale.ROOT) },
                        )
                    )
                }

                (1..9).forEach { level ->
                    val levelSpells = spellsByLevel[level].orEmpty()
                    if (levelSpells.isNotEmpty()) {
                        add(
                            SpellLevelUiModel(
                                level = level,
                                spells = levelSpells.sortedBy { it.name.lowercase(Locale.ROOT) },
                            )
                        )
                    }
                }
            }

            val displayName = if (sourceKey == UNASSIGNED_SOURCE_KEY) {
                ""
            } else {
                spellcastingClasses.firstOrNull { clazz ->
                    normalizeSourceKey(clazz.id) == sourceKey || normalizeSourceKey(clazz.name) == sourceKey
                }?.name ?: spells.firstOrNull()?.sourceLabel?.ifBlank { null }
                ?: formatSourceLabel(sourceKey)
            }

            SpellcastingClassUiModel(
                sourceKey = sourceKey,
                name = displayName,
                isUnassigned = sourceKey == UNASSIGNED_SOURCE_KEY,
                spellcastingAbility = abilityLabel,
                spellSaveDc = spellDc,
                spellAttackBonus = spellAttack,
                spellLevels = spellLevels,
            )
        }
        .sortedWith(
            compareBy<SpellcastingClassUiModel> { it.sourceKey == UNASSIGNED_SOURCE_KEY }
                .thenBy { it.name.lowercase(Locale.ROOT) },
        )

    val warlockKeys = spellcastingClasses
        .filter { clazz ->
            clazz.id.equals("warlock", ignoreCase = true) || clazz.name.equals("Warlock", ignoreCase = true)
        }
        .flatMap { clazz -> listOf(normalizeSourceKey(clazz.id), normalizeSourceKey(clazz.name)) }
        .toSet()
    val hasWarlockSpells = warlockKeys.isNotEmpty() && spellsBySource.keys.any { key -> key in warlockKeys }
    val pactSlotUi = when {
        hasWarlockSpells || pactSlots != null -> {
            pactSlots?.let { slot ->
                PactSlotUiModel(
                    slotLevel = slot.slotLevel,
                    total = slot.total,
                    expended = slot.expended.coerceIn(0, slot.total.coerceAtLeast(0)),
                    isConfigured = true,
                )
            } ?: PactSlotUiModel(
                slotLevel = null,
                total = 0,
                expended = 0,
                isConfigured = false,
            )
        }

        else -> null
    }

    val concentrationUi = concentrationSpellId?.let { spellId ->
        val label = spellLookup[spellId]?.name ?: spellId
        ConcentrationUiModel(spellId = spellId, label = label)
    }

    val highestSpellLevel = spellEntries.maxOfOrNull { it.level }?.coerceAtLeast(1) ?: 1
    val highestSlotLevel = allSharedSlots.filter { it.total > 0 }.maxOfOrNull { it.level } ?: 1
    val maxDisplayedSlotLevel = maxOf(highestSpellLevel, highestSlotLevel)
    val sharedSlots = allSharedSlots.filter { it.level <= maxDisplayedSlotLevel }

    return SpellsTabState(
        spellcastingClasses = classModels,
        canAddSpells = allSpells.isNotEmpty(),
        sharedSlots = sharedSlots,
        hasConfiguredSharedSlots = hasConfiguredSharedSlots,
        pactSlots = pactSlotUi,
        concentration = concentrationUi,
    )
}

internal fun CharacterSheet.toCastSpellState(
    spellId: String,
    allSpells: List<Spell>,
): SpellCastUiModel {
    val spell = allSpells.firstOrNull { it.id == spellId }
    val spellLevel = spell?.level ?: 0
    return SpellCastUiModel(
        spellId = spellId,
        name = spell?.name ?: spellId,
        level = spellLevel,
        isRitual = spell?.ritual == true,
        isConcentration = spell?.concentration == true,
        higherLevel = spell?.higherLevel.orEmpty(),
        slotOptions = buildCastSlotOptions(this, spell, spellLevel),
    )
}

internal fun buildCastSlotOptions(
    sheet: CharacterSheet,
    spell: Spell?,
    spellLevel: Int,
): List<CastSlotOptionUiModel> {
    if (spell == null || spellLevel <= 0) return emptyList()

    val normalizedSharedSlots = sheet.spellSlots.ifEmpty { defaultSpellSlots() }
    val sharedByLevel = normalizedSharedSlots.associateBy(SpellSlotState::level)

    val sharedOptions = (spellLevel..9).map { slotLevel ->
        val slot = sharedByLevel[slotLevel] ?: SpellSlotState(level = slotLevel)
        val total = slot.total.coerceAtLeast(0)
        val expended = slot.expended.coerceIn(0, total)
        val available = (total - expended).coerceAtLeast(0)
        CastSlotOptionUiModel(
            pool = SpellSlotPool.Shared,
            slotLevel = slotLevel,
            available = available,
            total = total,
            enabled = available > 0,
        )
    }

    val pactOption = sheet.pactSlots?.let { slot ->
        val total = slot.total.coerceAtLeast(0)
        val expended = slot.expended.coerceIn(0, total)
        val available = (total - expended).coerceAtLeast(0)
        val pactLevel = slot.slotLevel.coerceIn(1, 9)
        CastSlotOptionUiModel(
            pool = SpellSlotPool.Pact,
            slotLevel = pactLevel,
            available = available,
            total = total,
            enabled = available > 0 && pactLevel >= spellLevel,
        )
    }

    return buildList {
        addAll(sharedOptions)
        pactOption?.let(::add)
    }
}

private fun List<CharacterClass>.resolveSpellcastingAbility(sourceKey: String): AbilityId? {
    val normalizedKey = sourceKey.trim().lowercase(Locale.ROOT)
    val clazz = firstOrNull { it.id.equals(normalizedKey, ignoreCase = true) }
        ?: firstOrNull { normalizeSourceKey(it.name) == normalizedKey }
    val abilityId = clazz?.spellcasting?.spellcastingAbility?.id
    return abilityId?.trim()?.lowercase(Locale.ROOT)?.takeIf { it in AbilityIds.standardOrder }
}

internal fun normalizeSourceKey(sourceClass: String): String {
    val normalized = sourceClass.trim().lowercase(Locale.ROOT)
    return normalized.ifBlank { UNASSIGNED_SOURCE_KEY }
}

private fun formatSourceLabel(sourceClass: String): String {
    val trimmed = sourceClass.trim()
    if (trimmed.isBlank()) return ""
    return trimmed
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { char -> char.titlecase(Locale.ROOT) }
        }
}

private const val UNASSIGNED_SOURCE_KEY = "__unassigned__"
