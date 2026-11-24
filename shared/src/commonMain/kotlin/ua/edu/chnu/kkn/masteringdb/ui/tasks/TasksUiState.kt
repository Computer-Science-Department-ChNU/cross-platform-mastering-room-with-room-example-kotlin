package ua.edu.chnu.kkn.masteringdb.ui.tasks

import ua.edu.chnu.kkn.masteringdb.data.model.Task
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val activeCount: Int = 0,
    val editor: TaskEditorState? = null,
)

/** State of the add/edit dialog; [taskId] is null when creating a new task. */
data class TaskEditorState(
    val taskId: Long? = null,
    val title: String = "",
    val description: String = "",
) {
    val canSave: Boolean get() = title.isNotBlank()
}
