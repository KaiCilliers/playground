package com.example.playground.testing.data.source.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.playground.testing.data.Result
import com.example.playground.testing.data.Result.Success
import com.example.playground.testing.data.Result.Error
import com.example.playground.testing.data.Task
import com.example.playground.testing.data.source.TaskDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of a data source as a db
 */
class TasksLocalDataSource internal constructor(
    private val dao: TasksDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskDataSource {
    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return dao.observeTasks().map { Success(it) }
    }

    override fun observeTask(id: String): LiveData<Result<Task>> {
        return dao.observeTaskById(id).map { Success(it) }
    }

    override suspend fun getTasks(): Result<List<Task>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(dao.tasks())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun refreshTasks() {}

    override suspend fun refreshTask(taskId: String) {}

    override suspend fun getTask(taskId: String): Result<Task> = withContext(ioDispatcher) {
        try {
            val task = dao.taskById(taskId)
            if (task != null) {
                return@withContext Success(task)
            } else {
                return@withContext Error(Exception("Task not found!"))
            }
        } catch (e: Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun saveTask(task: Task) = withContext(ioDispatcher) {
        dao.insert(task)
    }

    override suspend fun completeTask(task: Task) = withContext(ioDispatcher) {
        dao.updateCompleted(task.id, true)
    }

    override suspend fun completeTask(taskId: String) {
        dao.updateCompleted(taskId, true)
    }

    override suspend fun activateTask(task: Task) = withContext(ioDispatcher) {
        dao.updateCompleted(task.id, false)
    }

    override suspend fun activateTask(taskId: String) {
        dao.updateCompleted(taskId, false)
    }

    override suspend fun clearCompletedTasks() = withContext<Unit>(ioDispatcher) {
        dao.deleteCompleted()
    }

    override suspend fun deleteAllTasks() = withContext(ioDispatcher) {
        dao.clear()
    }

    override suspend fun deleteTask(taskId: String) = withContext<Unit>(ioDispatcher) {
        dao.deleteById(taskId)
    }
}