package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterSheet
import com.github.arhor.spellbindr.domain.model.Weapon
import javax.inject.Inject

class UpdateWeaponListUseCase @Inject constructor() {

    sealed interface Action {
        data class Save(val weapon: Weapon) : Action
        data class Delete(val id: String) : Action
    }

    operator fun invoke(sheet: CharacterSheet, action: Action): CharacterSheet = when (action) {
        is Action.Save -> {
            val updated = sheet.weapons.toMutableList()
            val existingIndex = updated.indexOfFirst { it.id == action.weapon.id }
            if (existingIndex >= 0) {
                updated[existingIndex] = action.weapon
            } else {
                updated += action.weapon
            }
            sheet.copy(weapons = updated)
        }
        is Action.Delete -> sheet.copy(weapons = sheet.weapons.filterNot { it.id == action.id })
    }
}
