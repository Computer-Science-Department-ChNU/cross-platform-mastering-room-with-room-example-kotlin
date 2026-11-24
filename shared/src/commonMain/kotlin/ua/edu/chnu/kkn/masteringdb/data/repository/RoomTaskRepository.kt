package ua.edu.chnu.kkn.masteringdb.data.repository

import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ua.edu.chnu.kkn.masteringdb.data.local.dao.TaskDao
import ua.edu.chnu.kkn.masteringdb.data.local.entity.TaskEntity
import ua.edu.chnu.kkn.masteringdb.data.local.mapper.toModel
import ua.edu.chnu.kkn.masteringdb.data.model.Task
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter

class RoomTaskRepository(
    private val taskDao: TaskDao,
    private val clock: Clock = Clock.System,
) : TaskRepository {

    override fun observeTasks(filter: TaskFilter): Flow<List<Task>> {
        val entities = when (filter) {
            TaskFilter.ALL -> taskDao.observeAll()
            TaskFilter.ACTIVE -> taskDao.observeByStatus(done = false)
            TaskFilter.DONE -> taskDao.observeByStatus(done = true)
        }
        return entities.map { list -> list.map(TaskEntity::toModel) }
    }

    override fun observeActiveCount(): Flow<Int> = taskDao.observeActiveCount()

    override suspend fun addTask(title: String, description: String?): Long =
        taskDao.insert(
            TaskEntity(
                title = title,
                description = description,
                createdAt = clock.now().toEpochMilliseconds(),
            )
        )

    override suspend fun updateTask(id: Long, title: String, description: String?) {
        val task = taskDao.getById(id) ?: return
        taskDao.update(task.copy(title = title, description = description))
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        taskDao.setDone(id, done)
    }

    override suspend fun deleteTask(id: Long) {
        taskDao.deleteById(id)
    }

    override suspend fun clearCompleted() {
        taskDao.deleteCompleted()
    }
}
