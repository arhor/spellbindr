package com.github.arhor.spellbindr.data.local.database

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.github.arhor.spellbindr.data.local.database.entity.CharacterSheetSnapshot
import com.github.arhor.spellbindr.domain.model.EntityRef
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedTypeConverter
class Converters @Inject constructor(
    private val json: Json,
) {

    @TypeConverter
    fun fromEntityRef(ref: EntityRef?): String? = ref?.id

    @TypeConverter
    fun toEntityRef(id: String?): EntityRef? = id?.let { EntityRef(it) }

    @TypeConverter
    fun fromEntityRefMap(map: Map<EntityRef, Int>?): String? = map?.let {
        json.encodeToString(it.mapKeys { (key, _) -> key.id })
    }

    @TypeConverter
    fun toEntityRefMap(json: String?): Map<EntityRef, Int>? = json?.let {
        this.json.decodeFromString<Map<String, Int>>(it)
            .mapKeys { (key, _) -> EntityRef(key) }
    }

    @TypeConverter
    fun fromEntityRefSet(set: Set<EntityRef>?): String? = set?.let {
        json.encodeToString(it.map { ref -> ref.id })
    }

    @TypeConverter
    fun toEntityRefSet(json: String?): Set<EntityRef>? = json?.let {
        this.json.decodeFromString<Set<String>>(it)
            .map { id -> EntityRef(id) }
            .toSet()
    }

    @TypeConverter
    fun fromCharacterSheetSnapshot(snapshot: CharacterSheetSnapshot?): String? =
        snapshot?.let { json.encodeToString(CharacterSheetSnapshot.serializer(), it) }

    @TypeConverter
    fun toCharacterSheetSnapshot(data: String?): CharacterSheetSnapshot? =
        data?.let { json.decodeFromString(CharacterSheetSnapshot.serializer(), it) }
}
