package ui.draw

import fs.com.alex.utils.Utils
import rs.cache.ComponentDefinition
import rs.cache.ComponentDefinition.Companion.getInterface
import rs.cache.ComponentType
import rs.cache.sprite.SpriteLoader
import ui.Consts
import util.ImageUtils
import util.InterfaceUtils
import java.awt.*
import java.awt.image.BufferedImage
import java.lang.Integer.min


object InterfaceDraw {
    var editMode = false
    private const val FIXED_SHIFT = 14
    private val DEFAULT_FONT = Font("Helvetica", Font.PLAIN, 11)

    enum class ViewMode {
        NONE,
        BY_TYPE
    }

    private val childrenCache = mutableMapOf<Int, List<ComponentDefinition>>()
    private val spriteCache = mutableMapOf<String, BufferedImage>()
    private val textCache = mutableMapOf<String, List<String>>()

    private var preparedRoots: List<ComponentDefinition> = emptyList()

    fun prepareInterface(interfaceId: Int) {
        val all = getInterface(interfaceId, false)?.filterNotNull() ?: return
        childrenCache.clear()

        all.groupBy { it.parentId }.forEach { (parent, list) -> childrenCache[parent] = list }

        preparedRoots = childrenCache[-1] ?: emptyList()

        fun layout(comp: ComponentDefinition, parentW: Int, parentH: Int) {
            realSize(comp, parentW, parentH)
            realPosition(comp, parentW, parentH)
            comp.scrollMaxH = comp.scrollMaxH
            comp.scrollMaxV = comp.scrollMaxV

            childrenCache[comp.componentHash]?.forEach { layout(it, comp.width, comp.height) }
        }

        preparedRoots.forEach { layout(it, Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT) }
    }

    fun drawPreparedInterface(g: Graphics, showContainers: Boolean, showHidden: Boolean, showModels: Boolean, viewMode: ViewMode, selectedType: Int?, selectedComponent: ComponentDefinition?) {
        val g2 = g as Graphics2D
        drawBackground(g2)
        g2.clip = Rectangle(0, 0, Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)

        preparedRoots.forEach {
            drawComponent(g2, it, 0, 0, showContainers, showHidden, showModels, viewMode, selectedType, selectedComponent)
        }
    }

    private fun drawComponent(g: Graphics2D, comp: ComponentDefinition, parentAbsX: Int, parentAbsY: Int, showContainers: Boolean, showHidden: Boolean, showModels: Boolean, viewMode: ViewMode, selectedType: Int?, selectedComponent: ComponentDefinition?) {
        if (Utils.isHidden(comp) && !showHidden) return

        val x = parentAbsX + comp.positionX
        val y = parentAbsY + comp.positionY
        val w = comp.width
        val h = comp.height

        if (w <= 0 || h <= 0) return
        if (x > Consts.CENTER_PANEL_WIDTH || y > Consts.CENTER_PANEL_HEIGHT || x + w < 0 || y + h < 0) return

        val shouldDraw =
            when (viewMode)
            {
                ViewMode.NONE -> true
                ViewMode.BY_TYPE -> selectedType != null && comp.type == selectedType
            }

        if (shouldDraw)
        {
            when (comp.type)
            {
                ComponentType.LAYER  -> drawLayer(g, comp, x, y, w, h, showContainers)
                ComponentType.SPRITE -> drawSprite(g, comp, x, y, w, h)
                ComponentType.FIGURE -> drawFigure(g, comp, x, y, w, h)
                ComponentType.MODEL  -> if (showModels) drawModel(g, x, y, w, h)
                ComponentType.TEXT   -> drawText(g, comp, x, y, w, h)
                ComponentType.LINE   -> drawLine(g, comp)
            }

            val selected = selectedComponent == comp
            InterfaceUtils.drawOutlines(g, comp, x, y, w, h, selected)
        }

        childrenCache[comp.componentHash]?.forEach {
            drawComponent(g, it, x, y, showContainers, showHidden, showModels, viewMode, selectedType, selectedComponent)
        }
    }

    private fun drawSprite(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        var sprite = SpriteLoader.getSprite(comp.spriteId) ?: return

        if (comp.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
        if (comp.vFlip) sprite = ImageUtils.verticalFlip(sprite)

        if (comp.spriteTiling) {
            val sw = sprite.width
            val sh = sprite.height
            var tx = x
            while (tx < x + w) {
                var ty = y
                while (ty < y + h) {
                    val dw = min(sw, x + w - tx)
                    val dh = min(sh, y + h - ty)
                    g.drawImage(sprite, tx, ty, tx + dw, ty + dh, 0, 0, dw, dh, null)
                    ty += sh
                }
                tx += sw
            }
            return
        }

        val key = "${comp.spriteId}-$w-$h-${comp.hFlip}-${comp.vFlip}"
        val resized = spriteCache.getOrPut(key) { ImageUtils.resize(sprite, w, h) }

        val oldComposite = g.composite
        val alpha = 255 - comp.transparency
        if (alpha < 255) g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f)

        val oldClip = g.clip
        g.clip = Rectangle(x, y, w, h)
        g.drawImage(resized, x, y, null)
        g.clip = oldClip

        g.composite = oldComposite
    }

