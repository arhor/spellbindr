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

    operator fun invoke(sheet: CharacterSheet, action: Action): CharacterSheet = when (action) {
        is Action.Toggle -> sheet.updateSpellSlot(action.level) { slot ->
            val totalSlots = slot.total.coerceAtLeast(0)
            if (totalSlots == 0) return@updateSpellSlot slot
            val normalizedIndex = action.slotIndex.coerceIn(0, totalSlots - 1)
            val newExpended =
                if (normalizedIndex < slot.expended) normalizedIndex
                else (normalizedIndex + 1).coerceAtMost(totalSlots)
            slot.copy(expended = newExpended)
        }
        is Action.SetTotal -> sheet.updateSpellSlot(action.level) { slot ->
            val safeTotal = action.total.coerceAtLeast(0)
            slot.copy(
                total = safeTotal,
                expended = slot.expended.coerceIn(0, safeTotal),
            )
        }
        is Action.TogglePact -> sheet.updatePactSlots { slot ->
            val totalSlots = slot.total.coerceAtLeast(0)
            if (totalSlots == 0) return@updatePactSlots slot
            val normalizedIndex = action.slotIndex.coerceIn(0, totalSlots - 1)
            val newExpended =
                if (normalizedIndex < slot.expended) normalizedIndex
                else (normalizedIndex + 1).coerceAtMost(totalSlots)
            slot.copy(expended = newExpended)
        }

        is Action.SetPactTotal -> sheet.updatePactSlots { slot ->
            val safeTotal = action.total.coerceAtLeast(0)
            slot.copy(
                total = safeTotal,
                expended = slot.expended.coerceIn(0, safeTotal),
            )
        }

        is Action.SetPactSlotLevel -> sheet.updatePactSlots { slot ->
            slot.copy(slotLevel = action.level.coerceIn(1, 9))
        }

        Action.LongRest -> {
            val normalizedSharedSlots = if (sheet.spellSlots.isEmpty()) defaultSpellSlots() else sheet.spellSlots
            val resetSharedSlots = normalizedSharedSlots
                .map { slot -> slot.copy(expended = 0) }
                .sortedBy { it.level }

            val resetPactSlots = sheet.pactSlots?.let { slot ->
                slot.copy(expended = 0)
            }

            sheet.copy(
                spellSlots = resetSharedSlots,
                pactSlots = resetPactSlots,
                concentrationSpellId = null,
            )
        }

        Action.ShortRest -> sheet.pactSlots?.let { slot ->
            sheet.copy(
                pactSlots = slot.copy(
                    expended = 0,
                ),
            )
        } ?: sheet
    }

    private fun CharacterSheet.updateSpellSlot(
        level: Int,
        transform: (SpellSlotState) -> SpellSlotState,
    ): CharacterSheet {
        val normalized = if (spellSlots.isEmpty()) defaultSpellSlots() else spellSlots
        val slotMap = normalized.associateBy { it.level }.toMutableMap()
        val current = slotMap[level] ?: SpellSlotState(level = level)
        slotMap[level] = transform(current)
        return copy(spellSlots = slotMap.values.sortedBy { it.level })
    }

    private fun CharacterSheet.updatePactSlots(
        transform: (PactSlotState) -> PactSlotState,
    ): CharacterSheet {
        val current = pactSlots ?: PactSlotState()
        return copy(pactSlots = transform(current))
    }
}
