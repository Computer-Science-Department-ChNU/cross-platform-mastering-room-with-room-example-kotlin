package ua.edu.chnu.kkn.masteringdb

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ua.edu.chnu.kkn.masteringdb.ui.tasks.TasksScreen

/** Root composable. Koin must be started (see [ua.edu.chnu.kkn.masteringdb.di.initKoin]) before it is shown. */
@Composable
fun App() {
    MaterialTheme {
        TasksScreen()
    }
}
