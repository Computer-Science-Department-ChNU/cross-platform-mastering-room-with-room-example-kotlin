package ua.edu.chnu.kkn.masteringdb.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ua.edu.chnu.kkn.masteringdb.data.local.getDatabaseBuilder

actual fun platformModule(): Module = module {
    single { getDatabaseBuilder() }
}

/** Swift-friendly entry point (Swift does not see Kotlin default arguments). */
fun initKoinIos() = initKoin()
