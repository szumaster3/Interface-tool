package rs2.sprites.utils

import rs2.sprites.SpriteArchive
import java.io.File
import javax.imageio.ImageIO

object SpriteExportUtil {
    fun exportSprite(
        archive: SpriteArchive,
        spriteId: Int,
        directory: File,
    ) {
        directory.mkdirs()

        for (i in 0 until archive.size()) {
            val img = archive.getSprite(i) ?: continue
            val file = File(directory, "sprite_${spriteId}_frame_$i.png")
            ImageIO.write(img, "png", file)
        }

        println("[SpriteExport] Exported sprite $spriteId to ${directory.absolutePath}")
    }
}
