package ua.edu.chnu.kkn.masteringdb.di

import androidx.room3.Room
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import ua.edu.chnu.kkn.masteringdb.data.local.AppDatabase
import ua.edu.chnu.kkn.masteringdb.ui.tasks.TasksViewModel

/** Fails when a dependency is missing from the Koin modules (e.g. a new DAO or repository). */
@OptIn(ExperimentalCoroutinesApi::class)
class KoinModulesTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun viewModelGraphResolves() {
        val testPlatformModule = module { single { Room.inMemoryDatabaseBuilder<AppDatabase>() } }
        val app = koinApplication { modules(testPlatformModule, dataModule, uiModule) }
        try {
            app.koin.get<TasksViewModel>()
        } finally {
            app.koin.get<AppDatabase>().close()
            app.close()
        }
    }
}
