package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MagicItem(
    val id: String,
    val name: String,
    val desc: List<String>,
    val rarity: Rarity,
    val equipmentCategory: String,
    val variants: List<EntityRef>? = null,
) {
    @Serializable
    enum class Rarity {
        @SerialName("Common")
        COMMON,

        @SerialName("Uncommon")
        UNCOMMON,

        @SerialName("Rare")
        RARE,

        @SerialName("Very Rare")
        VERY_RARE,

        @SerialName("Legendary")
        LEGENDARY,

        @SerialName("Varies")
        VARIES,
    }
}
