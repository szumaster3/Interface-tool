package rs2.interfaces.draw

import properties.Constants
import rs2.cache.loaders.ComponentDefinition
import rs2.cache.loaders.ComponentDefinition.Companion.getInterface
import rs2.interfaces.ComponentConstants
import rs2.interfaces.ContainerHelper
import rs2.interfaces.InterfaceUtils
import rs2.sprites.ImageUtils
import rs2.sprites.SpriteLoader
import ui.InterfaceUI
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

object InterfaceDraw {
    var editMode = true
    const val SELECTED_ALPHA = 80
    private const val FIXED_SHIFT = 14
    private val childrenCache = mutableMapOf<Pair<Int, Int>, List<ComponentDefinition>>()
    enum class ViewMode { NONE, BY_TYPE, BY_LAYER }

    fun getOrderedComps(
        interfaceId: Int,
        showHidden: Boolean = false,
    ): List<ComponentDefinition> {
        val ordered = mutableListOf<ComponentDefinition>()
        val added = mutableSetOf<ComponentDefinition>()
        val allComps = getInterface(interfaceId, false)?.filterNotNull() ?: return ordered

        childrenCache.clear()
        for (comp in allComps) {
            val parentHash = comp.parentId
            val key = interfaceId to parentHash
            childrenCache[key] = childrenCache.getOrElse(key) { mutableListOf() }.plus(comp)
        }

        val maxDepth = 50

        for (c in allComps) {
            if (c.parentId == -1) {
                addCom(interfaceId, c, ordered, added, true, mutableSetOf(), 0, maxDepth)
            }
        }

        return ordered
    }

    private fun addCom(
        interfaceId: Int,
        comp: ComponentDefinition,
        list: MutableList<ComponentDefinition>,
        added: MutableSet<ComponentDefinition>,
        showHidden: Boolean,
        path: MutableSet<ComponentDefinition>,
        depth: Int,
        maxDepth: Int,
    ) {
        if (added.contains(comp)) return
        if (path.contains(comp)) return
        if (depth > maxDepth) return

        list.add(comp)
        added.add(comp)
        path.add(comp)

        val children = childrenCache[interfaceId to comp.componentHash] ?: emptyList()
        for (child in children) {
            addCom(interfaceId, child, list, added, true, path.toMutableSet(), depth + 1, maxDepth)
        }

        path.remove(comp)
    }

    private fun realSize(
        component: ComponentDefinition,
        parentWidth: Int,
        parentHeight: Int
    ) {
        component.width = when (component.aspectWidthType.toInt()) {
            0 -> component.baseWidth // "abs"
            1 -> parentWidth - component.baseWidth // "minus"
            2, 4 -> parentWidth * component.baseWidth shr FIXED_SHIFT
            3 -> component.width // "mode_3"
            else -> component.width
        }

        component.height = when (component.aspectHeightType.toInt()) {
            0 -> component.baseHeight
            1 -> parentHeight - component.baseHeight
            2, 4 -> parentHeight * component.baseHeight shr FIXED_SHIFT
            3 -> component.height
            else -> component.height
        }

        if (component.type == 0) {
            if (component.width <= 0) component.width = 5
            if (component.height <= 0) component.height = 5
        }
    }

    private fun realPosition(component: ComponentDefinition, parentWidth: Int, parentHeight: Int) {
        component.positionX = when (component.yMode.toInt()) {
            0 -> component.baseX // "abs_left"
            1 -> (parentWidth - component.width) / 2 + component.baseX
            2 -> parentWidth - component.width - component.baseX
            3 -> component.baseX * parentWidth shr FIXED_SHIFT
            4 -> (parentWidth - component.width) / 2 + (component.baseX * parentWidth shr FIXED_SHIFT)
            5 -> parentWidth - component.width - (component.baseX * parentWidth shr FIXED_SHIFT)
            else -> component.baseX
        }

        component.positionY = when (component.xMode.toInt()) {
            0 -> component.baseY
            1 -> (parentHeight - component.height) / 2 + component.baseY
            2 -> parentHeight - component.height - component.baseY
            3 -> component.baseY * parentHeight shr FIXED_SHIFT
            4 -> (parentHeight - component.height) / 2 + (component.baseY * parentHeight shr FIXED_SHIFT)
            5 -> parentHeight - component.height - (component.baseY * parentHeight shr FIXED_SHIFT)
            else -> component.baseY
        }
    }

