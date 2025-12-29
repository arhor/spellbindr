package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class ArmorClass {
    abstract val type: String
    abstract val value: Int
    open val desc: String? get() = null

    @Serializable
    @SerialName("dex")
    data class ArmorClassDex(
        override val type: String = "dex",
        override val value: Int,
        override val desc: String? = null
    ) : ArmorClass()

    @Serializable
    @SerialName("natural")
    data class ArmorClassNatural(
        override val type: String = "natural",
        override val value: Int,
        override val desc: String? = null
    ) : ArmorClass()

    @Serializable
    @SerialName("armor")
    data class ArmorClassArmor(
        override val type: String = "armor",
        override val value: Int,
        override val desc: String? = null,
        val armor: List<EntityRef>? = null
    ) : ArmorClass()

    @Serializable
    @SerialName("spell")
    data class ArmorClassSpell(
        override val type: String = "spell",
        override val value: Int,
        override val desc: String? = null,
        val spell: EntityRef
    ) : ArmorClass()

    @Serializable
    @SerialName("condition")
    data class ArmorClassCondition(
        override val type: String = "condition",
        override val value: Int,
        override val desc: String? = null,
        val condition: EntityRef
    ) : ArmorClass()
}
