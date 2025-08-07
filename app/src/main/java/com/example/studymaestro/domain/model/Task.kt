package com.example.studymaestro.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    val title: String,
    val description: String,
    val dueDate: Long,
    val priority: Int,
    val relatedToSubject: String,
    val isComplete: Boolean,
    val taskSubjectId:Int,
    @PrimaryKey(autoGenerate = true)
    val taskId:Int? = null,
)

// Helper to generate current + days in milliseconds
fun daysFromNow(days: Int): Long = System.currentTimeMillis() + days * 24 * 60 * 60 * 1000

val tasks = listOf(
    Task(
        title = "Prepare notes",
        description = "Summarize chapter 3 of Physics textbook",
        dueDate = daysFromNow(3),
        priority = 2,
        relatedToSubject = "Physics",
        isComplete = false,
        taskSubjectId=1,
        taskId = 1,
    ),
    Task(
        title = "Write essay",
        description = "Complete the essay on World War II for History class",
        dueDate = daysFromNow(5),
        priority = 1,
        relatedToSubject = "History",
        isComplete = false,
        taskSubjectId=2,
        taskId = 2,
    ),
    Task(
        title = "Math homework",
        description = "Solve integration problems on page 52",
        dueDate = daysFromNow(2),
        priority = 3,
        relatedToSubject = "Mathematics",
        isComplete = true,
        taskSubjectId=3,
        taskId = 3,
    ),
    Task(
        title = "Biology quiz prep",
        description = "Revise digestive system notes for the quiz",
        dueDate = daysFromNow(1),
        priority = 1,
        relatedToSubject = "Biology",
        isComplete = false,
        taskSubjectId=4,
        taskId = 4,
    ),
    Task(
        title = "Group project sync",
        description = "Meet with team to plan Computer Science project",
        dueDate = daysFromNow(4),
        priority = 2,
        relatedToSubject = "Computer Science",
        isComplete = false,
        taskSubjectId=5,
        taskId = 5,
    )
)
