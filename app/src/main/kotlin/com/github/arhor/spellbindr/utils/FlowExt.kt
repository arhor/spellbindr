package com.github.arhor.spellbindr.utils

import com.github.arhor.spellbindr.domain.model.Loadable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun <T> Flow<T>.asLoadableFlow(): Flow<Loadable<T>> =
    this.map<T, Loadable<T>> { Loadable.Content(it) }
        .onStart { emit(Loadable.Loading) }
        .catch { emit(Loadable.Failure(cause = it)) }
