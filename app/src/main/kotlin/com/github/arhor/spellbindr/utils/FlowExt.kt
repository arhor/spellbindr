package com.github.arhor.spellbindr.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.domain.model.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

context(vm: ViewModel)
fun <T> Flow<Loadable<T>>.toSharedStateFlow(): StateFlow<Loadable<T>> = stateIn(
    scope = vm.viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = Loadable.Loading
)

inline fun <T, R> Flow<Loadable<T>>.mapLoadable(crossinline transform: (T) -> R): Flow<Loadable<R>> =
    map { it.map(transform) }
