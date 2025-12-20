package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.AlignmentRepository
import com.github.arhor.spellbindr.data.repository.RacesRepository
import com.github.arhor.spellbindr.data.repository.ReferenceDataRepositoryImpl
import com.github.arhor.spellbindr.data.repository.TraitsRepository
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository as AlignmentRepositoryContract
import com.github.arhor.spellbindr.domain.repository.RacesRepository as RacesRepositoryContract
import com.github.arhor.spellbindr.domain.repository.ReferenceDataRepository
import com.github.arhor.spellbindr.domain.repository.TraitsRepository as TraitsRepositoryContract
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReferenceDataDomainModule {
    @Binds
    abstract fun bindAlignmentRepository(impl: AlignmentRepository): AlignmentRepositoryContract

    @Binds
    abstract fun bindRacesRepository(impl: RacesRepository): RacesRepositoryContract

    @Binds
    abstract fun bindTraitsRepository(impl: TraitsRepository): TraitsRepositoryContract

    @Binds
    abstract fun bindReferenceDataRepository(impl: ReferenceDataRepositoryImpl): ReferenceDataRepository
}
