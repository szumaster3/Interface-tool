package rs2.interfaces.controller

import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.draw.InterfaceDraw
import java.awt.Color
import java.awt.Graphics
import java.awt.Point

enum class ResizeHandle {
    N,
    S,
    E,
    W,
    NE,
    NW,
    SE,
    SW;

    fun drawResizeHandles(g: Graphics, comp: ComponentDefinition) {
        val abs = InterfaceDraw.computeAbsolutePosition(comp)
        val w = comp.width
        val h = comp.height
        val size = 6

        val points = mapOf(
            NW     to Point(abs.x, abs.y),
            N      to Point(abs.x + w / 2, abs.y),
            NE     to Point(abs.x + w, abs.y),
            W      to Point(abs.x, abs.y + h / 2),
            E      to Point(abs.x + w, abs.y + h / 2),
            SW     to Point(abs.x, abs.y + h),
            S      to Point(abs.x + w / 2, abs.y + h),
            SE     to Point(abs.x + w, abs.y + h)
        )

        g.color = Color.WHITE
        points.values.forEach { p ->
            g.fillRect(p.x - size, p.y - size, size * 2, size * 2)
        }
    }
}
