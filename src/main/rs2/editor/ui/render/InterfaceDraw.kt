package rs2.editor.ui.render

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentDefinition.Companion.getInterface
import com.alex.loaders.interfaces.ComponentSprites
import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.mesh.Unknown4
import com.alex.loaders.mesh.Unknown3
import com.alex.loaders.sprites.util.SpriteLoader
import com.alex.utils.Utils
import rs2.editor.ui.Consts
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.selection.SelectionModel
import rs2.editor.ui.selection.SelectionSource
import rs2.editor.ui.utils.ImageUtils
import rs2.editor.ui.utils.InterfaceUtils
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.math.min


object InterfaceDraw {
    var editMode = false
    private const val FIXED_SHIFT = 14
    private val DEFAULT_FONT = Font("Helvetica", Font.PLAIN, 11)
    private const val HANDLE_SIZE = 6
    private val TAG_REGEX = Regex("<[^>]+>")
    enum class ViewMode { NONE, BY_TYPE }
    var viewMode: ViewMode = ViewMode.NONE
    var selectedType: Int? = null
    val childrenCache = mutableMapOf<Int, List<ComponentDefinition>>()
    private val spriteCache = mutableMapOf<String, BufferedImage>()
    private val textCache = mutableMapOf<String, List<String>>()
    private val fontMetricsCache = mutableMapOf<Font, FontMetrics>()
    private val modelRendererCache = mutableMapOf<Int, Unknown3>()
    var preparedRoots: List<ComponentDefinition> = emptyList()
        private set

