package ua.edu.chnu.kkn.masteringdb.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ua.edu.chnu.kkn.masteringdb.data.model.Task
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter
import ua.edu.chnu.kkn.masteringdb.data.repository.TaskRepository

class TasksViewModel(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    /** UI-only state that is not stored in the database. */
    private data class ScreenState(
        val filter: TaskFilter = TaskFilter.ALL,
        val editor: TaskEditorState? = null,
    )

    private val screen = MutableStateFlow(ScreenState())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasks = screen
        .map { it.filter }
        .distinctUntilChanged()
        .flatMapLatest(taskRepository::observeTasks)

    val uiState: StateFlow<TasksUiState> = combine(
        screen,
        tasks,
        taskRepository.observeActiveCount(),
    ) { screen, tasks, activeCount ->
        TasksUiState(
            tasks = tasks,
            filter = screen.filter,
            activeCount = activeCount,
            editor = screen.editor,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    // ---------- Filters ----------

    fun onFilterChange(filter: TaskFilter) {
        screen.update { it.copy(filter = filter) }
    }

    // ---------- Task editor ----------

    fun onAddClick() {
        screen.update { it.copy(editor = TaskEditorState()) }
    }

    fun onEditClick(task: Task) {
        val editor = TaskEditorState(
            taskId = task.id,
            title = task.title,
            description = task.description.orEmpty(),
        )
        screen.update { it.copy(editor = editor) }
    }

    fun onEditorTitleChange(title: String) = updateEditor { it.copy(title = title) }

    fun onEditorDescriptionChange(description: String) = updateEditor { it.copy(description = description) }

    fun onEditorDismiss() {
        screen.update { it.copy(editor = null) }
    }

    fun onEditorSave() {
        val editor = screen.value.editor ?: return
        if (!editor.canSave) return
        screen.update { it.copy(editor = null) }
        val title = editor.title.trim()
        val description = editor.description.trim().ifEmpty { null }
        viewModelScope.launch {
            if (editor.taskId == null) {
                taskRepository.addTask(title, description)
            } else {
                taskRepository.updateTask(editor.taskId, title, description)
            }
        }
    }

    private fun updateEditor(transform: (TaskEditorState) -> TaskEditorState) {
        screen.update { state -> state.copy(editor = state.editor?.let(transform)) }
    }

    // ---------- Task actions ----------

    fun onToggleDone(task: Task) {
        viewModelScope.launch { taskRepository.setDone(task.id, !task.isDone) }
    }

    fun onDelete(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task.id) }
    }

    fun onClearCompleted() {
        viewModelScope.launch { taskRepository.clearCompleted() }
    }
}
