package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.domain.model.EntityRef as DomainEntityRef
import kotlinx.serialization.Serializable


/**
 * Represents the Difficulty Class (DC) for a spell or ability.
 *
 * @property dcType Reference to the entity (e.g., ability score) that the DC is based on.
 * @property dcValue The numerical value of the Difficulty Class. Optional for calculated DCs.
 * @property successType The type of success condition (e.g., on a successful save, half damage).
 */
@Serializable
data class DifficultyClass(
    val dcType: DomainEntityRef,
    val dcValue: Int? = null,
    val successType: SuccessType
)
