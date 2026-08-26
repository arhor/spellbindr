package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.LanguagesAssetDataStore
import io.github.arhor.dnd.companion.domain.model.Language
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.LanguagesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguagesRepositoryImpl @Inject constructor(
    private val languagesDataStore: LanguagesAssetDataStore,
) : LanguagesRepository {
    override val allLanguagesState: Flow<Loadable<List<Language>>>
        get() = languagesDataStore.data
}
