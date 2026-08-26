package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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

    @SerialName("light")
    LIGHT,

    @SerialName("heavy")
    HEAVY,

    @SerialName("medium")
    MEDIUM,

    @SerialName("ammunition")
    AMMUNITION,

    @SerialName("equipment-pack")
    EQUIPMENT_PACK,

    @SerialName("artisans-tool")
    ARTISANS_TOOL,

    @SerialName("gaming-sets")
    GAMING_SETS,

    @SerialName("mounts-and-other-animals")
    MOUNTS_AND_OTHER_ANIMALS,

    @SerialName("vehicle")
    VEHICLE,

    @SerialName("tack-harness-and-drawn-vehicle")
    TACK_HARNESS_AND_DRAWN_VEHICLE,

    @SerialName("waterborne-vehicle")
    WATERBORNE_VEHICLE,
}
