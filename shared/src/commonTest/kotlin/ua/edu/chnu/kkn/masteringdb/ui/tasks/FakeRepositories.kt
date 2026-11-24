package ua.edu.chnu.kkn.masteringdb.ui.tasks

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ua.edu.chnu.kkn.masteringdb.data.model.Task
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter
import ua.edu.chnu.kkn.masteringdb.data.repository.TaskRepository

/** In-memory data shared by the fake repositories, mimicking the database. */
class FakeStore {
    val tasks = MutableStateFlow(emptyList<Task>())
    var nextId = 1L
}

class FakeTaskRepository(private val store: FakeStore = FakeStore()) : TaskRepository {

    override fun observeTasks(filter: TaskFilter): Flow<List<Task>> = store.tasks.map { list ->
        when (filter) {
            TaskFilter.ALL -> list
            TaskFilter.ACTIVE -> list.filter { !it.isDone }
            TaskFilter.DONE -> list.filter { it.isDone }
        }
    }

    override fun observeActiveCount(): Flow<Int> = store.tasks.map { list -> list.count { !it.isDone } }

    override suspend fun addTask(title: String, description: String?): Long {
        val id = store.nextId++
        store.tasks.update { it + Task(id, title, description, isDone = false, createdAt = id) }
        return id
    }

    override suspend fun updateTask(id: Long, title: String, description: String?) =
        updateTask(id) { it.copy(title = title, description = description) }

    override suspend fun setDone(id: Long, done: Boolean) = updateTask(id) { it.copy(isDone = done) }

    override suspend fun deleteTask(id: Long) = store.tasks.update { list -> list.filterNot { it.id == id } }

    override suspend fun clearCompleted() = store.tasks.update { list -> list.filterNot { it.isDone } }

    private fun updateTask(id: Long, transform: (Task) -> Task) =
        store.tasks.update { list -> list.map { if (it.id == id) transform(it) else it } }
}
