package rs2.interfaces

import rs2.cache.loaders.ComponentDefinition

object InterfaceUtils {
    @JvmStatic
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
        if (input.isNullOrBlank()) return null

        val values = input.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        if (values.isEmpty()) return null

        val objs = arrayOfNulls<Any>(values.size)

        objs[0] =
            try {
                values[0].toInt()
            } catch (ex: NumberFormatException) {
                return null
            }

        for (i in 1 until values.size) {
            objs[i] =
                try {
                    values[i].toInt()
                } catch (ex: NumberFormatException) {
                    values[i]
                }
        }

        return objs
    }

    fun getConfigArray(input: String?): IntArray? {
        if (input.isNullOrBlank()) return null

        val values = input.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        if (values.isEmpty()) return null

        return IntArray(values.size) { i -> values[i].toIntOrNull() ?: 0 }
    }

    fun getComponentType(type: Int): String =
        when (type) {
            ComponentConstants.LAYER -> "Layer"
            ComponentConstants.TEXT -> "Text"
            ComponentConstants.SPRITE -> "Sprite"
            ComponentConstants.FIGURE -> "Figure"
            ComponentConstants.MODEL -> "Model"
            ComponentConstants.LINE -> "Line"
            ComponentConstants.MODEL_LIST -> "Model List"
            ComponentConstants.INVENTORY -> "Inventory"
            else -> type.toString()
        }
}
