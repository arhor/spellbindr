package com.github.arhor.spellbindr.data.local.assets

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
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
        for (store in stores) {
            store.initialize()
        }
    }
}
