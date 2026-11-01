package com.alex.loaders.fonts

/**
 * A font of the cache. The decoding, the measuring, the line breaking and the drawing follow the client
 * (Font and SoftwareFont): a font is made of the metrics (index 13) and of 256 glyph sprites (index 8).
 *
 * Text is written in the 8 bit character set of the cache, one char of a string is one glyph, and it may contain
 * the tags `<col=rrggbb>`, `<shad=rrggbb>`, `<trans=n>`, `<str>`, `<u>`, `<br>`, `<img=n>` and the closing tags
 * `</col>`, `</shad>`, `</trans>`, `</str>` and `</u>`, and the entities `<lt>`, `<gt>`, `<nbsp>`, `<shy>`,
 * `<times>`, `<euro>`, `<copy>` and `<reg>`.
 */
class CacheFont(
    val id: Int,
    metrics: ByteArray,
    glyphs: List<SpriteFrame?>,
    private val nameIcons: List<SpriteFrame?>? = null
) {
    val glyphWidths = IntArray(256)

    /**
     * Distance between two lines of text.
     */
    var lineHeight = 0
        private set

    /**
     * Pairs of glyphs which are moved closer to each other, null when the font has no kerning.
     */
    private var kerning: ByteArray? = null

    private val offsetsX = IntArray(256)
    private val offsetsY = IntArray(256)
    private val widths = IntArray(256)
    private val heights = IntArray(256)
    private val pixels = arrayOfNulls<ByteArray>(256)

    private var paragraphTop = 0
    private var paragraphBottom = 0

    init {
        decodeMetrics(metrics)
        for (c in 0 until minOf(256, glyphs.size)) {
            val glyph = glyphs[c] ?: continue
            offsetsX[c] = glyph.offsetX
            offsetsY[c] = glyph.offsetY
            widths[c] = glyph.width
            heights[c] = glyph.height
            pixels[c] = glyph.pixels
        }
        var top = Int.MAX_VALUE
        var bottom = Int.MIN_VALUE
        for (c in 0 until 256) {
            if (offsetsY[c] < top && heights[c] != 0) top = offsetsY[c]
            if (offsetsY[c] + heights[c] > bottom) bottom = offsetsY[c] + heights[c]
        }
        paragraphTop = lineHeight - top
        paragraphBottom = bottom - lineHeight
    }

    private fun decodeMetrics(data: ByteArray) {
        var pos = 0
        for (c in 0 until 256) glyphWidths[c] = data[pos++].toInt() and 0xFF

        if (data.size == 257) {
            lineHeight = data[256].toInt() and 0xFF
            return
        }

        val glyphHeights = IntArray(256) { data[pos++].toInt() and 0xFF }
        val glyphTops = IntArray(256) { data[pos++].toInt() and 0xFF }

        // for every row of a glyph: the distance of its left and of its right edge to the border of the glyph
        val leftEdges = Array(256) { ByteArray(glyphHeights[it]) }
        val rightEdges = Array(256) { ByteArray(glyphHeights[it]) }
        for (edges in arrayOf(leftEdges, rightEdges)) {
            for (c in 0 until 256) {
                var value = 0
                for (row in edges[c].indices) {
                    value += data[pos++].toInt()
                    edges[c][row] = value.toByte()
                }
            }
        }

        val table = ByteArray(65536)
        for (a in 0 until 256) {
            if (a == 32 || a == 160) continue
            for (b in 0 until 256) {
                if (b == 32 || b == 160) continue
                table[(a shl 8) + b] = kerningOf(leftEdges, rightEdges, glyphTops, glyphHeights, a, b).toByte()
            }
        }
        kerning = table
        lineHeight = glyphTops[32] + glyphHeights[32]
    }

    private fun kerningOf(
        left: Array<ByteArray>, right: Array<ByteArray>, tops: IntArray, heights: IntArray, a: Int, b: Int
    ): Int {
        val topA = tops[a]
        val topB = tops[b]
        val start = maxOf(topA, topB)
        val end = minOf(topA + heights[a], topB + heights[b])
        var smallest = minOf(glyphWidths[a], glyphWidths[b])
        for (row in start until end) {
            val gap = right[a][row - topA] + left[b][row - topB]
            if (gap < smallest) smallest = gap
        }
        return -smallest
    }

    // Measuring

    private class Tag(val name: String, val glyph: Int)

    private fun glyphOf(tag: String): Int = when (tag) {
        "lt" -> 60
        "gt" -> 62
        "nbsp" -> 160
        "shy" -> 173
        "times" -> 215
        "euro" -> 128
        "copy" -> 169
        "reg" -> 174
        else -> -1
    }

    /**
     * One char of the text as a glyph index, the chars outside
     * of the character set become a question mark.
     */
    private fun charOf(text: String, index: Int): Int {
        val c = text[index].code
        return if (c > 255) 63 else c
    }

    private fun kern(previous: Int, glyph: Int): Int {
        val table = kerning ?: return 0
        return if (previous != 0) table[(previous shl 8) + glyph].toInt() else 0
    }

    private fun icon(tag: String): SpriteFrame? =
        tag.substring(4).toIntOrNull()?.let { nameIcons?.getOrNull(it) }

    /**
     * Width of a line of text, the tags are not counted.
     */
    fun stringWidth(text: String?): Int {
        if (text == null) return 0
        var tagStart = -1
        var previous = 0
        var width = 0
        for (i in text.indices) {
            var c = charOf(text, i)
            if (c == 60) {
                tagStart = i
                continue
            }
            if (c == 62 && tagStart != -1) {
                val tag = text.substring(tagStart + 1, i)
                tagStart = -1
                val glyph = glyphOf(tag)
                if (glyph >= 0) {
                    c = glyph
                } else {
                    if (tag.startsWith("img=")) {
                        icon(tag)?.let {
                            width += it.width
                            previous = 0
                        }
                    }
                    continue
                }
            }
            if (tagStart == -1) {
                width += glyphWidths[c] + kern(previous, c)
                previous = c
            }
        }
        return width
    }

    /**
     * Breaks the text into lines which are not wider than [widths] (the last width is used for the following
     * lines), at spaces and after hyphens. The tags stay in the lines, `<br>` starts a new line.
     * Without widths the text is only broken at `<br>`.
     */
    fun splitParagraph(text: String?, widths: IntArray?): List<String> {
        if (text == null) return emptyList()
        val buffer = StringBuilder()
        val lines = ArrayList<String>()
        var lineStart = 0
        var lineWidth = 0
        var breakPosition = -1
        var breakWidth = 0
        var breakDrop = 0
        var tagStart = -1
        var previous = 0

        for (i in text.indices) {
            var c = charOf(text, i)
            if (c == 60) {
                tagStart = i
                continue
            }
            if (c == 62 && tagStart != -1) {
                val tag = text.substring(tagStart + 1, i)
                tagStart = -1
                buffer.append('<').append(tag).append('>')
                val glyph = glyphOf(tag)
                when {
                    tag == "br" -> {
                        lines.add(buffer.substring(lineStart))
                        lineStart = buffer.length
                        lineWidth = 0
                        breakPosition = -1
                        previous = 0
                    }
                    glyph >= 0 -> {
                        lineWidth += glyphWidths[glyph] + kern(previous, glyph)
                        previous = glyph
                    }
                    tag.startsWith("img=") -> icon(tag)?.let {
                        lineWidth += it.width
                        previous = 0
                    }
                }
                c = 0
            }
            if (tagStart == -1) {
                if (c != 0) {
                    buffer.append(c.toChar())
                    lineWidth += glyphWidths[c] + kern(previous, c)
                    previous = c
                }
                if (c == 32) {
                    breakPosition = buffer.length
                    breakWidth = lineWidth
                    breakDrop = 1
                }
                if (widths != null && breakPosition >= 0 &&
                    lineWidth > widths[minOf(lines.size, widths.size - 1)]
                ) {
                    lines.add(buffer.substring(lineStart, breakPosition - breakDrop))
                    lineStart = breakPosition
                    breakPosition = -1
                    lineWidth -= breakWidth
                    previous = 0
                }
                if (c == 45) {
                    breakPosition = buffer.length
                    breakWidth = lineWidth
                    breakDrop = 0
                }
            }
        }
        if (buffer.length > lineStart) lines.add(buffer.substring(lineStart))
        return lines
    }

    /**
     * Width of the widest line when the text is broken like [drawParagraph] does it.
     */
    fun maxLineWidth(text: String?, width: Int): Int =
        splitParagraph(text, intArrayOf(width)).maxOfOrNull { stringWidth(it) } ?: 0

    // Drawing

    /**
     * The colours and the effects which are set by the tags while a text is drawn.
     */
    private class Style(val color: Int, val shadow: Int) {
        var colorNow = color
        var shadowNow = shadow
        var alphaNow = 256
        var strike = -1
        var underline = -1
        var spaceWidth = 0
        var extraSpace = 0

        fun reset() {
            colorNow = color
            shadowNow = shadow
            alphaNow = 256
            strike = -1
            underline = -1
            spaceWidth = 0
            extraSpace = 0
        }
    }

    private fun apply(tag: String, style: Style) {
        try {
            when {
                tag.startsWith("col=") -> style.colorNow = tag.substring(4).toInt(16)
                tag == "/col" -> style.colorNow = style.color
                tag.startsWith("trans=") -> style.alphaNow = tag.substring(6).toInt()
                tag == "/trans" -> style.alphaNow = 256
                tag.startsWith("str=") -> style.strike = tag.substring(4).toInt(16)
                tag == "str" -> style.strike = 0x800000
                tag == "/str" -> style.strike = -1
                tag.startsWith("u=") -> style.underline = tag.substring(2).toInt(16)
                tag == "u" -> style.underline = 0
                tag == "/u" -> style.underline = -1
                tag.startsWith("shad=") -> style.shadowNow = tag.substring(5).toInt(16)
                tag == "shad" -> style.shadowNow = 0
                tag == "/shad" -> style.shadowNow = style.shadow
                tag == "br" -> style.reset()
            }
        } catch (e: NumberFormatException) {
            // the client ignores tags it cannot read
        }
    }

    /**
     * Writes a line to the image, [baseline] is the y of the bottom of the line.
     */
    private fun renderLine(image: PixelImage, line: String, startX: Int, baseline: Int, style: Style) {
        val top = baseline - lineHeight
        var x = startX
        var tagStart = -1
        var previous = 0

        for (i in line.indices) {
            var c = charOf(line, i)
            if (c == 60) {
                tagStart = i
                continue
            }
            if (c == 62 && tagStart != -1) {
                val tag = line.substring(tagStart + 1, i)
                tagStart = -1
                val glyph = glyphOf(tag)
                if (glyph >= 0) {
                    c = glyph
                } else {
                    if (tag.startsWith("img=")) {
                        icon(tag)?.let {
                            image.drawFrame(it, x, baseline - it.height, style.alphaNow)
                            x += it.width
                            previous = 0
                        }
                    } else {
                        apply(tag, style)
                    }
                    continue
                }
            }
            if (tagStart != -1) continue

            x += kern(previous, c)
            if (c == 32) {
                if (style.spaceWidth > 0) {
                    style.extraSpace += style.spaceWidth
                    x += style.extraSpace shr 8
                    style.extraSpace = style.extraSpace and 0xFF
                }
            } else {
                if (style.shadowNow != -1) {
                    image.drawMask(pixels[c], widths[c], heights[c], x + offsetsX[c] + 1, top + offsetsY[c] + 1,
                        style.shadowNow, style.alphaNow)
                }
                image.drawMask(pixels[c], widths[c], heights[c], x + offsetsX[c], top + offsetsY[c],
                    style.colorNow, style.alphaNow)
            }

            val advance = glyphWidths[c]
            if (style.strike != -1) {
                image.horizontalLine(x, top + (lineHeight * 0.7).toInt(), advance, style.strike)
            }
            if (style.underline != -1) {
                image.horizontalLine(x, top + lineHeight + 1, advance, style.underline)
            }
            x += advance
            previous = c
        }
    }

    /**
     * Spreads the line over [width] by making the spaces wider.
     */
    private fun justify(line: String, width: Int, style: Style) {
        var spaces = 0
        var inTag = false
        for (ch in line) {
            when {
                ch == '<' -> inTag = true
                ch == '>' -> inTag = false
                !inTag && ch == ' ' -> spaces++
            }
        }
        if (spaces > 0) style.spaceWidth = ((width - stringWidth(line)) shl 8) / spaces
    }

    /**
     * Draws a text in the rectangle (x, y, w, h) of the image, like the client draws the text of a component.
     *
     * @param color   colour of the text (rgb)
     * @param shadow  colour of the shadow, -1 for no shadow
     * @param halign  0 left, 1 centre, 2 right, 3 justified
     * @param valign  0 top, 1 centre, 2 bottom, 3 spread over the height
     * @param spacing distance between the lines, 0 for the line height of the font
     * @return the number of lines
     */
    fun drawParagraph(
        image: PixelImage, text: String?, x: Int, y: Int, w: Int, h: Int,
        color: Int, shadow: Int, halign: Int, valign: Int, spacing: Int
    ): Int {
        if (text == null) return 0
        val style = Style(color, shadow)
        var step = if (spacing == 0) lineHeight else spacing

        // a box which fits a single line is not wrapped
        var limits: IntArray? = intArrayOf(w)
        if (h < paragraphTop + paragraphBottom + step && h < step + step) limits = null

        val lines = splitParagraph(text, limits)
        val count = lines.size
        val vertical = if (valign == 3 && count == 1) 1 else valign

        var baseline = when (vertical) {
            0 -> y + paragraphTop
            1 -> y + paragraphTop + (h - paragraphTop - paragraphBottom - (count - 1) * step) / 2
            2 -> y + h - paragraphBottom - (count - 1) * step
            else -> {
                val extra = maxOf(0, (h - paragraphTop - paragraphBottom - (count - 1) * step) / (count + 1))
                step += extra
                y + paragraphTop + extra
            }
        }

        for ((index, line) in lines.withIndex()) {
            when (halign) {
                0 -> renderLine(image, line, x, baseline, style)
                1 -> renderLine(image, line, x + (w - stringWidth(line)) / 2, baseline, style)
                2 -> renderLine(image, line, x + w - stringWidth(line), baseline, style)
                else -> if (index == count - 1) {
                    renderLine(image, line, x, baseline, style)
                } else {
                    justify(line, w, style)
                    renderLine(image, line, x, baseline, style)
                    style.spaceWidth = 0
                }
            }
            baseline += step
        }
        return count
    }
}
