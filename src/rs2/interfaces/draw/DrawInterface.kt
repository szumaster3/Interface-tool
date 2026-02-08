package rs2.interfaces.draw

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min
import properties.Constants
import rs2.cache.loaders.ComponentDefinition
import rs2.cache.loaders.ComponentDefinition.Companion.getX
import rs2.cache.loaders.ComponentDefinition.Companion.getY
import rs2.interfaces.ComponentConstants
import rs2.interfaces.ContainerHelper.isButton
import rs2.interfaces.ContainerHelper.isScrollBar
import rs2.interfaces.InterfaceUtils.getParent
import rs2.interfaces.InterfaceUtils.isHidden
import rs2.interfaces.draw.ComponentPosition.setValues
import rs2.sprites.ImageUtils.horizontalFlip
import rs2.sprites.ImageUtils.imageToBufferedImage
import rs2.sprites.ImageUtils.verticalFlip
import rs2.sprites.SpriteLoader.getSprite
import ui.InterfaceUI
import ui.InterfaceUI.Companion.getOrderedComps

object DrawInterface {
    private val DEFAULT_OUTLINE = Color(140, 140, 140)
    private const val SELECTED_ALPHA = 80

    private fun drawBackground(g: Graphics?) {
        val clip = if ((g != null)) g.clipBounds else null
        val w = if ((clip != null)) clip.width else Constants.VIEWPORT_WIDTH
        val h = if ((clip != null)) clip.height else Constants.VIEWPORT_HEIGHT

        g!!.color = Color.BLACK
        g.fillRect(0, 0, w, h)
        g.color = Color(80, 80, 80)
        val gridSize = 16
        var x = 0
        while (x <= w) {
            g.drawLine(x, 0, x, h)
            x += gridSize
        }
        var y = 0
        while (y <= h) {
            g.drawLine(0, y, w, y)
            y += gridSize
        }
    }

    private fun computeAbsolutePosition(comp: ComponentDefinition?): Point {
        if (comp == null) return Point(0, 0)
        setValues(comp)
        var ax = comp.positionX
        var ay = comp.positionY
        var parent = getParent(comp.parentId)
        while (parent != null) {
            setValues(parent)
            ax += parent.positionX
            ay += parent.positionY
            parent = getParent(parent.parentId)
        }
        return Point(ax, ay)
    }

    private fun computeAncestorClip(comp: ComponentDefinition): Rectangle? {
        var clip: Rectangle? = null
        var parent = getParent(comp.parentId)
        while (parent != null) {
            if (parent.type == ComponentConstants.LAYER && (parent.scrollMaxV > 0 || parent.scrollMaxH > 0)) {
                setValues(parent)
                val p = computeAbsolutePosition(parent)
                val w = if (parent.scrollMaxV > 0) parent.scrollMaxV else parent.width
                val h = if (parent.scrollMaxH > 0) parent.scrollMaxH else parent.height
                val r = Rectangle(p.x, p.y, w, h)
                clip = if ((clip == null)) r else clip.intersection(r)
            }
            parent = getParent(parent.parentId)
        }
        return clip
    }

