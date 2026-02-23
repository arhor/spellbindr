package com.github.arhor.spellbindr.domain.model

sealed interface CharacterSheetInputError {
    data object Required : CharacterSheetInputError
    data class MinValue(val min: Int) : CharacterSheetInputError
}
