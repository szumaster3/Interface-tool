package rs2.editor.properties

import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PropertyValues {
    var cache_path: String? = "data/cache/"
    private var dump_path: String = "dump/"
    private var sprite_path: String? = null
    private var input: InputStream? = null

    private fun getJarDir(): File {
        val jarPath = PropertyValues::class.java
            .protectionDomain
            .codeSource
            .location
            .toURI()

        val file = File(jarPath)
        return if (file.isFile) file.parentFile else file
    }

    @JvmStatic
    fun loadValues() {
        val prop = Properties()
        val propFileName = "config.properties"

        val jarDir = getJarDir()
        val propFile = File(jarDir, propFileName)

        try {
            if (propFile.exists()) {
                input = FileInputStream(propFile)
                prop.load(input)
                println("Loaded config from ${propFile.absolutePath}")
            } else {
                println("Config not found. Creating at ${propFile.absolutePath}")

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
            println("Exception while loading config: $e")
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

    @JvmStatic
    fun backupCache() {
        val cacheDir = cache_path?.let { File(it) } ?: run {
            javax.swing.JOptionPane.showMessageDialog(null, "Cache path is invalid: $cache_path", "Error", javax.swing.JOptionPane.ERROR_MESSAGE)
            return
        }

        if (!cacheDir.exists() || !cacheDir.isDirectory) {
            javax.swing.JOptionPane.showMessageDialog(null, "Cache path does not exist or is not a directory: $cache_path", "Error", javax.swing.JOptionPane.ERROR_MESSAGE)
            return
        }

        try {
            val backupFile = File(dump_path, "cache_backup_${System.currentTimeMillis()}.zip")
            backupFile.parentFile?.mkdirs()

            ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zipOut ->
                cacheDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        val entryName = file.relativeTo(cacheDir).path.replace("\\", "/")
                        zipOut.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { input -> input.copyTo(zipOut) }
                        zipOut.closeEntry()
                    }
                }
            }

            javax.swing.JOptionPane.showMessageDialog(null, "Cache backup created at:\n${backupFile.absolutePath}", "Backup completed", javax.swing.JOptionPane.INFORMATION_MESSAGE)

        } catch (ex: Exception) {
            ex.printStackTrace()
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to create cache backup:\n${ex.message}", "Error", javax.swing.JOptionPane.ERROR_MESSAGE)
        }
    }
}
