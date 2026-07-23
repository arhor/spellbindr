package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.AssetDataStore
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.ConditionsDataStore
import com.github.arhor.spellbindr.data.local.assets.DefaultAssetBootstrapper
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.FeaturesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.LanguagesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.TraitsAssetDataStore
import com.github.arhor.spellbindr.domain.AssetBootstrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AssetDataStoreModule {

    @Binds
    abstract fun bindAssetBootstrapper(bootstrapper: DefaultAssetBootstrapper): AssetBootstrapper

    @Binds
    @IntoSet
    abstract fun bindBackgroundsAssetDataStore(dataStore: BackgroundsAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindEquipmentAssetDataStore(dataStore: EquipmentAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindRacesAssetDataStore(dataStore: CharacterRaceAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindAlignmentsAssetDataStore(dataStore: AlignmentAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindCharacterClassesAssetDataStore(dataStore: CharacterClassAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindSpellsAssetDataStore(dataStore: SpellAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindTraitsAssetDataStore(dataStore: TraitsAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindFeaturesAssetDataStore(dataStore: FeaturesAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindLanguagesAssetDataStore(dataStore: LanguagesAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindAbilitiesAssetDataStore(dataStore: AbilityAssetDataStore)
        : AssetDataStore<*>

    @Binds
    @IntoSet
    abstract fun bindConditionsDataStore(dataStore: ConditionsDataStore)
        : AssetDataStore<*>
}
