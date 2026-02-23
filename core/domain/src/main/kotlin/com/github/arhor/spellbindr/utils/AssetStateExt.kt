package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Unwraps a [Flow] of [Loadable] containing a list of items into a [Flow] of the raw list.
 *
 * This extension function simplifies the handling of asset states by:
 * - Returning the data contained in [Loadable.Content].
 * - Returning an empty list if the state is [Loadable.Loading] or [Loadable.Failure].
 *
 * @param T the type of elements in the list.
 * @return a Flow emitting the data list or an empty list depending on the state.
 */
fun <T> Flow<Loadable<List<T>>>.unwrap(): Flow<List<T>> = map { state ->
    when (state) {
        is Loadable.Loading -> emptyList()
        is Loadable.Content -> state.data
        is Loadable.Failure -> emptyList()
    }
}
