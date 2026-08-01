package com.github.arhor.spellbindr.data.local.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.github.arhor.spellbindr.data.local.database.entity.CharacterEntity
import com.github.arhor.spellbindr.data.local.database.entity.CharacterProgressionEntity

data class CharacterWithProgressionEntity(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId",
    )
    val progression: CharacterProgressionEntity?,
)
