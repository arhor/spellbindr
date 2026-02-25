package com.github.arhor.spellbindr.di

import com.github.arhor.spellbindr.logging.AndroidLoggerFactory
import com.github.arhor.spellbindr.logging.LoggerFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    @Singleton
    abstract fun bindLoggerFactory(impl: AndroidLoggerFactory): LoggerFactory
}
