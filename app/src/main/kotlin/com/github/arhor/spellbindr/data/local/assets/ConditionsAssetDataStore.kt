package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.core.assets.StaticAssetDataStoreBase
import com.github.arhor.spellbindr.data.model.Condition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionsAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<Condition>(
    json = json,
    path = "data/conditions.json",
    context = context,
    serializer = Condition.serializer(),
) 
