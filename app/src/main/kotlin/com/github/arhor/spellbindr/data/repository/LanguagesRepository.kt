package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.LanguagesAssetDataStore
import com.github.arhor.spellbindr.domain.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class LanguagesRepository @Inject constructor(
    private val languagesDataStore: LanguagesAssetDataStore,
) {
    val allLanguages: Flow<List<Language>>
        get() = languagesDataStore.data.map { it ?: emptyList() }
}


