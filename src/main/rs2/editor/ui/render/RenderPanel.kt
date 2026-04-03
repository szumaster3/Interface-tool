package rs2.editor.ui.render

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.alex.utils.Utils
import rs2.editor.ui.Consts
import rs2.editor.ui.InterfaceUI
import rs2.editor.ui.render.InterfaceDraw.childrenCache
import rs2.editor.ui.render.InterfaceDraw.getCursor
import rs2.editor.ui.selection.SelectionModel
import rs2.editor.ui.selection.SelectionSource
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Toolkit
import java.awt.event.*
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.AbstractAction
import javax.swing.JPanel
import javax.swing.KeyStroke

class RenderPanel : JPanel() {
    var interfaceId: Int = -1
    var showContainers = true
    var showHidden = true
    var showModels = true
    var selectedComponent: ComponentDefinition? = null
    private var backBuffer: BufferedImage? = null
    private var dragging = false
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

    @JvmField var interactionEnabled = true

    val undoKey = "undo"
    val redoKey = "redo"
    val changeListeners = mutableListOf<() -> Unit>()

    private fun notifyChangeListeners()
    {
        changeListeners.forEach { it() }
    }

    fun setInteractionEnabled(enabled: Boolean)
    {
        interactionEnabled = enabled
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
                if (!interactionEnabled) return
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
                } else {
                    dragging = true

                    dragStartX = e.x
                    dragStartY = e.y

                    startBaseX = comp.baseX
                    startBaseY = comp.baseY

                    pushUndo(comp)
                    redoStack.clear()
                }
            }

            override fun mouseReleased(e: MouseEvent)
            {
                resizing = false
                dragging = false
                activeHandle = null
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter()
        {
            override fun mouseDragged(e: MouseEvent) {
                if (!interactionEnabled) return
                val comp = selectedComponent ?: return
                val dx = e.x - dragStartX
                val dy = e.y - dragStartY

                if (dragging) {
                    comp.baseX = startBaseX + dx
                    comp.baseY = startBaseY + dy

                    InterfaceDraw.preparedRoots.forEach { root ->
                        InterfaceDraw.layoutComponent(root, width, height)
                    }
                    selectedComponent?.let { InterfaceUI.instance?.buildPropertyPanel(it) }
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    repaint()
                    return
                }

                if (!resizing) return
                val handle = activeHandle ?: return

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

                val handle = if (SelectionModel.source == SelectionSource.MOUSE) {
                    selectedComponent?.let {
                        getCursor(e.x, e.y, it.positionX, it.positionY, it.width, it.height)
                    }
                } else null

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

        val inputMap = this.getInputMap(WHEN_IN_FOCUSED_WINDOW)
        val actionMap = this.actionMap

        val shortcut = Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut or InputEvent.SHIFT_DOWN_MASK),
            redoKey
        )

        actionMap.put(undoKey, object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                undo()
            }
        })

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut),
            redoKey
        )

        actionMap.put(redoKey, object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                redo()
            }
        })
    }

    private fun hitTestComponentRecursive(
        comp: ComponentDefinition,
        mouseX: Int,
        mouseY: Int,
        offsetX: Int = 0,
        offsetY: Int = 0
    ): ComponentDefinition? {

        val hidden = Utils.isHidden(comp)

        if (SelectionModel.source == SelectionSource.MOUSE && hidden && !showHidden) {
            return null
        }

        val x = offsetX + comp.positionX
        val y = offsetY + comp.positionY
        val w = comp.width
        val h = comp.height

        if (mouseX !in x until x + w || mouseY !in y until y + h) {
            return null
        }

        childrenCache[comp.componentHash]
            ?.asReversed()
            ?.forEach { child ->
                hitTestComponentRecursive(child, mouseX, mouseY, x, y)
                    ?.let { return it }
            }

        if (comp.type == ComponentType.LAYER) {
            return null
        }

        val handleSize = maxOf(InterfaceDraw.HANDLE_SIZE, 6)
        val margin = 4
        val half = handleSize / 2

        val handles = listOf(
            Pair(x, y), Pair(x + w / 2, y), Pair(x + w, y),
            Pair(x + w, y + h / 2), Pair(x + w, y + h),
            Pair(x + w / 2, y + h), Pair(x, y + h), Pair(x, y + h / 2)
        )

        if (handles.any { (cx, cy) ->
                mouseX in (cx - half - margin)..(cx + half + margin) &&
                        mouseY in (cy - half - margin)..(cy + half + margin)
            }) {
            return comp
        }

        return comp
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
        if (undoStack.size > 100)
            undoStack.removeAt(0)
        val compState = undoStack.pop()
        pushRedo(compState.c)
        compState.restore()
        refresh()
        selectedComponent?.let { InterfaceUI.instance?.buildPropertyPanel(it) }
        notifyChangeListeners()
        repaint()
    }

    fun redo()
    {
        if (redoStack.isEmpty()) return
        val compState = redoStack.pop()
        pushUndo(compState.c)
        compState.restore()
        refresh()
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

    private class ComponentState(val c: ComponentDefinition) {
        private val baseX = c.baseX
        private val baseY = c.baseY
        private val baseW = c.baseWidth
        private val baseH = c.baseHeight

        private val widthMode = c.widthMode
        private val heightMode = c.heightMode
        private val xMode = c.xMode
        private val yMode = c.yMode

        private val name = c.name
        private val hidden = c.hidden
        private val type = c.type
        private val contentType = c.contentType
        private val parentId = c.parentId

        private val spriteId = c.spriteId
        private val angle2d = c.angle2d
        private val spriteTiling = c.spriteTiling
        private val hasAlpha = c.hasAlpha
        private val transparency = c.transparency
        private val borderThickness = c.borderThickness
        private val shadowColor = c.shadowColor
        private val vFlip = c.vFlip
        private val hFlip = c.hFlip

        private val modelType = c.modelType
        private val modelId = c.modelId
        private val modelOriginX = c.modelOriginX
        private val modelOriginY = c.modelOriginY
        private val modelXAngle = c.modelXAngle
        private val modelYAngle = c.modelYAngle
        private val modelZAngle = c.modelZAngle
        private val modelZoom = c.modelZoom
        private val modelAnimId = c.modelAnimId
        private val modelOrtho = c.modelOrtho
        private val modelObjWidth = c.modelObjWidth
        private val modelObjHeight = c.modelObjHeight

        private val unknownModelProp3 = c.unknownModelProp3
        private val unknownModelProp4 = c.unknownModelProp4
        private val unknownModelProp5 = c.unknownModelProp5

        private val fontId = c.fontId
        private val text = c.text
        private val textLineHeight = c.textLineHeight
        private val textHorizontalAli = c.textHorizontalAli
        private val textVerticalAli = c.textVerticalAli
        private val shadow = c.shadow
        private val color = c.color

        private val filled = c.filled
        private val lineWidth = c.lineWidth
        private val unknownProp8 = c.unknownProp_8

        private val optionMask = c.optionMask
        private val optionBase = c.optionBase
        private val rightClickOptions = c.rightClickOptions?.clone()
        private val opId = c.opId?.clone()
        private val repeatCount = c.repeatCount?.clone()
        private val modifier = c.modifier?.clone()

        private val opCursors = c.opCursors?.clone()

        private val dragDeadzone = c.dragDeadzone
        private val dragDeadtime = c.dragDeadtime
        private val dragRenderBehavior = c.dragRenderBehavior
        private val optionCircumfix = c.optionCircumfix

        private val mask = c.mask
        private val targetOverCursor = c.targetOverCursor
        private val targetLeaveCursor = c.targetLeaveCursor

        private val onLoadScript = c.onLoadScript?.clone()
        private val onMouseHoverScript = c.onMouseHoverScript?.clone()
        private val onMouseLeaveScript = c.onMouseLeaveScript?.clone()
        private val onUseWith = c.onUseWith?.clone()
        private val onUse = c.onUse?.clone()

        private val onVarpTransmit = c.onVarpTransmit?.clone()
        private val onInvTransmit = c.onInvTransmit?.clone()
        private val onStatTransmit = c.onStatTransmit?.clone()
        private val onTimer = c.onTimer?.clone()

        private val onOptionClick = c.onOptionClick?.clone()
        private val onMouseRepeat = c.onMouseRepeat?.clone()
        private val onClickRepeat = c.onClickRepeat?.clone()

        private val onDrag = c.onDrag?.clone()
        private val onRelease = c.onRelease?.clone()
        private val onHold = c.onHold?.clone()

        private val onDragStart = c.onDragStart?.clone()
        private val onDragRelease = c.onDragRelease?.clone()
        private val onScroll = c.onScroll?.clone()

        private val onVarcTransmit = c.onVarcTransmit?.clone()
        private val onVarcStrTransmit = c.onVarcStrTransmit?.clone()

        private val varpTriggers = c.varpTriggers?.clone()
        private val inventoryTriggers = c.inventoryTriggers?.clone()
        private val statTriggers = c.statTriggers?.clone()
        private val varcTriggers = c.varcTriggers?.clone()
        private val varcstrTriggers = c.varcstrTriggers?.clone()

        fun restore() {
            val c = this.c

            c.baseX = baseX
            c.baseY = baseY
            c.baseWidth = baseW
            c.baseHeight = baseH

            c.widthMode = widthMode
            c.heightMode = heightMode
            c.xMode = xMode
            c.yMode = yMode

            c.name = name
            c.hidden = hidden
            c.type = type
            c.contentType = contentType
            c.parentId = parentId

            c.spriteId = spriteId
            c.angle2d = angle2d
            c.spriteTiling = spriteTiling
            c.hasAlpha = hasAlpha
            c.transparency = transparency
            c.borderThickness = borderThickness
            c.shadowColor = shadowColor
            c.vFlip = vFlip
            c.hFlip = hFlip

            c.modelType = modelType
            c.modelId = modelId
            c.modelOriginX = modelOriginX
            c.modelOriginY = modelOriginY
            c.modelXAngle = modelXAngle
            c.modelYAngle = modelYAngle
            c.modelZAngle = modelZAngle
            c.modelZoom = modelZoom
            c.modelAnimId = modelAnimId
            c.modelOrtho = modelOrtho
            c.modelObjWidth = modelObjWidth
            c.modelObjHeight = modelObjHeight

            c.fontId = fontId
            c.text = text
            c.textLineHeight = textLineHeight
            c.textHorizontalAli = textHorizontalAli
            c.textVerticalAli = textVerticalAli
            c.shadow = shadow
            c.color = color

            c.filled = filled
            c.lineWidth = lineWidth
            c.unknownProp_8 = unknownProp8

            c.optionMask = optionMask
            c.optionBase = optionBase
            c.rightClickOptions = rightClickOptions?.clone()
            c.opId = opId?.clone()
            c.repeatCount = repeatCount?.clone()
            c.modifier = modifier?.clone()

            c.opCursors = opCursors?.clone()

            c.dragDeadzone = dragDeadzone
            c.dragDeadtime = dragDeadtime
            c.dragRenderBehavior = dragRenderBehavior
            c.optionCircumfix = optionCircumfix

            c.mask = mask
            c.targetOverCursor = targetOverCursor
            c.targetLeaveCursor = targetLeaveCursor

            c.onLoadScript = onLoadScript?.clone()
            c.onMouseHoverScript = onMouseHoverScript?.clone()
            c.onMouseLeaveScript = onMouseLeaveScript?.clone()
            c.onUseWith = onUseWith?.clone()
            c.onUse = onUse?.clone()

            c.onVarpTransmit = onVarpTransmit?.clone()
            c.onInvTransmit = onInvTransmit?.clone()
            c.onStatTransmit = onStatTransmit?.clone()
            c.onTimer = onTimer?.clone()

            c.onOptionClick = onOptionClick?.clone()
            c.onMouseRepeat = onMouseRepeat?.clone()
            c.onClickRepeat = onClickRepeat?.clone()

            c.onDrag = onDrag?.clone()
            c.onRelease = onRelease?.clone()
            c.onHold = onHold?.clone()

            c.onDragStart = onDragStart?.clone()
            c.onDragRelease = onDragRelease?.clone()
            c.onScroll = onScroll?.clone()

            c.onVarcTransmit = onVarcTransmit?.clone()
            c.onVarcStrTransmit = onVarcStrTransmit?.clone()

            c.varpTriggers = varpTriggers?.clone()
            c.inventoryTriggers = inventoryTriggers?.clone()
            c.statTriggers = statTriggers?.clone()
            c.varcTriggers = varcTriggers?.clone()
            c.varcstrTriggers = varcstrTriggers?.clone()
        }
    }

    private fun refresh() {
        InterfaceDraw.preparedRoots.forEach { root ->
            InterfaceDraw.layoutComponent(root, width, height)
        }
        repaint()
    }
}