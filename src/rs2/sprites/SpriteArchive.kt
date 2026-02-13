package rs2.sprites

import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Represents a Sprite Archive.
 *
 * @author Graham
 * @author `Discardedx2
 */
class SpriteArchive
    @JvmOverloads
    constructor(
        width: Int,
        height: Int,
        size: Int = 1,
    ) {
        val width: Int

        val height: Int

        private val frames: Array<BufferedImage?>

        init {
            require(size >= 1)

            this.width = width
            this.height = height
            this.frames = arrayOfNulls(size)
        }

        @Throws(IOException::class)
        fun encode(): ByteBuffer {
            val bout = ByteArrayOutputStream()
            val os = DataOutputStream(bout)
            try {
                val palette: MutableList<Int> = ArrayList()
                palette.add(0)

                for (image in frames) {
                    if (image!!.width != width || image.height != height) throw IOException("All frames must be the same size.")

                    var flags = FLAG_VERTICAL

                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val argb = image.getRGB(x, y)
                            val alpha = (argb shr 24) and 0xFF
                            var rgb = argb and 0xFFFFFF
                            if (rgb == 0) rgb = 1

                            if (alpha != 0 && alpha != 255) flags = flags or FLAG_ALPHA

                            if (!palette.contains(rgb)) {
                                if (palette.size >= 256) throw IOException("Too many colours in this sprite!")
                                palette.add(rgb)
                            }
                        }
                    }

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

                for (i in 1 until palette.size) {
                    val rgb = palette[i]
                    os.write((rgb shr 16).toByte().toInt())
                    os.write((rgb shr 8).toByte().toInt())
                    os.write(rgb.toByte().toInt())
                }

                os.writeShort(width)
                os.writeShort(height)
                os.write(palette.size - 1)

                for (i in frames.indices) {
                    os.writeShort(0)
                    os.writeShort(0)
                    os.writeShort(width)
                    os.writeShort(height)
                }

                os.writeShort(frames.size)

                val bytes = bout.toByteArray()
                return ByteBuffer.wrap(bytes)
            } finally {
                os.close()
            }
        }

        fun getSprite(id: Int): BufferedImage? = frames[id]

        fun setFrame(
            id: Int,
            frame: BufferedImage,
        ) {
            require(
                !(frame.width != width || frame.height != height),
            ) { "The frame's dimensions do not match with the sprite's dimensions." }

            frames[id] = frame
        }

        fun size(): Int = frames.size

        companion object {
            const val FLAG_VERTICAL: Int = 0x01

            const val FLAG_ALPHA: Int = 0x02

            @JvmStatic
            fun decode(buffer: ByteBuffer): SpriteArchive? {
                try {
                    buffer.position(buffer.limit() - 2)
                    val size = buffer.getShort().toInt() and 0xFFFF

                    val offsetsX = IntArray(size)
                    val offsetsY = IntArray(size)
                    val subWidths = IntArray(size)
                    val subHeights = IntArray(size)

                    buffer.position(buffer.limit() - size * 8 - 7)
                    val width = buffer.getShort().toInt() and 0xFFFF
                    val height = buffer.getShort().toInt() and 0xFFFF
                    val palette = IntArray((buffer.get().toInt() and 0xFF) + 1)

                    val set = SpriteArchive(width, height, size)

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

                    buffer.position(buffer.limit() - size * 8 - 7 - (palette.size - 1) * 3)
                    palette[0] = 0
                    for (index in 1 until palette.size) {
                        palette[index] = getMedium(buffer)
                        if (palette[index] == 0) palette[index] = 1
                    }

                    buffer.position(0)
                    for (id in 0 until size) {
                        val subWidth = subWidths[id]
                        val subHeight = subHeights[id]
                        val offsetX = offsetsX[id]
                        val offsetY = offsetsY[id]
                        if (subWidth > 1000 || subHeight > 1000 || width > 1000 || height > 1000) continue
                        set.frames[id] = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        val image = set.frames[id]
                        val indices = Array(subWidth) { IntArray(subHeight) }

                        val flags = buffer.get().toInt() and 0xFF

                        if (image != null) {
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
                                                image.setRGB(x + offsetX, y + offsetY, alpha shl 24 or palette[indices[x][y]])
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

            fun getMedium(buf: ByteBuffer): Int =
                ((buf.get().toInt() and 0xFF) shl 16) or ((buf.get().toInt() and 0xFF) shl 8) or (buf.get().toInt() and 0xFF)
        }
    }
