package com.example.playground.testing.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.example.playground.testing.data.Result.Error
import com.example.playground.testing.data.Result.Success
import com.example.playground.testing.data.source.TaskDataSource
import com.example.playground.testing.data.source.local.TasksLocalDataSource
import com.example.playground.testing.data.source.local.TodoDatabase
import com.example.playground.testing.data.source.remote.TaskRemoteDataSource
import kotlinx.coroutines.*

/**
 * Concrete implementation to load tasks from the data source into a cache
 */
class DefaultTaskRepository private constructor(app: Application) {
    private val local: TaskDataSource by lazy {
        TasksLocalDataSource(
            Room.databaseBuilder(
                app.applicationContext,
                TodoDatabase::class.java,
                "Tasks.db"
            ).build().taskDao()
        )
    }
    private val remote: TaskDataSource by lazy { TaskRemoteDataSource }
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    companion object {
        @Volatile
        private var INSTANCE: DefaultTaskRepository? = null
        fun repo(app: Application): DefaultTaskRepository {
            return INSTANCE ?: synchronized(this) {
                DefaultTaskRepository(app).also {
                    INSTANCE = it
                }
            }
        }
    }

    suspend fun getTasks(forceUpdate: Boolean = false): Result<List<Task>> {
        if (forceUpdate) {
            try {
                updateTasksFromRemoteDataSource()
            } catch (ex: Exception) {
                return Error(ex)
            }
        }
        return local.getTasks()
    }

    suspend fun refreshTasks() {
        updateTasksFromRemoteDataSource()
    }

    fun observeTasks(): LiveData<Result<List<Task>>> {
        return local.observeTasks()
    }

    suspend fun refreshTask(taskId: String) {
        updateTaskFromRemoteDataSource(taskId)
    }

    private suspend fun updateTasksFromRemoteDataSource() {
        val remoteTasks = remote.getTasks()

        if (remoteTasks is Success) {
            // Real apps might want to do a proper sync.
            local.deleteAllTasks()
            remoteTasks.data.forEach { task ->
                local.saveTask(task)
            }
        } else if (remoteTasks is Error) {
            throw remoteTasks.exception
        }
    }

    fun observeTask(taskId: String): LiveData<Result<Task>> {
        return local.observeTask(taskId)
    }

    private suspend fun updateTaskFromRemoteDataSource(taskId: String) {
        val remoteTask = remote.getTask(taskId)

        if (remoteTask is Success) {
            local.saveTask(remoteTask.data)
        }
    }

    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID.
     */
    suspend fun getTask(taskId: String,  forceUpdate: Boolean = false): Result<Task> {
        if (forceUpdate) {
            updateTaskFromRemoteDataSource(taskId)
        }
        return local.getTask(taskId)
    }

    suspend fun saveTask(task: Task) {
        coroutineScope {
            launch { remote.saveTask(task) }
            launch { local.saveTask(task) }
        }
    }

    suspend fun completeTask(task: Task) {
        coroutineScope {
            launch { remote.completeTask(task) }
            launch { local.completeTask(task) }
        }
    }

    suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                completeTask(it.data)
            }
        }
    }

    suspend fun activateTask(task: Task) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { remote.activateTask(task) }
            launch { local.activateTask(task) }
        }
    }

    suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                activateTask(it.data)
            }
        }
    }

    suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { remote.clearCompletedTasks() }
            launch { local.clearCompletedTasks() }
        }
    }

    suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { remote.deleteAllTasks() }
                launch { local.deleteAllTasks() }
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { remote.deleteTask(taskId) }
            launch { local.deleteTask(taskId) }
        }
    }

    private suspend fun getTaskWithId(id: String): Result<Task> {
        return local.getTask(id)
    }
}