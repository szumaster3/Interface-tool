package rs2.interfaces.render

import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.draw.OldschoolDraw
import rs2.interfaces.draw.OldschoolDrawPosition
import ui.InterfaceUI

object InterfaceHitTest {

    fun findComponentAt(
        x: Int,
        y: Int,
        interfaceId: Int
    ): ComponentDefinition? {

        val comps = InterfaceUI.getOrderedComps(interfaceId) ?: return null

        for (i in comps.indices.reversed()) {
            val comp = comps[i] ?: continue
            OldschoolDrawPosition.setValues(comp)

            val abs = OldschoolDraw.computeAbsolutePosition(comp)
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
