package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class CharacterSpellUiModel(
    val spellId: String,
    val name: String,
    val level: Int,
    val school: String,
    val castingTime: String,
    val sourceClass: String,
    val sourceLabel: String,
    val sourceKey: String,
)
