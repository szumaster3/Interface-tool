package properties

import java.io.*
import java.util.*

/**
 *
 * @author paolo
 */
object PropertyValues {
    /**
     * all the stored values
     */
    var cache_path: String? = null
    var dump_path: String = "dump/"
    var sprite_path: String? = null

    /*
	 * random
	 */
    var input: InputStream? = null


    @JvmStatic
    @Throws(IOException::class)
    fun loadValues() {
        try {
            val prop = Properties()
            val propFileName = "config.properties"

            input = FileInputStream("config.properties")

            if (input != null) {
                prop.load(input)
            } else {
                throw FileNotFoundException("property file '$propFileName' not found in the classpath")
            }


            // get the property value and tools.print it out
            println(prop.getProperty("cache_path"))
            cache_path = prop.getProperty("cache_path")
            dump_path = prop.getProperty("dump_path")
            sprite_path = prop.getProperty("sprite_path")
        } catch (e: Exception) {
            println("Exception: $e")
        } finally {
            input!!.close()
        }
    }

    @JvmStatic
    fun setCachePath(path: String) {
        var path = path
        if (!path.endsWith(File.separator)) path += File.separator

        cache_path = path
        dump_path = cache_path + "IFToolDumps" + File.separator
        sprite_path = cache_path
    }
}
