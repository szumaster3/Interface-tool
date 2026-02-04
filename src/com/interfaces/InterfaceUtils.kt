package com.interfaces

import com.rs.cache.loaders.ComponentDefinition

object InterfaceUtils {
    fun isHidden(component: ComponentDefinition): Boolean {
        if (component.hidden) return true
        var parent = getParent(component.parentId)
        var hidden = false
        while (parent != null) {
            if (parent.hidden) {
                hidden = true
                break
            }
            parent = getParent(parent.parentId)
        }
        return hidden
    }

	@JvmStatic
	fun getParent(parentHash: Int): ComponentDefinition? {
        if (parentHash == -1) return null
        val interfaceId = parentHash shr 16
        val baseHash = interfaceId shl 16
        val componentId = parentHash - baseHash
        return ComponentDefinition.getInterfaceComponent(interfaceId, componentId)
    }

    fun getScriptArray(input: String?): Array<Any?>? {
        if (input == null || input === "" || input === " ") return null
        val values = input.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val objs = arrayOfNulls<Any>(values.size)
        try {
            values[0].toInt()
        } catch (exp: Exception) { //return null if the first value isn't a number
            return null
        }
        for (i in values.indices) {
            try {
                val x = values[i].toInt()
                objs[i] = x
            } catch (ex: Exception) {
                objs[i] = values[i]
            }
        }
        return objs
    }

    fun getConfigArray(input: String?): IntArray? {
        if (input == null || input.isEmpty() || input.equals(" ", ignoreCase = true)) return null
        val values = input.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val intArray = IntArray(values.size)
        for (i in values.indices) {
            intArray[i] = values[i].toInt()
        }
        return intArray
    }
}
