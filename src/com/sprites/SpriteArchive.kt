/**
 * Copyright (c) OpenRS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.sprites

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Represents a [SpriteArchive] which may contain one or more frames.
 *
 * @author Graham
 * @author `Discardedx2
 */
class SpriteArchive @JvmOverloads constructor(width: Int, height: Int, size: Int = 1) {
    /**
     * Gets the width of this sprite.
     *
     * @return The width of this sprite.
     */
    /**
     * The width of this sprite.
     */
    val width: Int

    /**
     * Gets the height of this sprite.
     *
     * @return The height of this sprite.
     */
    /**
     * The height of this sprite.
     */
    val height: Int

    /**
     * The array of animation frames in this sprite.
     */
    private val frames: Array<BufferedImage?>

    /**
     * Creates a new sprite with the specified number of frames.
     *
     * @param width
     * The width of the sprite in pixels.
     * @param height
     * The height of the sprite in pixels.
     * @param size
     * The number of animation frames.
     */
    /**
     * Creates a new sprite with one frame.
     *
     * @param width
     * The width of the sprite in pixels.
     * @param height
     * The height of the sprite in pixels.
     */
    init {
        require(size >= 1)

        this.width = width
        this.height = height
        this.frames = arrayOfNulls(size)
    }

    /**
     * Encodes this [SpriteArchive] into a [ByteBuffer].
     *
     *
     * Please note that this is a fairly simple implementation which only
     * supports vertical encoding. It does not attempt to use the offsets to
     * save space.
     *
     * @return The buffer.
     * @throws IOException
     * if an I/O exception occurs.
     */
    @Throws(IOException::class)
    fun encode(): ByteBuffer {
        val bout = ByteArrayOutputStream()
        val os = DataOutputStream(bout)
        try {/* set up some variables */
            val palette: MutableList<Int> = ArrayList()
            palette.add(0) /* transparent colour */

            /* write the sprites */
            for (image in frames) {/* check if we can encode this */
                if (image!!.width != width || image.height != height) throw IOException("All frames must be the same size.")

                /* loop through all the pixels constructing a palette */
                var flags = FLAG_VERTICAL // TODO: do we need to support
                // horizontal encoding?
                for (x in 0 until width) {
                    for (y in 0 until height) {/* grab the colour of this pixel */
                        val argb = image.getRGB(x, y)
                        val alpha = (argb shr 24) and 0xFF
                        var rgb = argb and 0xFFFFFF
                        if (rgb == 0) rgb = 1

                        /* we need an alpha channel to encode this image */
                        if (alpha != 0 && alpha != 255) flags = flags or FLAG_ALPHA

                        /*
						 * add the colour to the palette if it isn't already in
						 * the palette
						 */
                        if (!palette.contains(rgb)) {
                            if (palette.size >= 256) throw IOException("Too many colours in this sprite!")
                            palette.add(rgb)
                        }
                    }
                }

                /* write this sprite */
                os.write(flags)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val argb = image.getRGB(x, y)
                        val alpha = (argb shr 24) and 0xFF
                        var rgb = argb and 0xFFFFFF
                        if (rgb == 0) rgb = 1

                        if ((flags and FLAG_ALPHA) == 0 && alpha == 0) {
                            os.write(0)
                        } else {
                            os.write(palette.indexOf(rgb))
                        }
                    }
                }

                /* write the alpha channel if this sprite has one */
                if ((flags and FLAG_ALPHA) != 0) {
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val argb = image.getRGB(x, y)
                            val alpha = (argb shr 24) and 0xFF
                            os.write(alpha)
                        }
                    }
                }
            }

            /* write the palette */
            for (i in 1 until palette.size) {
                val rgb = palette[i]
                os.write((rgb shr 16).toByte().toInt())
                os.write((rgb shr 8).toByte().toInt())
                os.write(rgb.toByte().toInt())
            }

            /* write the max width, height and palette size */
            os.writeShort(width)
            os.writeShort(height)
            os.write(palette.size - 1)

            /* write the individual offsets and dimensions */
            for (i in frames.indices) {
                os.writeShort(0) // offset X
                os.writeShort(0) // offset Y
                os.writeShort(width)
                os.writeShort(height)
            }

            /* write the number of frames */
            os.writeShort(frames.size)

            /* convert the stream to a byte array and then wrap a buffer */
            val bytes = bout.toByteArray()
            return ByteBuffer.wrap(bytes)
        } finally {
            os.close()
        }
    }

    /**
     * Gets the frame with the specified id.
     *
     * @param id
     * The id.
     * @return The frame.
     */
    fun getSprite(id: Int): BufferedImage? {
        return frames[id]
    }

    /**
     * Sets the frame with the specified id.
     *
     * @param id
     * The id.
     * @param frame
     * The frame.
     */
    fun setFrame(id: Int, frame: BufferedImage) {
        require(!(frame.width != width || frame.height != height)) { "The frame's dimensions do not match with the sprite's dimensions." }

        frames[id] = frame
    }

    /**
     * Gets the number of frames in this set.
     *
     * @return The number of frames.
     */
    fun size(): Int {
        return frames.size
    }

    companion object {
        /**
         * This flag indicates that the pixels should be read vertically instead of
         * horizontally.
         */
        const val FLAG_VERTICAL: Int = 0x01

        /**
         * This flag indicates that every pixel has an alpha, as well as red, green
         * and blue, component.
         */
        const val FLAG_ALPHA: Int = 0x02

        /**
         * Decodes the [SpriteArchive] from the specified [ByteBuffer].
         *
         * @param buffer
         * The buffer.
         * @return The sprite.
         */
        @JvmStatic
        fun decode(buffer: ByteBuffer): SpriteArchive? {
            try {/* find the size of this sprite set */
                buffer.position(buffer.limit() - 2)
                val size = buffer.getShort().toInt() and 0xFFFF

                /* allocate arrays to store info */
                val offsetsX = IntArray(size)
                val offsetsY = IntArray(size)
                val subWidths = IntArray(size)
                val subHeights = IntArray(size)

                /* read the width, height and palette size */
                buffer.position(buffer.limit() - size * 8 - 7)
                val width = buffer.getShort().toInt() and 0xFFFF
                val height = buffer.getShort().toInt() and 0xFFFF
                val palette = IntArray((buffer.get().toInt() and 0xFF) + 1)

                /* and allocate an object for this sprite set */
                val set = SpriteArchive(width, height, size)

                /* read the offsets and dimensions of the individual sprites */
                for (i in 0 until size) {
                    offsetsX[i] = buffer.getShort().toInt() and 0xFFFF
                }
                for (i in 0 until size) {
                    offsetsY[i] = buffer.getShort().toInt() and 0xFFFF
                }
                for (i in 0 until size) {
                    subWidths[i] = buffer.getShort().toInt() and 0xFFFF
                }
                for (i in 0 until size) {
                    subHeights[i] = buffer.getShort().toInt() and 0xFFFF
                }

                /* read the palette */
                buffer.position(buffer.limit() - size * 8 - 7 - (palette.size - 1) * 3)
                palette[0] = 0 /* transparent colour (black) */
                for (index in 1 until palette.size) {
                    palette[index] = getMedium(buffer)
                    if (palette[index] == 0) palette[index] = 1
                }

                /* read the pixels themselves */
                buffer.position(0)
                for (id in 0 until size) {/* grab some frequently used values */
                    val subWidth = subWidths[id]
                    val subHeight = subHeights[id]
                    val offsetX = offsetsX[id]
                    val offsetY = offsetsY[id]
                    if (subWidth > 1000 || subHeight > 1000 || width > 1000 || height > 1000) //since it give jpc errors
                        continue/* create a BufferedImage to store the resulting image */
                    set.frames[id] = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                    val image = set.frames[id]/* allocate an array for the palette indices */
                    val indices = Array(subWidth) { IntArray(subHeight) }

                    /*
			 * read the flags so we know whether to read horizontally or
			 * vertically
			 */
                    val flags = buffer.get().toInt() and 0xFF

                    /* now read the image */
                    if (image != null) {/* read the palette indices */
                        if ((flags and FLAG_VERTICAL) != 0) {
                            for (x in 0 until subWidth) {
                                for (y in 0 until subHeight) {
                                    indices[x][y] = buffer.get().toInt() and 0xFF
                                }
                            }
                        } else {
                            for (y in 0 until subHeight) {
                                for (x in 0 until subWidth) {
                                    try {
                                        indices[x][y] = buffer.get().toInt() and 0xFF
                                    } catch (ex: Exception) {
                                    }
                                }
                            }
                        }

                        /*
				 * read the alpha (if there is alpha) and convert values to ARGB
				 */
                        if ((flags and FLAG_ALPHA) != 0) {
                            if ((flags and FLAG_VERTICAL) != 0) {
                                for (x in 0 until subWidth) {
                                    for (y in 0 until subHeight) {
                                        val alpha = buffer.get().toInt() and 0xFF
                                        image.setRGB(x + offsetX, y + offsetY, alpha shl 24 or palette[indices[x][y]])
                                    }
                                }
                            } else {
                                for (y in 0 until subHeight) {
                                    for (x in 0 until subWidth) {
                                        val alpha = buffer.get().toInt() and 0xFF
                                        try {
                                            image.setRGB(
                                                x + offsetX, y + offsetY, alpha shl 24 or palette[indices[x][y]]
                                            )
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            }
                        } else {
                            for (x in 0 until subWidth) {
                                for (y in 0 until subHeight) {
                                    val index = indices[x][y]
                                    if (index == 0) {
                                        image.setRGB(x + offsetX, y + offsetY, 0)
                                    } else {
                                        image.setRGB(x + offsetX, y + offsetY, -0x1000000 or palette[index])
                                    }
                                }
                            }
                        }
                    }
                }
                return set
            } catch (ex: Exception) {
            }
            return null
        }

        fun getMedium(buf: ByteBuffer): Int {
            return ((buf.get().toInt() and 0xFF) shl 16) or ((buf.get().toInt() and 0xFF) shl 8) or (buf.get()
                .toInt() and 0xFF)
        }
    }
}
