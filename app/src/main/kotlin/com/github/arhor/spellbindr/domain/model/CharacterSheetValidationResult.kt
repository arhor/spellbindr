package com.github.arhor.spellbindr.domain.model

data class CharacterSheetValidationResult(
    val nameError: CharacterSheetInputError?,
    val levelError: CharacterSheetInputError?,
    val abilityErrors: Map<String, CharacterSheetInputError>,
    val maxHpError: CharacterSheetInputError?,
) {
    val hasErrors: Boolean =
        nameError != null || levelError != null || maxHpError != null || abilityErrors.isNotEmpty()
}
