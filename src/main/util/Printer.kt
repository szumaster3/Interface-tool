package util

import fs.com.alex.Cache
import properties.PropertyValues
import properties.PropertyValues.loadValues
import util.GenericPrinter.printInterface
import java.util.*
import javax.swing.UIManager

object Printer {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        printInterface(834)
    }

    fun printKeys() {
        val defaults = UIManager.getDefaults()
        val keysList = Collections.list(defaults.keys())
        for (key in keysList) {
            println(key)
        }
    }
}