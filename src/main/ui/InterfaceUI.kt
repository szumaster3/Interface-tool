package ui

import fs.com.alex.Cache
import fs.com.alex.utils.Utils
import properties.PropertyValues
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath
import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import rs.cache.sprite.util.SpriteManager
import ui.draw.ComponentRender
import ui.draw.InterfaceDraw
import ui.draw.RenderPanel
import util.ColorPicker
import util.GenericPrinter
import util.InterfaceUtils
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

class InterfaceUI : JFrame()
{
    private var treeView = true
    private val interfacesListModel = DefaultListModel<Int>()
    private val interfacesList      = JList(interfacesListModel)
    private val treeTab             = JButton("Tree View")
    private val listTab             = JButton("List View")
    private val componentTreeRoot   = DefaultMutableTreeNode("Components")
    private val treeModel           = DefaultTreeModel(componentTreeRoot)
    private val componentTree       = JTree(treeModel)
    private val componentListModel  = DefaultListModel<ComponentDefinition>()
    private val componentList       = JList(componentListModel)
    private val componentScrollPane = JScrollPane(componentTree)
    private val fieldMap            = mutableMapOf<String, JComponent>()
    private val renderPanel         = RenderPanel()
    private val propertyPanel       = JPanel()

    private var orderedComponents: List<ComponentDefinition> = emptyList()
    var selectedComponent: ComponentDefinition? = null
    var clonedComponent: ComponentDefinition? = null

    companion object
    {
        @JvmStatic var instance: InterfaceUI? = null

        @JvmStatic
        fun main(args: Array<String>)
        {
            EventQueue.invokeLater {
                try {
                    if (args.isEmpty()) loadValues() else setCachePath(args[0])
                    Cache.init(PropertyValues.cache_path)
                    instance = InterfaceUI().apply {
                        setLocationRelativeTo(null)
                        isVisible = true
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    init
    {
        title = "Interface Editor v${Consts.APP_VERSION}"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(Consts.EDITOR_WIDTH, Consts.EDITOR_HEIGHT)
        layout = BorderLayout()
        jMenuBar = InterfaceUIMenu()
        loadTree()
        loadPanels()
        loadInterfaces()
    }

    private fun loadTree()
    {
        val renderer = ComponentRender { _ ->
            componentTree.repaint()
            componentList.repaint()
            treeModel.reload()
        }

        componentList.cellRenderer = renderer
        componentTree.cellRenderer = renderer

        componentTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        componentTree.addTreeSelectionListener {
            val node = componentTree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val comp = node?.userObject as? ComponentDefinition
            comp?.let { selectComponent(it) }
        }

        componentList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        componentList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) componentList.selectedValue?.let { selectComponent(it) }
        }

        componentTree.addMouseListener(object : MouseAdapter()
        {
            override fun mouseClicked(e: MouseEvent)
            {
                componentTree.repaint()
            }
        })
        componentList.addMouseListener(object : MouseAdapter()
        {
            override fun mouseClicked(e: MouseEvent)
            {
                componentList.repaint()
            }
        })
    }

    private fun loadPanels()
    {
        val mainPanel = JPanel(BorderLayout())
        val topPanel = JPanel(BorderLayout())

        val leftPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createLoweredBevelBorder()
            preferredSize = Dimension(Consts.LEFT_PANEL_WIDTH, 0)
        }

        leftPanel.add(JScrollPane(interfacesList).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy   = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS

        }, BorderLayout.CENTER)

        interfacesList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        interfacesList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                interfacesList.selectedValue?.let { id ->
                    renderPanel.interfaceId = id
                    loadComponents(id)
                }
            }
        }