    fun drawComponents(
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
    ) {
        val result = BufferedImage(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g = result.graphics
        try {
            drawInterfaceToGraphics(
                g,
                interfaceId,
                showContainers,
                showHidden,
                showModels,
            )
        } finally {
            g.dispose()
        }
    }

    fun drawInterfaceToGraphics(
        g: Graphics?,
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
    ) {
        if (g == null) return

        drawBackground(g)

        val orderedComponents: List<ComponentDefinition?>? = getOrderedComps(interfaceId)
        if (orderedComponents == null || orderedComponents.isEmpty()) return

        val g2 = g.create() as Graphics2D
        try {
            for (comp in orderedComponents) {
                if (comp == null) continue

                val selected = (InterfaceUI.selectedComponent != null && InterfaceUI.selectedComponent == comp)

                setValues(comp)
                if (isHidden(comp) && !showHidden) continue

                val abs = computeAbsolutePosition(comp)
                val x = abs.x
                val y = abs.y
                val width = comp.width
                val height = comp.height

                val prevClip = g2.clip
                val ancClip = computeAncestorClip(comp)
                if (ancClip != null) g2.clip = ancClip

                if (comp.type == ComponentConstants.SPRITE && comp.spriteId > -1) {
                    var sprite = getSprite(comp.spriteId)
                    if (sprite != null) {
                        if (comp.hFlip) sprite = horizontalFlip(sprite)
                        if (comp.vFlip) sprite = verticalFlip(sprite)

                        if (comp.spriteTiling) {
                            val sw = sprite.width
                            val sh = sprite.height
                            var tx = 0
                            while (tx < width) {
                                var ty = 0
                                while (ty < height) {
                                    val dw = min(sw.toDouble(), (width - tx).toDouble()).toInt()
                                    val dh = min(sh.toDouble(), (height - ty).toDouble()).toInt()
                                    g2.drawImage(sprite, x + tx, y + ty, x + tx + dw, y + ty + dh, 0, 0, dw, dh, null)
                                    ty += sh
                                }
                                tx += sw
                            }
                        } else {
                            val scaled =
                                sprite.getScaledInstance(
                                    max(1.0, width.toDouble()).toInt(),
                                    max(1.0, height.toDouble()).toInt(),
                                    Image.SCALE_SMOOTH
                                )
                            g2.drawImage(imageToBufferedImage(scaled), x, y, null)
                        }
                    }
                }

                if (comp.type == ComponentConstants.FIGURE && comp.filled) {
                    val color = if (comp.color == 0) Color.BLACK else Color(comp.color)
                    g2.color = color
                    g2.fillRect(x, y, width, height)
                }

                if (comp.type == ComponentConstants.MODEL && showModels) {
                    g2.color = Color.BLUE
                    g2.drawRect(getX(comp, interfaceId), getY(comp, interfaceId), comp.width, comp.height)
                }

                if (comp.type == ComponentConstants.LAYER && showContainers) {
                    var sprite: BufferedImage? = null
                    var tiling = false

                    if (isScrollBar(comp)) {
                        sprite = getScrollBarSprite(comp)
                        tiling = comp.spriteTiling
                    } else if (isButton(comp)) {
                        sprite = getButtonSprite(comp)
                        tiling = comp.spriteTiling
                    }

                    if (sprite != null) {
                        if (comp.hFlip) sprite = horizontalFlip(sprite)
                        if (comp.vFlip) sprite = verticalFlip(sprite)

                        if (tiling) {
                            val sw = sprite.width
                            val sh = sprite.height
                            var tx = 0
                            while (tx < width) {
                                var ty = 0
                                while (ty < height) {
                                    val dw = min(sw.toDouble(), (width - tx).toDouble()).toInt()
                                    val dh = min(sh.toDouble(), (height - ty).toDouble()).toInt()
                                    g2.drawImage(sprite, x + tx, y + ty, x + tx + dw, y + ty + dh, 0, 0, dw, dh, null)
                                    ty += sh
                                }
                                tx += sw
                            }
                        } else {
                            val scaled =
                                sprite.getScaledInstance(
                                    max(1.0, width.toDouble()).toInt(),
                                    max(1.0, height.toDouble()).toInt(),
                                    Image.SCALE_DEFAULT
                                )
                            g2.drawImage(imageToBufferedImage(scaled), x, y, null)
                        }
                    }
                }

                if (comp.type == ComponentConstants.TEXT && comp.text != null) {
                    val color = Color(comp.color)

                    val font = Font("Helvetica", Font.PLAIN, 11)
                    g2.font = font
                    val fm = g2.getFontMetrics(font)

                    val text = comp.text
                    val ascent = fm.ascent
                    val descent = fm.descent

                    val lines: MutableList<String> = ArrayList()
                    var currentLine = StringBuilder()
                    for (word in text!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        if (fm.stringWidth(currentLine.toString() + (if (currentLine.length > 0) " " else "") + word) <= width) {
                            if (currentLine.length > 0) currentLine.append(" ")
                            currentLine.append(word)
                        } else {
                            lines.add(currentLine.toString())
                            currentLine = StringBuilder(word)
                        }
                    }
                    if (currentLine.length > 0) lines.add(currentLine.toString())

                    val totalHeight = lines.size * (ascent + descent)
                    var startY = y + (height - totalHeight) / 2 + ascent
                    g2.color = color
                    for (line in lines) {
                        val lineWidth = fm.stringWidth(line)
                        val textX = x + (width - lineWidth) / 2
                        g2.drawString(line, textX, startY)
                        startY += ascent + descent
                    }
                }

                var outlineColor: Color
                var fillColor: Color? = null
                if (selected) {
                    when (comp.type) {
                        ComponentConstants.SPRITE -> {
                            outlineColor = Color(195, 27, 206)
                            fillColor = Color(238, 0, 255, SELECTED_ALPHA)
                        }
                        ComponentConstants.FIGURE -> {
                            outlineColor = Color(112, 112, 112)
                            fillColor = Color(112, 112, 112, SELECTED_ALPHA)
                        }
                        ComponentConstants.TEXT -> {
                            outlineColor = Color(255, 235, 0, 255)
                            fillColor = Color(255, 251, 0, 50)
                        }
                        ComponentConstants.MODEL -> {
                            outlineColor = Color(61, 119, 117)
                            fillColor = Color(0, 215, 255, 50)
                        }
                        ComponentConstants.LAYER -> {
                            outlineColor = Color(0, 255, 0)
                            fillColor = Color(0, 240, 0, 50)
                        }
                        else -> {
                            outlineColor = Color(255, 255, 255, 250)
                            fillColor = Color(255, 255, 255)
                        }
                    }
                } else {
                    outlineColor = Color(255, 255, 255, 200)
                }

                if (fillColor != null) {
                    g2.color = fillColor
                    g2.fillRect(x, y, width, height)
                }

                g2.color = outlineColor
                g2.stroke = BasicStroke(1.5f)
                g2.drawRect(x, y, width, height)

                g2.clip = prevClip
            }
        } finally {
            g2.dispose()
        }
    }

    fun isScrollBarScript(component: ComponentDefinition): Boolean {
        return component.onLoadScript != null && component.onLoadScript!![0].toString().toInt() == 30
    }

    fun isButtonScript(component: ComponentDefinition): Boolean {
        return component.onLoadScript != null && component.onLoadScript!![0].toString().toInt() == 92
    }

    fun getScrollBarSprite(comp: ComponentDefinition): BufferedImage? {
        if (!isScrollBarScript(comp)) return null
        try {
            var sprite = ImageIO.read(File("data/scriptsprites/scroll_bar.png"))
            if (comp.hFlip) sprite = horizontalFlip(sprite!!)
            if (comp.vFlip) sprite = verticalFlip(sprite!!)
            return sprite
        } catch (e: IOException) {
            return null
        }
    }

    fun getButtonSprite(comp: ComponentDefinition): BufferedImage? {
        if (!isButtonScript(comp)) return null
        try {
            var sprite = ImageIO.read(File("data/scriptsprites/button.png"))
            if (comp.hFlip) sprite = horizontalFlip(sprite!!)
            if (comp.vFlip) sprite = verticalFlip(sprite!!)
            return sprite
        } catch (e: IOException) {
            return null
        }
    }
}
