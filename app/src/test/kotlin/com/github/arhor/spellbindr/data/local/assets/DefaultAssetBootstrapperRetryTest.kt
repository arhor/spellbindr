package com.github.arhor.spellbindr.data.local.assets

import com.github.arhor.spellbindr.domain.model.Loadable
import com.github.arhor.spellbindr.logging.NoOpLoggerFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAssetBootstrapperRetryTest {

    @Test
    fun `retryFailedLoads should initialize only failed asset stores`() = runTest {
        // Given
        val failedStore = mockk<AssetDataStore<List<String>>>()
        val readyStore = mockk<AssetDataStore<List<String>>>()
        every { failedStore.data } returns MutableStateFlow(Loadable.Failure(cause = RuntimeException("boom")))
        every { readyStore.data } returns MutableStateFlow(Loadable.Content(listOf("ready")))
        coEvery { failedStore.initialize() } returns Unit
        coEvery { readyStore.initialize() } returns Unit
        val bootstrapper = DefaultAssetBootstrapper(
            appCoroutineScope = this,
            assetsDataStores = setOf(failedStore, readyStore),
            loggerFactory = NoOpLoggerFactory,
        )

        // When
        bootstrapper.retryFailedLoads()

        // Then
        coVerify(exactly = 1) { failedStore.initialize() }
        coVerify(exactly = 0) { readyStore.initialize() }
    }
}
