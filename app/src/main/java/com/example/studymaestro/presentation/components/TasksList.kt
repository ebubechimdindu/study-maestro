package com.example.studymaestro.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.studymaestro.R
import com.example.studymaestro.domain.model.Task
import com.example.studymaestro.util.Priority
import com.example.studymaestro.util.formatMillisToDate

@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.tasksList(
    sectionTitle: String,
    tasks: List<Task>,
    emptyListText: String,
    onCheckBoxClick: (Task) -> Unit,
    onTaskCardClick: (Int?) -> Unit
) {
    item {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
    if (tasks.isEmpty()) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.img_tasks),
                    contentDescription = emptyListText,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = emptyListText,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    items(tasks) {
        TaskCard(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            task = it,
            onCheckBoxClick = {onCheckBoxClick(it)},
            onClick = {onTaskCardClick(it.taskId)}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TaskCard(
    modifier: Modifier = Modifier,
    task: Task,
    onCheckBoxClick: () -> Unit,
    onClick: () -> Unit
) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            TaskCheckBox(
                isComplete = task.isComplete,
                borderColor = Priority.fromInt(task.priority).color,
                onCheckBoxClick = onCheckBoxClick
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = task.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isComplete) {
                        TextDecoration.LineThrough
                    } else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.dueDate.formatMillisToDate(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}