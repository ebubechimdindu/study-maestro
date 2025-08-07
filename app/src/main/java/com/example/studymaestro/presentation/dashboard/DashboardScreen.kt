package com.example.studymaestro.presentation.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studymaestro.R
import com.example.studymaestro.domain.model.Session
import com.example.studymaestro.domain.model.Subject
import com.example.studymaestro.domain.model.Task
import com.example.studymaestro.presentation.components.AddSubjectDialog
import com.example.studymaestro.presentation.components.CountCard
import com.example.studymaestro.presentation.components.DeleteDialog
import com.example.studymaestro.presentation.components.SubjectCard
import com.example.studymaestro.presentation.components.studySessionsList
import com.example.studymaestro.presentation.components.tasksList
import com.example.studymaestro.presentation.destinations.SessionScreenRouteDestination
import com.example.studymaestro.presentation.destinations.SubjectScreenRouteDestination
import com.example.studymaestro.presentation.destinations.TaskScreenRouteDestination
import com.example.studymaestro.presentation.subject.SubjectScreenNavArgs
import com.example.studymaestro.presentation.task.TaskScreenNavArgs
import com.example.studymaestro.util.SnackBarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@RootNavGraph(start = true)
@Destination
@Composable
fun DashboardScreenRoute(navigator: DestinationsNavigator) {

    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSessions.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        tasks = tasks,
        recentSessions = recentSessions,
        onEvent = viewModel::onEvent,
        snackBarEvent = viewModel.snackbarEventFlow,
        onSubjectCardClick = {
            it.let {
                val navArg = SubjectScreenNavArgs(subjectId = it)
                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
            }
        },
        onTaskCardClick = {
            it.let {
                val navArg = TaskScreenNavArgs(taskId = it, subjectId = null)
                navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
            }
        },
        onStartSessionButtonClick = {
            navigator.navigate(SessionScreenRouteDestination())
        },
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DashboardScreen(
    state: DashboardState,
    tasks: List<Task>,
    recentSessions: List<Session>,
    onEvent: (DashboardEvent) -> Unit,
    snackBarEvent: SharedFlow<SnackBarEvent>,
    onSubjectCardClick: (Int) -> Unit,
    onTaskCardClick: (Int?) -> Unit,
    onStartSessionButtonClick: () -> Unit,
) {
    var isAddSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isDeleteSessionDialogOpen by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    //It is used to run suspend functions or side effects in a Composable
    LaunchedEffect(key1 = true) {//The key1 = true part means this block will only launch once because the key never changes — it's always true.
        snackBarEvent.collectLatest {
            when (it) {
                is SnackBarEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = it.message,
                        duration = it.duration
                    )
                }

                SnackBarEvent.NavigateUp -> {}
            }
        }
    }

    AddSubjectDialog(
        isOpen = isAddSubjectDialogOpen,
        onDismissRequest = { isAddSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.SaveSubject)
            isAddSubjectDialogOpen = false
        },
        onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(DashboardEvent.OnGoalStudyHoursChange(it)) },
        selectedColors = state.subjectCardColors,
        onColorChange = { onEvent(DashboardEvent.OnSubjectCardColorChange(it)) },
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
    )

    DeleteDialog(
        isOpen = isDeleteSessionDialogOpen,
        title = "Delete Session?",
        bodyText = "Are you sure, you want to delete this session? Your studied hours will be reduced " +
                "by this session time. This action can not be undone.",
        onDismissRequest = { isDeleteSessionDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.DeleteSession)
            isDeleteSessionDialogOpen = false
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // tells scaffold Here's how I want to display snackbars on this screen
        topBar = { DashboardTopBar(modifier = Modifier) }
    ) { it ->
        LazyColumn(
            contentPadding = it,
            modifier = Modifier
                .fillMaxSize()

        ) {
            item {
                CountCardSection(
                    subjectCount = state.totalSubjectCount,
                    studiedHours = state.totalStudiedHours.toString(),
                    goalHours = state.totalGoalStudyHours.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
            item {
                SubjectCardSection(
                    subjectList = state.subjects,
                    emptyListText = "",
                    onAddIconClicked = { isAddSubjectDialogOpen = true },
                    modifier = Modifier,
                    onSubjectCardClick = onSubjectCardClick
                )
                Button(
                    onClick = onStartSessionButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp, horizontal = 30.dp)
                ) {
                    Text(text = "Start Study")
                }
            }
            tasksList(
                sectionTitle = "UPCOMING TASKS",
                tasks = tasks,
                emptyListText = "You don't have any upcoming tasks.\n " +
                        "Click the + button in subject screen to add new task.",
                onCheckBoxClick = { onEvent(DashboardEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                sessions = recentSessions,
                emptyListText = "You don't have any upcoming tasks.\n " +
                        "Click the + button in subject screen to add new task.",
                onDeleteIconClick = {
                    onEvent(DashboardEvent.OnDeleteSessionButtonClick(it))
                    isDeleteSessionDialogOpen = true
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(modifier: Modifier) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Study Maestro",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CountCardSection(
    subjectCount: Int,
    goalHours: String,
    studiedHours: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        CountCard(
            headingText = "Subject Count",
            count = "$subjectCount",
            modifier = Modifier.weight(1f)
        )
        CountCard(
            headingText = "Subject Hours",
            count = studiedHours,
            modifier = Modifier.weight(1f)
        )
        CountCard(
            headingText = "Goal Study Hours",
            count = goalHours,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubjectCardSection(
    subjectList: List<Subject>,
    modifier: Modifier = Modifier,
    emptyListText: String = "You don't have any subjects.\\n Click the + button to add new subject.",
    onAddIconClicked: () -> Unit,
    onSubjectCardClick: (Int) -> Unit,

    ) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "SUBJECTS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = { onAddIconClicked() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject"
                )
            }
        }

        if (subjectList.isEmpty()) {
            Image(
                painter = painterResource(R.drawable.img_books),
                contentDescription = emptyListText,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = emptyListText,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
        ) {
            items(subjectList) {
                SubjectCard(
                    subjectName = it.name,
                    gradientColors = it.colors.map { Color(it) },
                    onClick = { it.subjectId?.let { it1 -> onSubjectCardClick(it1) } })
            }
        }
    }
}