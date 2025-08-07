package com.example.studymaestro.presentation.subject

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymaestro.domain.model.Subject
import com.example.studymaestro.domain.model.Task
import com.example.studymaestro.domain.repository.SessionRepository
import com.example.studymaestro.domain.repository.SubjectRepository
import com.example.studymaestro.domain.repository.TaskRepository
import com.example.studymaestro.presentation.dashboard.DashboardEvent
import com.example.studymaestro.presentation.dashboard.DashboardState
import com.example.studymaestro.presentation.navArgs
import com.example.studymaestro.util.SnackBarEvent
import com.example.studymaestro.util.toHours
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository,
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle//SavedStateHandle is a key-value map provided by Jetpack's ViewModel system that allows your ViewModel to persist and restore UI-related data after process death or configuration changes
) : ViewModel() {

    private val navArgs: SubjectScreenNavArgs = savedStateHandle.navArgs()


    private val _state = MutableStateFlow(SubjectState())
    val state: StateFlow<SubjectState> = combine(
        _state,
        taskRepository.getUpcomingTasksForSubject(navArgs.subjectId),
        taskRepository.getCompletedTasksForSubject(navArgs.subjectId),
        sessionRepository.getRecentTenSessionsForSubject(navArgs.subjectId),
        sessionRepository.getTotalSessionsDurationBySubject(navArgs.subjectId)
    ) { state, upcomingTasks, completedTask, recentSessions, totalSessionsDuration ->
        state.copy(
            upcomingTasks = upcomingTasks,
            completedTasks = completedTask,
            recentSessions = recentSessions,
            studiedHours = totalSessionsDuration.toHours()
        )
    }.stateIn(
        scope = viewModelScope, // Here we are saying the state is tied to the viewModel and it will be destroyed when the associated Viewmodel is cleared or no longer in you
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubjectState()
    )

    private val _snackbarEventFlow = MutableSharedFlow<SnackBarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()


    init {
        fetchSubject()
    }

    fun onEvent(event: SubjectEvent) {
        when (event) {
            SubjectEvent.DeleteSession -> deleteSession()
            SubjectEvent.DeleteSubject -> deleteSubject()
            is SubjectEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }

            is SubjectEvent.OnGoalStudyHoursChange -> {
                _state.update {
                    it.copy(goalStudyHours = event.hours)
                }
            }

            is SubjectEvent.OnSubjectCardColorChange -> {
                _state.update {
                    it.copy(subjectCardColors = event.color)
                }
            }

            is SubjectEvent.OnSubjectNameChange -> {
                _state.update {
                    it.copy(subjectName = event.name)
                }
            }

            is SubjectEvent.OnTaskIsCompleteChange -> {
                updateTask(event.task)
            }

            SubjectEvent.UpdateProgress -> {
                val goalStudyHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f
                _state.update {
                    it.copy(
                        progress = (state.value.studiedHours / goalStudyHours).coerceIn(0f, 1f)
                    )
                }
            }

            SubjectEvent.UpdateSubject -> updateSubject()
        }
    }

    private fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.upsertTask(
                    task = task.copy(isComplete = !task.isComplete)
                )
                if (task.isComplete) {
                    _snackbarEventFlow.emit(
                        SnackBarEvent.ShowSnackBar(message = "Saved in upcoming tasks.")
                    )
                } else {
                    _snackbarEventFlow.emit(
                        SnackBarEvent.ShowSnackBar(message = "Saved in Completed tasks.")
                    )
                }

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

    private fun updateSubject() {
        viewModelScope.launch {
            try {
                subjectRepository.upsertSubject(
                    subject = Subject(
                        subjectId = state.value.currentSubjectId,
                        name = state.value.subjectName,
                        goalHours = state.value.goalStudyHours.toFloatOrNull() ?: 1f,
                        colors = state.value.subjectCardColors.map { it.toArgb() }
                    )
                )
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(message = "Subject updated successfully.")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Couldn't update subject. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

    private fun fetchSubject() {
        viewModelScope.launch {
            subjectRepository.getSubjectById(navArgs.subjectId)?.let { subject ->
                _state.update {
                    it.copy(
                        subjectName = subject.name,
                        goalStudyHours = subject.goalHours.toString(),
                        subjectCardColors = subject.colors.map { colors -> Color(colors) },
                        currentSubjectId = subject.subjectId
                    )
                }
            }
        }
    }

    private fun deleteSubject() {
        viewModelScope.launch {
            try {
                val currentSubjectId = state.value.currentSubjectId
                if (currentSubjectId != null) {
                    withContext(Dispatchers.IO) {
                        subjectRepository.deleteSubject(subjectId = currentSubjectId)
                    }
                    _snackbarEventFlow.emit(
                        SnackBarEvent.ShowSnackBar(message = "Subject deleted successfully")
                    )
                    _snackbarEventFlow.emit(SnackBarEvent.NavigateUp)
                } else {
                    _snackbarEventFlow.emit(
                        SnackBarEvent.ShowSnackBar(message = "No Subject to delete")
                    )
                }
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Couldn't delete subject. ${e.message}",
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