package com.aryaleelastudios.astroveda.di

import android.content.Context
import com.aryaleelastudios.astroveda.data.local.AstroVedaDatabase
import com.aryaleelastudios.astroveda.data.local.PersonDao
import com.aryaleelastudios.astroveda.data.preferences.UserPreferencesRepository
import com.aryaleelastudios.astroveda.data.repository.ContentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AstroVedaDatabase {
        return AstroVedaDatabase.getDatabase(context)
    }

    @Provides
    fun providePersonDao(database: AstroVedaDatabase): PersonDao {
        return database.personDao()
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): UserPreferencesRepository {
        return UserPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideContentRepository(@ApplicationContext context: Context): ContentRepository {
        return ContentRepository(context)
    }
}
