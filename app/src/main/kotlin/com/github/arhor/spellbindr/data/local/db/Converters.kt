package com.github.arhor.spellbindr.data.local.db

import androidx.room.TypeConverter
import com.github.arhor.spellbindr.data.common.EntityRef
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromEntityRef(ref: EntityRef?): String? {
        return ref?.id
    }

    @TypeConverter
    fun toEntityRef(id: String?): EntityRef? {
        return id?.let { EntityRef(it) }
    }

    @TypeConverter
    fun fromEntityRefMap(map: Map<EntityRef, Int>?): String? {
        return map?.let { Json.encodeToString(it.mapKeys { (key, _) -> key.id }) }
    }

    @TypeConverter
    fun toEntityRefMap(json: String?): Map<EntityRef, Int>? {
        return json?.let {
            Json.decodeFromString<Map<String, Int>>(it).mapKeys { (key, _) -> EntityRef(key) }
        }
    }

    @TypeConverter
    fun fromEntityRefSet(set: Set<EntityRef>?): String? {
        return set?.let { Json.encodeToString(it.map { ref -> ref.id }) }
    }

    @TypeConverter
    fun toEntityRefSet(json: String?): Set<EntityRef>? {
        return json?.let {
            Json.decodeFromString<Set<String>>(it).map { id -> EntityRef(id) }.toSet()
        }
    }
} 
