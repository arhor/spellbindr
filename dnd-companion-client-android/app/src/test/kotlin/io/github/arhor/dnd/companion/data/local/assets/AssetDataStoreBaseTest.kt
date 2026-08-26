package io.github.arhor.dnd.companion.data.local.assets

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.arhor.dnd.companion.di.AppInfrastructureModule
import io.github.arhor.dnd.companion.di.DatabaseModule
import io.github.arhor.dnd.companion.di.FavoritesDataStoreModule
import io.github.arhor.dnd.companion.settings.di.SettingsDataStoreModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    AppInfrastructureModule::class,
    DatabaseModule::class,
    FavoritesDataStoreModule::class,
    SettingsDataStoreModule::class,
)
@RunWith(AndroidJUnit4::class)
class AssetDataStoreBaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var stores: Set<@JvmSuppressWildcards AssetDataStore<*>>

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `initialize should load static assets without exceptions when stores are injected`() = runTest {
        // Given
        val errors = mutableListOf<String>()

        // When
        for (store in stores) {
            try {
                store.initialize()
            } catch (e: Exception) {
                errors += "${store::class.java.simpleName}: ${e.message}"
            }
        }

        // Then
        if (errors.isNotEmpty()) {
            fail(
                """
                Static asset stores failed to initialize:
                    ${errors.joinToString(separator = "\n\t")}
                """.trimIndent()
            )
        }
    }
}
