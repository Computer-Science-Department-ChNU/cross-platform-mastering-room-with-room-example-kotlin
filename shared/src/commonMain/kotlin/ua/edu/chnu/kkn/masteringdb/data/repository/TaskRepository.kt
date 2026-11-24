package ua.edu.chnu.kkn.masteringdb.data.repository

import kotlinx.coroutines.flow.Flow
import ua.edu.chnu.kkn.masteringdb.data.model.Task
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter

interface TaskRepository {
    fun observeTasks(filter: TaskFilter): Flow<List<Task>>
    fun observeActiveCount(): Flow<Int>

    suspend fun addTask(title: String, description: String?): Long
    suspend fun updateTask(id: Long, title: String, description: String?)
    suspend fun setDone(id: Long, done: Boolean)
    suspend fun deleteTask(id: Long)
    suspend fun clearCompleted()
}
