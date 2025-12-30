package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.DefaultAssetBootstrapper
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.FeaturesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.InitializableAssetDataStore
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
    abstract fun bindBackgroundsAssetDataStore(backgroundsDataStore: BackgroundsAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindEquipmentAssetDataStore(equipmentDataStore: EquipmentAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindRacesAssetDataStore(dacesAssetDataStore: CharacterRaceAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindAlignmentsAssetDataStore(alignmentAssetDataStore: AlignmentAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindCharacterClassesAssetDataStore(characterClassesDataStore: CharacterClassAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindSpellsAssetDataStore(spellsDataStore: SpellAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindTraitsAssetDataStore(traitsDataStore: TraitsAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindFeaturesAssetDataStore(featuresDataStore: FeaturesAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindLanguagesAssetDataStore(languagesAssetDataStore: LanguagesAssetDataStore)
            : InitializableAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindAbilitiesAssetDataStore(abilitiesAssetDataStore: AbilityAssetDataStore)
            : InitializableAssetDataStore
}
