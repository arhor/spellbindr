package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.domain.repository.CharacterRepository
import com.github.arhor.spellbindr.domain.repository.ThemeRepository
import com.github.arhor.spellbindr.domain.usecase.BuildCharacterSheetFromInputsUseCase
import com.github.arhor.spellbindr.domain.usecase.ComputeDerivedBonusesUseCase
import com.github.arhor.spellbindr.domain.usecase.DeleteCharacterUseCase
import com.github.arhor.spellbindr.domain.usecase.LoadCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveCharacterSheetsUseCase
import com.github.arhor.spellbindr.domain.usecase.ObserveThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.SaveCharacterSheetUseCase
import com.github.arhor.spellbindr.domain.usecase.SetThemeModeUseCase
import com.github.arhor.spellbindr.domain.usecase.ToggleSpellSlotUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateHitPointsUseCase
import com.github.arhor.spellbindr.domain.usecase.UpdateWeaponListUseCase
import com.github.arhor.spellbindr.domain.usecase.ValidateCharacterSheetUseCase
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

    @Provides
    fun provideValidateCharacterSheetUseCase(): ValidateCharacterSheetUseCase = ValidateCharacterSheetUseCase()

    @Provides
    fun provideComputeDerivedBonusesUseCase(): ComputeDerivedBonusesUseCase = ComputeDerivedBonusesUseCase()

    @Provides
    fun provideBuildCharacterSheetFromInputsUseCase(): BuildCharacterSheetFromInputsUseCase =
        BuildCharacterSheetFromInputsUseCase()

    @Provides
    fun provideUpdateHitPointsUseCase(): UpdateHitPointsUseCase = UpdateHitPointsUseCase()

    @Provides
    fun provideToggleSpellSlotUseCase(): ToggleSpellSlotUseCase = ToggleSpellSlotUseCase()

    @Provides
    fun provideUpdateWeaponListUseCase(): UpdateWeaponListUseCase = UpdateWeaponListUseCase()
}
