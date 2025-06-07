package com.github.arhor.spellbindr.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Feature(
    val id: String,
    val name: String,
    val desc: List<String>,
    @SerialName("class")
    val clazz: EntityRef,
    val parent: EntityRef? = null,
    val level: Int,
    val prerequisites: List<CommonPrerequisite>? = null,
    val reference: String? = null,
    val subclass: EntityRef? = null,
    val featureSpecific: FeatureSpecific? = null
)
