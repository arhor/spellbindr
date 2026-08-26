package com.github.arhor.spellbindr.data.local.database.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.github.arhor.spellbindr.domain.model.EntityRef
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.contracts.contract

@Singleton
@ProvidedTypeConverter
class EntityRefConverter @Inject constructor(
    private val json: Json,
) : Converter {

    @TypeConverter
    fun fromEntityRef(value: EntityRef?): String? {
        contract {
            (value != null) implies (returnsNotNull())
        }
        return value?.id
    }

    @TypeConverter
    fun intoEntityRef(value: String?): EntityRef? {
        contract {
            (value != null) implies (returnsNotNull())
        }
        return value?.let { EntityRef(it) }
    }

    @TypeConverter
    fun fromEntityRefMap(refs: Map<EntityRef, Int>?): String? =
        refs?.mapKeys { (key, _) -> fromEntityRef(key) }
            ?.let(json::encodeToString)

    @TypeConverter
    fun intoEntityRefMap(value: String?): Map<EntityRef, Int>? = value?.let {
        json.decodeFromString<Map<String, Int>>(it)
            .mapKeys { (key, _) -> intoEntityRef(key) }
    }

    @TypeConverter
    fun fromEntityRefSet(refs: Set<EntityRef>?): String? =
        refs?.map(::fromEntityRef)
            ?.let(json::encodeToString)

    @TypeConverter
    fun intoEntityRefSet(value: String?): Set<EntityRef>? = value?.let {
        json.decodeFromString<Set<String>>(it)
            .map { id -> EntityRef(id) }
            .toSet()
    }
}
