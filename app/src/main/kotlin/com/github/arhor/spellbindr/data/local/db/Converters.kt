package com.github.arhor.spellbindr.data.local.db

import androidx.room.TypeConverter
import com.github.arhor.spellbindr.data.model.EntityRef
import com.github.arhor.spellbindr.data.model.next.Reference
import kotlinx.serialization.json.Json

object Converters {
    @TypeConverter
    fun fromEntityRef(ref: EntityRef?): String? = ref?.id

    @TypeConverter
    fun toEntityRef(id: String?): EntityRef? = id?.let { EntityRef(it) }

    @TypeConverter
    fun fromEntityRefMap(map: Map<EntityRef, Int>?): String? = map?.let {
        Json.encodeToString(it.mapKeys { (key, _) -> key.id })
    }

    @TypeConverter
    fun toEntityRefMap(json: String?): Map<EntityRef, Int>? = json?.let {
        Json.decodeFromString<Map<String, Int>>(it)
            .mapKeys { (key, _) -> EntityRef(key) }
    }

    @TypeConverter
    fun fromEntityRefSet(set: Set<EntityRef>?): String? = set?.let {
        Json.encodeToString(it.map { ref -> ref.id })
    }

    @TypeConverter
    fun toEntityRefSet(json: String?): Set<EntityRef>? = json?.let {
        Json.decodeFromString<Set<String>>(it)
            .map { id -> EntityRef(id) }
            .toSet()
    }

    @TypeConverter
    fun fromReference(ref: Reference?): String? = ref?.id

    @TypeConverter
    fun intoReference(id: String?): Reference? = id?.let(::Reference)
} 