        val rightPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createLoweredBevelBorder()
            preferredSize = Dimension(Consts.RIGHT_PANEL_WIDTH, 0)
        }
        setupRightPanel(rightPanel)

        val centerWrapper = JPanel(GridBagLayout())
        centerWrapper.background = Color.DARK_GRAY
        centerWrapper.add(renderPanel)

        topPanel.add(leftPanel, BorderLayout.WEST)
        topPanel.add(centerWrapper, BorderLayout.CENTER)
        topPanel.add(rightPanel, BorderLayout.EAST)
        topPanel.border = BorderFactory.createLoweredBevelBorder()
        mainPanel.add(topPanel, BorderLayout.CENTER)

        val bottomPanel = JPanel(BorderLayout())

        val buttonPanel = JPanel(GridLayout(1, 1, 1, 1)).apply {
            border = BorderFactory.createEmptyBorder(1, 1, 1, 1)
        }

        listOf(
            createNewInterfaceButton(),
            createDeleteInterfaceButton(),
            createAddLayerButton(),
            createAddFigureButton(),
            createAddSpriteButton(),
            createAddModelButton(),
            createAddTextButton(),
            createAddLineButton(),
            createSaveButton(),
            createPrintComponentButton(),
            createPrintInterfaceButton(),
            createCopyButton(),
            createPasteButton(),
            createSpriteLibraryButton(),
            createColorPickButton()
        ).forEach { buttonPanel.add(it) }

        bottomPanel.add(buttonPanel, BorderLayout.NORTH)

        propertyPanel.removeAll()
        propertyPanel.layout = BoxLayout(propertyPanel, BoxLayout.X_AXIS)
        propertyPanel.border = BorderFactory.createEmptyBorder(1,1,1,1)
        initPropertyLayout()

        val sectionsScroll = JScrollPane(propertyPanel).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            border = BorderFactory.createLoweredBevelBorder()
            verticalScrollBar.unitIncrement = 7
        }

        bottomPanel.add(sectionsScroll, BorderLayout.CENTER)
        mainPanel.add(bottomPanel, BorderLayout.SOUTH)

        add(mainPanel, BorderLayout.CENTER)
    }

    private fun setupRightPanel(rightPanel: JPanel) {
        val tabsPanel = JPanel(GridLayout(1, 2)).apply {
            preferredSize = Dimension(Consts.RIGHT_PANEL_WIDTH, Consts.BUTTON_HEIGHT)
        }

        treeTab.addActionListener {
            treeView = true
            updateTabs()
        }
        listTab.addActionListener {
            treeView = false
            updateTabs()
        }

        tabsPanel.border = BorderFactory.createLoweredBevelBorder()
        tabsPanel.add(treeTab)
        tabsPanel.add(listTab)

        componentTree.background = Color.BLACK
        componentTree.foreground = Color.GREEN
        componentTree.isOpaque = true

        componentList.background = Color.BLACK
        componentList.foreground = Color.GREEN
        componentList.isOpaque = true

        componentTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        componentTree.addTreeSelectionListener {
            val node = componentTree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val comp = node?.userObject as? ComponentDefinition
            comp?.let { selectComponent(it) }
        }

        componentList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        componentList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) componentList.selectedValue?.let { selectComponent(it) }
        }

        val optionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        }

        val checkboxes = listOf(
            JCheckBox("Show Layers", renderPanel.showContainers),
            JCheckBox("Show Hidden", renderPanel.showHidden),
            JCheckBox("Show Models", renderPanel.showModels)
        )
        checkboxes.forEach { cb ->
            cb.alignmentX = Component.LEFT_ALIGNMENT
            cb.addActionListener {
                renderPanel.showContainers = checkboxes[0].isSelected
                renderPanel.showHidden     = checkboxes[1].isSelected
                renderPanel.showModels     = checkboxes[2].isSelected
                renderPanel.repaint()
            }
            optionsPanel.add(cb)
        }

        val renderPanelRow = JPanel(BorderLayout(0, 0)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            border = BorderFactory.createLoweredBevelBorder()
        }
        val renderLabel = JLabel("Render").apply {
            preferredSize = Dimension(100, preferredSize.height)
            border = BorderFactory.createRaisedSoftBevelBorder()
        }
        val modeCombo = JComboBox(arrayOf("Preview", "Edit")).apply {
            background = Color.WHITE
            selectedIndex = 0
            addActionListener {
                InterfaceDraw.editMode = (selectedItem == "Edit")
                renderPanel.repaint()
            }
        }

        renderPanelRow.add(renderLabel, BorderLayout.WEST)
        renderPanelRow.add(modeCombo, BorderLayout.CENTER)

        optionsPanel.add(Box.createVerticalStrut(5))
        optionsPanel.add(renderPanelRow)

        val container = JPanel(BorderLayout()).apply {
            add(tabsPanel, BorderLayout.NORTH)
            add(componentScrollPane, BorderLayout.CENTER)
            add(optionsPanel, BorderLayout.SOUTH)
        }

        rightPanel.add(container, BorderLayout.CENTER)
        updateTabs()
    }

    private fun updateTabs()
    {
        treeTab.background = if (treeView) Color.LIGHT_GRAY else null
        listTab.background = if (!treeView) Color.LIGHT_GRAY else null
        updateComponentView()
    }

    private fun selectComponent(comp: ComponentDefinition)
    {
        selectedComponent = comp
        renderPanel.selectedComponent = comp
        buildPropertyPanel(comp)
        renderPanel.repaint()
    }

    private fun loadComponents(interfaceId: Int) {
        clearSelection()
        InterfaceDraw.prepareInterface(interfaceId)

        orderedComponents = getOrderedComponents(interfaceId)

        val nodeMap = mutableMapOf<Int, DefaultMutableTreeNode>()
        componentTreeRoot.removeAllChildren()
        componentListModel.clear()

        orderedComponents.forEach { comp ->
            val node = DefaultMutableTreeNode(comp)
            nodeMap[comp.componentHash] = node
            val parentNode = if (comp.parentId == -1) componentTreeRoot else nodeMap[comp.parentId] ?: componentTreeRoot
            parentNode.add(node)
            componentListModel.addElement(comp)
        }

        treeModel.reload()
        componentTree.expandRow(0)
        renderPanel.repaint()
    }

    private fun clearSelection()
    {
        selectedComponent = null
        renderPanel.selectedComponent = null
        componentTree.clearSelection()
        componentList.clearSelection()
    }

    private fun updateComponentView()
    {
        componentScrollPane.setViewportView(if (treeView) componentTree else componentList)
        if (!treeView) rebuildListModel()
    }

    private fun rebuildListModel()
    {
        componentListModel.clear()
        orderedComponents.forEach { componentListModel.addElement(it) }
    }

    private fun loadInterfaces()
    {
        interfacesListModel.clear()

        val store = Cache.getStore() ?: return
        val index3 = store.indexes[3]

        for (i in 0..index3.lastArchiveId)
        {
            val data = index3.getFile(i, 0) ?: continue
            val version = data[0].toInt() and 0xFF
            if (version == 255) {
                interfacesListModel.addElement(i)
            }
        }
    }

    private fun getOrderedComponents(interfaceId: Int): List<ComponentDefinition>
    {
        val comps = mutableListOf<ComponentDefinition>()

        val containers = ComponentDefinition.getInterfaceContainers(interfaceId) ?: emptyList()
        val allComps = ComponentDefinition.getInterface(interfaceId) ?: return emptyList()
        for (container in containers) {
            if (!comps.contains(container)) {
                comps.add(container)
            }

            ComponentDefinition.getChildsByParent(interfaceId, container.componentHash)?.forEach { child ->
                comps.add(child)
            }
        }

        for (comp in allComps.filterNotNull()) {
            InterfaceDraw.setValues(comp)

            val found = comps.any { it.componentId == comp.componentId }
            if (!found) comps.add(comp)
        }

        return comps
    }

    private fun createNewInterfaceButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        val newId = ComponentDefinition.getInterfaceDefinitionsSize()
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("New")
        btn.addActionListener {
            val comp = ComponentDefinition().apply {
                componentId = newId
                type = ComponentType.SPRITE
                optionBase = ""
                baseX = 0
                baseY = 0
                baseWidth = 18
                baseHeight = 18
                spriteId = 18
                parentId = -1
            }
            Cache.getStore()!!.indexes[3].putFile(comp.componentId, 0, comp.encodeIf3())
            ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
            interfacesListModel.addElement(newId)
            interfacesList.setSelectedValue(newId, true)
            interfacesList.ensureIndexIsVisible(interfacesListModel.size - 1)
            renderPanel.interfaceId = newId
            loadComponents(newId)
        }

        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createDeleteInterfaceButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Clean")
        btn.addActionListener {
            val interfaceId = interfacesList.selectedValue ?: return@addActionListener
            val confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clean interface $interfaceId?", "Warning", JOptionPane.YES_NO_OPTION)
            if (confirm != JOptionPane.YES_OPTION) return@addActionListener
            val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)
            for (i in 0 until size) {
                if (i == 0) {
                    val base = ComponentDefinition.getInterfaceComponent(interfaceId, 0)
                    base?.parentId = -1
                    Cache.getStore()!!.indexes[3]
                        .putFile(interfaceId, 0, base!!.encodeIf3())
                } else {
                    Cache.getStore()!!.indexes[3]
                        .removeFile(interfaceId, i)
                }
            }
            try {
                Cache.init(PropertyValues.cache_path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsSize())
            loadComponents(interfaceId)
            renderPanel.interfaceId = interfaceId
            renderPanel.repaint()
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddSpriteButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Sprite")
        btn.border = BorderFactory.createLineBorder(Consts.SPRITE_COLOR, 2)
        btn.addActionListener { addComponent(801, 4) }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddModelButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Model")
        btn.border = BorderFactory.createLineBorder(Consts.MODEL_COLOR, 2)
        btn.addActionListener { addComponent(205, 90) }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddLayerButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Layer")
        btn.border = BorderFactory.createLineBorder(Consts.LAYER_COLOR, 2)
        btn.addActionListener {
            addComponent(778, 0) {
                it.baseX = 0
                it.baseY = 0
                it.baseWidth = 50
                it.baseHeight = 50
            }
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddFigureButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Figure")
        btn.border = BorderFactory.createLineBorder(Consts.FIGURE_COLOR, 2)
        btn.addActionListener {
            addComponent(625, 0) {
                it.baseX = 0
                it.baseY = 0
            }
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddTextButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Text")
        btn.border = BorderFactory.createLineBorder(Consts.TEXT_COLOR, 2)
        btn.addActionListener {
            addComponent(752, 5) {
                it.baseX = 0
                it.baseY = 0
            }
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddLineButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Line")
        btn.border = BorderFactory.createLineBorder(Consts.LINE_COLOR, 2)
        btn.addActionListener {
            addComponent(658, 39) {
                it.baseX = 0
                it.baseY = 0
            }
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createSaveButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Save")
        btn.addActionListener {
            if (selectedComponent!!.interfaceId != -1 && selectedComponent!!.componentId != -1) save(selectedComponent!!)
            else JOptionPane.showMessageDialog(this, "Please select a component & interface before saving it.")
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPrintComponentButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Print")
        btn.addActionListener {
            if(selectedComponent!!.interfaceId <= 0) return@addActionListener
            GenericPrinter.printComponent(selectedComponent!!.interfaceId, selectedComponent!!.componentId)
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPrintInterfaceButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Print")
        btn.addActionListener {
            if(selectedComponent!!.interfaceId <= 0) return@addActionListener
            GenericPrinter.printInterface(selectedComponent!!.interfaceId)
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createCopyButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Copy")
        btn.toolTipText = "Copy selected interface."
        val comp = selectedComponent
        btn.addActionListener {
            if (comp!!.interfaceId <= 0 || comp == null) return@addActionListener
            clonedComponent = ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)
            clonedComponent?.let {
                println("Component [${it.componentId}] copied from interface [${it.interfaceId}]")
            }
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPasteButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Paste")
        btn.toolTipText = "Paste your copied component into the selected interface"
        val comp = selectedComponent
        btn.addActionListener {
            if (comp!!.interfaceId <= 0 || comp == null) return@addActionListener
            InterfaceUtils.pasteComponent()
            InterfaceDraw.setValues(comp)
            repaint()
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createSpriteLibraryButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Sprites")
        btn.addActionListener {
            SpriteManager(this) { spriteId, w, h, frameId ->
                val comp = selectedComponent
                if (comp!!.interfaceId <= 0 || comp == null) return@SpriteManager
                comp.spriteId = (spriteId ?: frameId)!!
                comp.baseWidth = w
                comp.baseHeight = h
                InterfaceDraw.setValues(comp)
                repaint()
            }
        }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createColorPickButton(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Color")

        btn.addActionListener {
            val comp = selectedComponent ?: return@addActionListener
            val colorField = fieldMap["Color"] as? JTextField ?: return@addActionListener
            val picker = ColorPicker(comp, colorField) {
                set("Color", comp.color)
                InterfaceDraw.setValues(comp)
                repaint()
            }
            picker.pack()
            picker.setLocationRelativeTo(null)
            picker.isVisible = true
        }

        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun initPropertyLayout()
    {
        val standardSection = createSection(
            "Standard",
            listOf("Hidden", "Hash", "Type", "ContentType", "ParentID", "X", "Y", "Width", "Height", "WidthMode", "HeightMode", "XMode", "YMode"))
        addSection(standardSection)

        val spriteSection = createSection(
            "Sprite",
            listOf("SpriteID", "Transparency", "Border", "HFlip", "VFlip", "Repeat", "HasAlpha", "Color", "ShadowColor"))
        addSection(spriteSection)

        val textSection = createSection(
            "Text",
            listOf("Text", "FontID", "Color", "XAlign", "YAlign", "VPadding", "Shadow", "OptionMask"))
        addSection(textSection)

        val modelSection = createSection(
            "Model",
            listOf("ModelID", "AnimID", "XAngle", "YAngle", "YOffset", "Zoom", "Orthogonal", "DynamicWidth", "DynamicHeight", "UnknownProp5"))
        addSection(modelSection)

        val scriptSection = createSection(
            "Scripts",
            listOf("Load", "MouseHover", "MouseLeave", "OptionClick", "UseWith", "Use", "VarpTransmit", "InvTransmit", "StatTransmit", "Timer", "MouseRepeat", "ClickRepeat", "Drag", "Release", "Hold", "DragStart", "DragRelease", "Scroll", "VarcTransmit", "VarcStrTransmit"))
        addSection(scriptSection)

        val optionSection = createSection(
            "Options",
            listOf("LeftClick", "Option1", "Option2", "Option3", "Option4", "Option5"))
        addSection(optionSection)

        val triggerSection = createSection(
            "Triggers",
            listOf("VarpTriggers", "InventoryTriggers", "StatTriggers", "VarcTriggers", "VarcStrTriggers"))
        addSection(triggerSection)

        val layerSection = createSection(
            "Layer",
            listOf("ScrollMaxH", "ScrollMaxV", "NoClickThrough"))
        addSection(layerSection)

        val lineSection = createSection(
            "Line",
            listOf("Color", "LineWidth", "UnknownProp8"))
        addSection(lineSection)

        val figureSection = createSection(
            "Figure",
            listOf("Color", "Transparency", "Filled"))
        addSection(figureSection)
    }

    private fun buildPropertyPanel(comp: ComponentDefinition)
    {
        var c = ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)!!

        set("Hash",             c.componentHash)
        set("Type",             Utils.getComponentType(c.type))
        set("ContentType",      c.contentType)
        set("ParentID",         c.parentId)
        set("X",                c.baseX)
        set("Y",                c.baseY)
        set("Width",            c.baseWidth)
        set("Height",           c.baseHeight)
        set("WidthMode",        c.aspectWidthType)
        set("HeightMode",       c.aspectHeightType)
        set("XMode",            c.xMode)
        set("YMode",            c.yMode)
        set("Hidden",           c.hidden)

        set("LeftClick", "")
        set("Option1",   "")
        set("Option2",   "")
        set("Option3",   "")
        set("Option4",   "")
        set("Option5",   "")
        comp.rightClickOptions?.forEachIndexed { i, v ->
            when (i) {
                0 -> set("LeftClick", v)
                1 -> set("Option1",   v)
                2 -> set("Option2",   v)
                3 -> set("Option3",   v)
                4 -> set("Option4",   v)
                5 -> set("Option5",   v)
            }
        }

        set("ScrollMaxH",       c.scrollMaxH)
        set("ScrollMaxV",       c.scrollMaxV)
        set("NoClickThrough",   c.noClickThrough)

        set("SpriteID",         c.spriteId)
        set("Transparency",     c.transparency)
        set("Border",           c.borderThickness)
        set("HFlip",            c.hFlip)
        set("VFlip",            c.vFlip)
        set("Repeat",           c.spriteTiling)
        set("HasAlpha",         c.hasAlpha)
        set("Color",            c.color)
        set("ShadowColor",      c.shadowColor)

        set("Text",             c.text)
        set("FontID",           c.fontId)
        set("XAlign",           c.textHorizontalAli)
        set("YAlign",           c.textVerticalAli)
        set("VPadding",         c.vPadding)
        set("Shadow",           c.shadow)
        set("OptionMask",       c.optionMask)

        set("ModelID",          c.modelId)
        set("AnimID",           c.modelAnimId)
        set("XAngle",           c.modelXAngle)
        set("YAngle",           c.modelYAngle)
        set("YOffset",          c.modelYOffset)
        set("Zoom",             c.modelZoom)
        set("Orthogonal",       c.modelOrtho)
        set("DynamicWidth",     c.dynamicWidth)
        set("DynamicHeight",    c.dynamicHeight)
        set("UnknownProp5",     c.unknownModelProp5)

        set("LineWidth",        c.lineWidth)
        set("UnknownProp8",     c.unknownProp_8)

        set("Transparency",     c.transparency)
        set("Filled",           c.filled)

        set("Load",             c.onLoadScript?.joinToString(";"))
        set("MouseHover",       c.onMouseHoverScript?.joinToString(";"))
        set("MouseLeave",       c.onMouseLeaveScript?.joinToString(";"))
        set("OptionClick",      c.onOptionClick?.joinToString(";"))
        set("UseWith",          c.onUseWith?.joinToString(";"))
        set("Use",              c.onUse?.joinToString(";"))
        set("VarpTransmit",     c.onVarpTransmit?.joinToString(";"))
        set("InvTransmit",      c.onInvTransmit?.joinToString(";"))
        set("StatTransmit",     c.onStatTransmit?.joinToString(";"))
        set("Timer",            c.onTimer?.joinToString(";"))
        set("MouseRepeat",      c.onMouseRepeat?.joinToString(";"))
        set("ClickRepeat",      c.onClickRepeat?.joinToString(";"))
        set("Drag",             c.onDrag?.joinToString(";"))
        set("Release",          c.onRelease?.joinToString(";"))
        set("Hold",             c.onHold?.joinToString(";"))
        set("DragStart",        c.onDragStart?.joinToString(";"))
        set("DragRelease",      c.onDragRelease?.joinToString(";"))
        set("Scroll",           c.onScroll?.joinToString(";"))
        set("VarcTransmit",     c.onVarcTransmit?.joinToString(";"))
        set("VarcStrTransmit",  c.onVarcStrTransmit?.joinToString(";"))

        set("VarpTriggers",     c.varpTriggers?.joinToString(";"))
        set("InventoryTriggers",c.inventoryTriggers?.joinToString(";"))
        set("StatTriggers",     c.statTriggers?.joinToString(";"))
        set("VarcTriggers",     c.varcTriggers?.joinToString(";"))
        set("VarcStrTriggers",  c.varcstrTriggers?.joinToString(";"))
    }

    private fun save(comp: ComponentDefinition)
    {
        var c = selectedComponent?.let { ComponentDefinition.getInterfaceComponent(comp.interfaceId, it.componentId) }
        fieldMap.forEach { (name, component) ->
            val text = when (component) {
                is JTextField -> component.text
                is JCheckBox -> component.isSelected.toString()
                else -> return@forEach
            }
            when (name) {
                "X"              -> c?.baseX          = text.toIntOrNull() ?: 0
                "Y"              -> c?.baseY          = text.toIntOrNull() ?: 0
                "Width"          -> c?.baseWidth      = text.toIntOrNull() ?: 0
                "Height"         -> c?.baseHeight     = text.toIntOrNull() ?: 0
                "ParentID"       -> c?.parentId       = text.toIntOrNull() ?: -1
                "Transparency"   -> c?.transparency   = text.toIntOrNull() ?: 255
                "Color"          -> c?.color          = text.toIntOrNull() ?: 0
                "LineWidth"      -> c?.lineWidth      = text.toIntOrNull()?.toByte() ?: 0
                "ScrollMaxH"     -> c?.scrollMaxH     = text.toIntOrNull() ?: 0
                "ScrollMaxV"     -> c?.scrollMaxV     = text.toIntOrNull() ?: 0
                "SpriteID"       -> c?.spriteId       = text.toIntOrNull() ?: -1
                "ModelID"        -> c?.modelId        = text.toIntOrNull() ?: -1
                "AnimID"         -> c?.modelAnimId    = text.toIntOrNull() ?: -1
                "XAngle"         -> c?.modelXAngle    = text.toIntOrNull() ?: 0
                "YAngle"         -> c?.modelYAngle    = text.toIntOrNull() ?: 0
                "YOffset"        -> c?.modelYOffset   = text.toIntOrNull() ?: 0
                "Zoom"           -> c?.modelZoom      = text.toIntOrNull() ?: 100
                "Hidden"         -> c?.hidden         = text.toBoolean()
                "NoClickThrough" -> c?.noClickThrough = text.toBoolean()
                "HFlip"          -> c?.hFlip          = text.toBoolean()
                "VFlip"          -> c?.vFlip          = text.toBoolean()
                "Repeat"         -> c?.spriteTiling   = text.toBoolean()
                "HasAlpha"       -> c?.hasAlpha       = text.toBoolean()
                "Shadow"         -> c?.shadow         = text.toBoolean()
                "Filled"         -> c?.filled         = text.toBoolean()
                else -> {
                    if (
                        name in
                        listOf("Load", "MouseHover", "MouseLeave", "OptionClick", "UseWith", "Use", "VarpTransmit", "InvTransmit", "StatTransmit", "Timer", "MouseRepeat", "ClickRepeat", "Drag", "Release", "Hold", "DragStart", "DragRelease", "Scroll", "VarcTransmit", "VarcStrTransmit")
                    ) {
                        val arr = Utils.getScriptArray(text)
                        when (name) {
                            "Load"              -> c?.onLoadScript        = arr
                            "MouseHover"        -> c?.onMouseHoverScript  = arr
                            "MouseLeave"        -> c?.onMouseLeaveScript  = arr
                            "OptionClick"       -> c?.onOptionClick       = arr
                            "UseWith"           -> c?.onUseWith           = arr
                            "Use"               -> c?.onUse               = arr
                            "VarpTransmit"      -> c?.onVarpTransmit      = arr
                            "InvTransmit"       -> c?.onInvTransmit       = arr
                            "StatTransmit"      -> c?.onStatTransmit      = arr
                            "Timer"             -> c?.onTimer             = arr
                            "MouseRepeat"       -> c?.onMouseRepeat       = arr
                            "ClickRepeat"       -> c?.onClickRepeat       = arr
                            "Drag"              -> c?.onDrag              = arr
                            "Release"           -> c?.onRelease           = arr
                            "Hold"              -> c?.onHold              = arr
                            "DragStart"         -> c?.onDragStart         = arr
                            "DragRelease"       -> c?.onDragRelease       = arr
                            "Scroll"            -> c?.onScroll            = arr
                            "VarcTransmit"      -> c?.onVarcTransmit      = arr
                            "VarcStrTransmit"   -> c?.onVarcStrTransmit   = arr
                        }
                    }
                    else if (name in listOf("VarpTriggers", "InventoryTriggers", "StatTriggers", "VarcTriggers", "VarcStrTriggers"))
                    {
                        val arr = Utils.getConfigArray(text)
                        when (name) {
                            "VarpTriggers"      -> c?.varpTriggers        = arr
                            "InventoryTriggers" -> c?.inventoryTriggers   = arr
                            "StatTriggers"      -> c?.statTriggers        = arr
                            "VarcTriggers"      -> c?.varcTriggers        = arr
                            "VarcStrTriggers"   -> c?.varcstrTriggers     = arr
                        }
                    }
                }
            }
        }
    }

    private fun createSection(title: String, fields: List<String>): JPanel
    {
        val sectionPanel = JPanel(BorderLayout()).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            background = Color.WHITE
            isOpaque = true
        }

        val titlePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createLoweredBevelBorder()
            background = Color.WHITE
            add(JLabel(title, JLabel.LEFT), BorderLayout.CENTER)
            preferredSize = Dimension(Consts.SECTION_WIDTH, Consts.BUTTON_HEIGHT)
            maximumSize   = Dimension(Int.MAX_VALUE, Consts.BUTTON_HEIGHT)
        }
        sectionPanel.add(titlePanel, BorderLayout.NORTH)

        val contentPanel = JPanel(GridBagLayout()).apply {
            isOpaque = true
        }

        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        val labelWidth = 90
        val fieldWidth = 160
        val fieldHeight = Consts.BUTTON_HEIGHT

        fields.forEachIndexed { index, name ->
            val rowPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                alignmentX = Component.LEFT_ALIGNMENT
                preferredSize = Dimension(labelWidth + fieldWidth, fieldHeight)
            }

            val labelPanel = JPanel(BorderLayout()).apply {
                border = BorderFactory.createRaisedSoftBevelBorder()
                add(JLabel(name), BorderLayout.CENTER)
                preferredSize = Dimension(labelWidth, fieldHeight)
            }

            val fieldPanel = JPanel(BorderLayout()).apply {
                val field = JTextField().apply {
                    preferredSize = Dimension(fieldWidth, fieldHeight)
                }
                add(field, BorderLayout.CENTER)
                fieldMap[name] = field
                preferredSize = Dimension(fieldWidth, fieldHeight)
            }

            rowPanel.add(labelPanel)
            rowPanel.add(fieldPanel)

            gbc.gridy = index
            gbc.gridx = 0
            contentPanel.add(rowPanel, gbc)
        }

        val filler = JPanel()
        gbc.gridy = fields.size
        gbc.gridx = 0
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.VERTICAL
        contentPanel.add(filler, gbc)

        val scrollPane = JScrollPane(contentPanel).apply {
            verticalScrollBar.unitIncrement = 7
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = BorderFactory.createLoweredBevelBorder()
            background = Color.WHITE
            isOpaque = true
        }

        sectionPanel.add(scrollPane, BorderLayout.CENTER)
        sectionPanel.maximumSize   = Dimension(Int.MAX_VALUE, Consts.SECTION_HEIGHT)
        sectionPanel.preferredSize = Dimension(Consts.SECTION_WIDTH, Consts.SECTION_HEIGHT)
        return sectionPanel
    }

    private fun addSection(section: JPanel)
    {
        section.alignmentY = Component.TOP_ALIGNMENT
        propertyPanel.add(section)
    }

    fun set(name: String, value: Any?)
    {
        val field = fieldMap[name] ?: return
        when (field) {
            is JTextField -> field.text = value?.toString() ?: ""
            is JCheckBox  -> field.isSelected = value as? Boolean ?: false
        }

    }

    private fun addComponent(templateInterface: Int, templateComponentId: Int, setup: (ComponentDefinition) -> Unit = {})
    {
        val parentComp = InterfaceUI.instance?.selectedComponent
        if (parentComp == null) return
        val newId = ComponentDefinition.getInterfaceDefinitionsComponentsSize(parentComp.interfaceId)
        val templateComp = ComponentDefinition.getInterfaceComponent(templateInterface, templateComponentId) ?: return
        templateComp.parentId = if (parentComp.type == ComponentType.LAYER) parentComp.componentId else -1
        setup(templateComp)
        templateComp.componentHash = newId + (parentComp.interfaceId shl 16)
        buildPropertyPanel(templateComp)
        Cache.getStore()?.indexes?.get(3)?.putFile(parentComp.interfaceId, newId, templateComp.encode(true))
        ComponentDefinition.getInterface(parentComp.interfaceId, true)
        loadComponents(parentComp.interfaceId)
        selectComponent(templateComp)
        renderPanel.repaint()
    }
}