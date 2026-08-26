package com.github.arhor.spellbindr.data.local.database.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.github.arhor.spellbindr.data.local.database.entity.CharacterSheetSnapshot
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedTypeConverter
class CharacterSheetConverter @Inject constructor(
    private val json: Json,
) : Converter {

    @TypeConverter
    fun fromCharacterSheetSnapshot(snapshot: CharacterSheetSnapshot?): String? =
        snapshot?.let { json.encodeToString(CharacterSheetSnapshot.serializer(), it) }

    @TypeConverter
    fun intoCharacterSheetSnapshot(data: String?): CharacterSheetSnapshot? =
        data?.let { json.decodeFromString(CharacterSheetSnapshot.serializer(), it) }
}
