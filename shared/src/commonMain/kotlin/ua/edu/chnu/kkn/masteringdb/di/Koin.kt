package ua.edu.chnu.kkn.masteringdb.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ua.edu.chnu.kkn.masteringdb.data.local.AppDatabase
import ua.edu.chnu.kkn.masteringdb.data.local.getRoomDatabase
import ua.edu.chnu.kkn.masteringdb.data.repository.RoomTaskRepository
import ua.edu.chnu.kkn.masteringdb.data.repository.TaskRepository
import ua.edu.chnu.kkn.masteringdb.ui.tasks.TasksViewModel

/** Provides `RoomDatabase.Builder<AppDatabase>` — the only platform-specific dependency. */
expect fun platformModule(): Module

val dataModule = module {
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().taskDao() }
    single<TaskRepository> { RoomTaskRepository(get()) }
}

val uiModule = module {
    viewModelOf(::TasksViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(platformModule(), dataModule, uiModule)
    }
}
