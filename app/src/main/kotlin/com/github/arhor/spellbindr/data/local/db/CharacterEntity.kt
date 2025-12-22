package com.github.arhor.spellbindr.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.arhor.spellbindr.data.model.CharacterSheetSnapshot
import com.github.arhor.spellbindr.domain.model.EntityRef

@Entity(tableName = "characters")
@TypeConverters(Converters::class)
data class CharacterEntity(
    @PrimaryKey
    val id: String,
    val name: String = "",
    val race: EntityRef = EntityRef("unknown"),
    val subrace: EntityRef? = null,
    val classes: Map<EntityRef, Int> = emptyMap(),
    val background: EntityRef = EntityRef("unknown"),
    val abilityScores: Map<EntityRef, Int> = emptyMap(),
    val proficiencies: Set<EntityRef> = emptySet(),
    val equipment: Set<EntityRef> = emptySet(),
    val inventory: Map<EntityRef, Int> = emptyMap(),
    val spells: Set<EntityRef> = emptySet(),
    val manualSheet: CharacterSheetSnapshot? = null,
)
