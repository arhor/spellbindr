package com.github.arhor.spellbindr.data.model.predefined

import com.github.arhor.spellbindr.utils.CaseInsensitiveEnumSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = WeaponType.Companion::class)
enum class WeaponType(
    val displayName: String,
) {
    SIMPLE("Simple"),
    MARTIAL("Martial");

    companion object : KSerializer<WeaponType> by CaseInsensitiveEnumSerializer()
}
