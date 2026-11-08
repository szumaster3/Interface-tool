package com.editor

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.alex.utils.Utils
import com.editor.EditorInterfaceDraw.childrenCache
import com.editor.utils.Consts
import com.editor.utils.SelectionModel
import com.editor.utils.SelectionSource
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

class CompontentRenderPanel : JPanel() {

    companion object {
        /** Pixels a layer is scrolled by one notch of the mouse wheel. */
        private const val SCROLL_STEP = 24
        const val MAX_ZOOM = 4
    }

    /** The interface is shown this many times larger. */
    var zoom = 1
        set(value) {
            field = value.coerceIn(1, MAX_ZOOM)
            applyScreenSize()
        }

    /** Mouse position in the pixels of the interface. */
    private fun px(e: MouseEvent) = e.x / zoom
    private fun py(e: MouseEvent) = e.y / zoom

    /** Gives the panel the size of the screen mode (and the zoom). Call it after the size of the screen changed. */
    fun applyScreenSize() {
        val size = Dimension(EditorInterfaceDraw.screenWidth * zoom, EditorInterfaceDraw.screenHeight * zoom)
        preferredSize = size
        minimumSize = size
        maximumSize = size
        backBuffer = null
        revalidate()
        parent?.revalidate()
        repaint()
    }
    var interfaceId: Int = -1
    var showContainers = true
    var showHidden = true
    var showModels = true
    var selectedComponent: ComponentDefinition? = null
    private var backBuffer: BufferedImage? = null
    private var dragging = false
    private var activeHandle: com.editor.ComponentResizeType? = null
    private var resizing = false

    private var dragStartX = 0
    private var dragStartY = 0

    // position and size of the component (relative to its parent) when the change was started
    private var startRelX = 0
    private var startRelY = 0
    private var startW = 0
    private var startH = 0
    private var parentW = 0
    private var parentH = 0

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
        applyScreenSize()

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
                requestFocusInWindow()

                // the handles of the selected component come first, so they can be grabbed over other components
                selectedComponent?.let { selected ->
                    val placement = EditorInterfaceDraw.placementOf(selected)
                    val handle = placement?.let { EditorInterfaceDraw.handleAt(px(e), py(e), it.bounds) }
                    if (placement != null && handle != null) {
                        startChange(selected, placement, e)
                        resizing = true
                        activeHandle = handle
                        return
                    }
                }

                val clicked = hitTestComponent(px(e), py(e))
                if (clicked == null) {
                    SelectionModel.setSelection(null, SelectionSource.MOUSE)
                    return
                }
                if (clicked != selectedComponent) {
                    SelectionModel.setSelection(clicked, SelectionSource.MOUSE)
                }

                val comp = selectedComponent ?: return
                val placement = EditorInterfaceDraw.placementOf(comp) ?: return
                startChange(comp, placement, e)
                dragging = true
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
                val dx = px(e) - dragStartX
                val dy = py(e) - dragStartY

                if (dragging) {
                    changeBounds(comp, startRelX + dx, startRelY + dy, startW, startH)
                    cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                    return
                }

                val handle = activeHandle ?: return
                if (!resizing) return

                var x = startRelX
                var y = startRelY
                var w = startW
                var h = startH
                val left = handle == ComponentResizeType.LEFT || handle == ComponentResizeType.TOP_LEFT || handle == ComponentResizeType.BOTTOM_LEFT
                val right = handle == ComponentResizeType.RIGHT || handle == ComponentResizeType.TOP_RIGHT || handle == ComponentResizeType.BOTTOM_RIGHT
                val top = handle == ComponentResizeType.TOP || handle == ComponentResizeType.TOP_LEFT || handle == ComponentResizeType.TOP_RIGHT
                val bottom = handle == ComponentResizeType.BOTTOM || handle == ComponentResizeType.BOTTOM_LEFT || handle == ComponentResizeType.BOTTOM_RIGHT

                if (left) {
                    // the opposite side stays where it is, also when the component gets very small
                    w = maxOf(1, startW - dx)
                    x = startRelX + startW - w
                }
                if (right) w = maxOf(1, startW + dx)
                if (top) {
                    h = maxOf(1, startH - dy)
                    y = startRelY + startH - h
                }
                if (bottom) h = maxOf(1, startH + dy)

