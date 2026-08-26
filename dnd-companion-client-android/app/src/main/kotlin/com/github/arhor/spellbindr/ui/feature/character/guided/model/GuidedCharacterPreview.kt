package com.github.arhor.spellbindr.ui.feature.character.guided.model

import androidx.compose.runtime.Immutable
import com.github.arhor.spellbindr.domain.model.AbilityScores

@Immutable
data class GuidedCharacterPreview(
    val abilityScores: AbilityScores,
    val maxHitPoints: Int,
    val armorClass: Int,
    val speed: Int,
    val languagesCount: Int,
    val proficienciesCount: Int,
)
