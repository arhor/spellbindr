package com.github.arhor.spellbindr.data.local.assets

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class StaticAssetDataStoreBaseTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var stores: Set<@JvmSuppressWildcards InitializableStaticAssetDataStore>

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `should load static assets without exceptions`() = runTest {
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
                "Static asset stores failed to initialize:${
                    errors.joinToString(
                        separator = "\n\t",
                        prefix = "\n\t",
                        postfix = "\n",
                    )
                }"
            )
        }
    }
}
