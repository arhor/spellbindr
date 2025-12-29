package com.github.arhor.spellbindr.domain.model

data class AssetBootstrapState(
    val initialDelayPassed: Boolean = false,
    val criticalAssetsReady: Boolean = false,
    val deferredAssetsReady: Boolean = false,
    val criticalAssetsError: Throwable? = null,
    val deferredAssetsError: Throwable? = null,
) {
    val readyForInteraction: Boolean
        get() = initialDelayPassed && criticalAssetsReady

    val fullyReady: Boolean
        get() = readyForInteraction && deferredAssetsReady
}
