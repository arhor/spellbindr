package com.github.arhor.spellbindr.ui.feature.character.guided.internal

import com.github.arhor.spellbindr.domain.model.ClassSpellRef
import com.github.arhor.spellbindr.domain.model.SpellChanges

internal fun buildGuidedLevelOneSpellChanges(
    classId: String,
    cantripSpellIds: Set<String>,
    levelOneSpellIds: Set<String>,
): SpellChanges {
    val cantrips = cantripSpellIds.mapTo(linkedSetOf()) { spellId ->
        ClassSpellRef(classId = classId, spellId = spellId)
    }
    val levelOneSpells = levelOneSpellIds.mapTo(linkedSetOf()) { spellId ->
        ClassSpellRef(classId = classId, spellId = spellId)
    }
    return when (classId) {
        "cleric", "druid" -> SpellChanges(learned = cantrips)
        "wizard" -> SpellChanges(
            learned = cantrips,
            addedToSpellbook = levelOneSpells,
        )

        else -> SpellChanges(learned = cantrips + levelOneSpells)
    }
}
