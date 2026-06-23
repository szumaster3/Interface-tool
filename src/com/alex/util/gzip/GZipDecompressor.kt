package com.alex.util.gzip

import com.alex.io.Stream
import java.util.zip.Inflater

object GZipDecompressor {

    private val inflaterInstance = Inflater(true)

    @JvmStatic
    fun decompress(stream: Stream, data: ByteArray): Boolean {
        synchronized(inflaterInstance) {
            val buffer = stream.buffer
            val offset = stream.offset

            if (buffer[offset] != 31.toByte() || buffer[offset + 1] != (-117).toByte()) {
                return false
            }

            return try {
                val length = buffer.size - offset - 18
                inflaterInstance.setInput(buffer, offset + 10, length)
                inflaterInstance.inflate(data)
                true
            } catch (_: Exception) {
                false
            } finally {
                inflaterInstance.reset()
            }
        }
    }
}