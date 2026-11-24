package ua.edu.chnu.kkn.masteringdb.ui.tasks

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ua.edu.chnu.kkn.masteringdb.data.model.TaskFilter

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelTest {

    private val store = FakeStore()
    private val taskRepository = FakeTaskRepository(store)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(): TasksViewModel {
        val viewModel = TasksViewModel(taskRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun addTaskThroughEditor() = runTest {
        val viewModel = createViewModel()

        viewModel.onAddClick()
        viewModel.onEditorTitleChange("  Buy milk ")
        viewModel.onEditorSave()

        val state = viewModel.uiState.value
        assertNull(state.editor)
        assertEquals(listOf("Buy milk"), state.tasks.map { it.title })
        assertEquals(1, state.activeCount)
    }

    @Test
    fun filterShowsOnlyMatchingTasks() = runTest {
        val viewModel = createViewModel()
        val id = taskRepository.addTask("Done one", null)
        taskRepository.addTask("Active one", null)
        taskRepository.setDone(id, true)

        viewModel.onFilterChange(TaskFilter.DONE)

        assertEquals(listOf("Done one"), viewModel.uiState.value.tasks.map { it.title })
    }

}
