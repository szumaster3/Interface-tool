package com.alex.loaders.fonts

import java.nio.ByteBuffer

/**
 * One frame of a sprite archive as the client keeps it: the pixels of the inner rectangle only, together with
 * its offset inside the canvas of the sprite. The pixels are palette indices in rows, 0 is transparent.
 */
class SpriteFrame(
    val offsetX: Int,
    val offsetY: Int,
    val width: Int,
    val height: Int,
    val pixels: ByteArray,
    val palette: IntArray,
    val alpha: ByteArray?
)

object SpriteFrames {

    private const val FLAG_VERTICAL = 0x01
    private const val FLAG_ALPHA = 0x02

    /**
     * Decodes a sprite archive (see SpriteArchive),
     * the frames which cannot be decoded are null.
     */
    fun decode(data: ByteArray): List<SpriteFrame?>? {
        try {
            val buffer = ByteBuffer.wrap(data)
            buffer.position(buffer.limit() - 2)
            val size = buffer.short.toInt() and 0xFFFF

            buffer.position(buffer.limit() - size * 8 - 7)
            buffer.short // width of the canvas
            buffer.short // height of the canvas
            val palette = IntArray((buffer.get().toInt() and 0xFF) + 1)

            val offsetsX = IntArray(size) { buffer.short.toInt() and 0xFFFF }
            val offsetsY = IntArray(size) { buffer.short.toInt() and 0xFFFF }
            val widths = IntArray(size) { buffer.short.toInt() and 0xFFFF }
            val heights = IntArray(size) { buffer.short.toInt() and 0xFFFF }

            buffer.position(buffer.limit() - size * 8 - 7 - (palette.size - 1) * 3)
            for (i in 1 until palette.size) {
                val rgb = ((buffer.get().toInt() and 0xFF) shl 16) or
                        ((buffer.get().toInt() and 0xFF) shl 8) or
                        (buffer.get().toInt() and 0xFF)
                palette[i] = if (rgb == 0) 1 else rgb
            }

            buffer.position(0)
            val frames = ArrayList<SpriteFrame?>(size)
            for (id in 0 until size) {
                val w = widths[id]
                val h = heights[id]
                val flags = buffer.get().toInt() and 0xFF
                val vertical = (flags and FLAG_VERTICAL) != 0

                val pixels = readPlane(buffer, w, h, vertical)
                val alpha = if ((flags and FLAG_ALPHA) != 0) readPlane(buffer, w, h, vertical) else null
                frames.add(SpriteFrame(offsetsX[id], offsetsY[id], w, h, pixels, palette, alpha))
            }
            return frames
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Reads w * h bytes, stored in columns when [vertical] is set, and returns them in rows.
     */
    private fun readPlane(buffer: ByteBuffer, w: Int, h: Int, vertical: Boolean): ByteArray {
        val plane = ByteArray(w * h)
        if (vertical) {
            for (x in 0 until w) {
                for (y in 0 until h) plane[y * w + x] = buffer.get()
            }
        } else {
            buffer.get(plane)
        }
        return plane
    }
}
