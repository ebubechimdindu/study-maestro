package com.example.studymaestro.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.studymaestro.data.local.AppDatabase
import com.example.studymaestro.data.local.SessionDao
import com.example.studymaestro.data.local.SubjectDao
import com.example.studymaestro.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//In Dagger Hilt, a Component is a container that tells Hilt how long a dependency should live

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

//    @Provides defines the mapping from abstraction (Repository) to implementation (RepositoryImpl
    @Provides
    @Singleton
    fun provideDatabase(
        application: Application
    ): AppDatabase {
        return Room.databaseBuilder<AppDatabase>(
            application,
            "studumaestro.db"
        ).build()
    }


    @Provides
    @Singleton//@Singleton ensures that Hilt creates only one instance of the provided dependency and reuses it.
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    @Singleton
    fun provideTaskDaoDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }
}