package io.github.arhor.dnd.companion.ui.feature.character.guided.model

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.AbilityScores

@Immutable
data class GuidedCharacterPreview(
    val abilityScores: AbilityScores,
    val maxHitPoints: Int,
    val armorClass: Int,
    val speed: Int,
    val languagesCount: Int,
    val proficienciesCount: Int,
)
