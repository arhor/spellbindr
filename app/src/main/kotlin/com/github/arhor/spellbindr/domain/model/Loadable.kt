package com.github.arhor.spellbindr.domain.model

/**
 * Represents the lifecycle state of a generic asset within the application.
 *
 * @param T the type of the asset data held when the state is [Success]
 */
sealed class Loadable<out T> {
    /**
     * Represents the state when an asset is currently being loaded or processed.
     */
    data object Loading : Loadable<Nothing>()

    /**
     * Represents the state when an asset has been successfully loaded and is ready for use.
     *
     * @param T the type of the loaded asset data.
     * @property data the actual content of the loaded asset.
     */
    data class Success<T>(val data: T) : Loadable<T>()

    /**
     * Represents the state when an asset loading or processing has failed.
     *
     * @property errorMessage a human-readable description of the failure.
     * @property cause the exception or error that caused the failure.
     */
    data class Failure(val errorMessage: String? = null, val cause: Throwable? = null) : Loadable<Nothing>()
}

inline fun <T, R> Loadable<T>.map(transform: (T) -> R): Loadable<R> =
    when (this) {
        is Loadable.Loading, is Loadable.Failure -> this
        is Loadable.Success -> Loadable.Success(transform(data))
    }
