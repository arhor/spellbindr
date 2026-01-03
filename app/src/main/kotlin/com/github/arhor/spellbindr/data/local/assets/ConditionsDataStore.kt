package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Condition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionsDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : AssetDataStoreBase<Condition>(
    json = json,
    path = "data/conditions.json",
    context = context,
    serializer = Condition.serializer(),
    priority = AssetLoadingPriority.DEFERRED,
)
