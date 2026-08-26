package com.github.arhor.spellbindr.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "character_progressions",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["characterId"])],
)
data class CharacterProgressionEntity(
    @PrimaryKey
    val characterId: String,
    val stateJson: String,
)
