package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class PactSlotUiModel(
    val slotLevel: Int?,
    val total: Int,
    val expended: Int,
    val isConfigured: Boolean,
)
