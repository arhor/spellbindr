package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.CharacterSheet

@Immutable
data class CharacterSheetEditingState(
    val maxHp: String,
    val currentHp: String,
    val tempHp: String,
    val speed: String,
    val hitDice: String,
    val senses: String,
    val languages: String,
    val proficiencies: String,
    val equipment: String,
) {
    companion object {
        fun fromSheet(sheet: CharacterSheet): CharacterSheetEditingState = CharacterSheetEditingState(
            maxHp = sheet.maxHitPoints.toString(),
            currentHp = sheet.currentHitPoints.toString(),
            tempHp = sheet.temporaryHitPoints.toString(),
            speed = sheet.speed,
            hitDice = sheet.hitDice,
            senses = sheet.senses,
            languages = sheet.languages,
            proficiencies = sheet.proficiencies,
            equipment = sheet.equipment,
        )
    }
}
