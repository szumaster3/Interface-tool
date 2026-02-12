package rs2.sprites

import properties.PropertyValues
import rs2.logging.LogFactory
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO

object SpriteDumper {
    private val logger: Logger = LogFactory.createLogger(SpriteDumper::class.java.name)

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
                logger.log(
                    Level.SEVERE,
                    "Could not dump sprite $archiveId error -> ${e.message}",
                )
            }
        }
    }
}
