package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable

@Immutable
data class HitPointSummary(
    val max: Int,
    val current: Int,
    val temporary: Int,
)
