package io.github.arhor.dnd.companion.domain.repository

import io.github.arhor.dnd.companion.domain.model.Background
import io.github.arhor.dnd.companion.domain.model.Loadable
import kotlinx.coroutines.flow.Flow

interface BackgroundsRepository {
    val allBackgroundsState: Flow<Loadable<List<Background>>>

    suspend fun findBackgroundById(id: String): Background?
}

