package com.github.arhor.spellbindr.ui

import android.util.Log
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import com.github.arhor.spellbindr.domain.model.AssetBootstrapState
import com.github.arhor.spellbindr.domain.model.ThemeMode
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import com.github.arhor.spellbindr.ui.components.app.SpellbindrAppViewModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    fun `readyForInteraction should reflect bootstrapper state updates`() = runTest {
        // Given
        val bootstrapper = FakeAssetBootstrapper()
        val viewModel = SpellbindrAppViewModel(
            assetBootstrapper = bootstrapper,
            themeRepository = FakeThemeRepository(),
        )

        // When
        bootstrapper.update(
            AssetBootstrapState(
                initialDelayPassed = false,
                criticalAssetsReady = true,
                deferredAssetsReady = false,
            ),
        )
        runCurrent()
        val intermediateState = viewModel.state.value

        bootstrapper.update(
            AssetBootstrapState(
                initialDelayPassed = true,
                criticalAssetsReady = true,
                deferredAssetsReady = false,
            ),
        )
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
    fun `fullyReady should become true when deferred assets are ready`() = runTest {
        // Given
        val bootstrapper = FakeAssetBootstrapper(
            AssetBootstrapState(
                initialDelayPassed = true,
                criticalAssetsReady = true,
                deferredAssetsReady = false,
            ),
        )
        val viewModel = SpellbindrAppViewModel(
            assetBootstrapper = bootstrapper,
            themeRepository = FakeThemeRepository(),
        )

        // When
        runCurrent()
        val stateBeforeDeferredReady = viewModel.state.value

        bootstrapper.update(
            AssetBootstrapState(
                initialDelayPassed = true,
                criticalAssetsReady = true,
                deferredAssetsReady = true,
            ),
        )
        runCurrent()
        val stateAfterDeferredReady = viewModel.state.value

        // Then
        assertThat(stateBeforeDeferredReady.readyForInteraction).isTrue()
        assertThat(stateBeforeDeferredReady.fullyReady).isFalse()
        assertThat(stateAfterDeferredReady.deferredAssetsReady).isTrue()
        assertThat(stateAfterDeferredReady.fullyReady).isTrue()
    }

    @Test
    fun `theme updates should propagate into app state`() = runTest {
        // Given
        val bootstrapper = FakeAssetBootstrapper()
        val themeRepository = FakeThemeRepository()
        val viewModel = SpellbindrAppViewModel(
            assetBootstrapper = bootstrapper,
            themeRepository = themeRepository,
        )

        // When
        themeRepository.setThemeMode(ThemeMode.DARK)
        runCurrent()
        val stateAfterDark = viewModel.state.value

        // Then
        assertThat(stateAfterDark.isDarkTheme).isTrue()
    }
}

private class FakeAssetBootstrapper(
    initialState: AssetBootstrapState = AssetBootstrapState(),
) : AssetBootstrapper {
    private val mutableState = MutableStateFlow(initialState)

    override val state: StateFlow<AssetBootstrapState>
        get() = mutableState

    override fun start(scope: CoroutineScope) = Unit

    fun update(state: AssetBootstrapState) {
        mutableState.value = state
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
