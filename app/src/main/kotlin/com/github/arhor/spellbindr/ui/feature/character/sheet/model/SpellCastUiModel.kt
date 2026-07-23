package com.github.arhor.spellbindr.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class SpellCastUiModel(
    val spellId: String,
    val name: String,
    val level: Int,
    val isRitual: Boolean,
    val isConcentration: Boolean,
    val higherLevel: List<String>,
    val slotOptions: List<CastSlotOptionUiModel>,
)

enum class SpellSlotPool {
    Shared,
    Pact,
}

@Immutable
data class CastSlotOptionUiModel(
    val pool: SpellSlotPool,
    val slotLevel: Int,
    val available: Int,
    val total: Int,
    val enabled: Boolean,
)

