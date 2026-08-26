package io.github.arhor.dnd.companion.ui.feature.character.sheet.model

import androidx.compose.runtime.Immutable
import io.github.arhor.dnd.companion.domain.model.Skill

@Immutable
data class SkillUiModel(
    val id: Skill,
    val name: String,
    val abilityAbbreviation: String,
    val totalBonus: Int,
    val proficient: Boolean,
    val expertise: Boolean,
)
