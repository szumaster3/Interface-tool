package tools.build

import java.io.IOException
import properties.PropertyValues.loadValues
import rs2.cache.Cache
import tools.build.InterfaceBuilder.build

object LaunchBuilder {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        loadValues()
        Cache.init()
        build()
    }
}
