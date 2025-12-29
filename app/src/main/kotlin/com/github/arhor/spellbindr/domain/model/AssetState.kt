package com.github.arhor.spellbindr.domain.model

sealed class AssetState<out T> {
    data object Loading : AssetState<Nothing>()
    data class Ready<T>(val data: T) : AssetState<T>()
    data class Error(val cause: Throwable) : AssetState<Nothing>()
}
