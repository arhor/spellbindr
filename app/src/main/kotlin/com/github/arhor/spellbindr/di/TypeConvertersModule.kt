package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.local.database.converter.CharacterSheetConverter
import com.github.arhor.spellbindr.data.local.database.converter.Converter
import com.github.arhor.spellbindr.data.local.database.converter.EntityRefConverter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class TypeConvertersModule {

    @Binds
    @IntoSet
    abstract fun bindEntityRefConverter(impl: EntityRefConverter): Converter

    @Binds
    @IntoSet
    abstract fun bindCharacterSheetConverter(impl: CharacterSheetConverter): Converter
}
