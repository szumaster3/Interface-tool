package ui.theme

import java.util.*
import javax.swing.UIManager

object UIKeys {
    fun printKeys() {
        val defaults = UIManager.getDefaults()
        val keysList = Collections.list(defaults.keys())
        for (key in keysList) {
            println(key)
        }
    }
}
