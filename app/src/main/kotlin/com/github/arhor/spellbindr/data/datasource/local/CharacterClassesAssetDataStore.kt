package com.github.arhor.spellbindr.data.datasource.local

import android.content.Context
import com.github.arhor.spellbindr.data.model.CharacterClass
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassesAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<CharacterClass>(
    json = json,
    path = "data/classes.json",
    context = context,
    serializer = CharacterClass.serializer(),
)
