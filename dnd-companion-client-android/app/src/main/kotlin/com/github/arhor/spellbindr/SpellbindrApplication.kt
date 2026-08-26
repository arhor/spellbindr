package com.github.arhor.spellbindr

import android.app.Application
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SpellbindrApplication : Application() {

    @Inject
    lateinit var assetBootstrapper: AssetBootstrapper

    override fun onCreate() {
        super.onCreate()
        assetBootstrapper.start()
    }
}
