package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.repository.AbilityRepositoryImpl
import com.github.arhor.spellbindr.data.repository.AlignmentRepositoryImpl
import com.github.arhor.spellbindr.data.repository.CharacterClassRepositoryImpl
import com.github.arhor.spellbindr.data.repository.CharacterRepositoryImpl
import com.github.arhor.spellbindr.data.repository.EquipmentRepositoryImpl
import com.github.arhor.spellbindr.data.repository.FavoritesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.RacesRepositoryImpl
import com.github.arhor.spellbindr.data.repository.SpellsRepositoryImpl
import com.github.arhor.spellbindr.data.repository.ThemeRepositoryImpl
import com.github.arhor.spellbindr.data.repository.TraitsRepositoryImpl
import com.github.arhor.spellbindr.domain.repository.AbilityRepository
import com.github.arhor.spellbindr.domain.repository.AlignmentRepository
import com.github.arhor.spellbindr.domain.repository.CharacterClassRepository
import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.repository.EquipmentRepository
import com.github.arhor.spellbindr.domain.repository.FavoritesRepository
import com.github.arhor.spellbindr.domain.repository.RacesRepository
import com.github.arhor.spellbindr.domain.repository.SpellsRepository
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import com.github.arhor.spellbindr.domain.repository.TraitsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CommonDomainModule {
    @Binds
    abstract fun bindCharactersRepository(impl: CharacterRepositoryImpl): CharacterRepository

    @Binds
    abstract fun bindCharacterClassRepository(impl: CharacterClassRepositoryImpl): CharacterClassRepository

    @Binds
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

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
    abstract fun bindThemeSettingsRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    abstract fun bindEquipmentRepository(impl: EquipmentRepositoryImpl): EquipmentRepository
}
