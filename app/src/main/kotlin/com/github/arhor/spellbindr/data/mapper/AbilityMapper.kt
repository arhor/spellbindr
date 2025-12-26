package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.AbilityAssetModel
import com.github.arhor.spellbindr.domain.model.Ability

fun AbilityAssetModel.toDomainAbilityOrNull(): Ability? {
    return Ability.entries.firstOrNull { ability ->
        ability.id.equals(id, ignoreCase = true) || ability.displayName.equals(name, ignoreCase = true)
    }
}
