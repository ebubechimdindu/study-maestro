package com.example.studymaestro.presentation.dashboard

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studymaestro.domain.model.Session
import com.example.studymaestro.domain.model.Subject
import com.example.studymaestro.domain.model.Task
import com.example.studymaestro.domain.repository.SessionRepository
import com.example.studymaestro.domain.repository.SubjectRepository
import com.example.studymaestro.domain.repository.TaskRepository
import com.example.studymaestro.util.SnackBarEvent
import com.example.studymaestro.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel // Tells hilt that this class is a ViewModel
class DashboardViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = combine(
        _state,
        subjectRepository.getTotalSubjectCount(),
        subjectRepository.getTotalGoalHours(),
        subjectRepository.getAllSubjects(),
        sessionRepository.getTotalSessionsDuration()
    ) { state, subjectCount, goalHours, subjects, totalSessionDuration ->
        state.copy(
            totalSubjectCount = subjectCount,
            totalGoalStudyHours = goalHours,
            subjects = subjects,
            totalStudiedHours = totalSessionDuration.toHours()
        )
        // Hot Flow (like StateFlow): Always active, and can share emissions with multiple collectors.
    }.stateIn(// stateIn is used to convert a cold flow into a hot,stateful flow(Converts a Flow into a StateFlow)
        scope = viewModelScope, // Here we are saying the state is tied to the viewModel and it will be destroyed when the associated Viewmodel is cleared or no longer in you
        started = SharingStarted.WhileSubscribed(5000),//Keeps things running only when needed
        initialValue = DashboardState()
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackBarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()


    private fun saveSubject() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        colors = state.value.subjectCardColors.map { it.toArgb() }
                    )
                )
                _state.update {
                    it.copy(
                        subjectName = "",
                        goalStudyHours = "",
                        subjectCardColors = Subject.subjectCardColors.random()
                    )
                }
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        "Subject Saved Successfully."
                    )
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        "Couldn't Save Subject. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }

        }
    }

    //tasks and recentSessions are independent streams of data.
    //They don't modify the main DashboardState, and the UI can use them on their own.
    val tasks: StateFlow<List<Task>> = taskRepository.getAllUpcomingTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed((5000)),
        initialValue = emptyList()
    )


    val recentSessions: StateFlow<List<Session>> =
        sessionRepository.getRecentFiveSessions().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed((5000)),
            initialValue = emptyList()
        )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.DeleteSession -> deleteSession()
            is DashboardEvent.OnDeleteSessionButtonClick -> _state.update {
                it.copy(session = event.session)
            }

            is DashboardEvent.OnGoalStudyHoursChange -> _state.update {
                it.copy(goalStudyHours = event.hours)
            }

            is DashboardEvent.OnSubjectCardColorChange -> _state.update {
                it.copy(subjectCardColors = event.colors)
            }

            is DashboardEvent.OnSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }

            is DashboardEvent.OnTaskIsCompleteChange -> {
                updateTask(event.task)
            }

            DashboardEvent.SaveSubject -> saveSubject()
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(isComplete = !task.isComplete)
                )
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(message = "Saved in Completed tasks.")
                )
                _snackbarEventFlow.emit(SnackBarEvent.NavigateUp)
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Couldn't update task status. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun deleteSession() {
        viewModelScope.launch {
            try {
                state.value.session?.let {
                    sessionRepository.deleteSession(it)
                    _snackbarEventFlow.emit(
                        SnackBarEvent.ShowSnackBar(message = "Session deleted successfully")
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Couldn't delete session. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }
}