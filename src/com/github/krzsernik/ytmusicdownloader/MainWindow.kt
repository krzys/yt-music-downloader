package com.github.krzsernik.ytmusicdownloader

import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import tornadofx.*

val youtubeLinkRegex = "https://music\\.youtube\\.com/watch\\?v=([a-zA-Z\\-_0-9]+).*".toRegex(RegexOption.IGNORE_CASE)

class MainWindow : Fragment() {
    val video = VideoModel()
    val hasPlaylist = SimpleBooleanProperty(false)
    val videosList = FXCollections.observableArrayList<Video>()

    override val root = borderpane {
        top = form {
            fieldset("", labelPosition = Orientation.VERTICAL) {
                gridpane {
                    row {
                        label("YouTube Music link")
                    }
                    row {
                        textfield(video.id).validator {
                            if (it.isNullOrBlank() || !it.matches(youtubeLinkRegex)) {
                                error("Your link doesn't match YouTube Music link structure")
                            } else {
                                null
                            }
                        }
                    }
                    row {
                        button("Get playlist and download") {
                            enableWhen(video.valid)
                            gridpaneConstraints {
                                marginTop = 10.0
                            }
                            action {
                                setVideo(video.id.value)
                            }
                        }
                    }
                }
            }
        }

        center = tableview<Video>(videosList) {
            enableWhen(hasPlaylist)

            column("Author", Video::author)
            column("Title", Video::title)
        }
    }

    init {

    }

    fun setVideo(link: String) {
        val home = HomeProcessing(link)
        videosList.setAll(home.videosList)
        hasPlaylist.set(true)
    }
}
