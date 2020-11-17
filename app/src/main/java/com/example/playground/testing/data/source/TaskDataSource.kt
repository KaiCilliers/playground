package com.example.playground.testing.data.source

import androidx.lifecycle.LiveData
import com.example.playground.testing.data.Task
import com.example.playground.testing.data.Result

/**
 * Main entry point for accessing tasks data
 */
interface TaskDataSource {
    fun observeTasks(): LiveData<Result<List<Task>>>
    fun observeTask(id: String) : LiveData<Result<Task>>
    suspend fun getTasks(): Result<List<Task>>
    suspend fun refreshTasks()
    suspend fun getTask(taskId: String): Result<Task>
    suspend fun refreshTask(taskId: String)
    suspend fun saveTask(task: Task)
    suspend fun completeTask(task: Task)
    suspend fun completeTask(taskId: String)
    suspend fun activateTask(task: Task)
    suspend fun activateTask(taskId: String)
    suspend fun clearCompletedTasks()
    suspend fun deleteAllTasks()
    suspend fun deleteTask(taskId: String)
}