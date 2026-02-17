package util

import fs.com.alex.Cache
import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import ui.Consts
import ui.Consts.fillColors
import ui.Consts.outlineColors
import ui.InterfaceUI
import ui.draw.InterfaceDraw
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import javax.swing.JOptionPane

object InterfaceUtils {

    @JvmStatic
    fun drawOutlines(g2: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int, selected: Boolean)
    {
        if (!selected) return

        val fillColor = fillColors[comp.type] ?: Color.GRAY
        g2.color = fillColor
        g2.fillRect(x, y, w, h)

        val outlineColor = outlineColors[comp.type] ?: Color.DARK_GRAY
        g2.color = outlineColor
        g2.stroke = BasicStroke(2f)
        g2.drawRect(x, y, w, h)
    }

    @JvmStatic
    fun pasteComponent()
    {
        val inter = InterfaceUI.instance!!.selectedComponent
        val comp  = InterfaceUI.instance!!.clonedComponent
        if (comp == null) {
            JOptionPane.showMessageDialog(null, "No component was selected to paste.")
            return
        }
        if (comp.type == ComponentType.LAYER) {
            val containerPlace = ComponentDefinition.getInterfaceDefinitionsComponentsSize(comp.interfaceId)
            comp.parentId = -1

            Cache.getStore()!!.indexes[3].putFile(inter!!.interfaceId,
                containerPlace, comp.encode(true)
            )
            val childs = ComponentDefinition.getChildsByParent(comp.interfaceId, comp.componentHash)
            for (c in childs) {
                if (c.type != ComponentType.LAYER) {
                    Cache.getStore()!!.indexes[3].putFile(inter.interfaceId,
                        ComponentDefinition.getInterfaceDefinitionsComponentsSize(inter.interfaceId),
                        c.encode(true)
                    )
                }
            }
            ComponentDefinition.getInterface(inter.interfaceId, true)
            val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(inter.interfaceId)
            val parent = ComponentDefinition.getInterfaceComponent(inter.interfaceId, containerPlace)
            for (i in size - childs.size - 1 until size) {
                val component = ComponentDefinition.getInterfaceComponent(inter.interfaceId, i)
                if (component != null && component.type != 0) {
                    if (parent != null) {
                        component.parentId = parent.componentHash
                    }
                    Cache.getStore()!!.indexes[3].putFile(inter.interfaceId, i, component.encode(true))
                }
            }
        } else {
            comp.parentId = -1
            Cache.getStore()!!.indexes[3].putFile(
                inter!!.interfaceId,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(inter.interfaceId),
                comp.encode(true)
            )
        }
        ComponentDefinition.getInterface(inter.interfaceId, true)
    }
}