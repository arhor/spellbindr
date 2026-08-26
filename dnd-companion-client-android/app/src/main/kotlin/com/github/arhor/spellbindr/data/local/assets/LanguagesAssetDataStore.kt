package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.logging.LoggerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
    loggerFactory: LoggerFactory,
) : AssetDataStoreBase<Language>(
    json = json,
    path = "data/languages.json",
    context = context,
    serializer = Language.serializer(),
    loggerFactory = loggerFactory,
    priority = AssetLoadingPriority.DEFERRED,
)
