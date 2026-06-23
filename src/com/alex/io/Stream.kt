package com.alex.io

abstract class Stream {

    var offset: Int = 0

    var length: Int = 0
        protected set

    var buffer: ByteArray = ByteArray(0)
    
    @JvmOverloads
    fun decodeXTEA(keys: IntArray, start: Int = 5, end: Int = length) {
        val originalOffset = offset
        offset = start

        val blocks = (end - start) / 8

        repeat(blocks) {
            var v0 = readIntInternal()
            var v1 = readIntInternal()

            var sum = -957401312
            val delta = -1640531527

            repeat(32) {
                v1 -= keys[(sum ushr 11) and 3] + sum xor ((v0 ushr 5 xor (v0 shl 4)) + v0)
                sum -= delta
                v0 -= ((v1 ushr 5 xor (v1 shl 4)) + v1) xor (keys[sum and 3] + sum)
            }

            offset -= 8
            writeInt(v0)
            writeInt(v1)
        }

        offset = originalOffset
    }

    fun encodeXTEA(keys: IntArray, start: Int, end: Int) {
        val originalOffset = offset
        offset = start

        val blocks = (end - start) / 8

        repeat(blocks) {
            var v0 = readIntInternal()
            var v1 = readIntInternal()

            var sum = 0
            val delta = -1640531527

            repeat(32) {
                v0 += (keys[sum and 3] + sum) xor ((v1 ushr 5 xor (v1 shl 4)) + v1)
                sum += delta
                v1 += ((v0 ushr 5 xor (v0 shl 4)) + v0) xor (keys[(sum ushr 11) and 3] + sum)
            }

            offset -= 8
            writeInt(v0)
            writeInt(v1)
        }

        offset = originalOffset
    }

    private fun readIntInternal(): Int {
        offset += 4

        return ((buffer[offset - 4].toInt() and 0xFF) shl 24) or ((buffer[offset - 3].toInt() and 0xFF) shl 16) or ((buffer[offset - 2].toInt() and 0xFF) shl 8) or (buffer[offset - 1].toInt() and 0xFF)
    }

    open fun writeInt(value: Int) {
        buffer[offset++] = (value ushr 24).toByte()
        buffer[offset++] = (value ushr 16).toByte()
        buffer[offset++] = (value ushr 8).toByte()
        buffer[offset++] = value.toByte()
    }

    fun getBytes(data: ByteArray, off: Int, len: Int) {
        for (i in off until off + len) {
            data[i] = buffer[offset++]
        }
    }
}