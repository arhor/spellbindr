package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AbilityAssetModel
import com.github.arhor.spellbindr.domain.model.Ability

fun AbilityAssetModel.toDomainAbilityOrNull(): Ability {
    return Ability(
        id = id,
        displayName = displayName,
        description = description,
    )
}
