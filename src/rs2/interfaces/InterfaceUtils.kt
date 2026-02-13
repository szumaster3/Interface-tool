package rs2.interfaces

import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.draw.InterfaceDraw
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import kotlin.math.max

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

    fun drawSelectedComponent(
        g: Graphics,
        comp: ComponentDefinition,
        fill: Boolean = true
    ) {
        if (!InterfaceDraw.editMode) return

        val abs = InterfaceDraw.computeAbsolutePosition(comp)
        val x = abs.x
        val y = abs.y
        val width = max(1, comp.width)
        val height = max(1, comp.height)

        val fillColor = when (comp.type) {
            ComponentConstants.SPRITE -> Color(127, 80, 190, InterfaceDraw.SELECTED_ALPHA)
            ComponentConstants.FIGURE -> Color(110, 110, 110, InterfaceDraw.SELECTED_ALPHA)
            ComponentConstants.TEXT   -> Color(255, 251, 0, InterfaceDraw.SELECTED_ALPHA)
            ComponentConstants.MODEL  -> Color(87, 87, 210, InterfaceDraw.SELECTED_ALPHA)
            ComponentConstants.LAYER  -> Color(0, 174, 0, InterfaceDraw.SELECTED_ALPHA)
            else -> null
        }

        val outlineColor = when (comp.type) {
            ComponentConstants.SPRITE -> Color(141, 86, 211)
            ComponentConstants.FIGURE -> Color(110, 110, 110)
            ComponentConstants.TEXT   -> Color(255, 251, 0)
            ComponentConstants.MODEL  -> Color(87, 87, 210)
            ComponentConstants.LAYER  -> Color(0, 174, 0)
            else -> Color.YELLOW
        }

        val g2 = g as Graphics2D
        if (fill) {
            fillColor?.let { g2.color = it; g2.fillRect(x, y, width, height) }
        }
        g2.color = outlineColor
        g2.stroke = BasicStroke(1.5f)
        g2.drawRect(x, y, width, height)

    }


}
