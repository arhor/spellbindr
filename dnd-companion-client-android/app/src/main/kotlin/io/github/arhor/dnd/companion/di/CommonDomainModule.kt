package io.github.arhor.dnd.companion.di

import io.github.arhor.dnd.companion.data.repository.AbilityRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.AlignmentRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.BackgroundsRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.CharacterClassRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.ConditionsRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.EquipmentRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.FeaturesRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.FeatsRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.LanguagesRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.RacesRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.SpellsRepositoryImpl
import io.github.arhor.dnd.companion.data.repository.TraitsRepositoryImpl
import io.github.arhor.dnd.companion.domain.repository.AbilityRepository
import io.github.arhor.dnd.companion.domain.repository.AlignmentRepository
import io.github.arhor.dnd.companion.domain.repository.BackgroundsRepository
import io.github.arhor.dnd.companion.domain.repository.CharacterClassRepository
import io.github.arhor.dnd.companion.domain.repository.ConditionsRepository
import io.github.arhor.dnd.companion.domain.repository.EquipmentRepository
import io.github.arhor.dnd.companion.domain.repository.FeaturesRepository
import io.github.arhor.dnd.companion.domain.repository.FeatsRepository
import io.github.arhor.dnd.companion.domain.repository.LanguagesRepository
import io.github.arhor.dnd.companion.domain.repository.RacesRepository
import io.github.arhor.dnd.companion.domain.repository.SpellsRepository
import io.github.arhor.dnd.companion.domain.repository.TraitsRepository
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
    abstract fun bindFeatsRepository(impl: FeatsRepositoryImpl): FeatsRepository

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
