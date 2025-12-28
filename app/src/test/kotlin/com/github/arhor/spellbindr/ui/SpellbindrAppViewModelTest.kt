package com.github.arhor.spellbindr.ui

import android.util.Log
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.local.assets.AssetLoadingPriority
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import com.github.arhor.spellbindr.ui.components.app.SpellbindrAppViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpellbindrAppViewModelTest {

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
    fun `readyForInteraction should wait for critical assets and initial delay when app starts`() = runTest {
        // Given
        val criticalLoader = FakeAssetLoader(AssetLoadingPriority.CRITICAL, initializationDelayMillis = 500)
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_000)
        val viewModel = SpellbindrAppViewModel(
            loaders = setOf(criticalLoader, deferredLoader),
            themeRepository = FakeThemeRepository(),
        )

        // When
        advanceTimeBy(1_000)
        runCurrent()
        val intermediateState = viewModel.state.value

        advanceTimeBy(600)
        runCurrent()
        val finalState = viewModel.state.value

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
    fun `fullyReady should flip after deferred assets finish when initial delay has passed`() = runTest {
        // Given
        val criticalLoader = FakeAssetLoader(AssetLoadingPriority.CRITICAL)
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_500)
        val viewModel = SpellbindrAppViewModel(
            loaders = setOf(criticalLoader, deferredLoader),
            themeRepository = FakeThemeRepository(),
        )

        // When
        advanceTimeBy(1_600)
        runCurrent()
        val stateBeforeDeferredReady = viewModel.state.value

        advanceUntilIdle()
        val stateAfterDeferredReady = viewModel.state.value

        // Then
        assertThat(stateBeforeDeferredReady.readyForInteraction).isTrue()
        assertThat(stateBeforeDeferredReady.fullyReady).isFalse()
        assertThat(stateAfterDeferredReady.deferredAssetsReady).isTrue()
        assertThat(stateAfterDeferredReady.fullyReady).isTrue()
    }

    @Test
    fun `critical readiness should resolve immediately when no critical loaders are present`() = runTest {
        // Given
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_000)
        val viewModel = SpellbindrAppViewModel(
            loaders = setOf(deferredLoader),
            themeRepository = FakeThemeRepository(),
        )

        // When
        advanceTimeBy(1_500)
        runCurrent()
        val state = viewModel.state.value

        // Then
        assertThat(state.criticalAssetsReady).isTrue()
        assertThat(state.readyForInteraction).isTrue()
        assertThat(state.deferredAssetsReady).isFalse()
    }
}

private class FakeAssetLoader(
    override val loadingPriority: AssetLoadingPriority,
    private val initializationDelayMillis: Long = 0L,
) : InitializableStaticAssetDataStore {

    override suspend fun initialize() {
        if (initializationDelayMillis > 0) {
            delay(initializationDelayMillis)
        }
    }
}

private class FakeThemeRepository(
    initialMode: ThemeMode? = null,
) : ThemeRepository {
    private val mode = MutableStateFlow(initialMode)

    override val themeMode: Flow<ThemeMode?>
        get() = mode

    override suspend fun setThemeMode(mode: ThemeMode?) {
        this.mode.value = mode
    }
}
