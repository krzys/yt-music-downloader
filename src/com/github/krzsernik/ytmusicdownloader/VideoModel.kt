package com.github.krzsernik.ytmusicdownloader

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class VideoModel : ViewModel() {
    val id = bind { SimpleStringProperty() }
}