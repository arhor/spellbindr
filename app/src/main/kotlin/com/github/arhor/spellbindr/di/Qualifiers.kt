package com.github.arhor.spellbindr.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppSettingsDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FavoritesDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
