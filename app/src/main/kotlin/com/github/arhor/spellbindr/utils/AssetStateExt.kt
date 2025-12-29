package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.domain.model.AssetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Unwraps a [Flow] of [AssetState] containing a list of items into a [Flow] of the raw list.
 *
 * This extension function simplifies the handling of asset states by:
 * - Returning the data contained in [AssetState.Ready].
 * - Returning an empty list if the state is [AssetState.Loading] or [AssetState.Error].
 *
 * @param T the type of elements in the list.
 * @return a Flow emitting the data list or an empty list depending on the state.
 */
fun <T> Flow<AssetState<List<T>>>.unwrap(): Flow<List<T>> = map { state ->
    when (state) {
        is AssetState.Loading -> emptyList()
        is AssetState.Ready -> state.data
        is AssetState.Error -> emptyList()
    }
}
