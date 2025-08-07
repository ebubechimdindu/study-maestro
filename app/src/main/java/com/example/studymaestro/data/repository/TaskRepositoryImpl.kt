package com.example.studymaestro.data.repository

import com.example.studymaestro.data.local.SubjectDao
import com.example.studymaestro.data.local.TaskDao
import com.example.studymaestro.domain.model.Task

import com.example.studymaestro.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.tanh

class TaskRepositoryImpl @Inject constructor(private val taskDao: TaskDao) : TaskRepository {
    override suspend fun upsertTask(task: Task) {
        taskDao.upsertTask(task)
    }

    override suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTask(taskId)
    }

    override suspend fun getTaskById(taskId: Int): Task? {
       return taskDao.getTaskById(taskId)
    }

    override fun getUpcomingTasksForSubject(subjectId: Int): Flow<List<Task>> {
        return taskDao.getTasksForSubject(subjectId)
            .map { tasks -> tasks.filter { it.isComplete.not() } }
            .map { sortTasks(it) }
    }

    override fun getCompletedTasksForSubject(subjectId: Int): Flow<List<Task>> {
        return taskDao.getTasksForSubject(subjectId)
            .map { tasks -> tasks.filter { it.isComplete } }
            .map { sortTasks(it) }
    }

    override fun getAllUpcomingTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
            .map { tasks -> tasks.filter { it.isComplete.not() } }
            .map { sortTasks(it) }

    }

    private fun sortTasks(tasks: List<Task>): List<Task> {
        return tasks.sortedWith(compareBy<Task> { it.dueDate }.thenByDescending { it.priority })
    }
}