package com.github.arhor.spellbindr.data.next.repository

import android.content.Context
import com.github.arhor.spellbindr.data.next.model.CharacterClass
import com.github.arhor.spellbindr.data.next.model.EntityRef
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassRepository @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetLoaderBase<CharacterClass>(
    context = context,
    json = json,
    path = "data/classes.json",
    serializer = CharacterClass.serializer()
) {
    suspend fun findSpellcastingClassesRefs(): List<EntityRef> =
        getAsset()
            .filter { it.spellcasting != null }
            .map { EntityRef(it.id) }
}
