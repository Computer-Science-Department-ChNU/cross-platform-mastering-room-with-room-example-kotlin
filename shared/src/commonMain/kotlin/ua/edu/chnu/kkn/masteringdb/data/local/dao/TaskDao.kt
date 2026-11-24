package ua.edu.chnu.kkn.masteringdb.data.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import ua.edu.chnu.kkn.masteringdb.data.local.entity.TaskEntity

@Dao
interface TaskDao {

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_done = :done ORDER BY created_at DESC")
    fun observeByStatus(done: Boolean): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks WHERE is_done = 0")
    fun observeActiveCount(): Flow<Int>

    @Update
    suspend fun update(task: TaskEntity): Int

    @Query("UPDATE tasks SET is_done = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Delete
    suspend fun delete(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE is_done = 1")
    suspend fun deleteCompleted(): Int
}
