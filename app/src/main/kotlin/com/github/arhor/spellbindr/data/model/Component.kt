package com.github.arhor.spellbindr.data.model

sealed class Component {

    data object Verbal : Component() {
        override fun toString() = "V"
    }

    data object Somatic : Component() {
        override fun toString() = "S"
    }

    data class Material(
        val description: String,
        val consumedDuringCast: Boolean = false,
        val hasCost: Boolean = false,
    ) : Component() {
        override fun toString() = "M ($description)"
    }
}
