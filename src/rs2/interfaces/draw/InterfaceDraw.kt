package rs2.interfaces.draw

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min
import properties.Constants
import rs2.cache.loaders.ComponentDefinition
import rs2.cache.loaders.ComponentDefinition.Companion.getInterface
import rs2.interfaces.ComponentConstants
import rs2.interfaces.ContainerHelper
import rs2.interfaces.InterfaceUtils
import rs2.sprites.ImageUtils
import rs2.sprites.SpriteLoader
import ui.InterfaceUI

object InterfaceDraw {
    var editMode = true
    private var drawEnabled = true
    const val SELECTED_ALPHA = 80
    private const val FIXED_SHIFT = 14
    private var cachedInterfaceId = -1
    private val resizedSpriteCache = mutableMapOf<Triple<Int, Int, Int>, BufferedImage>()
    private val childrenCache = mutableMapOf<Pair<Int, Int>, MutableList<ComponentDefinition>>()
    val sizeCache = mutableMapOf<ComponentDefinition, Dimension>()

    enum class ViewMode {
        NONE,
        BY_TYPE,
        BY_LAYER
    }

    private val DEFAULT_FONT = Font("Helvetica", Font.PLAIN, 11)

    fun buildTree(interfaceId: Int): List<ComponentDefinition> {
        val all = getInterface(interfaceId, false)?.filterNotNull() ?: return emptyList()
        childrenCache.clear()
        for (comp in all) {
            val key = interfaceId to comp.parentId
            childrenCache.getOrPut(key) { mutableListOf() }.add(comp)
        }
        return childrenCache[interfaceId to -1] ?: emptyList()
    }

    private fun realSize(c: ComponentDefinition, pw: Int, ph: Int) {
        c.width =
            when (c.aspectWidthType.toInt()) {
                0 -> c.baseWidth
                1 -> pw - c.baseWidth
                2,
                4 -> pw * c.baseWidth shr FIXED_SHIFT
                else -> c.width
            }
        c.height =
            when (c.aspectHeightType.toInt()) {
                0 -> c.baseHeight
                1 -> ph - c.baseHeight
                2,
                4 -> ph * c.baseHeight shr FIXED_SHIFT
                else -> c.height
            }
        if (c.type == ComponentConstants.LAYER) {
            if (c.width <= 0) c.width = 5
            if (c.height <= 0) c.height = 5
        }
    }

    private fun realPosition(c: ComponentDefinition, pw: Int, ph: Int) {
        c.positionX =
            when (c.yMode.toInt()) {
                0 -> c.baseX
                1 -> (pw - c.width) / 2 + c.baseX
                2 -> pw - c.width - c.baseX
                3 -> c.baseX * pw shr FIXED_SHIFT
                4 -> (pw - c.width) / 2 + (c.baseX * pw shr FIXED_SHIFT)
                5 -> pw - c.width - (c.baseX * pw shr FIXED_SHIFT)
                else -> c.baseX
            }
        c.positionY =
            when (c.xMode.toInt()) {
                0 -> c.baseY
                1 -> (ph - c.height) / 2 + c.baseY
                2 -> ph - c.height - c.baseY
                3 -> c.baseY * ph shr FIXED_SHIFT
                4 -> (ph - c.height) / 2 + (c.baseY * ph shr FIXED_SHIFT)
                5 -> ph - c.height - (c.baseY * ph shr FIXED_SHIFT)
                else -> c.baseY
            }
    }

    fun setValues(component: ComponentDefinition?) {
        if (component == null) return
        if (sizeCache.containsKey(component)) return

        val parent = InterfaceUtils.getParent(component.parentId)
        val pw: Int
        val ph: Int
        if (parent == null) {
            pw = Constants.VIEWPORT_WIDTH
            ph = Constants.VIEWPORT_HEIGHT
        } else {
            setValues(parent)
            pw = parent.width
            ph = parent.height
        }
        realSize(component, pw, ph)
        realPosition(component, pw, ph)
        sizeCache[component] = Dimension(component.width, component.height)
    }

    fun computeAbsolutePosition(comp: ComponentDefinition?): Point {
        if (comp == null) return Point(0, 0)
        setValues(comp)
        var ax = comp.positionX
        var ay = comp.positionY
        var parent = InterfaceUtils.getParent(comp.parentId)
        while (parent != null) {
            setValues(parent)
            ax += parent.positionX
            ay += parent.positionY
            parent = InterfaceUtils.getParent(parent.parentId)
        }
        return Point(ax, ay)
    }

