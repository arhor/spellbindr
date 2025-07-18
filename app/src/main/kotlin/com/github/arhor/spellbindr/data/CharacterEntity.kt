package com.github.arhor.spellbindr.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.arhor.spellbindr.data.common.EntityRef
import com.github.arhor.spellbindr.data.local.db.Converters

@Entity(tableName = "characters")
@TypeConverters(Converters::class)
data class CharacterEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val race: EntityRef,
    val subrace: EntityRef?,
    val classes: Map<EntityRef, Int>,
    val background: EntityRef,
    val abilityScores: Map<EntityRef, Int>,
    val proficiencies: Set<EntityRef>,
    val equipment: Set<EntityRef>,
    val inventory: Map<EntityRef, Int>,
    val spells: Set<EntityRef>,
)
