package tools.print

import com.rs.cache.Cache
import properties.PropertyValues.loadValues
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
