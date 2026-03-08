package rs2.editor.ui.render

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import rs2.editor.ui.Consts
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.render.InterfaceDraw.childrenCache
import rs2.editor.ui.render.InterfaceDraw.getCursor
import rs2.editor.ui.selection.SelectionModel
import rs2.editor.ui.selection.SelectionSource
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JPanel

class RenderPanel : JPanel() {
    var interfaceId: Int = -1
    var showContainers = true
    var showHidden = true
    var showModels = true
    var selectedComponent: ComponentDefinition? = null
    private var backBuffer: BufferedImage? = null

    private var activeHandle: ResizeType? = null
    private var resizing = false

    private var dragStartX = 0
    private var dragStartY = 0

    private var startBaseW = 0
    private var startBaseH = 0
    private var startBaseX = 0
    private var startBaseY = 0

    private val undoStack = Stack<ComponentState>()
    private val redoStack = Stack<ComponentState>()

    val changeListeners = mutableListOf<() -> Unit>()

    private fun notifyChangeListeners()
    {
        changeListeners.forEach { it() }
    }

    init {
        preferredSize = Dimension(Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
        minimumSize   = Dimension(Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
        maximumSize   = Dimension(Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)

        SelectionModel.addListener { comp ->
            selectedComponent = comp
            repaint()
        }

        addMouseListener(object : MouseAdapter()
        {
            override fun mousePressed(e: MouseEvent)
            {
                if (resizing) return

                val clicked = hitTestComponent(e.x, e.y)
                if (clicked == null) {
                    SelectionModel.setSelection(null, SelectionSource.MOUSE)
                    return
                }

                if (clicked != selectedComponent) {
                    SelectionModel.setSelection(clicked, SelectionSource.MOUSE)
                }

                val comp = selectedComponent ?: return

                val handle = getCursor(e.x, e.y, comp.positionX, comp.positionY, comp.width, comp.height)
                if (handle != null) {
                    resizing = true
                    activeHandle = handle

                    dragStartX = e.x
                    dragStartY = e.y

                    startBaseW = comp.baseWidth
                    startBaseH = comp.baseHeight
                    startBaseX = comp.baseX
                    startBaseY = comp.baseY

                    pushUndo(comp)
                    redoStack.clear()
                }
            }

            override fun mouseReleased(e: MouseEvent)
            {
                resizing = false
                activeHandle = null
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter()
        {
            override fun mouseDragged(e: MouseEvent) {
                if (!resizing) return

                val comp = selectedComponent ?: return
                val handle = activeHandle ?: return

                val dx = e.x - dragStartX
                val dy = e.y - dragStartY

                when (handle) {
                    ResizeType.RIGHT -> comp.baseWidth = startBaseW + dx
                    ResizeType.BOTTOM -> comp.baseHeight = startBaseH + dy
                    ResizeType.LEFT -> {
                        comp.baseX = startBaseX + dx
                        comp.baseWidth = startBaseW - dx
                    }
                    ResizeType.TOP -> {
                        comp.baseY = startBaseY + dy
                        comp.baseHeight = startBaseH - dy
                    }
                    ResizeType.TOP_LEFT -> {
                        comp.baseX = startBaseX + dx
                        comp.baseY = startBaseY + dy
                        comp.baseWidth = startBaseW - dx
                        comp.baseHeight = startBaseH - dy
                    }
                    ResizeType.TOP_RIGHT -> {
                        comp.baseY = startBaseY + dy
                        comp.baseWidth = startBaseW + dx
                        comp.baseHeight = startBaseH - dy
                    }
                    ResizeType.BOTTOM_LEFT -> {
                        comp.baseX = startBaseX + dx
                        comp.baseWidth = startBaseW - dx
                        comp.baseHeight = startBaseH + dy
                    }
                    ResizeType.BOTTOM_RIGHT -> {
                        comp.baseWidth = startBaseW + dx
                        comp.baseHeight = startBaseH + dy
                    }
                }

                comp.baseWidth = maxOf(1, comp.baseWidth)
                comp.baseHeight = maxOf(1, comp.baseHeight)

                InterfaceDraw.preparedRoots.forEach { root ->
                    InterfaceDraw.layoutComponent(root, width, height)
                }

                selectedComponent?.let { InterfaceUI.instance?.buildPropertyPanel(it) }

                repaint()
            }

            override fun mouseMoved(e: MouseEvent)
            {
                val comp = hitTestComponent(e.x, e.y)
                val handle = comp?.let { getCursor(e.x, e.y, it.positionX, it.positionY, it.width, it.height) }

                cursor = when (handle)
                {
                    ResizeType.TOP, ResizeType.BOTTOM -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)
                    ResizeType.LEFT, ResizeType.RIGHT -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
                    ResizeType.TOP_LEFT, ResizeType.BOTTOM_RIGHT -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)
                    ResizeType.TOP_RIGHT, ResizeType.BOTTOM_LEFT -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)
                    else -> Cursor.getDefaultCursor()
                }
            }
        })
    }

    private fun hitTestComponentRecursive(comp: ComponentDefinition, mouseX: Int, mouseY: Int, offsetX: Int = 0, offsetY: Int = 0): ComponentDefinition?
    {
        val x = offsetX + comp.positionX
        val y = offsetY + comp.positionY
        val w = comp.width
        val h = comp.height

        childrenCache[comp.componentHash]?.asReversed()?.forEach { child ->
            hitTestComponentRecursive(child, mouseX, mouseY, x, y)?.let { return it }
        }

        if (comp.type != ComponentType.LAYER && comp.type != ComponentType.FIGURE && !comp.filled) {
            if (mouseX in x..(x + w) && mouseY in y..(y + h)) {
                return comp
            }
        }

        return null
    }

    private fun hitTestComponent(mouseX: Int, mouseY: Int): ComponentDefinition?
    {
        return InterfaceDraw.preparedRoots.asReversed().firstNotNullOfOrNull { root ->
            hitTestComponentRecursive(root, mouseX, mouseY)
        }
    }

    fun undo()
    {
        if (undoStack.isEmpty()) return
        val compState = undoStack.pop()
        pushRedo(compState.component)
        compState.restore()
        selectedComponent?.let { InterfaceUI.instance?.buildPropertyPanel(it) }
        notifyChangeListeners()
        repaint()
    }

    fun redo()
    {
        if (redoStack.isEmpty()) return
        val compState = redoStack.pop()
        pushUndo(compState.component)
        compState.restore()
        selectedComponent?.let { InterfaceUI.instance?.buildPropertyPanel(it) }
        notifyChangeListeners()
        repaint()
    }

    private fun pushUndo(comp: ComponentDefinition)
    {
        undoStack.push(ComponentState(comp))
        notifyChangeListeners()
    }

    private fun pushRedo(comp: ComponentDefinition)
    {
        redoStack.push(ComponentState(comp))
        notifyChangeListeners()
    }

    fun hasUndo() = undoStack.isNotEmpty()
    fun hasRedo() = redoStack.isNotEmpty()

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (interfaceId == -1) return


        if (backBuffer == null || backBuffer!!.width != width || backBuffer!!.height != height) {
            backBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        }

        val g2 = backBuffer!!.createGraphics()
        g2.clearRect(0, 0, width, height)


        InterfaceDraw.preparedRoots.forEach { comp ->
            InterfaceDraw.layoutComponent(comp, width, height)
        }

        InterfaceDraw.drawPreparedInterface(
            g2,
            showContainers,
            showHidden,
            showModels,
            InterfaceDraw.viewMode,
            InterfaceDraw.selectedType,
            selectedComponent
        )

        g2.dispose()
        g.drawImage(backBuffer, 0, 0, width, height, null)
    }

    private class ComponentState(val component: ComponentDefinition) {
        private val w = component.baseWidth
        private val h = component.baseHeight
        private val x = component.baseX
        private val y = component.baseY

        fun restore() {
            component.baseWidth = w
            component.baseHeight = h
            component.baseX = x
            component.baseY = y
        }
    }
}