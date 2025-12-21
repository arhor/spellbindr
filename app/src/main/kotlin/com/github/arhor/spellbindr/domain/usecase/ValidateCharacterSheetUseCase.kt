package com.github.arhor.spellbindr.domain.usecase

import com.github.arhor.spellbindr.ui.feature.characters.AbilityFieldState
import com.github.arhor.spellbindr.ui.feature.characters.CharacterEditorUiState

data class CharacterSheetValidationResult(
    val nameError: String?,
    val levelError: String?,
    val abilityStates: List<AbilityFieldState>,
    val maxHpError: String?,
) {
    val hasErrors: Boolean =
        nameError != null || levelError != null || maxHpError != null || abilityStates.any { it.error != null }
}

class ValidateCharacterSheetUseCase {
    operator fun invoke(state: CharacterEditorUiState): CharacterSheetValidationResult {
        val updatedAbilities = state.abilities.map { ability ->
            val value = ability.score.toIntOrNull()
            if (value == null) ability.copy(error = "Required") else ability.copy(error = null)
        }
        val nameError = if (state.name.isBlank()) "Required" else null
        val levelValue = state.level.toIntOrNull()
        val levelError = if (levelValue == null || levelValue < 1) "Level must be ≥ 1" else null
        val maxHpValue = state.maxHitPoints.toIntOrNull()
        val maxHpError = if (maxHpValue == null || maxHpValue <= 0) "Required" else null
        return CharacterSheetValidationResult(
            nameError = nameError,
            levelError = levelError,
            abilityStates = updatedAbilities,
            maxHpError = maxHpError,
        )
    }
}
