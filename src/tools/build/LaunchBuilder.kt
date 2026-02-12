package tools.build

import properties.PropertyValues.loadValues
import rs2.cache.Cache
import tools.build.InterfaceBuilder.build
import java.io.IOException

object LaunchBuilder {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        loadValues()
        Cache.init()
        build()
    }
}
