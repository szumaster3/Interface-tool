package com.alex.utils

import com.alex.io.InputStream
import com.alex.io.OutputStream
import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentDefinition.Companion.getInterfaceComponent
import com.alex.loaders.interfaces.ComponentType
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

object Utils {
    @JvmStatic
    fun cryptRSA(data: ByteArray?, exponent: BigInteger?, modulus: BigInteger?): ByteArray {
        return BigInteger(data).modPow(exponent, modulus).toByteArray()
    }

    fun getJagString(stream: InputStream): String {
        val first = stream.readUnsignedByte()
        if (first == 0) {
            return ""
        }
        return first.toChar().toString() + getString(stream)
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

    @JvmStatic
    fun readMedium(buffer: ByteBuffer): Int {
        return ((buffer.get().toInt() and 0xFF) shl 16) or ((buffer.get().toInt() and 0xFF) shl 8) or (buffer.get()
            .toInt() and 0xFF)
    }

    @JvmStatic
    fun writeMedium(buffer: ByteBuffer, value: Int) {
        buffer.put((value shr 16).toByte())
        buffer.put((value shr 8).toByte())
        buffer.put(value.toByte())
    }

    @JvmStatic
    fun getNameHash(name: String): Int {
        return name.lowercase(Locale.getDefault()).hashCode()
    }

    fun getParent(parentHash: Int): ComponentDefinition? {
        if (parentHash == -1) return null

        val visited: MutableSet<Int> = HashSet()
        var currentHash = parentHash

        while (currentHash != -1) {
            if (!visited.add(currentHash)) {
                return null
            }

            val interfaceId = currentHash ushr 16
            val baseHash = interfaceId shl 16
            val componentId = currentHash - baseHash

            val comp = getInterfaceComponent(interfaceId, componentId) ?: return null

            currentHash = comp.parentId
        }

        return null
    }

    fun isHidden(component: ComponentDefinition): Boolean {
        if (component.hidden) return true
        var parent = getParent(component.parentId)
        while (parent != null) {
            if (parent.hidden) return true
            parent = getParent(parent.parentId)
        }
        return false
    }

    fun getScriptArray(input: String?): Array<Any?>? {
        if (input == null || input.isEmpty()) return null
        val parts = input.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val values: MutableList<String> = ArrayList()
        for (s in parts) {
            val trimmed = s.trim { it <= ' ' }
            if (!trimmed.isEmpty()) values.add(trimmed)
        }
        if (values.isEmpty()) return null

        val objs = arrayOfNulls<Any>(values.size)
        try {
            objs[0] = values[0].toInt()
        } catch (e: NumberFormatException) {
            return null
        }

        for (i in 1 until values.size) {
            try {
                objs[i] = values[i].toInt()
            } catch (e: NumberFormatException) {
                objs[i] = values[i]
            }
        }
        return objs
    }

    fun getConfigArray(input: String?): IntArray? {
        if (input == null || input.isEmpty()) return null
        val parts = input.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val values: MutableList<String> = ArrayList()
        for (s in parts) {
            val trimmed = s.trim { it <= ' ' }
            if (!trimmed.isEmpty()) values.add(trimmed)
        }
        if (values.isEmpty()) return null

        val result = IntArray(values.size)
        for (i in values.indices) {
            try {
                result[i] = values[i].toInt()
            } catch (e: NumberFormatException) {
                result[i] = 0
            }
        }
        return result
    }

    fun getComponentFromInt(type: Int): String {
        return when (type) {
            ComponentType.LAYER -> "layer"
            ComponentType.TEXT -> "text"
            ComponentType.SPRITE -> "sprite"
            ComponentType.FIGURE -> "figure"
            ComponentType.MODEL -> "model"
            ComponentType.LINE -> "line"
            ComponentType.MODEL_LIST -> "model list"
            ComponentType.INVENTORY -> "inventory"
            else -> type.toString()
        }
    }

    fun getComponentFromString(name: String?): Int {
        if (name == null) return -1

        return when (name) {
            "layer" -> ComponentType.LAYER
            "text" -> ComponentType.TEXT
            "sprite" -> ComponentType.SPRITE
            "figure" -> ComponentType.FIGURE
            "model" -> ComponentType.MODEL
            "line" -> ComponentType.LINE
            "model list" -> ComponentType.MODEL_LIST
            "inventory" -> ComponentType.INVENTORY
            else -> try {
                name.toInt()
            } catch (e: NumberFormatException) {
                -1
            }
        }
    }
}
