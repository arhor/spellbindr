package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.local.assets.AbilityAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterRaceAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.FeaturesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.LanguagesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.TraitsAssetDataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@Suppress("UNUSED")
@InstallIn(SingletonComponent::class)
abstract class StaticAssetsModule {

    @Binds
    @IntoSet
    abstract fun bindBackgroundsAssetDataStore(backgroundsDataStore: BackgroundsAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindEquipmentAssetDataStore(equipmentDataStore: EquipmentAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindRacesAssetDataStore(dacesAssetDataStore: CharacterRaceAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindAlignmentsAssetDataStore(alignmentAssetDataStore: AlignmentAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindCharacterClassesAssetDataStore(characterClassesDataStore: CharacterClassAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindSpellsAssetDataStore(spellsDataStore: SpellAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindTraitsAssetDataStore(traitsDataStore: TraitsAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindFeaturesAssetDataStore(featuresDataStore: FeaturesAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindLanguagesAssetDataStore(languagesAssetDataStore: LanguagesAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindAbilitiesAssetDataStore(abilitiesAssetDataStore: AbilityAssetDataStore)
        : InitializableStaticAssetDataStore
}
