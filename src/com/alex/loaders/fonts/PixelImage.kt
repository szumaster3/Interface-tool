package com.alex.loaders.fonts

/**
 * An ARGB image which the fonts draw to. The pixels outside of the image are not drawn.
 */
class PixelImage @JvmOverloads constructor(val width: Int, val height: Int, val pixels: IntArray = IntArray(width * height)) {

    private fun put(x: Int, y: Int, rgb: Int, alpha: Int) {
        if (x < 0 || y < 0 || x >= width || y >= height) return
        val index = y * width + x
        if (alpha >= 256) {
            pixels[index] = 0xFF000000.toInt() or rgb
            return
        }
        val below = pixels[index]
        val belowAlpha = below ushr 24
        pixels[index] = if (belowAlpha == 0) {
            ((alpha * 255) shr 8 shl 24) or (rgb and 0xFFFFFF)
        } else {
            val inverse = 256 - alpha
            val r = (((rgb shr 16) and 0xFF) * alpha + ((below shr 16) and 0xFF) * inverse) shr 8
            val g = (((rgb shr 8) and 0xFF) * alpha + ((below shr 8) and 0xFF) * inverse) shr 8
            val b = ((rgb and 0xFF) * alpha + (below and 0xFF) * inverse) shr 8
            0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
        }
    }

    /**
     * Draws a glyph: every pixel which is not 0 gets the colour.
     */
    fun drawMask(mask: ByteArray?, w: Int, h: Int, x: Int, y: Int, rgb: Int, alpha: Int) {
        if (mask == null) return
        for (row in 0 until h) {
            for (column in 0 until w) {
                if (mask[row * w + column].toInt() != 0) put(x + column, y + row, rgb, alpha)
            }
        }
    }

    /**
     * Draws a sprite frame with its own colours.
     */
    fun drawFrame(frame: SpriteFrame, x: Int, y: Int, alpha: Int) {
        for (row in 0 until frame.height) {
            for (column in 0 until frame.width) {
                val index = frame.pixels[row * frame.width + column].toInt() and 0xFF
                if (index == 0) continue
                val opacity = frame.alpha?.let { (it[row * frame.width + column].toInt() and 0xFF) * alpha / 255 } ?: alpha
                if (opacity > 0) put(x + frame.offsetX + column, y + frame.offsetY + row, frame.palette[index], opacity)
            }
        }
    }

    fun horizontalLine(x: Int, y: Int, length: Int, rgb: Int) {
        for (i in 0 until length) put(x + i, y, rgb, 256)
    }
}
