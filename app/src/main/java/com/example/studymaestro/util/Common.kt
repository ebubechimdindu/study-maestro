package com.example.studymaestro.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.example.studymaestro.presentation.theme.primaryLight
import com.example.studymaestro.presentation.theme.tertiaryLight
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class Priority(val title: String, val color: Color, val value: Int) {
    LOW(title = "Low", color = primaryLight, value = 0),
    MEDIUM(title = "Medium", color = tertiaryLight, value = 1),
    HIGH(title = "High", color = Color.Red, value = 2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull() { it.value == value } ?: MEDIUM
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Long?.formatMillisToDate(): String {
    val date: LocalDate = this?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
            .toLocalDate()
    } ?: LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")
    return date.format(formatter)

//    val instant = Instant.ofEpochMilli(millis)
//    val zonedDateTime = instant.atZone(ZoneId.of(timeZone))
//    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy") // Customize this
//    return zonedDateTime.format(formatter)
}

fun Long.toHours(): Float {
    val hours = this.toFloat() / 3600f
    return "%.2f".format(hours).toFloat()
}

sealed class SnackBarEvent {
    data class ShowSnackBar(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short
    ) : SnackBarEvent()

    data object NavigateUp : SnackBarEvent()
}

fun Int.pad(): String {
    return this.toString().padStart(length = 2, padChar = '0')
}