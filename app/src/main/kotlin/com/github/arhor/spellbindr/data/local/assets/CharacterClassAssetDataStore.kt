package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.CharacterClass
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterClassAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : AssetDataStoreBase<CharacterClass>(
    json = json,
    path = "data/classes.json",
    context = context,
    serializer = CharacterClass.serializer(),
)
