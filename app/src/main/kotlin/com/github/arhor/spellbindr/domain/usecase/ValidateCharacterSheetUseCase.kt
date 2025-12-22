package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.domain.model.CharacterEditorInput
import com.github.arhor.spellbindr.domain.model.CharacterSheetInputError
import com.github.arhor.spellbindr.domain.model.CharacterSheetValidationResult
import javax.inject.Inject

class ValidateCharacterSheetUseCase @Inject constructor() {
    operator fun invoke(input: CharacterEditorInput): CharacterSheetValidationResult {
        val abilityErrors = input.abilities.mapNotNull { ability ->
            val value = ability.score.toIntOrNull()
            if (value == null) {
                ability.ability to CharacterSheetInputError.Required
            } else {
                null
            }
        }.toMap()
        val nameError = if (input.name.isBlank()) CharacterSheetInputError.Required else null
        val levelValue = input.level.toIntOrNull()
        val levelError =
            if (levelValue == null || levelValue < 1) CharacterSheetInputError.MinValue(1) else null
        val maxHpValue = input.maxHitPoints.toIntOrNull()
        val maxHpError = if (maxHpValue == null || maxHpValue <= 0) CharacterSheetInputError.Required else null
        return CharacterSheetValidationResult(
            nameError = nameError,
            levelError = levelError,
            abilityErrors = abilityErrors,
            maxHpError = maxHpError,
        )
    }
}
