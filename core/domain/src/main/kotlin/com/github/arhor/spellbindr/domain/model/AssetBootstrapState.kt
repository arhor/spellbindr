package com.github.arhor.spellbindr.domain.model

data class AssetBootstrapState(
    val criticalAssetsReady: Boolean = false,
    val deferredAssetsReady: Boolean = false,
    val criticalAssetsError: Throwable? = null,
    val deferredAssetsError: Throwable? = null,
) {
    val readyForInteraction: Boolean
        get() = criticalAssetsReady

    val fullyReady: Boolean
        get() = criticalAssetsReady && deferredAssetsReady
}
