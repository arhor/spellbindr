package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.data.AppDatabase
import com.github.arhor.spellbindr.repository.CharacterRepository
import com.github.arhor.spellbindr.repository.CharacterRepositoryImpl
import com.github.arhor.spellbindr.repository.SpellRepository
import com.github.arhor.spellbindr.viewmodel.CharacterViewModel
import com.github.arhor.spellbindr.viewmodel.SpellSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(get()).characterDao() }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    viewModel { CharacterViewModel(get()) }
    
    // Spell-related modules
    single { SpellRepository(get()) }
    viewModel { SpellSearchViewModel(get()) }
}
