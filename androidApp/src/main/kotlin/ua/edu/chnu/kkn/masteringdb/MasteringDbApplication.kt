package ua.edu.chnu.kkn.masteringdb

import android.app.Application
import org.koin.android.ext.koin.androidContext
import ua.edu.chnu.kkn.masteringdb.di.initKoin

class MasteringDbApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MasteringDbApplication)
        }
    }
}
