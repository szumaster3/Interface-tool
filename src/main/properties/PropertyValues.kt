package properties

import java.io.*
import java.util.*

object PropertyValues {
    var cache_path: String? = "data/cache/"
    var dump_path: String = "dump/"
    var sprite_path: String? = null
    var input: InputStream? = null

    @JvmStatic
    fun loadValues() {
        val prop = Properties()
        val propFileName = "config.properties"
        val propFile = File(propFileName)

        try {
            if (propFile.exists()) {
                input = FileInputStream(propFile)
                prop.load(input)
                println("Loaded config from $propFileName")
            } else {
                println("Config file not found. Creating default config at $propFileName")

                prop.setProperty("cache_path", cache_path ?: "data/cache/")
                prop.setProperty("dump_path", dump_path)
                prop.setProperty("sprite_path", sprite_path ?: "")

                propFile.parentFile?.mkdirs()
                FileOutputStream(propFile).use { out ->
                    prop.store(out, "Default configuration")
                }
            }

            cache_path = prop.getProperty("cache_path") ?: cache_path
            dump_path = prop.getProperty("dump_path") ?: dump_path
            sprite_path = prop.getProperty("sprite_path") ?: sprite_path

        } catch (e: Exception) {
            println("Exception while loading or creating config: $e")
        } finally {
            input?.close()
        }
    }

    @JvmStatic
    fun setCachePath(path: String) {
        var fixedPath = path
        if (!fixedPath.endsWith(File.separator)) fixedPath += File.separator

        cache_path = fixedPath
        dump_path = cache_path + "IFToolDumps" + File.separator
        sprite_path = cache_path
    }
}
