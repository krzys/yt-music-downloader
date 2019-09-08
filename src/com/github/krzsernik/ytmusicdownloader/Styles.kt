package com.github.krzsernik.ytmusicdownloader

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    init {
        form {
            backgroundColor += c("#24323d")
        }

        tableView {
            baseColor = c("#1e3742")
        }

        label {
            textFill = Color.WHITE
            fontFamily = "Lato"
            fontSize = 2.em
        }

        textField {
            insets(5, 10)
            textFill = Color.WHITE
            borderColor = multi(box(c("#a0b3b0")))
            backgroundColor = multi(c("white", 0.0))
            fontSize = 22.px

            and(focused) {
                borderColor = multi(box(c("#1ab188")))
            }
        }

        button {
            insets(15, 0)
            backgroundColor = multi(c("#1ab188"))
            fontSize = 2.em
            fontWeight = FontWeight.findByWeight(600)
            textFill = Color.WHITE
        }
    }
}