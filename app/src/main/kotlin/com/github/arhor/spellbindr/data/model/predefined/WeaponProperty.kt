package com.github.arhor.spellbindr.data.model.predefined

import com.github.arhor.spellbindr.utils.CaseInsensitiveEnumSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable(with = WeaponProperty.Companion::class)
enum class WeaponProperty {
    AMMUNITION,
    HEAVY,
    REACH,
    TWO_HANDED,
    VERSATILE,
    LOADING,
    LIGHT,
    FINESSE,
    THROWN,
    SPECIAL;

    companion object : KSerializer<WeaponProperty> by CaseInsensitiveEnumSerializer()
}