    fun drawInterfaceToGraphics(
        g: Graphics,
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        viewMode: ViewMode,
        selectedType: Int? = null
    ) {
        val g2 = g as Graphics2D
        sizeCache.clear()

        if (cachedInterfaceId != interfaceId) {
            childrenCache.clear()
            cachedInterfaceId = interfaceId
        }

        drawBackground(g2)
        g2.clip = Rectangle(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)

        val roots = buildTree(interfaceId)

        for (root in roots) {
            drawComponent(g2, interfaceId, root, 0, 0, showContainers, showHidden, showModels, viewMode, selectedType)
        }
    }

    private fun drawComponent(
        g2: Graphics2D,
        interfaceId: Int,
        comp: ComponentDefinition,
        parentAbsX: Int,
        parentAbsY: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        viewMode: ViewMode,
        selectedType: Int?
    ) {
        if (!showHidden && InterfaceUtils.isHidden(comp)) return

        setValues(comp)

        if (comp.width <= 0 || comp.height <= 0) return

        val x = parentAbsX + comp.positionX
        val y = parentAbsY + comp.positionY
        val w = comp.width
        val h = comp.height

        val shouldDraw =
            when (viewMode) {
                ViewMode.NONE -> true
                ViewMode.BY_LAYER -> comp.type == ComponentConstants.LAYER
                ViewMode.BY_TYPE -> selectedType != null && comp.type == selectedType
            }

        if (shouldDraw) {
            when (comp.type) {
                ComponentConstants.LAYER -> drawLayer(g2, comp, x, y, w, h, showContainers)
                ComponentConstants.SPRITE -> drawSprite(g2, comp, x, y, w, h)
                ComponentConstants.FIGURE -> drawFigure(g2, comp, x, y, w, h)
                ComponentConstants.MODEL -> if (showModels) drawModel(g2, comp, x, y, w, h)
                ComponentConstants.TEXT -> drawText(g2, comp, x, y, w, h)
                ComponentConstants.LINE -> drawLine(g2, comp, x, y, w, h)
            }

            val selected = InterfaceUI.instance.selectedComponent == comp
            drawOutlines(g2, comp, x, y, w, h, selected)
        }

        childrenCache[interfaceId to comp.componentHash]?.let { children ->
            val oldClip = g2.clip

            if (comp.type == ComponentConstants.LAYER) {
                val layerBounds = Rectangle(x, y, w, h)
                g2.clip = oldClip?.bounds?.intersection(layerBounds) ?: layerBounds
            }

            for (child in children) {
                drawComponent(g2, interfaceId, child, x, y, showContainers, showHidden, showModels, viewMode, selectedType)
            }

            g2.clip = oldClip
        }
    }

    private fun drawBackground(g: Graphics2D) {
        g.color = if (editMode) Color.BLACK else Constants.BG_FILL_COLOR
        g.fillRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)

