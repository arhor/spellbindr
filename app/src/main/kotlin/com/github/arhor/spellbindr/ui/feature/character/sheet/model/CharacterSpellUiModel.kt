package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class CharacterSpellUiModel(
    val spellId: String,
    val name: String,
    val level: Int,
    val school: String,
    val castingTime: String,
    val range: String,
    val components: List<String>,
    val ritual: Boolean,
    val concentration: Boolean,
    val sourceClass: String,
    val sourceLabel: String,
    val sourceKey: String,
)
