package com.github.arhor.spellbindr.domain.repository

import com.github.arhor.spellbindr.domain.model.Background
import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface BackgroundsRepository {
    val allBackgroundsState: Flow<Loadable<List<Background>>>

    suspend fun findBackgroundById(id: String): Background?
}

