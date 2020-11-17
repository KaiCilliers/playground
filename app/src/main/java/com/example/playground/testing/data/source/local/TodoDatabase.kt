package com.example.playground.testing.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playground.testing.data.Task


/**
 * The Room Database that contains the Task table.
 *
 * Note that exportSchema should be true in production databases.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun taskDao(): TasksDao
}