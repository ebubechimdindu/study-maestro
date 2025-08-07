package com.example.studymaestro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.studymaestro.domain.model.Session
import com.example.studymaestro.domain.model.Subject
import com.example.studymaestro.domain.model.Task

//The :: operator is used to get a reference to a function, property, or class in Kotlin.
@Database(entities = [Subject::class, Session::class, Task::class], version = 1)
@TypeConverters(ColorListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao

    abstract fun sessionDao(): SessionDao

    abstract fun taskDao(): TaskDao
}