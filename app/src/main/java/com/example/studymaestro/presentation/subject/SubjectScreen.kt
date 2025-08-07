package com.example.studymaestro.presentation.subject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studymaestro.domain.model.Subject
import com.example.studymaestro.domain.model.sessions
import com.example.studymaestro.domain.model.tasks
import com.example.studymaestro.presentation.components.AddSubjectDialog
import com.example.studymaestro.presentation.components.CountCard
import com.example.studymaestro.presentation.components.DeleteDialog
import com.example.studymaestro.presentation.components.studySessionsList
import com.example.studymaestro.presentation.components.tasksList
import com.example.studymaestro.presentation.destinations.SubjectScreenRouteDestination
import com.example.studymaestro.presentation.destinations.TaskScreenRouteDestination
import com.example.studymaestro.presentation.task.TaskScreenNavArgs
import com.example.studymaestro.util.SnackBarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

data class SubjectScreenNavArgs(
    val subjectId: Int
)

@RequiresApi(Build.VERSION_CODES.O)
@Destination(navArgsDelegate = SubjectScreenNavArgs::class)
@Composable
fun SubjectScreenRoute(
    navigator: DestinationsNavigator
){
    val viewModel: SubjectViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()


    SubjectScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackBarEvent = viewModel.snackbarEventFlow,
        onBackButtonClick = {navigator.navigateUp()},
        onAddTaskButtonClick = {
            val navArg = TaskScreenNavArgs(taskId = null, subjectId = state.currentSubjectId)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
        onTaskCardClick = {
            val navArg = TaskScreenNavArgs(taskId = it, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        }
    )
}



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    state: SubjectState,
    onEvent: (SubjectEvent) -> Unit,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onBackButtonClick: () -> Unit,
    onAddTaskButtonClick: () -> Unit,
    onTaskCardClick: (Int?) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val isFABExpanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    var isEditSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(key1 = state.studiedHours, key2 = state.goalStudyHours) {//The key1 = true part means this block will only launch once because the key never changes — it's always true.
        onEvent(SubjectEvent.UpdateProgress)
    }

    AddSubjectDialog(
        isOpen = isEditSubjectDialogOpen,
        onDismissRequest = { isEditSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.UpdateSubject)
            isEditSubjectDialogOpen = false },
        onSubjectNameChange = { onEvent(SubjectEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(SubjectEvent.OnGoalStudyHoursChange(it)) },
        selectedColors = state.subjectCardColors,
        onColorChange = {onEvent(SubjectEvent.OnSubjectCardColorChange(it))},
        subjectName = state.subjectName,
        goalHours =state.goalStudyHours,
    )

    DeleteDialog(
        isOpen = isDeleteSubjectDialogOpen,
        title = "Delete Subject?",
        bodyText = "Are you sure, you want to delete this subject? All related " +
                "tasks and study sessions will be permanently removed. This action can not be undone",
        onDismissRequest = { isDeleteSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSubject)
            isDeleteSubjectDialogOpen = false
        }
    )

    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = "Delete Session?",
        bodyText = "Are you sure, you want to delete this session? Your studied hours will be reduced " +
                "by this session time. This action can not be undone.",
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(SubjectEvent.DeleteSession)
            isDeleteSessionDialogOpen = false
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubjectScreenTopBar(
                title = state.subjectName,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = { isDeleteSubjectDialogOpen = true},
                onEditButtonClick = { isEditSubjectDialogOpen = true},
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTaskButtonClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") },
                text = { Text(text = "Add Task") },
                expanded = isFABExpanded
            )
        }
    ) { paddingValue ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ) {
            item {
                SubjectOverviewSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    studiedHours = state.studiedHours.toString(),
                    goalHours = state.goalStudyHours,
                    progress = state.progress
                )
            }
            tasksList(
                sectionTitle = "UPCOMING TASKS",
                tasks = state.upcomingTasks,
                emptyListText = "You don't have any upcoming tasks.\n " +
                        "Click the + button in subject screen to add new task.",
                onCheckBoxClick = {onEvent(SubjectEvent.OnTaskIsCompleteChange(it))},
                onTaskCardClick = onTaskCardClick
            )

            tasksList(
                sectionTitle = "COMPLETED TASKS",
                tasks = state.completedTasks,
                emptyListText = "You don't have any completed tasks.\n " +
                        "Click the check box on completion of task.",
                onCheckBoxClick = {onEvent(SubjectEvent.OnTaskIsCompleteChange(it))},
                onTaskCardClick = onTaskCardClick
            )
            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                sessions = state.recentSessions,
                emptyListText = "You don't have any upcoming tasks.\n " +
                        "Click the + button in subject screen to add new task.",
                onDeleteIconClick = {
                    isDeleteSubjectDialogOpen = true
                    onEvent(SubjectEvent.OnDeleteSessionButtonClick(it))
                },
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreenTopBar(
    title: String,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onEditButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LargeTopAppBar(
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
            IconButton(onClick = onDeleteButtonClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Subject"
                )
            }
            IconButton(onClick = onEditButtonClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Subject"
                )
            }
        }
    )
}

@Composable
private fun SubjectOverviewSection(
    modifier: Modifier,
    studiedHours: String,
    goalHours: String,
    progress: Float
) {
    val percentageProgress = remember(key1 = progress) {
        (progress * 100).toInt().coerceIn(0, 100)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "Goal Study Hours",
            count = goalHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "Studied Hours",
            count = studiedHours
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(75.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 4.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                strokeCap = StrokeCap.Round,
            )
            Text(text = "$percentageProgress%")
        }
    }
}