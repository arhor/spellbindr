package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.AlignmentRepositoryImpl
import com.github.arhor.spellbindr.data.repository.RacesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.ReferenceDataRepositoryImpl
import com.github.arhor.spellbindr.data.repository.TraitsRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import com.github.arhor.spellbindr.domain.repository.ReferenceDataRepository
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReferenceDataDomainModule {
    @Binds
    abstract fun bindAlignmentRepository(impl: AlignmentRepositoryImpl): AlignmentRepository

    @Binds
    abstract fun bindRacesRepository(impl: RacesRepositoryImpl): RacesRepository

    @Binds
    abstract fun bindTraitsRepository(impl: TraitsRepositoryImpl): TraitsRepository

    @Binds
    abstract fun bindReferenceDataRepository(impl: ReferenceDataRepositoryImpl): ReferenceDataRepository
}
