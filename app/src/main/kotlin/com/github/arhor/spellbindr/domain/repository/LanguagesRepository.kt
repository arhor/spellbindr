package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Language
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface LanguagesRepository {
    val allLanguagesState: Flow<Loadable<List<Language>>>
}

