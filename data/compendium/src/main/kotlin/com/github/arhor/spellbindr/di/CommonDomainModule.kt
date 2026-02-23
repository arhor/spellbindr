package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.AbilityRepositoryImpl
import com.github.arhor.spellbindr.data.repository.AlignmentRepositoryImpl
import com.github.arhor.spellbindr.data.repository.BackgroundsRepositoryImpl
import com.github.arhor.spellbindr.data.repository.CharacterClassRepositoryImpl
import com.github.arhor.spellbindr.data.repository.ConditionsRepositoryImpl
import com.github.arhor.spellbindr.data.repository.EquipmentRepositoryImpl
import com.github.arhor.spellbindr.data.repository.FeaturesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.LanguagesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.RacesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.SpellsRepositoryImpl
import com.github.arhor.spellbindr.data.repository.TraitsRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.AbilityRepository
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import com.github.arhor.spellbindr.domain.repository.BackgroundsRepository
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.ConditionsRepository
import com.github.arhor.spellbindr.domain.repository.EquipmentRepository
import com.github.arhor.spellbindr.domain.repository.FeaturesRepository
import com.github.arhor.spellbindr.domain.repository.LanguagesRepository
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonDomainModule {
    @Binds
    abstract fun bindCharacterClassRepository(impl: CharacterClassRepositoryImpl): CharacterClassRepository

    @Binds
    abstract fun bindBackgroundsRepository(impl: BackgroundsRepositoryImpl): BackgroundsRepository

    @Binds
    abstract fun bindFeaturesRepository(impl: FeaturesRepositoryImpl): FeaturesRepository

    @Binds
    abstract fun bindLanguagesRepository(impl: LanguagesRepositoryImpl): LanguagesRepository

    @Binds
    abstract fun bindAbilityRepository(impl: AbilityRepositoryImpl): AbilityRepository

    @Binds
    abstract fun bindAlignmentRepository(impl: AlignmentRepositoryImpl): AlignmentRepository

    @Binds
    abstract fun bindRacesRepository(impl: RacesRepositoryImpl): RacesRepository

    @Binds
    abstract fun bindTraitsRepository(impl: TraitsRepositoryImpl): TraitsRepository

    @Binds
    abstract fun bindSpellsRepository(impl: SpellsRepositoryImpl): SpellsRepository

    @Binds
    abstract fun bindEquipmentRepository(impl: EquipmentRepositoryImpl): EquipmentRepository

    @Binds
    abstract fun bindConditionsRepository(impl: ConditionsRepositoryImpl): ConditionsRepository
}
