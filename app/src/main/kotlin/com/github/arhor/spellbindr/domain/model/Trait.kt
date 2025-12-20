package com.github.arhor.spellbindr.domain.model

import com.github.arhor.spellbindr.data.model.Choice
import com.github.arhor.spellbindr.data.model.Effect


data class Trait(
    val id: String,
    val name: String,
    val desc: List<String>,
    val effects: List<Effect>? = null,
    val spellChoice: Choice? = null,
    val languageChoice: Choice? = null,
    val proficiencyChoice: Choice? = null,
    val abilityBonusChoice: Choice? = null,
    val draconicAncestryChoice: Choice? = null,
)
