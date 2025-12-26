package com.github.arhor.spellbindr.ui

import android.util.Log
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.local.assets.AssetLoadingPriority
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
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
    fun `readyForInteraction waits for critical assets and delay`() = runTest {
        val criticalLoader = FakeAssetLoader(AssetLoadingPriority.CRITICAL, initializationDelayMillis = 500)
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_000)
        val viewModel = SpellbindrAppViewModel(
            loaders = setOf(criticalLoader, deferredLoader),
            themeRepository = FakeThemeRepository(),
        )

        advanceTimeBy(1_000)
        runCurrent()

        assertThat(viewModel.state.value.initialDelayPassed).isFalse()
        assertThat(viewModel.state.value.criticalAssetsReady).isTrue()
        assertThat(viewModel.state.value.readyForInteraction).isFalse()

        advanceTimeBy(600)
        runCurrent()

        with(viewModel.state.value) {
            assertThat(initialDelayPassed).isTrue()
            assertThat(criticalAssetsReady).isTrue()
            assertThat(readyForInteraction).isTrue()
            assertThat(deferredAssetsReady).isFalse()
        }
    }

    @Test
    fun `fullyReady flips after deferred assets finish`() = runTest {
        val criticalLoader = FakeAssetLoader(AssetLoadingPriority.CRITICAL)
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_500)
        val viewModel = SpellbindrAppViewModel(
            loaders = setOf(criticalLoader, deferredLoader),
            themeRepository = FakeThemeRepository(),
        )

        advanceTimeBy(1_600)
        runCurrent()

        assertThat(viewModel.state.value.readyForInteraction).isTrue()
        assertThat(viewModel.state.value.fullyReady).isFalse()

        advanceUntilIdle()

        assertThat(viewModel.state.value.deferredAssetsReady).isTrue()
        assertThat(viewModel.state.value.fullyReady).isTrue()
    }

    @Test
    fun `critical readiness resolves when no critical loaders present`() = runTest {
        val deferredLoader = FakeAssetLoader(AssetLoadingPriority.DEFERRED, initializationDelayMillis = 2_000)
        val viewModel = SpellbindrAppViewModel(
            loaders = setOf(deferredLoader),
            themeRepository = FakeThemeRepository(),
        )

        advanceTimeBy(1_500)
        runCurrent()

        assertThat(viewModel.state.value.criticalAssetsReady).isTrue()
        assertThat(viewModel.state.value.readyForInteraction).isTrue()
        assertThat(viewModel.state.value.deferredAssetsReady).isFalse()
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
