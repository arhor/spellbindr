package com.github.arhor.spellbindr.ui

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.github.arhor.spellbindr.MainDispatcherRule
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.data.model.AppThemeMode
import com.github.arhor.spellbindr.data.repository.ThemeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.io.path.createTempFile

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SpellbindrAppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `state leaves theme unset when preference is missing`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, _, cleanup) = createViewModel()
        try {
            advanceUntilIdle()
            advanceTimeBy(2_000)

            assertThat(viewModel.state.value.isDarkTheme).isNull()
        } finally {
            cleanup()
        }
    }

    @Test
    fun `state updates when theme preference changes`() = runTest(mainDispatcherRule.dispatcher) {
        val (viewModel, repository, cleanup) = createViewModel()
        try {
            advanceUntilIdle()
            advanceTimeBy(2_000)
            assertThat(viewModel.state.value.isDarkTheme).isNull()

            repository.setThemeMode(AppThemeMode.DARK)
            advanceUntilIdle()
            advanceTimeBy(1)

            assertThat(viewModel.state.value.isDarkTheme).isTrue()
        } finally {
            cleanup()
        }
    }

    private fun TestScope.createViewModel(): Triple<SpellbindrAppViewModel, ThemeRepository, () -> Unit> {
        val file = createTempFile(prefix = "app-vm", suffix = ".preferences_pb").toFile()
        val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
        val repository = ThemeRepository(dataStore)
        val loaders = setOf<InitializableStaticAssetDataStore>(
            object : InitializableStaticAssetDataStore {
                override suspend fun initialize() = Unit
            },
        )
        val viewModel = SpellbindrAppViewModel(loaders, repository)
        val cleanup: () -> Unit = { file.delete() }
        return Triple(viewModel, repository, cleanup)
    }
}