    private fun drawFigure(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        val old = g.composite
        val alpha = 255 - comp.transparency
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f)
        g.color = Color(comp.color)
        if (comp.filled) g.fillRect(x, y, w, h)
        g.composite = old
    }

    private fun drawLayer(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int, showContainers: Boolean) {
        if (showContainers) {
            g.color = if (comp.parentId > 0) Color.RED else Color.GREEN
            g.drawRect(x, y, w, h)
        }
    }

    private fun drawModel(g: Graphics2D, x: Int, y: Int, w: Int, h: Int) {
        g.color = Color.BLUE
        g.drawRect(x, y, w, h)
    }

    private fun drawLine(g: Graphics2D, comp: ComponentDefinition) {
        g.color = Color(comp.color)
        g.stroke = BasicStroke(comp.lineWidth.toFloat())
        val startX = comp.baseX
        val startY = comp.baseY
        val endX = if (comp.unknownProp_8) startX else startX + comp.baseWidth
        val endY = if (comp.unknownProp_8) startY + comp.baseHeight else startY
        g.drawLine(startX, startY, endX, endY)
    }

    private fun drawText(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        val raw = comp.text ?: return
        if (raw.isEmpty()) return
        g.font = DEFAULT_FONT
        val fm = g.fontMetrics
        val key = "$raw-$w"
        val lines = textCache.getOrPut(key) { wrapText(raw, w, fm) }

        val innerHeight = h
        val totalTextHeight = lines.size * fm.height
        val verticalOffset =
            when (comp.textVerticalAli) {
                1 -> (innerHeight - totalTextHeight) / 2
                2 -> innerHeight - totalTextHeight
                else -> 0
            }

        var drawY = y + verticalOffset
        val mainColor = Color(comp.color)

        for (line in lines) {
            val lineWidth = fm.stringWidth(line)
            val drawX =
                when (comp.textHorizontalAli) {
                    1 -> x + (w - lineWidth) / 2
                    2 -> x + w - lineWidth
                    else -> x
                }
            val finalY = drawY + fm.ascent
            if (comp.shadow) {
                g.color = comp.shadowColor?.let { Color(it) } ?: Color(0, 0, 0, 150)
                g.drawString(line, drawX + 1, finalY + 1)
            }
            g.color = mainColor
            g.drawString(line, drawX, finalY)
            drawY += fm.height
        }
    }

    private fun wrapText(raw: String, width: Int, fm: FontMetrics): List<String> {
        val lines = mutableListOf<String>()
        for (part in raw.split("<br>")) {
            var current = ""
            for (word in part.split(" ")) {
                val test = if (current.isEmpty()) word else "$current $word"
                if (fm.stringWidth(test) > width && current.isNotEmpty()) {
                    lines.add(current)
                    current = word
                } else current = test
            }
            if (current.isNotEmpty()) lines.add(current)
        }
        return lines
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

    private fun drawBackground(g: Graphics2D) {
        g.color = if (editMode) Color.BLACK else Color.LIGHT_GRAY
        g.fillRect(0, 0, Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
        if (!editMode) return
        val oldColor = g.color
        val oldStroke = g.stroke
        g.color = Color(80, 80, 80)
        g.stroke = BasicStroke(1f)
        for (x in 0..Consts.CENTER_PANEL_WIDTH step 16) g.drawLine(x, 0, x, Consts.CENTER_PANEL_HEIGHT)
        for (y in 0..Consts.CENTER_PANEL_HEIGHT step 16) g.drawLine(0, y, Consts.CENTER_PANEL_WIDTH, y)
        g.color = oldColor
        g.stroke = oldStroke
    }

    fun getParent(parentHash: Int): ComponentDefinition? {
        if (parentHash == -1) return null

        val interfaceId = parentHash shr 16
        val baseHash = interfaceId shl 16
        val componentId = parentHash - baseHash
        return ComponentDefinition.getInterfaceComponent(interfaceId, componentId)
    }

    fun setValues(component: ComponentDefinition?) {
        if (component == null) return

        val parent = getParent(component.parentId)
        val width: Int
        val height: Int

        if (parent == null) {
            width = Consts.CENTER_PANEL_WIDTH
            height = Consts.CENTER_PANEL_HEIGHT
        } else {
            setValues(parent)
            width = parent.width
            height = parent.height
        }

        realSize(component, width, height)
        realPosition(component, width, height)
    }
}
