package com.liquidos.launcher.di

import android.content.Context
import androidx.room.Room
import com.liquidos.launcher.data.database.LauncherDatabase
import com.liquidos.launcher.data.database.WorkspaceDao
import com.liquidos.launcher.data.repository.AppRepository
import com.liquidos.launcher.data.repository.AppRepositoryImpl
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
    fun provideLauncherDatabase(@ApplicationContext context: Context): LauncherDatabase {
        return Room.databaseBuilder(
            context,
            LauncherDatabase::class.java,
            "liquidos_launcher_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWorkspaceDao(database: LauncherDatabase): WorkspaceDao {
        return database.workspaceDao
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context
    ): AppRepository {
        return AppRepositoryImpl(context)
    }
}
