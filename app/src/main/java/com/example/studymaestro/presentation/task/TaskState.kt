package com.example.studymaestro.presentation.task

import androidx.compose.ui.graphics.Color
import com.example.studymaestro.domain.model.Session
import com.example.studymaestro.domain.model.Subject
import com.example.studymaestro.domain.model.Task
import com.example.studymaestro.util.Priority

data class TaskState(
    val title: String = "",
    val description: String = "",
    val dueDate: Long? = null,
    val isTaskComplete: Boolean = false,
    val priority: Priority = Priority.LOW,
    val relatedToSubject: String? = null,
    val subjects: List<Subject> = emptyList(),
    val subjectId: Int? = null,
    val currentTaskId: Int? = null
)
