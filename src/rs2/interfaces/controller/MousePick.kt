package rs2.interfaces.controller

import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.draw.InterfaceDraw
import ui.InterfaceUI

object MousePick {
    fun findComponentAt(
        x: Int,
        y: Int,
        interfaceId: Int,
    ): ComponentDefinition? {
        val comps = InterfaceUI.getOrderedComps(interfaceId) ?: return null

        for (i in comps.indices.reversed()) {
            val comp = comps[i] ?: continue
            InterfaceDraw.setValues(comp)

            val abs = InterfaceDraw.computeAbsolutePosition(comp)
            if (
                x >= abs.x && x <= abs.x + comp.width &&
                y >= abs.y && y <= abs.y + comp.height
            ) {
                return comp
            }
        }
        return null
    }
}
