package ua.edu.chnu.kkn.masteringdb.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ua.edu.chnu.kkn.masteringdb.data.local.entity.TaskEntity

class TaskDaoTest {
    private lateinit var db: AppDatabase

    @BeforeTest
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @AfterTest
    fun tearDown() = db.close()

    @Test
    fun insertAndRead() = runTest {
        val dao = db.taskDao()
        val id = dao.insert(TaskEntity(title = "Test", createdAt = 0))

        assertEquals("Test", dao.getById(id)?.title)
        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun setDoneAndDeleteCompleted() = runTest {
        val dao = db.taskDao()
        val first = dao.insert(TaskEntity(title = "First", createdAt = 1))
        dao.insert(TaskEntity(title = "Second", createdAt = 2))

        dao.setDone(first, true)
        assertEquals(1, dao.observeActiveCount().first())
        assertEquals(listOf("First"), dao.observeByStatus(done = true).first().map { it.title })

        dao.deleteCompleted()
        assertNull(dao.getById(first))
        assertEquals(listOf("Second"), dao.observeAll().first().map { it.title })
    }
}
