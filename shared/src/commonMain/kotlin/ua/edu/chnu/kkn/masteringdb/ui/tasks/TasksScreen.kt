package ua.edu.chnu.kkn.masteringdb.ui.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ua.edu.chnu.kkn.masteringdb.data.model.Task
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter

@Composable
fun TasksScreen(viewModel: TasksViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TasksContent(
        state = state,
        onFilterChange = viewModel::onFilterChange,
        onAddClick = viewModel::onAddClick,
        onEditClick = viewModel::onEditClick,
        onToggleDone = viewModel::onToggleDone,
        onDelete = viewModel::onDelete,
        onClearCompleted = viewModel::onClearCompleted,
    )
    state.editor?.let { editor ->
        TaskEditorDialog(
            state = editor,
            onTitleChange = viewModel::onEditorTitleChange,
            onDescriptionChange = viewModel::onEditorDescriptionChange,
            onSave = viewModel::onEditorSave,
            onDismiss = viewModel::onEditorDismiss,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksContent(
    state: TasksUiState,
    onFilterChange: (TaskFilter) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Task) -> Unit,
    onToggleDone: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onClearCompleted: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Tasks") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) { Text("+", style = MaterialTheme.typography.headlineSmall) }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ChipRow {
                TaskFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${state.activeCount} active",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearCompleted) { Text("Clear completed") }
            }
            if (state.tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks yet", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.tasks, key = Task::id) { task ->
                        TaskItem(
                            task = task,
                            onToggleDone = { onToggleDone(task) },
                            onEdit = { onEditClick(task) },
                            onDelete = { onDelete(task) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggleDone() })
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                )
                task.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
            }
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

private val TaskFilter.label: String
    get() = when (this) {
        TaskFilter.ALL -> "All"
        TaskFilter.ACTIVE -> "Active"
        TaskFilter.DONE -> "Done"
    }

@Preview
@Composable
private fun TasksContentPreview() {
    MaterialTheme {
        TasksContent(
            state = TasksUiState(
                tasks = listOf(
                    Task(1, "Buy milk", "2 litres", isDone = false, createdAt = 0),
                    Task(2, "Read the Room lecture", null, isDone = true, createdAt = 0),
                ),
                activeCount = 1,
            ),
            onFilterChange = {},
            onAddClick = {},
            onEditClick = {},
            onToggleDone = {},
            onDelete = {},
            onClearCompleted = {},
        )
    }
}
