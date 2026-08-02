package com.github.arhor.spellbindr.domain.model

/** Result of a stale-safe one-level persistence attempt. */
sealed interface ApplyLevelUpResult {
    data class Success(
        val sheet: CharacterSheet,
        val progression: CharacterProgression,
    ) : ApplyLevelUpResult

    data object MissingCharacter : ApplyLevelUpResult
    data object UnmanagedCharacter : ApplyLevelUpResult
    data object StaleState : ApplyLevelUpResult
    data class ValidationFailure(val issues: List<LevelUpValidationIssue>) : ApplyLevelUpResult
    data class PersistenceFailure(val message: String) : ApplyLevelUpResult
}
