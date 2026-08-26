package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Language
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface LanguagesRepository {
    val allLanguagesState: Flow<Loadable<List<Language>>>
}

