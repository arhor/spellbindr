package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {

    @Provides
    fun provideObserveCharacterSheetsUseCase(
        characterRepository: CharacterRepository,
    ): ObserveCharacterSheetsUseCase = ObserveCharacterSheetsUseCase(characterRepository)

    @Provides
    fun provideLoadCharacterSheetUseCase(
        characterRepository: CharacterRepository,
    ): LoadCharacterSheetUseCase = LoadCharacterSheetUseCase(characterRepository)

    @Provides
    fun provideSaveCharacterSheetUseCase(
        characterRepository: CharacterRepository,
    ): SaveCharacterSheetUseCase = SaveCharacterSheetUseCase(characterRepository)

    @Provides
    fun provideDeleteCharacterUseCase(
        characterRepository: CharacterRepository,
    ): DeleteCharacterUseCase = DeleteCharacterUseCase(characterRepository)

    @Provides
    fun provideObserveThemeModeUseCase(
        themeRepository: ThemeRepository,
    ): ObserveThemeModeUseCase = ObserveThemeModeUseCase(themeRepository)

    @Provides
    fun provideSetThemeModeUseCase(
        themeRepository: ThemeRepository,
    ): SetThemeModeUseCase = SetThemeModeUseCase(themeRepository)
}