        if (!editMode) return
        val oldColor = g.color
        val oldStroke = g.stroke
        g.color = Color(80, 80, 80)
        g.stroke = BasicStroke(1f)
        val step = 16
        var x = 0
        while (x <= Constants.VIEWPORT_WIDTH) {
            g.drawLine(x, 0, x, Constants.VIEWPORT_HEIGHT)
            x += step
        }
        var y = 0
        while (y <= Constants.VIEWPORT_HEIGHT) {
            g.drawLine(0, y, Constants.VIEWPORT_WIDTH, y)
            y += step
        }
        g.color = oldColor
        g.stroke = oldStroke
    }

    private fun drawFigure(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        if (w <= 0 || h <= 0) return
        if (drawEnabled) {
            val alpha = 255 - comp.transparency
            val rgb = comp.color
            val r = (rgb shr 16) and 0xFF
            val gCol = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            val oldComposite = g.composite
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f)
            g.color = Color(r, gCol, b)
            if (comp.filled) g.fillRect(x, y, w, h)
            g.composite = oldComposite
        }
    }

    private fun drawSprite(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        if (!drawEnabled || comp.spriteId < 0) return

        var sprite = SpriteLoader.getSprite(comp.spriteId) ?: return

        if (comp.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
        if (comp.vFlip) sprite = ImageUtils.verticalFlip(sprite)

        val oldComposite = g.composite
        val alpha = (255 - comp.transparency) / 255f
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

        if (comp.spriteTiling) {
            val sw = sprite.width
            val sh = sprite.height
            val maxX = x + w
            val maxY = y + h

            var tx = x
            while (tx < maxX) {
                var ty = y
                val dw = min(sw, maxX - tx)
                while (ty < maxY) {
                    val dh = min(sh, maxY - ty)
                    g.drawImage(sprite, tx, ty, tx + dw, ty + dh, 0, 0, dw, dh, null)
                    ty += sh
                }
                tx += sw
            }
        } else {
            val key = Triple(comp.spriteId, w, h)

            val scaled = resizedSpriteCache.getOrPut(key) { ImageUtils.resize(sprite, w, h) }

            g.drawImage(scaled, x, y, null)
        }

        g.composite = oldComposite
    }

    private fun drawLayer(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, width: Int, height: Int, showContainers: Boolean) {
        if (width <= 0 || height <= 0) return
        var sprite: BufferedImage? = null
        if (drawEnabled) {
            if (ContainerHelper.isScrollBar(comp)) sprite = loadSprite("data/theme/icons/scripts/scroll_bar.png", width, height, comp)
            else if (ContainerHelper.isButton(comp)) sprite = loadSprite("data/theme/icons/scripts/button.png", width, height, comp)
            sprite?.let { g.drawImage(it, x, y, width, height, null) }

            if (showContainers) {
                g.color = if (comp.parentId > 0) Color.RED else Color.GREEN
                g.stroke = BasicStroke(1f)
                g.drawRect(x, y, width, height)
            }
        }
    }

    private fun drawLine(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        if (drawEnabled) {
            val oldComposite = g.composite
            val alpha = 1f - (comp.transparency / 255f)
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

            g.color = Color(comp.color, true)
            g.stroke = BasicStroke(comp.lineWidth.toFloat())
            g.drawRect(x, y, w, h)

            g.composite = oldComposite
        }
    }

    private fun drawModel(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        if (drawEnabled) {
            g.color = Color.BLUE
            g.stroke = BasicStroke(1.0f)
            g.drawRect(x, y, w, h)
        }
    }

    private fun drawText(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        val raw = comp.text ?: return
        if (raw.isEmpty() || w <= 0 || h <= 0) return
        g.font = DEFAULT_FONT
        val fm = g.fontMetrics
        val lines = mutableListOf<String>()
        for (part in raw.split("<br>")) {
            val words = part.split(" ")
            var current = ""
            for (word in words) {
                val test = if (current.isEmpty()) word else "$current $word"
                if (fm.stringWidth(test) > w && current.isNotEmpty()) {
                    lines.add(current)
                    current = word
                } else current = test
            }
            if (current.isNotEmpty()) lines.add(current)
        }
        if (lines.isEmpty()) return
        val innerHeight = h - comp.vPadding * 2
        val totalTextHeight = lines.size * fm.height
        val verticalOffset =
            when (comp.textVerticalAli) {
                1 -> (innerHeight - totalTextHeight) / 2
                2 -> innerHeight - totalTextHeight
                else -> 0
            }
        var drawY = y + comp.vPadding + verticalOffset
        val mainColor = Color(comp.color)
        for (line in lines) {
            val drawX =
                when (comp.textHorizontalAli) {
                    1 -> x + (w - fm.stringWidth(line)) / 2
                    2 -> x + w - fm.stringWidth(line)
                    else -> x
                }
            val finalY = drawY + fm.ascent
            if (comp.shadow) {
                val shadowColor = comp.shadowColor?.let { Color(it) } ?: Color(0, 0, 0, 150)
                g.color = shadowColor
                g.drawString(line, drawX + 1, finalY + 1)
            }
            g.color = mainColor
            g.drawString(line, drawX, finalY)
            drawY += fm.height
        }
    }

    private fun loadSprite(path: String, width: Int, height: Int, comp: ComponentDefinition): BufferedImage? =
        try {
            var sprite = ImageUtils.resize(ImageIO.read(File(path)), width, height)
            if (comp.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
            if (comp.vFlip) sprite = ImageUtils.verticalFlip(sprite)
            sprite
        } catch (_: Exception) {
            null
        }

    private fun drawOutlines(g2: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int, selected: Boolean) {
        if (!editMode) return

        g2.color = Color.WHITE
        g2.stroke = BasicStroke(1.0f)
        g2.drawRect(x, y, w, h)

        if (!selected) return

        val alpha = 100

        val fillColor =
            when (comp.type) {
                ComponentConstants.SPRITE -> Color(127, 80, 190, alpha)
                ComponentConstants.FIGURE -> Color(110, 110, 110, alpha)
                ComponentConstants.TEXT -> Color(255, 251, 0, alpha)
                ComponentConstants.MODEL -> Color(87, 87, 210, alpha)
                ComponentConstants.LAYER -> Color(0, 174, 0, alpha)
                else -> Color(255, 255, 0, alpha)
            }

        g2.color = fillColor
        g2.fillRect(x, y, w, h)
    }

}
