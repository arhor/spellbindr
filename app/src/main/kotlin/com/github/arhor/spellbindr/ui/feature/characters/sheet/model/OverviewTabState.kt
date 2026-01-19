package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class OverviewTabState(
    val abilities: List<AbilityUiModel>,
    val hitDice: String,
    val senses: String,
    val languages: String,
    val proficiencies: String,
    val equipment: String,
    val background: String,
    val race: String,
    val alignment: String,
    val deathSaves: DeathSaveUiState,
)
