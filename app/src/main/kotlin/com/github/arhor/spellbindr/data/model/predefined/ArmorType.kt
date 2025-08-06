package com.github.arhor.spellbindr.data.model.predefined

import com.github.arhor.spellbindr.utils.CaseInsensitiveEnumSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

/**
 * Represents the different types of armor.
 *
 * @property displayName The user-friendly name of the armor type.
 */
@Serializable(with = ArmorType.Companion::class)
enum class ArmorType(
    val displayName: String,
) {
    LIGHT_ARMOR("Light Armor"),
    MEDIUM_ARMOR("Medium Armor"),
    HEAVY_ARMOR("Heavy Armor");

    companion object : KSerializer<ArmorType> by CaseInsensitiveEnumSerializer()
}
