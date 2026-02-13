package rs2.cache.util

import com.alex.io.InputStream

object InputStreamUtils {
    fun getJagString(stream: InputStream): String {
        val first = stream.readUnsignedByte()
        if (first == 0) return ""
        return first.toChar() + getString(stream)
    }

    private fun getString(stream: InputStream): String {
        val bldr = StringBuilder()
        var b: Int
        while (true) {
            b = stream.readUnsignedByte()
            if (b == 0) break
            bldr.append(b.toChar())
        }
        return bldr.toString()
    }
}
