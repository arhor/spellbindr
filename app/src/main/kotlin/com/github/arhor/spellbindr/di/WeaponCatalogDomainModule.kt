package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.WeaponCatalogRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.WeaponCatalogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WeaponCatalogDomainModule {
    @Binds
    abstract fun bindWeaponCatalogRepository(impl: WeaponCatalogRepositoryImpl): WeaponCatalogRepository
}
