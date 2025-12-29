package com.github.arhor.spellbindr.data.local.assets

sealed class AssetState<out T> {
    data object Loading : AssetState<Nothing>()
    data class Ready<T>(val data: T) : AssetState<T>()
    data class Error(val cause: Throwable) : AssetState<Nothing>()
}

fun <T> AssetState<T>.dataOrNull(): T? =
    when (this) {
        is AssetState.Ready -> data
        else -> null
    }
