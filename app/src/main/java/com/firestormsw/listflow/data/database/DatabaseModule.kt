package com.firestormsw.listflow.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, AppDatabase::class.java, "AppDatabase").build()

    @Singleton
    @Provides
    fun provideListDao(database: AppDatabase) = database.getListsDao()

    @Singleton
    @Provides
    fun provideListItemDao(database: AppDatabase) = database.getListItemsDao()

    @Singleton
    @Provides
    fun providePeerDao(database: AppDatabase) = database.getPeersDao()
}