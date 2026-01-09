package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T, R> Flow<Loadable<T>>.mapWhenReady(
    crossinline transform: suspend (T) -> R,
): Flow<Loadable<R>> {
    return map {
        when (it) {
            is Loadable.Ready -> {
                Loadable.Ready(transform(it.data))
            }

            is Loadable.Loading, is Loadable.Error -> {
                it
            }
        }
    }
}
