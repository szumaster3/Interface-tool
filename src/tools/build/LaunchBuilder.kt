package tools.build

import com.rs.cache.Cache
import properties.PropertyValues.loadValues
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
