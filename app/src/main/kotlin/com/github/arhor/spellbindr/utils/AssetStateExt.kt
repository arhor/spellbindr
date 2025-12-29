package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.domain.model.AssetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T> Flow<AssetState<List<T>>>.unwrap(): Flow<List<T>> = map { state ->
    when (state) {
        is AssetState.Loading -> emptyList()
        is AssetState.Ready -> state.data
        is AssetState.Error -> emptyList()
    }
}
