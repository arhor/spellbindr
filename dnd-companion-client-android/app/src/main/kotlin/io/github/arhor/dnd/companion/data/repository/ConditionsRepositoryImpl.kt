package io.github.arhor.dnd.companion.data.repository

import io.github.arhor.dnd.companion.data.local.assets.ConditionsDataStore
import io.github.arhor.dnd.companion.domain.model.Condition
import io.github.arhor.dnd.companion.domain.model.Loadable
import io.github.arhor.dnd.companion.domain.repository.ConditionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConditionsRepositoryImpl @Inject constructor(
    private val conditionsDataStore: ConditionsDataStore,
) : ConditionsRepository {

    override val allConditionsState: Flow<Loadable<List<Condition>>>
        get() = conditionsDataStore.data
}
