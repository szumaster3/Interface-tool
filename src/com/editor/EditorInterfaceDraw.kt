package com.editor

import com.alex.loaders.items.ItemIcons
import com.alex.loaders.fonts.CacheFonts
import com.alex.loaders.fonts.PixelImage
import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentDefinition.Companion.getInterface
import com.alex.loaders.interfaces.ComponentSprites
import com.alex.loaders.interfaces.ComponentType
import com.alex.loaders.models.ModelLoader
import com.alex.loaders.models.ModelRenderer
import com.editor.util.sprites.SpriteLoader
import com.alex.utils.Utils
import com.editor.utils.*
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.math.min


object EditorInterfaceDraw {
    var editMode = false

    /** Fills the inventory components with preview items (the cache only stores the empty inventories). */
    var showItems = false

    /** Item ids used for the preview, a default selection of items is used when it is empty. */
    var previewItems: List<Int> = emptyList()
    private const val FIXED_SHIFT = 14
    private val DEFAULT_FONT = Font("Helvetica", Font.PLAIN, 11)
    const val HANDLE_SIZE = 8

    /** Distance from the border of a component in which it can be grabbed to resize it. */
    private const val GRAB_DISTANCE = 5

    /** Size of the area the interfaces are drawn in (see ScreenMode). */
    var screenWidth = Consts.CENTER_PANEL_WIDTH
        private set
    var screenHeight = Consts.CENTER_PANEL_HEIGHT
        private set

    /** Scroll position of the layers which have more content than they can show, by component hash. */
    private val scrollX = mutableMapOf<Int, Int>()
    private val scrollY = mutableMapOf<Int, Int>()
    private val TAG_REGEX = Regex("<[^>]+>")
    enum class ViewMode { NONE, BY_TYPE }
    var viewMode: ViewMode = ViewMode.NONE
    var selectedType: Int? = null
    val childrenCache = mutableMapOf<Int, List<ComponentDefinition>>()
    private val spriteCache = mutableMapOf<String, BufferedImage>()
    private val textCache = mutableMapOf<String, List<String>>()

    /** Drawn texts of the cache fonts, so a text is only drawn again when something about it changes. */
    private data class TextKey(
        val font: Int, val text: String, val width: Int, val height: Int, val color: Int,
        val shadow: Int, val halign: Int, val valign: Int, val spacing: Int
    )
    private val textImages = mutableMapOf<TextKey, BufferedImage>()
    private val fontMetricsCache = mutableMapOf<Font, FontMetrics>()
    private val modelRenderers = mutableMapOf<Long, ModelRenderer>()
    var preparedRoots: List<ComponentDefinition> = emptyList()
        private set

    fun prepareInterface(interfaceId: Int) {
        val allComponents = getInterface(interfaceId, false)?.filterNotNull() ?: return

        childrenCache.clear()
        spriteCache.clear()
        textCache.clear()
        textImages.clear()
        fontMetricsCache.clear()
        modelRenderers.clear()

        childrenCache.putAll(allComponents.groupBy { it.parentId })
        preparedRoots = childrenCache[-1] ?: emptyList()
        preparedRoots.forEach {
            layoutComponent(it, screenWidth, screenHeight)
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

        val rootClip = Rectangle(0, 0, screenWidth, screenHeight)
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

    /** True when the component matches the filter of the view. */
    fun matchesFilter(comp: ComponentDefinition): Boolean = matchesViewMode(comp.type, viewMode, selectedType)

    /** True when the component or one of the components inside of it matches the filter of the view. */
    fun containsMatch(comp: ComponentDefinition): Boolean =
        matchesFilter(comp) || childrenCache[comp.componentHash]?.any { containsMatch(it) } == true

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
        if (!intersects(x, y, w, h, 0, 0, screenWidth, screenHeight)) return

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
                ComponentType.INVENTORY,
                ComponentType.INVENTORY_TEXT -> drawInventory(g, comp, x, y)
            }
        }

