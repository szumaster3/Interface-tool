package com.alex.io

class OutputStream @JvmOverloads constructor(
    capacity: Int = 16
) : Stream() {

    init {
        buffer = ByteArray(capacity)
    }

    fun checkCapacityPosition(position: Int) {
        if (position >= buffer.size) {
            val newBuffer = ByteArray(position + 16)
            buffer.copyInto(newBuffer)
            buffer = newBuffer
        }
    }

    fun writeBytes(data: ByteArray) {
        checkCapacityPosition(offset + data.size)

        data.copyInto(
            destination = buffer,
            destinationOffset = offset
        )

        offset += data.size

        if (offset > length) {
            length = offset
        }
    }

    @JvmOverloads
    fun writeByte(value: Int, position: Int = offset++) {
        checkCapacityPosition(position)

        buffer[position] = value.toByte()

        if (position + 1 > length) {
            length = position + 1
        }
    }

    fun writeBigSmart(value: Int) {
        if (value >= 52767) {
            writeInt(value - Int.MAX_VALUE - 1)
        } else {
            writeShort(if (value >= 0) value else 52767)
        }
    }

    fun writeShort(value: Int) {
        writeByte(value ushr 8)
        writeByte(value)
    }

    fun writeShortLE(value: Int) {
        writeByte(value)
        writeByte(value ushr 8)
    }

    fun write24BitInt(value: Int) {
        writeByte(value ushr 16)
        writeByte(value ushr 8)
        writeByte(value)
    }

    override fun writeInt(value: Int) {
        writeByte(value ushr 24)
        writeByte(value ushr 16)
        writeByte(value ushr 8)
        writeByte(value)
    }

    fun writeString(value: String) {
        val bytes = value.toByteArray()

        checkCapacityPosition(offset + bytes.size + 1)

        bytes.copyInto(
            destination = buffer,
            destinationOffset = offset
        )

        offset += bytes.size
        writeByte(0)
    }

    companion object {
        val BIT_MASK = IntArray(32) { index ->
            (1 shl index) - 1
        }
    }
}