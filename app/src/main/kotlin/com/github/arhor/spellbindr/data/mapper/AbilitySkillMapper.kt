package com.github.arhor.spellbindr.data.mapper

import com.github.arhor.spellbindr.data.model.Ability as DataAbility
import com.github.arhor.spellbindr.data.model.Skill as DataSkill
import com.github.arhor.spellbindr.domain.model.Ability as DomainAbility
import com.github.arhor.spellbindr.domain.model.Skill as DomainSkill

fun DataAbility.toDomain(): DomainAbility = DomainAbility.valueOf(name)

fun DomainAbility.toData(): DataAbility = DataAbility.valueOf(name)

fun DataSkill.toDomain(): DomainSkill = DomainSkill.valueOf(name)

fun DomainSkill.toData(): DataSkill = DataSkill.valueOf(name)
