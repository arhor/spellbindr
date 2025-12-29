package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a section of D&D rules.
 *
 * @property id The unique identifier of the rule section.
 * @property name The name of the rule section.
 * @property desc A list of strings, where each string is a paragraph describing the rule section.
 */
@Serializable
data class RuleSection(
    val id: String,
    val name: String,
    val desc: List<String>
)
