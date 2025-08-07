package com.example.studymaestro.presentation.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studymaestro.presentation.components.DeleteDialog
import com.example.studymaestro.presentation.components.SubjectListBottomSheet
import com.example.studymaestro.presentation.components.TaskCheckBox
import com.example.studymaestro.presentation.components.TaskDatePicker
import com.example.studymaestro.util.Priority
import com.example.studymaestro.util.SnackBarEvent
import com.example.studymaestro.util.formatMillisToDate
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

data class TaskScreenNavArgs(
    val taskId: Int?,
    val subjectId: Int?
)

@RequiresApi(Build.VERSION_CODES.O)
@Destination(navArgsDelegate = TaskScreenNavArgs::class)
@Composable
fun TaskScreenRoute(navigator: DestinationsNavigator) {

    val viewModel: TaskViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    TaskScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackBarEvent = viewModel.snackbarEventFlow,
        onBackButtonClick = { navigator.navigateUp() }
    )

}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreen(
    state: TaskState,
    onEvent: (TaskEvent) -> Unit,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onBackButtonClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()


    var isDatePickerDialogOpen by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = PastOrPresentSelectableDates,
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )

    val date = datePickerState.selectedDateMillis.formatMillisToDate()

    val sheetState = rememberModalBottomSheetState()
    var isBottomSheetOpen by remember { mutableStateOf(false) }


    var isDeleteTaskDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }


    var taskTitleError by rememberSaveable { mutableStateOf<String?>(null) }
    taskTitleError = when {
        state.title.isBlank() -> "Please enter task title."
        state.title.length < 4 -> "Task title is too short."
        state.title.length > 30 -> "Task title is too long."
        else -> null
    }

    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {//The key1 = true part means this block will only launch once because the key never changes — it's always true.
        snackBarEvent.collect {//processes each emission fully before handling the next one. If your handler is suspending (like showSnackbar()), the next event waits.
            when (it) {
                is SnackBarEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = it.message,
                        duration = it.duration
                    )
                }

                SnackBarEvent.NavigateUp -> {
                    onBackButtonClick()
                }
            }
        }
    }

    DeleteDialog(
        isOpen = isDeleteTaskDialogOpen,
        title = "Delete Task?",
        bodyText = "Are you sure, you want to delete this Task? All related " +
                "This action can not be undone",
        onDismissRequest = { isDeleteTaskDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(TaskEvent.DeleteTask)
            isDeleteTaskDialogOpen = false
        }
    )

    TaskDatePicker(
        state = datePickerState,
        isOpen = isDatePickerDialogOpen,
        onDismissRequest = { isDatePickerDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(TaskEvent.OnDateChange(millis = datePickerState.selectedDateMillis))
            isDeleteTaskDialogOpen = false
        }
    )

    SubjectListBottomSheet(
        sheetState = sheetState,
        isOpen = isBottomSheetOpen,
        subjects = state.subjects,
        onDismissRequest = { isBottomSheetOpen = false },
        onSubjectClicked = { subject ->
            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) isBottomSheetOpen = false
            }
            onEvent(TaskEvent.OnRelatedSubjectSelect(subject))
        }
    )



    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TaskScreenTopBar(
                title = state.title,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = { isDeleteTaskDialogOpen = true },
                scrollBehavior = scrollBehavior,
                isTaskExist = state.currentTaskId != null,
                isComplete = state.isTaskComplete,
                checkBoxBorderColor = MaterialTheme.colorScheme.primary,
                onCheckBoxClick = { onEvent(TaskEvent.OnIsCompleteChange) }
            )
        },

        ) { paddingValue ->
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .fillMaxSize()
                .padding(paddingValue)
                .padding(horizontal = 12.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.title,
                onValueChange = { onEvent(TaskEvent.OnTitleChange(it)) },
                label = { Text(text = "Title") },
                singleLine = true,
                isError = taskTitleError != null && state.title.isNotBlank(),
                supportingText = { Text(text = taskTitleError.orEmpty()) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.description,
                onValueChange = { onEvent(TaskEvent.OnDescriptionChange(it)) },
                label = { Text(text = "Description") },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Due Date",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.dueDate.formatMillisToDate(),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = { isDatePickerDialogOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Due Date"
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Priority",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Priority.entries.forEach { priority ->
                    PriorityButton(
                        modifier = Modifier.weight(1f),
                        label = priority.title,
                        backgroundColor = priority.color,
                        borderColor = if (priority == state.priority) {
                            Color.White
                        } else Color.Transparent,
                        labelColor = if (priority == state.priority) {
                            Color.White
                        } else Color.White.copy(alpha = 0.7f),
                        onClick = { onEvent(TaskEvent.OnPriorityChange(priority)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "Related to subject",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val firstSubject = state.subjects.firstOrNull()?.name ?: ""
                Text(
                    text = state.relatedToSubject ?: firstSubject,
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = { isBottomSheetOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Subject"
                    )
                }
            }
            Button(
                enabled = taskTitleError == null,
                onClick = { onEvent(TaskEvent.SaveTask) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Text(text = "Save")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskScreenTopBar(
    title: String,
    isTaskExist: Boolean,
    isComplete: Boolean,
    checkBoxBorderColor: Color,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onCheckBoxClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "navigate back"
                )
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            if (isTaskExist) {
                TaskCheckBox(
                    isComplete = isComplete,
                    borderColor = checkBoxBorderColor,
                    onCheckBoxClick = onCheckBoxClick
                )
                IconButton(onClick = onDeleteButtonClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task"
                    )
                }
            }
        }
    )
}

@Composable
private fun PriorityButton(
    modifier: Modifier = Modifier,
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    labelColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(5.dp)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = labelColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
object PastOrPresentSelectableDates : SelectableDates {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return Instant.now().toEpochMilli() <= utcTimeMillis
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun isSelectableYear(year: Int): Boolean {
        return year <= LocalDate.now().year
    }
}