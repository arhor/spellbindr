package com.github.arhor.spellbindr.data.repository

import androidx.compose.runtime.Stable
import com.github.arhor.spellbindr.data.local.assets.LanguagesAssetDataStore
import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Stable
@Singleton
class LanguagesRepository @Inject constructor(
    private val languagesDataStore: LanguagesAssetDataStore,
) {
    val allLanguagesState: Flow<Loadable<List<Language>>>
        get() = languagesDataStore.data
}
