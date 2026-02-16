package util

import fs.com.alex.Cache
import properties.PropertyValues
import properties.PropertyValues.loadValues
import util.GenericPrinter.printInterface

object Printer {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        loadValues()
        Cache.init(PropertyValues.cache_path)
        printInterface(834)
    }
}