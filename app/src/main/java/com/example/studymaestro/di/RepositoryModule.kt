package com.example.studymaestro.di

import com.example.studymaestro.data.repository.SessionRepositoryImpl
import com.example.studymaestro.data.repository.SubjectRepositoryImpl
import com.example.studymaestro.data.repository.TaskRepositoryImpl
import com.example.studymaestro.domain.repository.SessionRepository
import com.example.studymaestro.domain.repository.SubjectRepository
import com.example.studymaestro.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@Module tells Dagger Hilt that this class provides dependencies.
@Module////@Module is used to indicate the koltin object or class is a dagger hilt module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    //You use @Binds when you already have a class that can be injected via its constructor
    //, and you just want to bind an interface to its implementation.
    @Singleton
    @Binds
    abstract fun bindSubjectRepository(
        impl: SubjectRepositoryImpl
    ): SubjectRepository

    @Singleton
    @Binds
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Singleton
    @Binds
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository
}