    val sizeCache = mutableMapOf<ComponentDefinition, Dimension>()

    fun setValues(component: ComponentDefinition?) {
        if (component == null) return
        if (sizeCache.containsKey(component)) return

        val parent = InterfaceUtils.getParent(component.parentId)
        val parentWidth: Int
        val parentHeight: Int
        if (parent == null) {
            parentWidth = Constants.VIEWPORT_WIDTH
            parentHeight = Constants.VIEWPORT_HEIGHT
        } else {
            setValues(parent)
            parentWidth = parent.width
            parentHeight = parent.height
        }

        realSize(component, parentWidth, parentHeight)
        realPosition(component, parentWidth, parentHeight)
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

    fun drawComponents(
        interfaceId: Int,
        showContainers: Boolean = true,
        showHidden: Boolean = false,
        showModels: Boolean = false,
        viewMode: ViewMode = ViewMode.BY_TYPE
    ): BufferedImage {
        sizeCache.clear()
        val result = BufferedImage(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val g = result.createGraphics()
        try {
            drawInterfaceToGraphics(g, interfaceId, showContainers, showHidden, showModels, viewMode)
        } finally {
            g.dispose()
        }
        return result
    }

    fun drawInterfaceToGraphics(
        g: Graphics,
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        viewMode: ViewMode,
        selectedType: Int? = null
    ){
        val g2 = g.create() as Graphics2D
        try {
            drawBackground(g2)
            val comps = getOrderedComps(interfaceId, showHidden)

            val filteredComps: List<ComponentDefinition> = when (viewMode) {
                ViewMode.NONE -> comps
                ViewMode.BY_TYPE -> selectedType?.let { comps.filter { comp -> comp.type == it } } ?: emptyList()
                ViewMode.BY_LAYER -> comps.filter { it.type == ComponentConstants.LAYER }
            }

            val texts = mutableListOf<ComponentDefinition>()
            for (comp in filteredComps) {
                if (comp == null) continue
                if (!showHidden && InterfaceUtils.isHidden(comp)) continue

                if (comp.type == ComponentConstants.TEXT) {
                    texts.add(comp)
                    continue
                }

                val abs = computeAbsolutePosition(comp)
                val x = abs.x
                val y = abs.y
                val width = max(1, comp.width)
                val height = max(1, comp.height)

                if (width <= 0 || height <= 0) continue

                when (comp.type) {
                    ComponentConstants.SPRITE -> drawSprite(g2, comp, x, y, width, height)
                    ComponentConstants.FIGURE -> drawFigure(g2, comp, x, y, width, height)
                    ComponentConstants.MODEL -> if (showModels) drawModel(g2, comp, x, y, width, height)
                    ComponentConstants.LAYER -> drawLayer(g2, comp, x, y, width, height, showContainers)
                    ComponentConstants.LINE -> drawUnknown(g2, comp, x, y, width, height)
                }

                val selected = InterfaceUI.instance.selectedComponent == comp
                if (editMode) drawSelectionOutline(g2, comp, x, y, width, height, selected)
            }

            for (comp in texts) {
                val abs = computeAbsolutePosition(comp)
                val x = abs.x
                val y = abs.y
                val width = max(1, comp.width)
                val height = max(1, comp.height)
                drawText(g2, comp, x, y, width, height)
                val selected = InterfaceUI.instance.selectedComponent == comp
                if (editMode) drawSelectionOutline(g2, comp, x, y, width, height, selected)
            }
        } finally {
            g2.dispose()
        }
    }

    private fun drawBackground(g: Graphics2D) {
        g.color = if (editMode) Color.BLACK else Color.LIGHT_GRAY
        g.fillRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
        if (editMode) {
            g.color = Color(80, 80, 80)
            val grid = 16
            for (x in 0..Constants.VIEWPORT_WIDTH step grid) g.drawLine(x, 0, x, Constants.VIEWPORT_HEIGHT)
            for (y in 0..Constants.VIEWPORT_HEIGHT step grid) g.drawLine(0, y, Constants.VIEWPORT_WIDTH, y)
        }
    }

    private fun drawFigure(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        if (width <= 0 || height <= 0) return
        val baseColor = Color(comp.color, comp.hasAlpha)
        g2.color = baseColor
        if (comp.filled) g2.fillRect(x, y, width, height) else g2.drawRect(x, y, width, height)
    }

    private fun drawSprite(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        if (comp.spriteId < 0 || width <= 0 || height <= 0) return
        var sprite = SpriteLoader.getSprite(comp.spriteId) ?: return
        if (comp.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
        if (comp.vFlip) sprite = ImageUtils.verticalFlip(sprite)

        if (comp.spriteTiling) {
            val sw = sprite.width
            val sh = sprite.height
            val maxX = x + width
            val maxY = y + height

            var tx = x
            while (tx < maxX) {
                var ty = y
                val dw = min(sw, maxX - tx)
                while (ty < maxY) {
                    val dh = min(sh, maxY - ty)
                    g2.drawImage(sprite, tx, ty, tx + dw, ty + dh, 0, 0, dw, dh, null)
                    ty += sh
                }
                tx += sw
            }
        } else {
            val scaled = ImageUtils.resize(sprite, width, height)
            g2.drawImage(scaled, x, y, null)
        }
    }

    private fun drawText(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        val text = comp.text ?: return
        g2.font = Font("Helvetica", Font.PLAIN, 11)
        val fm = g2.fontMetrics

        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (fm.stringWidth(test) > width) {
                if (current.isNotEmpty()) lines.add(current)
                current = word
            } else {
                current = test
            }
        }
        if (current.isNotEmpty()) lines.add(current)

        var drawY = y + comp.vPadding + fm.ascent
        for (line in lines) {
            if (drawY > y + height) break
            val drawX =
                when (comp.textHorizontalAli) {
                    1 -> x + (width - fm.stringWidth(line)) / 2
                    2 -> x + width - fm.stringWidth(line)
                    else -> x
                }
            val drawYFinal =
                drawY +
                    when (comp.textVerticalAli) {
                        1 -> (height - lines.size * fm.height) / 2
                        2 -> height - lines.size * fm.height
                        else -> 0
                    }
            if (comp.shadow) {
                val shadowColor = comp.shadowColor?.let { Color(it) } ?: Color(0, 0, 0, 150)
                g2.color = shadowColor
                g2.drawString(line, drawX + 1, drawYFinal + 1)
            }
            g2.color = Color(comp.color)
            g2.drawString(line, drawX, drawYFinal)
            drawY += fm.height
        }
    }

    private fun drawModel(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        if(!editMode){
        g2.color = Color.BLUE
        g2.drawRect(x, y, width, height)
            }
    }

    private fun drawLayer(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        showContainers: Boolean,
    ) {
        var sprite: BufferedImage? = null
        if (ContainerHelper.isScrollBar(comp)) {
            sprite = loadSprite("data/theme/icons/scripts/scroll_bar.png", width, height, comp)
        } else if (ContainerHelper.isButton(comp)) {
            sprite = loadSprite("data/theme/icons/scripts/button.png", width, height, comp)
        } else if (showContainers) {
           if(!editMode) {
               g2.color = if (comp.parentId > 0) Color.RED else Color.GREEN
           }
            g2.drawRect(x, y, width, height)
        }
        sprite?.let { g2.drawImage(it, x, y, null) }
    }

    private fun drawUnknown(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        g2.color = Color(comp.color, true)
        g2.stroke = BasicStroke(comp.lineWidth.toFloat())
        if (comp.unknownProp_8) g2.fillRect(x, y, width, height) else g2.drawRect(x, y, width, height)
    }

    private fun loadSprite(
        path: String,
        width: Int,
        height: Int,
        comp: ComponentDefinition,
    ): BufferedImage? =
        try {
            var sprite = ImageUtils.resize(ImageIO.read(File(path)), width, height)
            if (comp.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
            if (comp.vFlip) sprite = ImageUtils.verticalFlip(sprite)
            sprite
        } catch (_: Exception) {
            null
        }

    private fun drawSelectionOutline(
        g2: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        selected: Boolean,
    ) {
        if (selected) {
            InterfaceUtils.drawSelectedComponent(g2, comp, fill = true)
        } else {
            g2.color = Color(255, 255, 255)
            g2.stroke = BasicStroke(2f)
            g2.drawRect(x, y, width, height)
        }
    }

}
