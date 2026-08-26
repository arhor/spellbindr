package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.CharacterClass
import com.github.arhor.spellbindr.domain.model.Choice
import com.github.arhor.spellbindr.domain.model.Feature

internal fun findGuidedLevelOneFeatureChoices(
    clazz: CharacterClass?,
    subclassId: String?,
    featuresById: Map<String, Feature>,
): List<Pair<String, Choice>> {
    if (clazz == null) return emptyList()
    val classFeatures = clazz.levels.firstOrNull { it.level == 1 }?.features.orEmpty()
    val subclassFeatures = clazz.subclasses
        .firstOrNull { it.id == subclassId }
        ?.levels
        ?.firstOrNull { it.level == 1 }
        ?.features
        .orEmpty()
    return (classFeatures + subclassFeatures)
        .distinct()
        .mapNotNull { featureId ->
            val choice = featuresById[featureId]?.choice ?: return@mapNotNull null
            featureId to choice
        }
}
