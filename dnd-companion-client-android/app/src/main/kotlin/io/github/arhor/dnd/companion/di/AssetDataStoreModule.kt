package io.github.arhor.dnd.companion.di

import io.github.arhor.dnd.companion.data.local.assets.AbilityAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.AlignmentAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.AssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.BackgroundsAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.CharacterClassAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.CharacterRaceAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.ConditionsDataStore
import io.github.arhor.dnd.companion.data.local.assets.DefaultAssetBootstrapper
import io.github.arhor.dnd.companion.data.local.assets.EquipmentAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.FeaturesAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.FeatsAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.LanguagesAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.SpellAssetDataStore
import io.github.arhor.dnd.companion.data.local.assets.TraitsAssetDataStore
import io.github.arhor.dnd.companion.domain.AssetBootstrapper
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
    abstract fun bindFeatsAssetDataStore(dataStore: FeatsAssetDataStore)
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
