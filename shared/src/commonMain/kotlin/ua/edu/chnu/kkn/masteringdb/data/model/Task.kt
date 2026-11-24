package ua.edu.chnu.kkn.masteringdb.data.model

/** Model exposed by the data layer; the UI never sees Room entities. */
data class Task(
    val id: Long,
    val title: String,
    val description: String?,
    val isDone: Boolean,
    val createdAt: Long,
)

enum class TaskFilter { ALL, ACTIVE, DONE }
