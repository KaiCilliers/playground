package com.example.playground.testing.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @ColumnInfo(name = "entry_id")
    var id: String = "${UUID.randomUUID()}",
    @ColumnInfo(name = "title")
    var title: String = "",
    @ColumnInfo(name = "description")
    var description: String = "",
    @ColumnInfo(name = "completed")
    var isComplete: Boolean = false
) {
    val titleForList: String
        get() = if (title.isNotEmpty()) title else description

    val isActive
        get() = !isComplete

    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}