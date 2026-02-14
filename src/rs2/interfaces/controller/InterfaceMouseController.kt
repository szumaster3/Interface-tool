package rs2.interfaces.controller

import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.draw.InterfaceDraw
import ui.InterfaceUI
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import kotlin.math.max

class InterfaceMouseController(
    private val panel: Component,
    var grid: Int = 1,
    enabled: Boolean,
) : MouseAdapter() {
    private var draggingComp: ComponentDefinition? = null
    private var resizingComp: ComponentDefinition? = null
    private var activeHandle: ResizeHandle? = null
    private var startMouse = Point()

    private var startBaseX = 0
    private var startBaseY = 0
    private var startBaseW = 0
    private var startBaseH = 0

    private var isEnabled = false

    init {
        instance = this
        if (enabled) enable() else disable()
    }

    fun enable() {
        if (isEnabled) return
        panel.addMouseListener(this)
        panel.addMouseMotionListener(this)
        isEnabled = true
    }

    fun disable() {
        if (!isEnabled) return
        panel.removeMouseListener(this)
        panel.removeMouseMotionListener(this)
        draggingComp = null
        resizingComp = null
        activeHandle = null
        panel.cursor = Cursor.getDefaultCursor()
        isEnabled = false
    }

    private fun snap(
        v: Int,
        e: MouseEvent,
    ): Int {
        if (e.isAltDown) return v
        return (v / grid) * grid
    }

    private fun getResizeHandle(
        comp: ComponentDefinition,
        mouse: Point,
    ): ResizeHandle? {
        val abs = InterfaceDraw.computeAbsolutePosition(comp)
        val s = 6

        fun hit(
            px: Int,
            py: Int,
        ) = Rectangle(px - s, py - s, s * 2, s * 2).contains(mouse)

        val x = abs.x
        val y = abs.y
        val w = comp.width
        val h = comp.height

        return when {
            hit(x, y) -> ResizeHandle.NW
            hit(x + w, y) -> ResizeHandle.NE
            hit(x, y + h) -> ResizeHandle.SW
            hit(x + w, y + h) -> ResizeHandle.SE
            hit(x + w / 2, y) -> ResizeHandle.N
            hit(x + w / 2, y + h) -> ResizeHandle.S
            hit(x, y + h / 2) -> ResizeHandle.W
            hit(x + w, y + h / 2) -> ResizeHandle.E
            else -> null
        }
    }

    override fun mousePressed(e: MouseEvent) {
        val comp =
            MousePick.findComponentAt(
                e.x,
                e.y,
                InterfaceUI.currentInterface,
            ) ?: return

        InterfaceUI.instance.selectComponent(comp.componentId)
        InterfaceDraw.setValues(comp)

        startMouse = e.point
        startBaseX = comp.baseX
        startBaseY = comp.baseY
        startBaseW = comp.baseWidth
        startBaseH = comp.baseHeight

        val handle = getResizeHandle(comp, e.point)
        if (handle != null) {
            resizingComp = comp
            activeHandle = handle
        } else {
            draggingComp = comp
            panel.cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
        }

        panel.repaint()
    }

    override fun mouseDragged(e: MouseEvent) {
        val dx = e.x - startMouse.x
        val dy = e.y - startMouse.y

        draggingComp?.let { comp ->
            comp.baseX = snap(startBaseX + dx, e)
            comp.baseY = snap(startBaseY + dy, e)
            InterfaceUI.instance.selectComponent(comp.componentId)
            panel.repaint()
            return
        }

        resizingComp?.let { comp ->
            when (activeHandle) {
                ResizeHandle.E -> {
                    comp.baseWidth = max(1, snap(startBaseW + dx, e))
                }

                ResizeHandle.S -> {
                    comp.baseHeight = max(1, snap(startBaseH + dy, e))
                }

                ResizeHandle.SE -> {
                    comp.baseWidth = max(1, snap(startBaseW + dx, e))
                    comp.baseHeight = max(1, snap(startBaseH + dy, e))
                }

                ResizeHandle.W -> {
                    comp.baseX = snap(startBaseX + dx, e)
                    comp.baseWidth = max(1, snap(startBaseW - dx, e))
                }

                ResizeHandle.N -> {
                    comp.baseY = snap(startBaseY + dy, e)
                    comp.baseHeight = max(1, snap(startBaseH - dy, e))
                }

                ResizeHandle.NW -> {
                    comp.baseX = snap(startBaseX + dx, e)
                    comp.baseY = snap(startBaseY + dy, e)
                    comp.baseWidth = max(1, snap(startBaseW - dx, e))
                    comp.baseHeight = max(1, snap(startBaseH - dy, e))
                }

                ResizeHandle.NE -> {
                    comp.baseY = snap(startBaseY + dy, e)
                    comp.baseWidth = max(1, snap(startBaseW + dx, e))
                    comp.baseHeight = max(1, snap(startBaseH - dy, e))
                }

                ResizeHandle.SW -> {
                    comp.baseX = snap(startBaseX + dx, e)
                    comp.baseWidth = max(1, snap(startBaseW - dx, e))
                    comp.baseHeight = max(1, snap(startBaseH + dy, e))
                }

                else -> {}
            }
            InterfaceUI.instance.selectComponent(comp.componentId)
            panel.repaint()
        }
    }

    override fun mouseReleased(e: MouseEvent) {
        draggingComp = null
        resizingComp = null
        activeHandle = null
        panel.cursor = Cursor.getDefaultCursor()
    }

    companion object {
        var instance: InterfaceMouseController? = null
            private set
        val InterfaceMouseController.isActive: Boolean
            get() = this.isEnabled
    }
}