                changeBounds(comp, x, y, w, h)
            }

            override fun mouseMoved(e: MouseEvent)
            {
                val selected = selectedComponent?.takeIf { interactionEnabled }
                val area = selected?.let { EditorInterfaceDraw.placementOf(it)?.bounds }
                val handle = area?.let { EditorInterfaceDraw.handleAt(px(e), py(e), it) }

                cursor = when (handle)
                {
                    ComponentResizeType.TOP, ComponentResizeType.BOTTOM -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)
                    ComponentResizeType.LEFT, ComponentResizeType.RIGHT -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)
                    ComponentResizeType.TOP_LEFT, ComponentResizeType.BOTTOM_RIGHT -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)
                    ComponentResizeType.TOP_RIGHT, ComponentResizeType.BOTTOM_LEFT -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)
                    else ->
                        if (area != null && area.contains(px(e), py(e))) Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                        else Cursor.getDefaultCursor()
                }
            }
        })

        // layers which have more content than they can show are scrolled with the mouse wheel
        addMouseWheelListener { e ->
            if (EditorInterfaceDraw.scrollAt(px(e), py(e), e.wheelRotation * SCROLL_STEP, e.isShiftDown)) {
                repaint()
            }
        }

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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut), undoKey)

        inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut),
            redoKey
        )

        actionMap.put(redoKey, object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                redo()
            }
        })

        // The arrow keys move the selected component by 1 pixel, with shift by 10 pixels. They work while the
        // panel has the focus, which it gets when the interface is clicked.
        isFocusable = true
        val focused = getInputMap(WHEN_FOCUSED)
        for ((key, dx, dy) in listOf(
            Triple(KeyEvent.VK_LEFT, -1, 0), Triple(KeyEvent.VK_RIGHT, 1, 0),
            Triple(KeyEvent.VK_UP, 0, -1), Triple(KeyEvent.VK_DOWN, 0, 1)
        )) {
            for ((modifier, step) in listOf(0 to 1, InputEvent.SHIFT_DOWN_MASK to 10)) {
                val name = "nudge-$key-$step"
                focused.put(KeyStroke.getKeyStroke(key, modifier), name)
                actionMap.put(name, object : AbstractAction() {
                    override fun actionPerformed(e: java.awt.event.ActionEvent?) = nudge(dx * step, dy * step)
                })
            }
        }
    }

    /** Remembers where the component is, before it is moved or resized with the mouse. */
    private fun startChange(comp: ComponentDefinition, placement: EditorInterfaceDraw.Placement, e: MouseEvent) {
        dragStartX = px(e)
        dragStartY = py(e)
        startRelX = comp.positionX
        startRelY = comp.positionY
        startW = comp.width
        startH = comp.height
        parentW = placement.parent.width
        parentH = placement.parent.height
        pushUndo(comp)
        redoStack.clear()
    }

    /** Moves and resizes the component, lays the interface out again and updates the property panel. */
    private fun changeBounds(comp: ComponentDefinition, x: Int, y: Int, w: Int, h: Int) {
        EditorInterfaceDraw.setBounds(comp, x, y, w, h, parentW, parentH)
        EditorInterfaceDraw.preparedRoots.forEach {
            EditorInterfaceDraw.layoutComponent(it, EditorInterfaceDraw.screenWidth, EditorInterfaceDraw.screenHeight)
        }
        EditorGUI.instance?.buildPropertyPanel(comp)
        notifyChangeListeners()
        repaint()
    }

    /** Moves the selected component with the arrow keys. */
    private fun nudge(dx: Int, dy: Int) {
        if (!interactionEnabled) return
        val comp = selectedComponent ?: return
        val placement = EditorInterfaceDraw.placementOf(comp) ?: return
        parentW = placement.parent.width
        parentH = placement.parent.height
        pushUndo(comp)
        redoStack.clear()
        changeBounds(comp, comp.positionX + dx, comp.positionY + dy, comp.width, comp.height)
    }

    private fun hitTestComponentRecursive(
        comp: ComponentDefinition,
        mouseX: Int,
        mouseY: Int,
        offsetX: Int = 0,
        offsetY: Int = 0,
        depth: Int = 0
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

        // the content of a scrolled layer is moved
        val childX = x - EditorInterfaceDraw.scrollXOf(comp)
        val childY = y - EditorInterfaceDraw.scrollYOf(comp)
        childrenCache[comp.componentHash]
            ?.asReversed()
            ?.forEach { child ->
                hitTestComponentRecursive(child, mouseX, mouseY, childX, childY, depth + 1)
                    ?.let { return it }
            }

        // the layers which are not the base of the interface can be selected too
        if (comp.type == ComponentType.LAYER && depth == 0) {
            return null
        }
        return comp
    }

    private fun hitTestComponent(mouseX: Int, mouseY: Int): ComponentDefinition?
    {
        return EditorInterfaceDraw.preparedRoots.asReversed().firstNotNullOfOrNull { root ->
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
        selectedComponent?.let { EditorGUI.instance?.buildPropertyPanel(it) }
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
        selectedComponent?.let { EditorGUI.instance?.buildPropertyPanel(it) }
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


        val screenWidth = EditorInterfaceDraw.screenWidth
        val screenHeight = EditorInterfaceDraw.screenHeight
        if (backBuffer == null || backBuffer!!.width != screenWidth || backBuffer!!.height != screenHeight) {
            backBuffer = BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB)
        }

        val g2 = backBuffer!!.createGraphics()
        g2.clearRect(0, 0, screenWidth, screenHeight)

        EditorInterfaceDraw.drawPreparedInterface(
            g2,
            showContainers,
            showHidden,
            showModels,
            EditorInterfaceDraw.viewMode,
            EditorInterfaceDraw.selectedType,
            selectedComponent
        )

        g2.dispose()
        (g as? java.awt.Graphics2D)?.setRenderingHint(
            java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        )
        g.drawImage(backBuffer, 0, 0, screenWidth * zoom, screenHeight * zoom, null)
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
        EditorInterfaceDraw.preparedRoots.forEach { root ->
            EditorInterfaceDraw.layoutComponent(root, EditorInterfaceDraw.screenWidth, EditorInterfaceDraw.screenHeight)
        }
        repaint()
    }
}