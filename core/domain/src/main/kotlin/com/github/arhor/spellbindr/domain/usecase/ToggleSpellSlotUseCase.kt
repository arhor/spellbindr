package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.PactSlotState
import com.github.arhor.spellbindr.domain.model.SpellSlotState
import com.github.arhor.spellbindr.domain.model.defaultSpellSlots
import javax.inject.Inject

class ToggleSpellSlotUseCase @Inject constructor() {

    sealed interface Action {
        data class Toggle(val level: Int, val slotIndex: Int) : Action
        data class SetTotal(val level: Int, val total: Int) : Action
        data class TogglePact(val slotIndex: Int) : Action
        data class SetPactTotal(val total: Int) : Action
        data class SetPactSlotLevel(val level: Int) : Action
        data object LongRest : Action
        data object ShortRest : Action
    }

    operator fun invoke(sheet: CharacterSheet, action: Action): CharacterSheet {
        return when (action) {
            is Action.Toggle -> handleToggle(sheet, action)
            is Action.SetTotal -> handleSetTotal(sheet, action)
            is Action.TogglePact -> handleTogglePact(sheet, action)
            is Action.SetPactTotal -> handleSetPactTotal(sheet, action)
            is Action.SetPactSlotLevel -> handleSetPactSlotLevel(sheet, action)
            is Action.LongRest -> handleLongRest(sheet)
            is Action.ShortRest -> handleShortRest(sheet)
        }
    }

    /* ------------------------------------------ internal implementation ------------------------------------------ */

    private fun handleToggle(sheet: CharacterSheet, action: Action.Toggle): CharacterSheet {
        return sheet.updateSpellSlot(action.level) {
            val totalSlots = it.total.coerceAtLeast(0)
            if (totalSlots == 0) {
                return@updateSpellSlot it
            }
            val normalizedIndex = action.slotIndex.coerceIn(0, totalSlots - 1)
            val newExpended = if (normalizedIndex < it.expended) {
                normalizedIndex
            } else {
                (normalizedIndex + 1).coerceAtMost(totalSlots)
            }
            it.copy(expended = newExpended)
        }
    }

    private fun handleSetTotal(
        sheet: CharacterSheet,
        action: Action.SetTotal
    ): CharacterSheet = sheet.updateSpellSlot(action.level) { slot ->
        val safeTotal = action.total.coerceAtLeast(0)
        slot.copy(
            total = safeTotal,
            expended = slot.expended.coerceIn(0, safeTotal),
        )
    }

    private fun handleSetPactSlotLevel(
        sheet: CharacterSheet,
        action: Action.SetPactSlotLevel
    ): CharacterSheet = sheet.updatePactSlots { slot ->
        slot.copy(slotLevel = action.level.coerceIn(1, 9))
    }

    private fun handleTogglePact(
        sheet: CharacterSheet,
        action: Action.TogglePact
    ): CharacterSheet = sheet.updatePactSlots { slot ->
        val totalSlots = slot.total.coerceAtLeast(0)
        if (totalSlots == 0) return@updatePactSlots slot
        val normalizedIndex = action.slotIndex.coerceIn(0, totalSlots - 1)
        val newExpended =
            if (normalizedIndex < slot.expended) normalizedIndex
            else (normalizedIndex + 1).coerceAtMost(totalSlots)
        slot.copy(expended = newExpended)
    }

    private fun handleSetPactTotal(
        sheet: CharacterSheet,
        action: Action.SetPactTotal
    ): CharacterSheet = sheet.updatePactSlots { slot ->
        val safeTotal = action.total.coerceAtLeast(0)
        slot.copy(
            total = safeTotal,
            expended = slot.expended.coerceIn(0, safeTotal),
        )
    }

    private fun handleLongRest(sheet: CharacterSheet): CharacterSheet {
        val normalizedSharedSlots = sheet.spellSlots.ifEmpty { defaultSpellSlots() }
        val resetSharedSlots = normalizedSharedSlots
            .map { it.copy(expended = 0) }
            .sortedBy { it.level }

        val resetPactSlots = sheet.pactSlots?.copy(expended = 0)

        return sheet.copy(
            spellSlots = resetSharedSlots,
            pactSlots = resetPactSlots,
            concentrationSpellId = null,
        )
    }

    private fun handleShortRest(sheet: CharacterSheet): CharacterSheet =
        sheet.pactSlots?.let { sheet.copy(pactSlots = it.copy(expended = 0)) }
            ?: sheet

    private fun CharacterSheet.updateSpellSlot(
        level: Int,
        transform: (SpellSlotState) -> SpellSlotState,
    ): CharacterSheet {
        val normalized = spellSlots.ifEmpty { defaultSpellSlots() }
        val slotMap = normalized.associateBy { it.level }.toMutableMap()
        val current = slotMap[level] ?: SpellSlotState(level = level)
        val updated = transform(current)

        slotMap[level] = updated

        return copy(spellSlots = slotMap.values.sortedBy { it.level })
    }

    private fun CharacterSheet.updatePactSlots(
        transform: (PactSlotState) -> PactSlotState,
    ): CharacterSheet {
        val current = pactSlots ?: PactSlotState()
        val updated = transform(current)

        return copy(pactSlots = updated)
    }
}
