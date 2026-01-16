package com.parkwoocheol.sample.kmpdatastore

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "KMP DataStore Sample") {
        App()
    }
}
