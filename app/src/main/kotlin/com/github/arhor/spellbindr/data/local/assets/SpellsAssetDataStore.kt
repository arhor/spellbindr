package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.data.model.Spell
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpellsAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : StaticAssetDataStoreBase<Spell>(
    json = json,
    path = "data/spells.json",
    context = context,
    serializer = Spell.serializer(),
)
