package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName

enum class EquipmentCategory {
    @SerialName("weapon")
    WEAPON,

    @SerialName("armor")
    ARMOR,

    @SerialName("tool")
    TOOL,

    @SerialName("gear")
    GEAR,

    @SerialName("holy-symbol")
    HOLY_SYMBOL,

    @SerialName("standard")
    STANDARD,

    @SerialName("musical-instrument")
    MUSICAL_INSTRUMENT,

    @SerialName("gaming-set")
    GAMING_SET,

    @SerialName("other")
    OTHER,

    @SerialName("arcane-focus")
    ARCANE_FOCUS,

    @SerialName("druidic-focus")
    DRUIDIC_FOCUS,

    @SerialName("kit")
    KIT,

    @SerialName("simple")
    SIMPLE,

    @SerialName("martial")
    MARTIAL,

    @SerialName("ranged")
    RANGED,

    @SerialName("melee")
    MELEE,

    @SerialName("shield")
    SHIELD,
}
