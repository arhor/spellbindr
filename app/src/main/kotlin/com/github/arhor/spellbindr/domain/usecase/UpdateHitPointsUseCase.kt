package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet

class UpdateHitPointsUseCase {

    sealed interface Action {
        data class AdjustCurrentHp(val delta: Int) : Action
        data class SetTemporaryHp(val value: Int) : Action
        data class SetDeathSaveSuccesses(val count: Int) : Action
        data class SetDeathSaveFailures(val count: Int) : Action
    }

    operator fun invoke(sheet: CharacterSheet, action: Action): CharacterSheet = when (action) {
        is Action.AdjustCurrentHp -> {
            val maxHp = sheet.maxHitPoints.coerceAtLeast(0)
            val next = (sheet.currentHitPoints + action.delta).coerceIn(0, maxHp)
            sheet.copy(currentHitPoints = next)
        }
        is Action.SetTemporaryHp -> sheet.copy(temporaryHitPoints = action.value.coerceAtLeast(0))
        is Action.SetDeathSaveSuccesses -> sheet.copy(
            deathSaves = sheet.deathSaves.copy(successes = action.count.coerceIn(0, 3)),
        )
        is Action.SetDeathSaveFailures -> sheet.copy(
            deathSaves = sheet.deathSaves.copy(failures = action.count.coerceIn(0, 3)),
        )
    }
}