    fun prepareInterface(interfaceId: Int) {
        val allComponents = getInterface(interfaceId, false)?.filterNotNull() ?: return

        childrenCache.clear()
        spriteCache.clear()
        textCache.clear()
        fontMetricsCache.clear()
        modelRendererCache.clear()

        childrenCache.putAll(allComponents.groupBy { it.parentId })
        preparedRoots = childrenCache[-1] ?: emptyList()
        preparedRoots.forEach {
            layoutComponent(it, Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
        }
    }

    fun layoutComponent(comp: ComponentDefinition, parentW: Int, parentH: Int) {
        realSize(comp, parentW, parentH)
        realPosition(comp, parentW, parentH)

        childrenCache[comp.componentHash]?.forEach {
            layoutComponent(it, comp.width, comp.height)
        }
    }

    fun drawPreparedInterface(
        g: Graphics2D,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        viewMode: ViewMode,
        selectedType: Int?,
        selectedComponent: ComponentDefinition?
    ) {
        drawBackground(g)

        val rootClip = Rectangle(
            0, 0,
            Consts.CENTER_PANEL_WIDTH,
            Consts.CENTER_PANEL_HEIGHT
        )
        g.clip = rootClip

        preparedRoots.forEach {
            drawComponent(
                g, it, 0, 0,
                showContainers, showHidden, showModels,
                viewMode, selectedType, selectedComponent
            )
        }
    }

    private fun matchesViewMode(type: Int, viewMode: ViewMode, selectedType: Int?): Boolean {
        return when (viewMode) {
            ViewMode.NONE -> true
            ViewMode.BY_TYPE -> selectedType == null || type == selectedType
        }
    }

    private fun intersects(
        x1: Int, y1: Int, w1: Int, h1: Int,
        x2: Int, y2: Int, w2: Int, h2: Int
    ): Boolean {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2
    }

    private fun drawComponent(
        g: Graphics2D,
        comp: ComponentDefinition,
        px: Int,
        py: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        viewMode: ViewMode,
        selectedType: Int?,
        selectedComponent: ComponentDefinition?
    ) {
        // Hidden comps
        val hidden = Utils.isHidden(comp)
        if (hidden && !showHidden) return

        val x = px + comp.positionX
        val y = py + comp.positionY
        val w = comp.width
        val h = comp.height

        if (w <= 0 || h <= 0) return

        // Culling
        if (!intersects(x, y, w, h, 0, 0, Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)) return

        val oldClip = g.clip

        // Layer clip
        if (comp.type == ComponentType.LAYER) {
            val layerRect = Rectangle(x, y, w, h)
            g.clip = if (oldClip != null) {
                oldClip.bounds.intersection(layerRect)
            } else {
                layerRect
            }
        }

        // Draw
        if (matchesViewMode(comp.type, viewMode, selectedType)) {
            when (comp.type) {
                ComponentType.LAYER  -> drawLayer(g, comp, x, y, w, h, showContainers)
                ComponentType.SPRITE -> drawSprite(g, comp, x, y, w, h)
                ComponentType.FIGURE -> drawFigure(g, comp, x, y, w, h)
                ComponentType.MODEL  -> drawModel(g, comp, x, y, w, h, showModels)
                ComponentType.TEXT   -> drawText(g, comp, x, y, w, h)
                ComponentType.LINE   -> drawLine(g, comp, x, y)
            }
        }

        // Draw child ids
        childrenCache[comp.componentHash]?.forEach { child ->
            if (matchesViewMode(child.type, viewMode, selectedType)) {
                drawComponent(
                    g,
                    child,
                    x,
                    y,
                    showContainers,
                    showHidden,
                    showModels,
                    viewMode,
                    selectedType,
                    selectedComponent
                )
            }
        }

        // Outlines
        InterfaceUtils.drawOutlines(
            g,
            comp,
            x,
            y,
            w,
            h,
            selectedComponent == comp
        )

        // Resize handles
        if (selectedComponent == comp && SelectionModel.source == SelectionSource.MOUSE) {
            drawResize(g, x, y, w, h)
        }

        g.clip = oldClip
    }

    fun getFilteredComponents(): List<ComponentDefinition> {
        return preparedRoots.filter { root ->
            viewMode == ViewMode.NONE || selectedType == null || root.type == selectedType
        }
    }

    @JvmStatic
    fun outline(src: BufferedImage, thickness: Int): BufferedImage {
        val w = src.width
        val h = src.height

        val result = BufferedImage(w + thickness * 2, h + thickness * 2, BufferedImage.TYPE_INT_ARGB)
        val g = result.createGraphics()

        for (x in -thickness..thickness) {
            for (y in -thickness..thickness) {
                if (x == 0 && y == 0) continue
                g.drawImage(src, x + thickness, y + thickness, null)
            }
        }

        g.drawImage(src, thickness, thickness, null)
        g.dispose()

        return result
    }

    @JvmStatic
    fun shadow(src: BufferedImage, shadowColor: Int): BufferedImage {
        val w = src.width
        val h = src.height

        val result = BufferedImage(w + 1, h + 1, BufferedImage.TYPE_INT_ARGB)
        val g = result.createGraphics()

        val color = Color(shadowColor, true)

        for (x in 0 until w) {
            for (y in 0 until h) {
                val pixel = src.getRGB(x, y)
                val alpha = pixel ushr 24 and 0xFF

                if (alpha > 0) {
                    val shadowPixel =
                        (alpha shl 24) or
                                (color.red shl 16) or
                                (color.green shl 8) or
                                color.blue

                    result.setRGB(x + 1, y + 1, shadowPixel)
                }
            }
        }

        g.drawImage(src, 0, 0, null)
        g.dispose()

        return result
    }

    private fun drawSprite(
        g: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        w: Int,
        h: Int
    ) {
        val key = "${comp.spriteId}-${w}-${h}-${comp.hFlip}-${comp.vFlip}-${comp.transparency}-${comp.borderThickness}-${comp.shadowColor}"

        val sprite = spriteCache.getOrPut(key) {
            var img = SpriteLoader.getSprite(comp.spriteId) ?: return

            if (comp.hFlip) img = ImageUtils.horizontalFlip(img)
            if (comp.vFlip) img = ImageUtils.verticalFlip(img)

            if (comp.borderThickness > 0) {
                img = outline(img, comp.borderThickness)
            }

            if (comp.shadowColor != 0) {
                img = shadow(img, comp.shadowColor)
            }

            ImageUtils.resize(img, w, h)
        }

        val oldComposite = g.composite
        val alpha = if (comp.hasAlpha) (255 - comp.transparency) else 255
        if (alpha < 255) {
            g.composite = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER,
                alpha / 255f
            )
        }

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
        } else {
            g.drawImage(sprite, x, y, null)
        }

