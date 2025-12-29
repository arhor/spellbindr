package com.github.arhor.spellbindr.domain.model

/**
 * Represents the lifecycle state of a generic asset within the application.
 *
 * @param T the type of the asset data held when the state is [Ready]
 */
sealed class AssetState<out T> {
    /**
     * Represents the state when an asset is currently being loaded or processed.
     */
    data object Loading : AssetState<Nothing>()

    /**
     * Represents the state when an asset has been successfully loaded and is ready for use.
     *
     * @param T the type of the loaded asset data.
     * @property data the actual content of the loaded asset.
     */
    data class Ready<T>(val data: T) : AssetState<T>()

    /**
     * Represents the state when an asset loading or processing has failed.
     *
     * @property cause the exception or error that caused the failure.
     */
    data class Error(val cause: Throwable) : AssetState<Nothing>()
}
