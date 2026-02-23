package com.github.arhor.spellbindr.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "favorites",
    primaryKeys = ["type", "entityId"],
    indices = [Index(value = ["type"])]
)
data class FavoriteEntity(
    val type: String,
    val entityId: String,
)
