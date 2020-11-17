package com.example.playground.testing.data.source.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.playground.testing.data.Task

/**
 * Data Access Object for the tasks table.
 */
@Dao
interface TasksDao {
    /**
     * Observes a list of tasks
     *
     * @return all tasks
     */
    @Query("SELECT * FROM tasks")
    fun observeTasks(): LiveData<List<Task>>
    /**
     * Observes a single task
     *
     * @param id the task id
     * @return the task with id
     */
    @Query("SELECT * FROM tasks WHERE :id = entry_id")
    fun observeTaskById(id: String): LiveData<Task>
    /**
     * Select all tasks from the tasks table
     *
     * @retun all tasks
     */
    @Query("SELECT * FROM tasks")
    suspend fun tasks(): List<Task>
    /**
     * Select a task by id
     *
     * @param id the task id
     * @return the task with id
     */
    @Query("SELECT * FROM tasks WHERE :id = entry_id")
    suspend fun taskById(id: String): Task?
    /**
     * Insert a task in the database. If the task already exists, replace it.
     *
     * @param task the task to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)
    /**
     * Update a task
     *
     * @param task task to be uploaded
     * @return the number of tasks updated. This should be 1
     */
    @Update
    suspend fun update(task: Task): Int
    /**
     * Update the complete status of a task
     *
     * @param id is of the task
     * @param completed status to be updated
     */
    @Query("UPDATE tasks SET completed = :completed WHERE :id = entry_id")
    suspend fun updateCompleted(id: String, completed: Boolean)
    /**
     * Delete a task by id
     *
     * @return the number of tasks deleted. This should always be 1
     */
    @Query("DELETE FROM tasks WHERE :id = entry_id")
    suspend fun deleteById(id: String): Int
    /**
     * Delete all tasks
     */
    @Query("DELETE FROM tasks")
    suspend fun clear()
    /**
     * Delete all completed tasks from the table
     *
     * @return the number of tasks deleted
     */
    @Query("DELETE FROM tasks WHERE completed = 1")
    suspend fun deleteCompleted(): Int
}