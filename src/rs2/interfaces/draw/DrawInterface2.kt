package rs2.interfaces.draw

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.logging.Logger
import javax.imageio.ImageIO
import kotlin.math.roundToInt
import properties.Constants
import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.ComponentConstants
import rs2.interfaces.ContainerHelper
import rs2.interfaces.InterfaceUtils
import rs2.logging.LogFactory
import rs2.sprites.ImageUtils
import rs2.sprites.SpriteLoader

object DrawInterface2 {
    private val logger: Logger = LogFactory.createLogger(DrawInterface2::class.java.name)
    private var result: BufferedImage? = null

    fun getOrderedComps(interfaceId: Int): ArrayList<ComponentDefinition>? {
        val comps = ArrayList<ComponentDefinition>()

        val containers = ComponentDefinition.getInterfaceContainers(interfaceId)
        val allComps = ComponentDefinition.getInterface(interfaceId, false) ?: return null

        if (containers != null) {
            for (comp in containers) {
                if (!comps.contains(comp)) {
                    comps.add(comp)
                }
                val children = ComponentDefinition.getChildsByParent(interfaceId, comp.componentHash)
                comps.addAll(children)
            }
        }

        for (comp in allComps) {
            comp ?: continue

            ComponentPosition2.setValues(comp)
            val found = comps.any { it.componentId == comp.componentId }

            if (!found) {
                comps.add(comp)
            }
        }
        return comps
    }

    fun drawComponents(
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
    ) {
        logger.info("Drawing preview interface")
        val result = BufferedImage(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g = result.graphics
        drawInterfaceToGraphics(
            g,
            interfaceId,
            showContainers,
            showHidden,
            showModels,
        )
    }

    fun drawInterfaceToGraphics(
        g: Graphics,
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
    ) {
        g.color = Constants.BG_FILL_COLOR
        g.fillRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)

        val orderedComponents = getOrderedComps(interfaceId) ?: return

        for (component in orderedComponents) {

            ComponentPosition2.setValues(component)

            if (InterfaceUtils.isHidden(component) && !showHidden) {
                continue
            }

            if (component.width <= 0 || component.height <= 0) {
                logger.warning("Skipping component ${component.componentId} " + "invalid size ${component.width}x${component.height}")
                continue
            }

            val x = ComponentDefinition.getX(component, interfaceId)
            val y = ComponentDefinition.getY(component, interfaceId)

            val parent = InterfaceUtils.getParent(component.parentId)

            if (component.type == ComponentConstants.SPRITE && component.spriteId > -1) {
                val unscaled = SpriteLoader.getSprite(component.spriteId) ?: continue

                val safeW = component.width.coerceAtLeast(1)
                val safeH = component.height.coerceAtLeast(1)

                var sprite = ImageUtils.imageToBufferedImage(unscaled.getScaledInstance(safeW, safeH, Image.SCALE_SMOOTH))

                if (component.hFlip) {
                    sprite = ImageUtils.horizontalFlip(sprite)
                }
                if (component.vFlip) {
                    sprite = ImageUtils.verticalFlip(sprite)
                }

                g.drawImage(sprite, x, y, null)
            }

            if (component.type == ComponentConstants.FIGURE) {
                g.color = if (component.color == 0) Color.BLACK else Color(component.color)

                g.drawRect(x, y, component.width, component.height)

                if (component.filled) {
                    g.fillRect(x, y, component.width, component.height)
                }
            }

            if (component.type == ComponentConstants.MODEL && showModels) {
                g.color = Color.BLUE
                g.drawRect(x, y, component.width, component.height)
            }

            if (component.type == ComponentConstants.LAYER) {
                var sprite: BufferedImage? = null

                when {
                    ContainerHelper.isScrollBar(component) -> {
                        try {
                            sprite =
                                ImageUtils.resize(
                                    ImageIO.read(File("data/theme/icons/scripts/scroll_bar.png")),
                                    component.width,
                                    component.height
                                )
                        } catch (_: IOException) {
                            println("scrollbar.jpg not found")
                        }
                    }
                    ContainerHelper.isButton(component) -> {
                        try {
                            sprite =
                                ImageUtils.resize(
                                    ImageIO.read(File("data/theme/icons/scripts/button.png")),
                                    component.width,
                                    component.height
                                )
                        } catch (_: IOException) {
                            println("button.png not found")
                        }
                    }
                    showContainers -> {
                        g.color = if (component.parentId > 0) Color.RED else Color.GREEN

                        val drawX = if (component.parentId > 0) x else component.positionX

                        val drawY = if (component.parentId > 0) y else component.positionY

                        g.drawRect(drawX, drawY, component.width, component.height)
                    }
                }

                sprite?.let { g.drawImage(it, x, y, null) }
            }

            if (component.type == ComponentConstants.TEXT) {
                g.font = Font("Helvetica", Font.PLAIN, 11)
                g.color = Color(component.color)

                val fm = g.fontMetrics
                val rect = fm.getStringBounds(component.text, g)

                if (component.parentId == -1) {
                    g.drawString(
                        component.text,
                        (component.positionX + component.width / 2 - rect.width / 2).roundToInt(),
                        (component.positionY + component.height / 2 + rect.height / 2).roundToInt()
                    )
                } else if (parent != null) {
                    ComponentPosition.setValues(parent)

                    if (component.baseWidth == 0 && component.baseHeight == 0) {
                        val metrics = g.fontMetrics
                        val tx = parent.positionX + (parent.width - metrics.stringWidth(component.text)) / 2
                        val ty = parent.positionY + ((parent.height - metrics.height) / 2) + metrics.ascent

                        g.drawString(component.text, tx, ty)
                    } else {
                        var posX = x
                        val posY = y

                        if (posX > parent.width + parent.positionX) {
                            posX = parent.width - component.width
                        }

                        g.drawString(
                            component.text,
                            (posX + component.width / 2 - rect.width / 2).roundToInt(),
                            (posY + component.height / 2 + rect.height / 2).roundToInt()
                        )
                    }
                }
            }
        }
    }
}
