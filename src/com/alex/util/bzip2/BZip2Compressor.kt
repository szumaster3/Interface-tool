package com.alex.util.bzip2

import org.apache.tools.bzip2.CBZip2OutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

object BZip2Compressor {
    @JvmStatic
    fun compress(data: ByteArray?): ByteArray? {
        val compressedBytes = ByteArrayOutputStream()

        try {
            val var3 = CBZip2OutputStream(compressedBytes)
            var3.write(data)
            var3.close()
            return compressedBytes.toByteArray()
        } catch (var31: IOException) {
            var31.printStackTrace()
            return null
        }
    }
}
