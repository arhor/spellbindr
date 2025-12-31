package com.github.arhor.spellbindr.data.local.assets

import android.util.Log
import com.github.arhor.spellbindr.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DefaultAssetBootstrapperTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `readyForInteraction should wait for critical assets and initial delay`() = runTest {
        // Given
        val criticalLoader = FakeAssetLoader(AssetLoadingPriority.CRITICAL, initializationDelayMillis = 500)
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_000)
        val bootstrapper = DefaultAssetBootstrapper(loaders = setOf(criticalLoader, deferredLoader))

        // When
        bootstrapper.start(this)
        advanceTimeBy(1_000)
        runCurrent()
        val intermediateState = bootstrapper.state.value

        advanceTimeBy(600)
        runCurrent()
        val finalState = bootstrapper.state.value

        // Then
        assertThat(intermediateState.initialDelayPassed).isFalse()
        assertThat(intermediateState.criticalAssetsReady).isTrue()
        assertThat(intermediateState.readyForInteraction).isFalse()
        assertThat(finalState.initialDelayPassed).isTrue()
        assertThat(finalState.criticalAssetsReady).isTrue()
        assertThat(finalState.readyForInteraction).isTrue()
        assertThat(finalState.deferredAssetsReady).isFalse()
    }

    @Test
    fun `fullyReady should flip after deferred assets finish`() = runTest {
        // Given
        val criticalLoader = FakeAssetLoader(AssetLoadingPriority.CRITICAL)
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_500)
        val bootstrapper = DefaultAssetBootstrapper(loaders = setOf(criticalLoader, deferredLoader))

        // When
        bootstrapper.start(this)
        advanceTimeBy(1_600)
        runCurrent()
        val stateBeforeDeferredReady = bootstrapper.state.value

        advanceUntilIdle()
        val stateAfterDeferredReady = bootstrapper.state.value

        // Then
        assertThat(stateBeforeDeferredReady.readyForInteraction).isTrue()
        assertThat(stateBeforeDeferredReady.fullyReady).isFalse()
        assertThat(stateAfterDeferredReady.deferredAssetsReady).isTrue()
        assertThat(stateAfterDeferredReady.fullyReady).isTrue()
    }

    @Test
    fun `critical readiness should resolve when no critical loaders are present`() = runTest {
        // Given
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_000)
        val bootstrapper = DefaultAssetBootstrapper(loaders = setOf(deferredLoader))

        // When
        bootstrapper.start(this)
        advanceTimeBy(1_500)
        runCurrent()
        val state = bootstrapper.state.value

        // Then
        assertThat(state.criticalAssetsReady).isTrue()
        assertThat(state.readyForInteraction).isTrue()
        assertThat(state.deferredAssetsReady).isFalse()
    }
}

private class FakeAssetLoader(
    override val priority: AssetLoadingPriority,
    private val initializationDelayMillis: Long = 0L,
) : InitializableAssetDataStore {

    override suspend fun initialize() {
        if (initializationDelayMillis > 0) {
            delay(initializationDelayMillis)
        }
    }
}
