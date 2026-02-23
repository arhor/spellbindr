package com.github.arhor.spellbindr.data.local.assets

import android.content.Context
import com.github.arhor.spellbindr.domain.model.Alignment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlignmentAssetDataStore @Inject constructor(
    @ApplicationContext
    context: Context,
    json: Json,
) : AssetDataStoreBase<Alignment>(
    json = json,
    path = "data/alignments.json",
    context = context,
    serializer = Alignment.serializer(),
)
