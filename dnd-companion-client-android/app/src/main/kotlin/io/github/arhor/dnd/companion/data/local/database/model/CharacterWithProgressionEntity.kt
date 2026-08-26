package io.github.arhor.dnd.companion.data.local.database.model

import androidx.room.Embedded
import androidx.room.Relation
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterEntity
import io.github.arhor.dnd.companion.data.local.database.entity.CharacterProgressionEntity

data class CharacterWithProgressionEntity(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId",
    )
    val progression: CharacterProgressionEntity?,
)
