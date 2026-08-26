package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class PactSlotUiModel(
    val slotLevel: Int?,
    val total: Int,
    val expended: Int,
    val isConfigured: Boolean,
)
