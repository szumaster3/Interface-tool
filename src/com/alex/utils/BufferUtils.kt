package com.alex.utils


class BufferUtils(private val buffer: ByteArray) {
    @JvmField
    var offset: Int = 0

    fun readUnsignedByte(): Int {
        return buffer[offset++].toInt() and 255
    }

    fun readByte(): Byte {
        return buffer[offset++]
    }

    fun readUnsignedShort(): Int {
        this.offset += 2
        return ((buffer[offset - 2].toInt() and 255) shl 8) + (buffer[offset - 1].toInt() and 255)
    }

    fun readShort(): Int {
        this.offset += 2
        return ((buffer[offset - 2].toInt() and 255) shl 8) + (buffer[offset - 1].toInt() and 255)
    }

    fun readSmart(): Int {
        return if ((buffer[offset].toInt() and 255) < 128) this.readUnsignedByte() - 64 else this.readUnsignedShort() - 49152
    }

    fun readMedium(): Int {
        this.offset += 3
        return ((buffer[offset - 3].toInt() and 255) shl 16) + ((buffer[offset - 2].toInt() and 255) shl 8) + (buffer[offset - 1].toInt() and 255)
    }
}
