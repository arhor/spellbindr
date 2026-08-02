package com.github.arhor.spellbindr.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MultiClassing(
    val prerequisites: List<AbilityScorePrerequisite> = emptyList(),
    @SerialName("proficiencies")
    val proficiencyGrants: List<String> = emptyList(),
    val proficiencyChoices: List<Choice> = emptyList(),
) {
    /** A stable key for a multiclass-grant choice persisted in progression records. */
    fun proficiencyChoiceId(classId: String, index: Int): String =
        "$classId:multiclass-proficiency:$index"
}
