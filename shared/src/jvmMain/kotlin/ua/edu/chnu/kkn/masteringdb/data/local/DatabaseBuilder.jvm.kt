package ua.edu.chnu.kkn.masteringdb.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(appDataDir(), DATABASE_FILE_NAME)
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}

// A persistent, per-user location (java.io.tmpdir may be cleared on reboot)
private fun appDataDir(): File {
    val os = System.getProperty("os.name").lowercase()
    val home = System.getProperty("user.home")
    val dir = when {
        "win" in os -> File(System.getenv("APPDATA") ?: home, "MasteringDB")
        "mac" in os -> File(home, "Library/Application Support/MasteringDB")
        else -> File(home, ".local/share/MasteringDB")
    }
    return dir.apply { mkdirs() }
}
