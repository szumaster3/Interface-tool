package util

import rs.cache.ComponentDefinition
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object ComponentSprites {
    val scrollBar: BufferedImage by lazy {
        ImageIO.read(this::class.java.getResource("/sprites/scrollbar.jpg"))
    }
    val button: BufferedImage by lazy {
        ImageIO.read(this::class.java.getResource("/sprites/button.png"))
    }

    @JvmStatic
    fun getSpriteForComponent(comp: ComponentDefinition): BufferedImage? {
        return when {
            ContainerHelper.isScrollBar(comp) -> scrollBar
            ContainerHelper.isButton(comp) -> button
            else -> null
        }
    }
}