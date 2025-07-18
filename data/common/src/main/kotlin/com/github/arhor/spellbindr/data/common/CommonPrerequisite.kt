package com.github.arhor.spellbindr.data.common

import kotlinx.serialization.Serializable

@Serializable
sealed class CommonPrerequisite {
    @Serializable
    data class LevelPrerequisiteWrapper(val level: LevelPrerequisite) : CommonPrerequisite()

    @Serializable
    data class FeaturePrerequisiteWrapper(val feature: FeaturePrerequisite) : CommonPrerequisite()

    @Serializable
    data class SpellPrerequisiteWrapper(val spell: SpellPrerequisite) : CommonPrerequisite()
}
