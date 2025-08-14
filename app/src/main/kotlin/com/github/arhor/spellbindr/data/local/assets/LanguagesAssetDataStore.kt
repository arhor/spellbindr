package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.data.model.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
): StaticAssetDataStoreBase<Language>(
    json = json,
    path = "data/languages.json",
    context = context,
    serializer = Language.serializer(),
)


