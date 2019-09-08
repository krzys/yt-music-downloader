package com.github.krzsernik.ytmusicdownloader

import tornadofx.*

class Application : App(MainWindow::class, Styles::class) {
    override fun stop() {
        try {
            super.stop()
        } catch (ex: Exception) {
        }
    }
}