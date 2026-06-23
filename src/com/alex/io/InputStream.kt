package com.alex.io

import kotlin.experimental.inv

class InputStream(data: ByteArray) : Stream() {

    init {
        buffer = data
        length = data.size
    }

    val remaining: Int
        get() = (length - offset).coerceAtLeast(0)

    fun readByte(): Int {
        return if (remaining > 0) {
            buffer[offset++].toInt()
        } else {
            0
        }
    }

    fun readBytes(dst: ByteArray, off: Int, len: Int) {
        for (i in off until off + len) {
            dst[i] = readByte().toByte()
        }
    }

    fun readUnsignedByte(): Int =
        readByte() and 0xFF

    fun readShort(): Int {
        var value = (readUnsignedByte() shl 8) or readUnsignedByte()

        if (value > Short.MAX_VALUE) {
            value -= 0x10000
        }

        return value
    }

    fun readUnsignedShort(): Int =
        (readUnsignedByte() shl 8) or readUnsignedByte()

    fun readInt(): Int =
        (readUnsignedByte() shl 24) or
                (readUnsignedByte() shl 16) or
                (readUnsignedByte() shl 8) or
                readUnsignedByte()

    fun read24BitInt(): Int =
        (readUnsignedByte() shl 16) or
                (readUnsignedByte() shl 8) or
                readUnsignedByte()

    fun readBigSmart(): Int {
        return if (buffer[offset].inv() <= -1) {
            val value = readUnsignedShort()
            if (value == 32767) -1 else value
        } else {
            readInt() and Int.MAX_VALUE
        }
    }
}