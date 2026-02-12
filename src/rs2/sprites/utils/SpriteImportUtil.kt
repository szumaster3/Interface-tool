package rs2.sprites.utils

import rs2.sprites.SpriteArchive
import java.io.File
import javax.imageio.ImageIO

object SpriteImportUtil {
    fun importFromPngs(files: List<File>): SpriteArchive {
        require(files.isNotEmpty()) { "No PNG files selected." }

        val images =
            files.map {
                ImageIO.read(it) ?: error("Failed to read PNG: ${it.name}")
            }

        val width = images.first().width
        val height = images.first().height

        images.forEach {
            require(it.width == width && it.height == height) {
                "All PNG files must have the same dimensions."
            }
        }

        val archive = SpriteArchive(width, height, images.size)
        images.forEachIndexed { index, image ->
            archive.setFrame(index, image)
        }

        println("[SpriteImport] Imported ${images.size} frame(s) (${width}x$height)")
        return archive
    }
}