        g.composite = oldComposite
    }

    private fun drawFigure(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        val old = g.composite
        val alpha = 255 - comp.transparency
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f)
        g.color = Color(comp.color)
        if (comp.filled) g.fillRect(x, y, w, h)
        else g.drawRect(x, y, w - 1, h - 1)
        g.composite = old
    }

    private fun drawLayer(
        g: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        showContainers: Boolean
    ) {
        val sprite = ComponentSprites.getSpriteForComponent(comp)
        if (sprite != null) {
            g.drawImage(sprite.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH), x, y, null)
            return
        }
        if (showContainers) {
            g.color = if (comp.parentId > 0) Color.RED else Color.GREEN
            g.drawRect(x, y, w - 1, h - 1)
        }
    }

    private fun drawModel(
        g: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        drawModel: Boolean
    ) {
        val drawWidth =
            if (comp.widthMode.toInt() != 0 && comp.modelObjWidth > 0)
                comp.modelObjWidth
            else w

        val drawHeight =
            if (comp.heightMode.toInt() != 0 && comp.modelObjHeight > 0)
                comp.modelObjHeight
            else h

        if (drawModel) {
            val model = Unknown4.getModel(comp.modelId)
            if (model != null) {
                val renderer = modelRendererCache.getOrPut(comp.modelId) {
                    Unknown3(drawWidth, drawHeight, InterfaceUI.instance?.renderPanel)
                }
                renderer.clear()
                renderer.updateModel(model, comp)
                renderer.updatePreview(x, y, g)
            }
        }

        g.color = Consts.MODEL_COLOR
        g.drawRect(x, y, drawWidth - 1, drawHeight - 1)
    }

    private fun drawLine(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int) {
        val oldStroke = g.stroke
        val oldColor = g.color

        g.color = Color(comp.color)
        g.stroke = BasicStroke(comp.lineWidth.toFloat())

        val endX = if (comp.unknownProp_8) x else x + comp.width
        val endY = if (!comp.unknownProp_8) y + comp.height else y

        g.drawLine(x, y, endX, endY)

        g.stroke = oldStroke
        g.color = oldColor
    }

    private fun drawText(
        g: Graphics2D,
        comp: ComponentDefinition,
        x: Int,
        y: Int,
        w: Int,
        h: Int
    ) {
        val rawText = comp.text ?: return
        if (rawText.isEmpty()) return

        g.font = DEFAULT_FONT
        val fm = fontMetricsCache.getOrPut(g.font) { g.getFontMetrics(g.font) }

        val cacheKey = "$rawText-$w-${comp.textLineHeight}"
        val lines = textCache.getOrPut(cacheKey) {
            wrapText(rawText, w, fm)
        }
        if (lines.isEmpty()) return

        val lineHeight = if (comp.textLineHeight > 0) {
            comp.textLineHeight
        } else {
            fm.height
        }

        val totalHeight = lines.size * lineHeight

        val verticalOffset = when (comp.textVerticalAli) {
            1 -> (h - totalHeight) / 2 // center
            2 -> h - totalHeight // bottom
            else -> 0 // top
        }

        val defaultColor = Color(comp.color)

        for ((index, line) in lines.withIndex()) {
            val cleanLine = line.replace(TAG_REGEX, "")
            val lineWidth = fm.stringWidth(cleanLine)

            val drawX = when (comp.textHorizontalAli) {
                1 -> x + (w - lineWidth) / 2
                2 -> x + w - lineWidth - 1
                else -> x
            }

            val drawY = y + verticalOffset + index * lineHeight + fm.ascent

            val fragments = parseNestedTags(line, defaultColor)
            var cursorX = drawX

            for (frag in fragments) {
                drawTextFragment(g, frag.text, cursorX, drawY, comp, frag.color, frag.underline, frag.underlineColor)
                cursorX += fm.stringWidth(frag.text.replace(TAG_REGEX, ""))
            }
        }
    }

    private fun drawTextFragment(
        g: Graphics2D,
        text: String,
        x: Int,
        y: Int,
        comp: ComponentDefinition,
        color: Color,
        underline: Boolean,
        underlineColor: Color?
    ) {
        if (text.isEmpty()) return
        val fm = g.fontMetrics
        val cleanText = text.replace(TAG_REGEX, "")

        // Shadow
        if (comp.shadow) {
            g.color = Color(comp.shadowColor)
            g.drawString(cleanText, x + 1, y + 1)
        }

        g.color = color
        g.drawString(cleanText, x, y)

        if (underline) {
            g.color = underlineColor ?: color
            g.drawLine(x, y + 1, x + fm.stringWidth(cleanText), y + 1)
        }
    }

    private data class TextFragment(
        val text: String,
        val color: Color,
        val underline: Boolean,
        val underlineColor: Color?
    )

    private fun parseNestedTags(line: String, defaultColor: Color): List<TextFragment> {
        val fragments = mutableListOf<TextFragment>()
        val tagRegex = Regex("</?(col|u)(?:=([0-9a-fA-F]{6}))?>")
        val stack = mutableListOf<Pair<Color, Color?>>() // Pair<textColor, underlineColor?>
        stack.add(Pair(defaultColor, null))

        var lastIndex = 0
        var underline = false

        for (match in tagRegex.findAll(line)) {
            if (match.range.first > lastIndex) {
                fragments.add(
                    TextFragment(
                    text = line.substring(lastIndex, match.range.first),
                    color = stack.last().first,
                    underline = underline,
                    underlineColor = stack.last().second
                )
                )
            }

            val tag = match.groupValues[1]
            val value = match.groupValues[2]

            if (match.value.startsWith("</")) {
                if (stack.size > 1) stack.removeAt(stack.size - 1)
                if (tag == "u") underline = stack.any { it.second != null || it.first == Color(0x6266FA) }
            } else {
                when (tag) {
                    "col" -> stack.add(Pair(Color(value.toInt(16)), stack.last().second))
                    "u" -> {
                        underline = true
                        val linkColor = Color(0x6266FA)
                        val underlineCol = Color(value.toInt(16))
                        stack.add(Pair(linkColor, underlineCol))
                    }
                }
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < line.length) {
            fragments.add(
                TextFragment(
                text = line.substring(lastIndex),
                color = stack.last().first,
                underline = underline,
                underlineColor = stack.last().second
            )
            )
        }

        return fragments
    }

    private fun wrapText(raw: String, width: Int, fm: FontMetrics): List<String> {
        val lines = mutableListOf<String>()

        for (part in raw.split("<br>")) {
            val clean = part.replace(TAG_REGEX, "")
            if (fm.stringWidth(clean) <= width) {
                lines.add(part)
                continue
            }

            var current = ""
            for (word in part.split(" ", "/")) {
                val test = if (current.isEmpty()) word else "$current $word"
                if (
                    fm.stringWidth(test.replace(TAG_REGEX, "")) > width &&
                    current.isNotEmpty()
                ) {
                    lines.add(current)
                    current = word
                } else {
                    current = test
                }
            }
            if (current.isNotEmpty())
                lines.add(current)
        }

        return lines
    }

    private fun realSize(c: ComponentDefinition, pw: Int, ph: Int) {
        c.width =
            when (c.widthMode.toInt()) {
                0 -> c.baseWidth
                1 -> pw - c.baseWidth
                2,
                4 -> pw * c.baseWidth shr FIXED_SHIFT
                else -> c.width
            }
        c.height =
            when (c.heightMode.toInt()) {
                0 -> c.baseHeight
                1 -> ph - c.baseHeight
                2,
                4 -> ph * c.baseHeight shr FIXED_SHIFT
                else -> c.height
            }
        if (c.modelObjWidth > 0) c.width = c.modelObjWidth
        if (c.modelObjHeight > 0) c.height = c.modelObjHeight
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
        val width = g.clipBounds?.width ?: Consts.CENTER_PANEL_WIDTH
        val height = g.clipBounds?.height ?: Consts.CENTER_PANEL_HEIGHT

        g.color = if (editMode) Color.BLACK else Color.LIGHT_GRAY
        g.fillRect(0, 0, width, height)

        if (!editMode) return

        val oldColor = g.color
        val oldStroke = g.stroke
        g.color = Color(80, 80, 80)
        g.stroke = BasicStroke(1f)
        for (x in 0..width step 16) g.drawLine(x, 0, x, height)
        for (y in 0..height step 16) g.drawLine(0, y, width, y)
        g.color = oldColor
        g.stroke = oldStroke
    }

    fun clearCache() {
        childrenCache.clear()
        spriteCache.clear()
        textCache.clear()
    }

    private fun drawResize(g: Graphics2D, x: Int, y: Int, w: Int, h: Int)
    {
        g.color = Color.WHITE
        g.stroke = BasicStroke(1f)
        val half = HANDLE_SIZE / 2

        fun handle(cx: Int, cy: Int)
        {
            g.fillRect(cx - half, cy - half, HANDLE_SIZE, HANDLE_SIZE)
            g.color = Color.BLACK
            g.drawRect(cx - half, cy - half, HANDLE_SIZE, HANDLE_SIZE)
            g.color = Color.WHITE
        }
        handle(x, y)
        handle(x + w / 2, y)
        handle(x + w, y)
        handle(x + w, y + h / 2)
        handle(x + w, y + h)
        handle(x + w / 2, y + h)
        handle(x, y + h)
        handle(x, y + h / 2)
    }

    fun getCursor(mouseX: Int, mouseY: Int, x: Int, y: Int, w: Int, h: Int): ResizeType?
    {

        val half = HANDLE_SIZE / 2

        fun click(cx: Int, cy: Int): Boolean = mouseX in
                (cx - half)..(cx + half) && mouseY in
                (cy - half)..(cy + half)

        return when
        {
            click(x, y) -> ResizeType.TOP_LEFT
            click(x + w / 2, y) -> ResizeType.TOP
            click(x + w, y) -> ResizeType.TOP_RIGHT
            click(x + w, y + h / 2) -> ResizeType.RIGHT
            click(x + w, y + h) -> ResizeType.BOTTOM_RIGHT
            click(x + w / 2, y + h) -> ResizeType.BOTTOM
            click(x, y + h) -> ResizeType.BOTTOM_LEFT
            click(x, y + h / 2) -> ResizeType.LEFT
            else -> null
        }
    }
}