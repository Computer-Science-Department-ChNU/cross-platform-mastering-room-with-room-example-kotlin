package ua.edu.chnu.kkn.masteringdb

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ua.edu.chnu.kkn.masteringdb.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MasteringDB",
        ) {
            App()
        }
    }
}