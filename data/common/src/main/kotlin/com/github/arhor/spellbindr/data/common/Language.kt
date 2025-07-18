package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: String,
    val name: String,
    val desc: String,
    val type: String,
    val script: String,
    @SerialName("typical_speakers")
    val typicalSpeakers: List<String>
)
