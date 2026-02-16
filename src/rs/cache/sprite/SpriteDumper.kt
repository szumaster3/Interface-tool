package rs.cache.sprite

import properties.PropertyValues
import java.io.File
import javax.imageio.ImageIO

object SpriteDumper {

    fun dump(archiveId: Int) {
        val directory = File(PropertyValues.dump_path)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val spra = SpriteLoader.getArchive(archiveId) ?: return

        for (frame in 0 until spra.size()) {
            val file = File(PropertyValues.dump_path, "${archiveId}_$frame.png")
            val image = spra.getSprite(frame)
            try {
                ImageIO.write(image, "png", file)
            } catch (e: Exception) {
                println(
                    "Could not dump sprite $archiveId error -> ${e.message}",
                )
            }
        }
    }
}
