package com.example.studymaestro.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.studymaestro.presentation.theme.gradient1
import com.example.studymaestro.presentation.theme.gradient2
import com.example.studymaestro.presentation.theme.gradient3
import com.example.studymaestro.presentation.theme.gradient4

@Entity
data class Subject(
    val name: String,
    val goalHours: Float,
    val colors: List<Int>,
    @PrimaryKey(autoGenerate = true)
    val subjectId:Int? = null,
) {
    companion object {
        val subjectCardColors = listOf(gradient1, gradient2, gradient3, gradient4)
    }
}

val subjects = listOf<Subject>(
    Subject(name="English",goalHours=10f, colors = Subject.subjectCardColors[0].map{it.toArgb()},subjectId=1),
    Subject(name="English",goalHours=10f, colors = Subject.subjectCardColors[1].map{it.toArgb()},subjectId=2),
    Subject(name="English",goalHours=10f, colors = Subject.subjectCardColors[2].map{it.toArgb()},subjectId=3),
    Subject(name="English",goalHours=10f, colors = Subject.subjectCardColors[3].map{it.toArgb()},subjectId=4)
)