        // Draw child ids: the children are always visited, a filter only decides which components are drawn
        val childX = x - scrollXOf(comp)
        val childY = y - scrollYOf(comp)
        childrenCache[comp.componentHash]?.forEach { child ->
            drawComponent(
                g,
                child,
                childX,
                childY,
                showContainers,
                showHidden,
                showModels,
                viewMode,
                selectedType,
                selectedComponent
            )
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
        if (selectedComponent == comp) {
            drawResize(g, x, y, w, h)
        }

        g.clip = oldClip
    }

    /** The root components which are shown: the ones which contain a component of the selected type. */
    fun getFilteredComponents(): List<ComponentDefinition> = preparedRoots.filter { containsMatch(it) }

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
        val oldComposite = g.composite
        val alpha = if (comp.hasAlpha) (255 - comp.transparency) else 255
        if (alpha < 255) {
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f)
        }

        if (comp.spriteTiling) {
            var sprite = SpriteLoader.getSprite(comp.spriteId) ?: return
            if (comp.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
            if (comp.vFlip) sprite = ImageUtils.verticalFlip(sprite)
            if (comp.borderThickness > 0) sprite = outline(sprite, comp.borderThickness)
            if (comp.shadowColor != 0) sprite = shadow(sprite, comp.shadowColor)

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
            val key = "${comp.spriteId}-${w}-${h}-${comp.hFlip}-${comp.vFlip}-${comp.transparency}-${comp.borderThickness}-${comp.shadowColor}"
            val sprite = spriteCache.getOrPut(key) {
                var img = SpriteLoader.getSprite(comp.spriteId) ?: return
                if (comp.hFlip) img = ImageUtils.horizontalFlip(img)
                if (comp.vFlip) img = ImageUtils.verticalFlip(img)
                if (comp.borderThickness > 0) img = outline(img, comp.borderThickness)
                if (comp.shadowColor != 0) img = shadow(img, comp.shadowColor)
                ImageUtils.resize(img, w, h)
            }
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
        if (drawModel && w > 0 && h > 0 && w <= 4096 && h <= 4096) {
            val model = ModelLoader.getModel(comp.modelId)
            if (model != null) {
                val key = (w.toLong() shl 32) or h.toLong()
                if (modelRenderers.size > 64) modelRenderers.clear()
                val renderer = modelRenderers.getOrPut(key) { ModelRenderer(w, h) }
                renderer.clear(0)
                // client: the origin offsets are scaled by (width << 8) / modelObjWidth when that value is set
                val scaleX = if (comp.modelObjWidth > 0) (w shl 8) / comp.modelObjWidth else 256
                val scaleY = if (comp.modelObjHeight > 0) (h shl 8) / comp.modelObjHeight else 256
                renderer.render(
                    model,
                    w / 2, h / 2,
                    comp.modelXAngle, comp.modelYAngle, comp.modelZAngle,
                    comp.modelZoom,
                    scaleX * comp.modelOriginX shr 8, scaleY * comp.modelOriginY shr 8,
                    comp.modelOrtho
                )
                // the image has the size of the component, so the model is clipped to it like in the client
                g.drawImage(renderer.image, x, y, null)
            }
        }

        g.color = Consts.MODEL_COLOR
        g.drawRect(x, y, w - 1, h - 1)
    }

    /**
     * Inventories (IF1 types 2 and 7). The client fills them with the items of the player; the cache only has the
     * slot layout and, for inventories, a sprite which is drawn in a slot while it is empty. With [showItems] the
     * slots are filled with the preview items (the icon for the inventory, the name for the inventory text).
     */
    private fun drawInventory(g: Graphics2D, comp: ComponentDefinition, x: Int, y: Int) {
        val isText = comp.type == ComponentType.INVENTORY_TEXT
        val slotW = if (isText) 115 else 32
        val slotH = if (isText) 12 else 32
        val cols = comp.baseWidth.coerceIn(0, 64)
        val rows = comp.baseHeight.coerceIn(0, 64)
        val items = if (showItems) previewItems.ifEmpty { ItemIcons.sampleItems() } else emptyList()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val slot = col + row * cols
                val sx = x + col * (slotW + comp.invMarginX)
                val sy = y + row * (slotH + comp.invMarginY)

                if (items.isNotEmpty()) {
                    val itemId = items[slot % items.size]
                    if (isText) {
                        drawText(g, comp, sx, sy, slotW, slotH, ItemIcons.name(itemId))
                    } else {
                        ItemIcons.icon(itemId)?.let { g.drawImage(it, sx, sy, null) }
                    }
                } else if (!isText && slot < 20 && comp.invSlotPresent[slot]) {
                    // sprite of an empty slot
                    SpriteLoader.getSprite(comp.invSprite?.get(slot) ?: -1)?.let {
                        g.drawImage(it, sx + (comp.invOffsetX?.get(slot) ?: 0), sy + (comp.invOffsetY?.get(slot) ?: 0), null)
                    }
                }

                if (editMode) {
                    g.color = Color(255, 255, 255, 90)
                    g.drawRect(sx, sy, slotW - 1, slotH - 1)
                }
            }
        }
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
        h: Int,
        text: String? = comp.text
    ) {
        val rawText = text ?: return
        if (rawText.isEmpty()) return

        // the font of the cache, drawn the way the client does it
        if (drawCacheText(g, comp, rawText, x, y, w, h)) return

        // no font in the cache (a cache without fonts or an invalid font id): a font of the system
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

    /** @return false when the cache has no font for the component. */
    private fun drawCacheText(g: Graphics2D, comp: ComponentDefinition, text: String, x: Int, y: Int, w: Int, h: Int): Boolean {
        val font = CacheFonts.get(comp.fontId) ?: return false
        if (w <= 0 || h <= 0 || w > 4096 || h > 4096) return true

        val color = comp.color and 0xFFFFFF
        val shadow = if (comp.shadow) 0 else -1
        val key = TextKey(font.id, text, w, h, color, shadow, comp.textHorizontalAli, comp.textVerticalAli, comp.textLineHeight)
        if (textImages.size > 500) textImages.clear()
        val image = textImages.getOrPut(key) {
            val canvas = PixelImage(w, h)
            font.drawParagraph(
                canvas, text, 0, 0, w, h, color, shadow,
                comp.textHorizontalAli, comp.textVerticalAli, comp.textLineHeight
            )
            BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB).apply { setRGB(0, 0, w, h, canvas.pixels, 0, w) }
        }
        // the image has the size of the component, so the text is clipped to it like in the client
        g.drawImage(image, x, y, null)
        return true
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
        /*
         * size modes of the client (InterfaceList):
         * 0 absolute,
         * 1 parent minus base,
         * 2 fraction of the parent,
         * 3 inventory (computed from slots),
         * 4 aspect ratio (needs values set by scripts, falls back to base)
         */
        c.width =
            when (c.widthMode.toInt()) {
                0 -> c.baseWidth
                1 -> pw - c.baseWidth
                2 -> pw * c.baseWidth shr FIXED_SHIFT
                3 -> when (c.type) {
                    ComponentType.INVENTORY -> c.baseWidth * 32 + (c.baseWidth - 1) * c.invMarginX
                    ComponentType.INVENTORY_TEXT -> c.baseWidth * 115 + c.invMarginX * (c.baseWidth - 1)
                    else -> c.baseWidth
                }
                else -> c.baseWidth
            }
        c.height =
            when (c.heightMode.toInt()) {
                0 -> c.baseHeight
                1 -> ph - c.baseHeight
                2 -> ph * c.baseHeight shr FIXED_SHIFT
                3 -> when (c.type) {
                    ComponentType.INVENTORY -> (c.baseHeight - 1) * c.invMarginY + c.baseHeight * 32
                    ComponentType.INVENTORY_TEXT -> c.baseHeight * 12 + (c.baseHeight - 1) * c.invMarginY
                    else -> c.baseHeight
                }
                else -> c.baseHeight
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
        val width = g.clipBounds?.width ?: screenWidth
        val height = g.clipBounds?.height ?: screenHeight

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
        textImages.clear()
        CacheFonts.clear()
    }

    private fun drawResize(g: Graphics2D, x: Int, y: Int, w: Int, h: Int) {
        g.color = Color.WHITE
        g.stroke = BasicStroke(1f)

        val drawSize = HANDLE_SIZE
        val half = drawSize / 2

        fun handle(cx: Int, cy: Int) {
            g.fillRect(cx - half, cy - half, drawSize, drawSize)
            g.color = Color.BLACK
            g.drawRect(cx - half, cy - half, drawSize, drawSize)
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

    // ------------------------------------------------------------------ scrolling

    /** How far the content of a layer can be scrolled. */
    private fun maxScrollX(comp: ComponentDefinition) = if (comp.type == ComponentType.LAYER) maxOf(0, comp.scrollWidth - comp.width) else 0
    private fun maxScrollY(comp: ComponentDefinition) = if (comp.type == ComponentType.LAYER) maxOf(0, comp.scrollHeight - comp.height) else 0

    fun scrollXOf(comp: ComponentDefinition): Int = (scrollX[comp.componentHash] ?: 0).coerceIn(0, maxScrollX(comp))
    fun scrollYOf(comp: ComponentDefinition): Int = (scrollY[comp.componentHash] ?: 0).coerceIn(0, maxScrollY(comp))

    /**
     * Scrolls the innermost layer at the position which has something to scroll, like the mouse wheel does it in
     * the client.
     *
     * @return true when a layer was scrolled
     */
    fun scrollAt(mouseX: Int, mouseY: Int, amount: Int, horizontal: Boolean): Boolean {
        fun search(comp: ComponentDefinition, x: Int, y: Int): ComponentDefinition? {
            if (mouseX !in x until x + comp.width || mouseY !in y until y + comp.height) return null
            val childX = x - scrollXOf(comp)
            val childY = y - scrollYOf(comp)
            childrenCache[comp.componentHash]?.asReversed()?.forEach { child ->
                search(child, childX + child.positionX, childY + child.positionY)?.let { return it }
            }
            val canScroll = if (horizontal) maxScrollX(comp) > 0 else maxScrollY(comp) > 0
            return if (canScroll) comp else null
        }

        val layer = preparedRoots.asReversed().firstNotNullOfOrNull { search(it, it.positionX, it.positionY) } ?: return false
        if (horizontal) {
            scrollX[layer.componentHash] = (scrollXOf(layer) + amount).coerceIn(0, maxScrollX(layer))
        } else {
            scrollY[layer.componentHash] = (scrollYOf(layer) + amount).coerceIn(0, maxScrollY(layer))
        }
        return true
    }

    // ------------------------------------------------------------------ position and size

    /** Where a component is in the drawn interface and the area of its parent, both in pixels of the panel. */
    class Placement(val bounds: Rectangle, val parent: Rectangle)

    fun placementOf(target: ComponentDefinition): Placement? {
        fun search(comp: ComponentDefinition, parentArea: Rectangle, x: Int, y: Int): Placement? {
            val area = Rectangle(x, y, comp.width, comp.height)
            if (comp.componentHash == target.componentHash) return Placement(area, parentArea)
            val childX = x - scrollXOf(comp)
            val childY = y - scrollYOf(comp)
            childrenCache[comp.componentHash]?.forEach { child ->
                search(child, area, childX + child.positionX, childY + child.positionY)?.let { return it }
            }
            return null
        }

        val screen = Rectangle(0, 0, screenWidth, screenHeight)
        return preparedRoots.firstNotNullOfOrNull { search(it, screen, it.positionX, it.positionY) }
    }

    /**
     * Moves and resizes a component. [x], [y], [w] and [h] are relative to the parent; they are turned into the
     * values which are stored in the component, which depend on the position and size modes of the component.
     */
    fun setBounds(comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int, parentW: Int, parentH: Int) {
        // a component which is not resized keeps its size, also when it has none
        val width = if (w == comp.width) w else maxOf(1, w)
        val height = if (h == comp.height) h else maxOf(1, h)

        fun fraction(value: Int, parent: Int) =
            if (parent > 0) Math.round(value * (1 shl FIXED_SHIFT).toDouble() / parent).toInt() else value

        // a value which does not change is left alone, so it does not lose precision (the sizes and positions
        // in the modes 2 to 5 are stored as fractions of the parent)
        val widthChanged = width != comp.width
        val heightChanged = height != comp.height
        val xChanged = widthChanged || x != comp.positionX
        val yChanged = heightChanged || y != comp.positionY

        if (widthChanged) {
            comp.baseWidth = when (comp.widthMode.toInt()) {
                0 -> width
                1 -> parentW - width
                2 -> fraction(width, parentW)
                else -> comp.baseWidth
            }
        }
        if (heightChanged) {
            comp.baseHeight = when (comp.heightMode.toInt()) {
                0 -> height
                1 -> parentH - height
                2 -> fraction(height, parentH)
                else -> comp.baseHeight
            }
        }
        // the width and the height which the layout will calculate
        val realW = if (comp.widthMode.toInt() >= 3) comp.width else width
        val realH = if (comp.heightMode.toInt() >= 3) comp.height else height

        if (xChanged) {
            comp.baseX = when (comp.yMode.toInt()) {
                1 -> x - (parentW - realW) / 2
                2 -> parentW - realW - x
                3 -> fraction(x, parentW)
                4 -> fraction(x - (parentW - realW) / 2, parentW)
                5 -> fraction(parentW - realW - x, parentW)
                else -> x
            }
        }
        if (yChanged) {
            comp.baseY = when (comp.xMode.toInt()) {
                1 -> y - (parentH - realH) / 2
                2 -> parentH - realH - y
                3 -> fraction(y, parentH)
                4 -> fraction(y - (parentH - realH) / 2, parentH)
                5 -> fraction(parentH - realH - y, parentH)
                else -> y
            }
        }
    }

    /** Sets the size of the area the interfaces are drawn in and lays the interface out again. */
    fun setScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        preparedRoots.forEach { layoutComponent(it, screenWidth, screenHeight) }
    }

    // ------------------------------------------------------------------ resize handles

    /**
     * The handle of the rectangle at the mouse position: the corners and the whole borders can be grabbed, a few
     * pixels outside of the component as well as inside of it.
     */
    fun handleAt(mouseX: Int, mouseY: Int, area: Rectangle): ComponentResizeType? {
        val right = area.x + area.width
        val bottom = area.y + area.height
        val grab = GRAB_DISTANCE

        // on a small component the corners must not take the whole border
        val cornerX = minOf(HANDLE_SIZE + 2, maxOf(grab - 1, area.width / 3))
        val cornerY = minOf(HANDLE_SIZE + 2, maxOf(grab - 1, area.height / 3))

        fun near(value: Int, target: Int, distance: Int) = value in (target - distance)..(target + distance)

        val onLeftCorner = near(mouseX, area.x, cornerX)
        val onRightCorner = near(mouseX, right, cornerX)
        val onTopCorner = near(mouseY, area.y, cornerY)
        val onBottomCorner = near(mouseY, bottom, cornerY)
        when {
            onLeftCorner && onTopCorner -> return ComponentResizeType.TOP_LEFT
            onRightCorner && onTopCorner -> return ComponentResizeType.TOP_RIGHT
            onLeftCorner && onBottomCorner -> return ComponentResizeType.BOTTOM_LEFT
            onRightCorner && onBottomCorner -> return ComponentResizeType.BOTTOM_RIGHT
        }

        val insideX = mouseX in (area.x - grab)..(right + grab)
        val insideY = mouseY in (area.y - grab)..(bottom + grab)
        return when {
            insideY && near(mouseX, area.x, grab) -> ComponentResizeType.LEFT
            insideY && near(mouseX, right, grab) -> ComponentResizeType.RIGHT
            insideX && near(mouseY, area.y, grab) -> ComponentResizeType.TOP
            insideX && near(mouseY, bottom, grab) -> ComponentResizeType.BOTTOM
            else -> null
        }
    }

}