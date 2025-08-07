package com.example.studymaestro.presentation.session

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studymaestro.domain.model.Session
import com.example.studymaestro.domain.repository.SessionRepository
import com.example.studymaestro.domain.repository.SubjectRepository
import com.example.studymaestro.util.SnackBarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SessionState())
    val state = combine(
        _state,
        subjectRepository.getAllSubjects(),
        sessionRepository.getAllSessions()
    ) { state, subjects, sessions ->
        state.copy(
            subjects = subjects,
            sessions = sessions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SessionState()
    )


    private val _snackbarEventFlow = MutableSharedFlow<SnackBarEvent>()
    val snackbarEventFlow = _snackbarEventFlow.asSharedFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun onEvent(event: SessionEvent) {
        when (event) {
            SessionEvent.NotifyToUpdateSubject -> notifyToUpdateSubject()
            SessionEvent.DeleteSession -> deleteSession()
            is SessionEvent.OnDeleteSessionButtonClick -> {
                _state.update {
                    it.copy(session = event.session)
                }
            }
            is SessionEvent.OnRelatedSubjectChange -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.subject.name,
                        subjectId = event.subject.subjectId
                    )
                }
            }

            is SessionEvent.SaveSession -> insertSession(event.duration)
            is SessionEvent.UpdateSubjectIdAndRelatedSubject -> {
                _state.update {
                    it.copy(
                        relatedToSubject = event.relatedToSubject,
                        subjectId = event.subjectId
                    )
                }
            }
        }
    }

    private fun notifyToUpdateSubject() {
        viewModelScope.launch {
            if (state.value.subjectId == null || state.value.relatedToSubject == null) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Please select subject related to the session."
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertSession(duration: Long) {
        viewModelScope.launch {
            if (duration < 36) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Single session can not be less than 36 seconds"
                    )
                )
                return@launch
            }
            try {
                sessionRepository.insertSession(
                    session = Session(
                        sessionSubjectId = state.value.subjectId ?: -1,
                        relatedToSubject = state.value.relatedToSubject ?: "",
                        date = Instant.now().toEpochMilli(),
                        duration = duration
                    )
                )
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(message = "Session saved successfully")
                )
            } catch (e: Exception) {
                _snackbarEventFlow.emit(
                    SnackBarEvent.ShowSnackBar(
                        message = "Couldn't save session. ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                )
            }
        }
    }

}