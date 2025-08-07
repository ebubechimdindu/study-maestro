package com.example.studymaestro.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    val relatedToSubject: String,
    val date: Long,
    val duration: Long,
    val sessionSubjectId: Int,
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int? = null
)

private const val HOUR_MS = 60 * 60 * 1000L

val sessions = listOf(
    Session(
        relatedToSubject = "Physics",
        date = daysFromNow(-2),          // two days ago
        duration = 1 * HOUR_MS + 30 * 60 * 1000L, // 1 h 30 m
        sessionSubjectId = 1,
        sessionId = 1
    ),
    Session(
        relatedToSubject = "History",
        date = daysFromNow(-1),          // yesterday
        duration = 1 * HOUR_MS,          // 1 h
        sessionSubjectId = 2,
        sessionId = 2
    ),
    Session(
        relatedToSubject = "Mathematics",
        date = daysFromNow(0),           // today
        duration = 2 * HOUR_MS,          // 2 h
        sessionSubjectId = 3,
        sessionId = 3
    ),
    Session(
        relatedToSubject = "Biology",
        date = daysFromNow(1),           // tomorrow
        duration = 45 * 60 * 1000L,      // 45 m
        sessionSubjectId = 4,
        sessionId = 4
    ),
    Session(
        relatedToSubject = "Computer Science",
        date = daysFromNow(2),           // in two days
        duration = (1 * HOUR_MS) + (20 * 60 * 1000L), // 1 h 20 m
        sessionSubjectId = 5,
        sessionId = 5
    )
)