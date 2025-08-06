package com.github.arhor.spellbindr.data.model.predefined

import com.github.arhor.spellbindr.utils.CaseInsensitiveEnumSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = WeaponRange.Companion::class)
enum class WeaponRange(
    val displayName: String,
) {
    MELEE("Melee"),
    RANGED("Ranged");

    companion object : KSerializer<WeaponRange> by CaseInsensitiveEnumSerializer()
}
