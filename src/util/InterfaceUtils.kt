package util

import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import java.awt.Color
import java.awt.Graphics2D

object InterfaceUtils {

    @JvmStatic
    fun drawOutlines(g2: Graphics2D, comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int, selected: Boolean)
    {
        /*
            if (!InterfaceDraw.editMode) return
            g2.color = Color.WHITE
            g2.stroke = BasicStroke(1f)
            g2.drawRect(x, y, w, h)
         */
        if (!selected) return
        val alpha = 100

        val fillColor = when (comp.type)
        {
            ComponentType.SPRITE -> Color(127, 80,  190, alpha)
            ComponentType.FIGURE -> Color(110, 110, 110, alpha)
            ComponentType.TEXT   -> Color(255, 251,   0, alpha)
            ComponentType.MODEL  -> Color( 87,  87, 210, alpha)
            ComponentType.LAYER  -> Color(  0, 174,   0, alpha)
            else                 -> Color(255, 255,   0, alpha)
        }

        g2.color = fillColor
        g2.fillRect(x, y, w, h)
    }

}