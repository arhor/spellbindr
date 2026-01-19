package com.github.arhor.spellbindr.ui.feature.characters.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellSlotUiModel(
    val level: Int,
    val total: Int,
    val expended: Int,
)
