package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class CharacterHeaderUiState(
    val name: String,
    val subtitle: String,
    val hitPoints: HitPointSummary,
    val armorClass: Int,
    val initiative: Int,
    val speed: String,
    val proficiencyBonus: Int,
    val inspiration: Boolean,
)
