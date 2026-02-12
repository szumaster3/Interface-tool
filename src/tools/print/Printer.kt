package tools.print

import properties.PropertyValues.loadValues
import rs2.cache.Cache
import tools.print.GenericPrinter.printInterface

object Printer {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        loadValues()
        Cache.init()

        printInterface(834)
    }
}
