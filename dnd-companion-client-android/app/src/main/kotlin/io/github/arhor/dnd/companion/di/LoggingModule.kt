package io.github.arhor.dnd.companion.di

import io.github.arhor.dnd.companion.logging.AndroidLoggerFactory
import io.github.arhor.dnd.companion.logging.LoggerFactory
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
