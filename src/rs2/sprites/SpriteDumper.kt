package rs2.sprites

import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import properties.PropertyValues
import rs2.logging.LogFactory

object SpriteDumper {
    private val logger: Logger = LogFactory.createLogger(SpriteDumper::class.java.name)

    fun dump() {
        val directory = File(PropertyValues.dump_path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val size = SpriteLoader.numSprites
        for (i in 0 until size) {
            val spra = SpriteLoader.getArchive(i) ?: continue

            for (frame in 0 until spra.size()) {
                val file = File(PropertyValues.dump_path, i.toString() + "_" + frame + ".png")
                val image = spra.getSprite(frame)
                try {
                    ImageIO.write(image, "png", file)
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "Could not dump sprite " + i + " error ->" + e.message)
                    continue
                }
            }
        }
    }
}
