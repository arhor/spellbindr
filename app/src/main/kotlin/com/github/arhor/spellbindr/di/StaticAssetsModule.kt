package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.local.assets.AlignmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.BackgroundsAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.CharacterClassAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.ConditionAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.EquipmentAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.InitializableStaticAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.RacesAssetDataStore
import com.github.arhor.spellbindr.data.local.assets.SpellAssetDataStore
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
    abstract fun bindRacesAssetDataStore(dacesAssetDataStore: RacesAssetDataStore)
        : InitializableStaticAssetDataStore

    @Binds
    @IntoSet
    abstract fun bindConditionsAssetDataStore(conditionAssetDataStore: ConditionAssetDataStore)
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
}
