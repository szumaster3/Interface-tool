package ui

import cache.ComponentDefinition
import fs.com.alex.Cache
import fs.com.alex.utils.Utils
import java.awt.*
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import properties.PropertyValues.cache_path
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath

class IfaceUI : JFrame() {
    private var treeView = true
    private val interfacesListModel = DefaultListModel<Int>()
    private val interfacesList      = JList(interfacesListModel)
    private val componentTreeRoot   = DefaultMutableTreeNode("Components")
    private val treeModel           = DefaultTreeModel(componentTreeRoot)
    private val componentTree       = JTree(treeModel)
    private val componentListModel  = DefaultListModel<ComponentDefinition>()
    private val componentList       = JList(componentListModel)
    private val componentScrollPane = JScrollPane(componentTree)
    private val fieldMap            = mutableMapOf<String, JComponent>()
    private val renderPanel         = RenderPanel()
    private var orderedComponents: List<ComponentDefinition> = emptyList()
    private var selectedComponent: ComponentDefinition? = null
    private val propertyTabs        = JTabbedPane()
    private val propertyPanel       = JPanel()
    private var sectionIndex        = 0
    private val MAX_COLUMNS         = 2

    companion object {
        @JvmStatic var instance: IfaceUI? = null

        @JvmStatic
        fun main(args: Array<String>)
        {
            EventQueue.invokeLater {
                try {
                    if (args.isEmpty()) loadValues() else setCachePath(args[0])
                    Cache.init(cache_path)
                    instance =
                        IfaceUI().apply {
                            setLocationRelativeTo(null)
                            isVisible = true
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }

    }

    init
    {
        title = "Interface Editor"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(930, 400)
        layout = BorderLayout()
        loadPanels()
        loadInterfaces()
    }

    private fun loadPanels()
    {
        val mainPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }

        val leftPanel =
            JPanel(BorderLayout()).apply {
                border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
                preferredSize = Dimension(UIConsts.LEFT_PANEL_WIDTH, UIConsts.DEFAULT_HEIGHT)
                add(JScrollPane(interfacesList), BorderLayout.CENTER)
            }

        interfacesList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        interfacesList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                interfacesList.selectedValue?.let { id ->
                    renderPanel.interfaceId = id
                    loadComponents(id)
                }
            }
        }

        propertyPanel.layout = GridBagLayout()
        propertyPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        initPropertyLayout()
        propertyTabs.addTab("Properties", JScrollPane(propertyPanel))
        propertyTabs.addTab("Events", JPanel())
        propertyTabs.addTab("Advanced", JPanel())

        val centerPanel = createCenterPanel()

        val rightPanel =
            JPanel(BorderLayout()).apply {
                border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
                preferredSize = Dimension(UIConsts.RIGHT_PANEL_WIDTH, UIConsts.DEFAULT_HEIGHT)
            }

        setupRightPanel(rightPanel)

        mainPanel.add(leftPanel)
        mainPanel.add(centerPanel)
        mainPanel.add(rightPanel)

        add(mainPanel, BorderLayout.CENTER)
    }

    private fun createCenterPanel(): JPanel
    {
        val renderScroll = JScrollPane(renderPanel)

        val verticalSplit = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            renderScroll,
            propertyTabs
        )

        verticalSplit.resizeWeight = 0.7
        verticalSplit.dividerSize = 6
        verticalSplit.border = null

        return JPanel(BorderLayout()).apply {
            border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
            add(verticalSplit, BorderLayout.CENTER)
        }
    }

    private fun setupRightPanel(rightPanel: JPanel)
    {
        val tabsPanel = JPanel(GridLayout(1, 2)).apply { preferredSize = Dimension(UIConsts.RIGHT_PANEL_WIDTH, 30) }
        val treeTab = JButton("Tree")
        val listTab = JButton("List")

        fun updateTabs() {
            treeTab.background = if (treeView) Color.LIGHT_GRAY else null
            listTab.background = if (!treeView) Color.LIGHT_GRAY else null
            updateComponentView()
        }

        treeTab.addActionListener {
            treeView = true
            updateTabs()
        }
        listTab.addActionListener {
            treeView = false
            updateTabs()
        }

        tabsPanel.add(treeTab)
        tabsPanel.add(listTab)

        componentTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        componentTree.addTreeSelectionListener {
            val node = componentTree.lastSelectedPathComponent as? DefaultMutableTreeNode
            val comp = node?.userObject as? ComponentDefinition
            comp?.let { selectComponent(it) }
        }

        componentList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        componentList.addListSelectionListener { e -> if (!e.valueIsAdjusting) componentList.selectedValue?.let { selectComponent(it) } }

        val optionsPanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
            }
        val checkboxes =
            listOf(
                JCheckBox("Show Layers", renderPanel.showContainers),
                JCheckBox("Show Hidden", renderPanel.showHidden),
                JCheckBox("Show Models", renderPanel.showModels)
            )
        checkboxes.forEach { cb ->
            cb.addActionListener {
                renderPanel.showContainers = checkboxes[0].isSelected
                renderPanel.showHidden = checkboxes[1].isSelected
                renderPanel.showModels = checkboxes[2].isSelected
                renderPanel.repaint()
            }
            optionsPanel.add(cb)
        }

        val container =
            JPanel(BorderLayout()).apply {
                add(tabsPanel, BorderLayout.NORTH)
                add(componentScrollPane, BorderLayout.CENTER)
                add(optionsPanel, BorderLayout.SOUTH)
            }
        rightPanel.add(container, BorderLayout.CENTER)

        updateTabs()
    }

    private fun selectComponent(comp: ComponentDefinition)
    {
        selectedComponent = comp
        renderPanel.selectedComponent = comp
        buildPropertyPanel(comp)
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
        val size = ComponentDefinition.getInterfaceDefinitionsSize()
        interfacesListModel.clear()
        for (i in 0 until size) interfacesListModel.addElement(i)
    }

    private fun loadComponents(interfaceId: Int)
    {
        clearSelection()
        IfaceDraw.prepareInterface(interfaceId)
        orderedComponents = buildOrderedComponents(interfaceId)
        componentTreeRoot.removeAllChildren()
        val nodeMap = mutableMapOf<Int, DefaultMutableTreeNode>()
        orderedComponents.forEach { comp ->
            val node = DefaultMutableTreeNode(comp)
            nodeMap[comp.componentHash] = node
            val parentNode = if (comp.parentId == -1) componentTreeRoot else nodeMap[comp.parentId] ?: componentTreeRoot
            parentNode.add(node)
        }

        treeModel.reload()
        componentTree.expandRow(0)
        rebuildListModel()
        renderPanel.repaint()
    }

    private fun buildOrderedComponents(interfaceId: Int): List<ComponentDefinition>
    {
        val comps = mutableListOf<ComponentDefinition>()
        val allComps = ComponentDefinition.getInterface(interfaceId, false) ?: return emptyList()

        ComponentDefinition.getInterfaceContainers(interfaceId)?.forEach { container ->
            comps.addIfAbsent(container)
            ComponentDefinition.getChildsByParent(interfaceId, container.componentHash).forEach { comps.addIfAbsent(it) }
        }
        allComps.filterNotNull().forEach { comp -> comps.addIfAbsent(comp) }
        return comps
    }

    private fun <T> MutableList<T>.addIfAbsent(item: T)
    {
        if (!contains(item)) add(item)
    }

    private fun createNewInterfaceButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "New Interface"
        )
        val btn = JButton("New Interface")
        btn.addActionListener { println("Placeholder: New Interface") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createDeleteInterfaceButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Delete Interface"
        )
        val btn = JButton("Delete Interface")
        btn.addActionListener { println("Placeholder: Delete Interface") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddSpriteButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Add Sprite"
        )
        val btn = JButton("Add Sprite")
        btn.addActionListener { println("Placeholder: Add Sprite") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddLayerButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Add Layer"
        )
        val btn = JButton("Add Layer")
        btn.addActionListener { println("Placeholder: Add Layer") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddFigureButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Add Figure"
        )
        val btn = JButton("Add Figure")
        btn.addActionListener { println("Placeholder: Add Figure") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddTextButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Add Text"
        )
        val btn = JButton("Add Text")
        btn.addActionListener { println("Placeholder: Add Text") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddLineButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Add Line"
        )
        val btn = JButton("Add Line")
        btn.addActionListener { println("Placeholder: Add Line") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createSaveButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Save"
        )
        val btn = JButton("Save")
        btn.addActionListener { selectedComponent?.let { saveDynamicFields(it) } }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPrintComponentButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Print Component"
        )
        val btn = JButton("Print Component")
        btn.addActionListener { println("Placeholder: Print Component") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPrintInterfaceButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Print Interface"
        )
        val btn = JButton("Print Interface")
        btn.addActionListener { println("Placeholder: Print Interface") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createCopyButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Copy"
        )
        val btn = JButton("Copy")
        btn.addActionListener { println("Placeholder: Copy") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPasteButton(): JPanel
    {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            "Paste"
        )
        val btn = JButton("Paste")
        btn.addActionListener { println("Placeholder: Paste") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun initPropertyLayout()
    {
        val topPanel = JPanel(GridLayout(0, 4, 4, 4))
        topPanel.add(createNewInterfaceButton())
        topPanel.add(createDeleteInterfaceButton())
        topPanel.add(createAddSpriteButton())
        topPanel.add(createAddLayerButton())
        topPanel.add(createAddFigureButton())
        topPanel.add(createAddTextButton())
        topPanel.add(createAddLineButton())
        topPanel.add(createSaveButton())
        topPanel.add(createPrintComponentButton())
        topPanel.add(createPrintInterfaceButton())
        topPanel.add(createCopyButton())
        topPanel.add(createPasteButton())
        propertyPanel.add(topPanel)

        val standardSection = createSection(
            "Standard",
            listOf("Hash", "Type", "ContentType", "ParentID", "X", "Y", "Width", "Height", "WidthMode", "HeightMode", "XMode", "YMode", "Hidden"))
        addSection(standardSection)

        val optionSection = createSection(
            "Options",
            listOf("LeftClick", "Option1", "Option2", "Option3", "Option4", "Option5"))
        addSection(optionSection)

        val layerSection = createSection(
            "Layer",
            listOf("ScrollMaxH", "ScrollMaxV", "NoClickThrough"))
        addSection(layerSection)

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
            listOf("ModelID", "AnimID", "XAngle", "YAngle", "YOffset", "Zoom", "Ortho", "DynamicWidth", "DynamicHeight", "UnknownProp5"))
        addSection(modelSection)

        val lineSection = createSection(
            "Line",
            listOf("Color", "LineWidth", "UnknownProp8"))
        addSection(lineSection)

        val figureSection = createSection(
            "Figure",
            listOf("Color", "Transparency", "Filled"))
        addSection(figureSection)

        val scriptSection = createSection(
            "Scripts",
            listOf("Load", "MouseHover", "MouseLeave", "OptionClick", "UseWith", "Use", "VarpTransmit", "InvTransmit", "StatTransmit", "Timer", "MouseRepeat", "ClickRepeat", "Drag", "Release", "Hold", "DragStart", "DragRelease", "Scroll", "VarcTransmit", "VarcStrTransmit"))
        addSection(scriptSection)

        val triggerSection = createSection(
            "Triggers",
            listOf("VarpTriggers", "InventoryTriggers", "StatTriggers", "VarcTriggers", "VarcStrTriggers"))
        addSection(triggerSection)
    }

    private fun buildPropertyPanel(comp: ComponentDefinition)
    {
        sectionIndex = 0
        propertyPanel.removeAll()

        set("Hash",             comp.componentHash)
        set("Type",             Utils.getComponentType(comp.type))
        set("ContentType",      comp.contentType)
        set("ParentID",         comp.parentId)
        set("X",                comp.baseX)
        set("Y",                comp.baseY)
        set("Width",            comp.baseWidth)
        set("Height",           comp.baseHeight)
        set("WidthMode",        comp.aspectWidthType)
        set("HeightMode",       comp.aspectHeightType)
        set("XMode",            comp.xMode)
        set("YMode",            comp.yMode)
        set("Hidden",           comp.hidden)

        comp.rightClickOptions?.forEachIndexed { i, v ->
            when (i) {
                0 -> set("LeftClick", v)
                1 -> set("Option1", v)
                2 -> set("Option2", v)
                3 -> set("Option3", v)
                4 -> set("Option4", v)
                5 -> set("Option5", v)
            }
        }

        set("ScrollMaxH",       comp.scrollMaxH)
        set("ScrollMaxV",       comp.scrollMaxV)
        set("NoClickThrough",   comp.noClickThrough)

        set("SpriteID",         comp.spriteId)
        set("Transparency",     comp.transparency)
        set("Border",           comp.borderThickness)
        set("HFlip",            comp.hFlip)
        set("VFlip",            comp.vFlip)
        set("Repeat",           comp.spriteTiling)
        set("HasAlpha",         comp.hasAlpha)
        set("Color",            comp.color)
        set("ShadowColor",      comp.shadowColor)

        set("Text",             comp.text)
        set("FontID",           comp.fontId)
        set("XAlign",           comp.textHorizontalAli)
        set("YAlign",           comp.textVerticalAli)
        set("VPadding",         comp.vPadding)
        set("Shadow",           comp.shadow)
        set("OptionMask",       comp.optionMask)

        set("ModelID",          comp.modelId)
        set("AnimID",           comp.modelAnimId)
        set("XAngle",           comp.modelXAngle)
        set("YAngle",           comp.modelYAngle)
        set("YOffset",          comp.modelYOffset)
        set("Zoom",             comp.modelZoom)
        set("Ortho",            comp.modelOrtho)
        set("DynamicWidth",     comp.dynamicWidth)
        set("DynamicHeight",    comp.dynamicHeight)
        set("UnknownProp5",     comp.unknownModelProp5)

        set("LineWidth",        comp.lineWidth)
        set("UnknownProp8",     comp.unknownProp_8)

        set("Transparency",     comp.transparency)
        set("Filled",           comp.filled)

        set("Load",             comp.onLoadScript?.joinToString(";"))
        set("MouseHover",       comp.onMouseHoverScript?.joinToString(";"))
        set("MouseLeave",       comp.onMouseLeaveScript?.joinToString(";"))
        set("OptionClick",      comp.onOptionClick?.joinToString(";"))
        set("UseWith",          comp.onUseWith?.joinToString(";"))
        set("Use",              comp.onUse?.joinToString(";"))
        set("VarpTransmit",     comp.onVarpTransmit?.joinToString(";"))
        set("InvTransmit",      comp.onInvTransmit?.joinToString(";"))
        set("StatTransmit",     comp.onStatTransmit?.joinToString(";"))
        set("Timer",            comp.onTimer?.joinToString(";"))
        set("MouseRepeat",      comp.onMouseRepeat?.joinToString(";"))
        set("ClickRepeat",      comp.onClickRepeat?.joinToString(";"))
        set("Drag",             comp.onDrag?.joinToString(";"))
        set("Release",          comp.onRelease?.joinToString(";"))
        set("Hold",             comp.onHold?.joinToString(";"))
        set("DragStart",        comp.onDragStart?.joinToString(";"))
        set("DragRelease",      comp.onDragRelease?.joinToString(";"))
        set("Scroll",           comp.onScroll?.joinToString(";"))
        set("VarcTransmit",     comp.onVarcTransmit?.joinToString(";"))
        set("VarcStrTransmit",  comp.onVarcStrTransmit?.joinToString(";"))

        set("VarpTriggers",     comp.varpTriggers?.joinToString(";"))
        set("InventoryTriggers",comp.inventoryTriggers?.joinToString(";"))
        set("StatTriggers",     comp.statTriggers?.joinToString(";"))
        set("VarcTriggers",     comp.varcTriggers?.joinToString(";"))
        set("VarcStrTriggers",  comp.varcstrTriggers?.joinToString(";"))
    }

    private fun saveDynamicFields(comp: ComponentDefinition)
    {
        fieldMap.forEach { (name, component) ->
            val text = when (component) {
                is JTextField -> component.text
                is JCheckBox -> component.isSelected.toString()
                else -> return@forEach
            }
            when (name) {
                "X"              -> comp.baseX = text.toIntOrNull() ?: 0
                "Y"              -> comp.baseY = text.toIntOrNull() ?: 0
                "Width"          -> comp.baseWidth = text.toIntOrNull() ?: 0
                "Height"         -> comp.baseHeight = text.toIntOrNull() ?: 0
                "ParentID"       -> comp.parentId = text.toIntOrNull() ?: -1
                "Transparency"   -> comp.transparency = text.toIntOrNull() ?: 255
                "Color"          -> comp.color = text.toIntOrNull() ?: 0
                "LineWidth"      -> comp.lineWidth = text.toIntOrNull()?.toByte() ?: 0
                "ScrollMaxH"     -> comp.scrollMaxH = text.toIntOrNull() ?: 0
                "ScrollMaxV"     -> comp.scrollMaxV = text.toIntOrNull() ?: 0
                "SpriteID"       -> comp.spriteId = text.toIntOrNull() ?: -1
                "ModelID"        -> comp.modelId = text.toIntOrNull() ?: -1
                "AnimID"         -> comp.modelAnimId = text.toIntOrNull() ?: -1
                "XAngle"         -> comp.modelXAngle = text.toIntOrNull() ?: 0
                "YAngle"         -> comp.modelYAngle = text.toIntOrNull() ?: 0
                "YOffset"        -> comp.modelYOffset = text.toIntOrNull() ?: 0
                "Zoom"           -> comp.modelZoom = text.toIntOrNull() ?: 100
                "Hidden"         -> comp.hidden = text.toBoolean()
                "NoClickThrough" -> comp.noClickThrough = text.toBoolean()
                "HFlip"          -> comp.hFlip = text.toBoolean()
                "VFlip"          -> comp.vFlip = text.toBoolean()
                "Repeat"         -> comp.spriteTiling = text.toBoolean()
                "HasAlpha"       -> comp.hasAlpha = text.toBoolean()
                "Shadow"         -> comp.shadow = text.toBoolean()
                "Filled"         -> comp.filled = text.toBoolean()
                else -> {
                    if (
                        name in
                        listOf("Load", "MouseHover", "MouseLeave", "OptionClick", "UseWith", "Use", "VarpTransmit", "InvTransmit", "StatTransmit", "Timer", "MouseRepeat", "ClickRepeat", "Drag", "Release", "Hold", "DragStart", "DragRelease", "Scroll", "VarcTransmit", "VarcStrTransmit")
                    ) {
                        val arr = Utils.getScriptArray(text)
                        when (name) {
                            "Load"              -> comp.onLoadScript = arr
                            "MouseHover"        -> comp.onMouseHoverScript = arr
                            "MouseLeave"        -> comp.onMouseLeaveScript = arr
                            "OptionClick"       -> comp.onOptionClick = arr
                            "UseWith"           -> comp.onUseWith = arr
                            "Use"               -> comp.onUse = arr
                            "VarpTransmit"      -> comp.onVarpTransmit = arr
                            "InvTransmit"       -> comp.onInvTransmit = arr
                            "StatTransmit"      -> comp.onStatTransmit = arr
                            "Timer"             -> comp.onTimer = arr
                            "MouseRepeat"       -> comp.onMouseRepeat = arr
                            "ClickRepeat"       -> comp.onClickRepeat = arr
                            "Drag"              -> comp.onDrag = arr
                            "Release"           -> comp.onRelease = arr
                            "Hold"              -> comp.onHold = arr
                            "DragStart"         -> comp.onDragStart = arr
                            "DragRelease"       -> comp.onDragRelease = arr
                            "Scroll"            -> comp.onScroll = arr
                            "VarcTransmit"      -> comp.onVarcTransmit = arr
                            "VarcStrTransmit"   -> comp.onVarcStrTransmit = arr
                        }
                    }
                    else if (name in listOf("VarpTriggers", "InventoryTriggers", "StatTriggers", "VarcTriggers", "VarcStrTriggers"))
                    {
                        val arr = Utils.getConfigArray(text)
                        when (name) {
                            "VarpTriggers"      -> comp.varpTriggers = arr
                            "InventoryTriggers" -> comp.inventoryTriggers = arr
                            "StatTriggers"      -> comp.statTriggers = arr
                            "VarcTriggers"      -> comp.varcTriggers = arr
                            "VarcStrTriggers"   -> comp.varcstrTriggers = arr
                        }
                    }
                }
            }
        }
    }

    private fun createSection(title: String, fields: List<String>): JPanel
    {
        val section = JPanel(GridBagLayout())
        section.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            title
        )

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.insets = Insets(2, 6, 2, 6)

        fields.forEachIndexed { index, name ->

            gbc.gridy = index
            gbc.gridx = 0
            gbc.weightx = 0.3
            section.add(JLabel(name), gbc)

            gbc.gridx = 1
            gbc.weightx = 0.7

            val field = JTextField()
            section.add(field, gbc)

            fieldMap[name] = field
        }

        return section
    }

    private fun addSection(section: JPanel)
    {
        val gbc = GridBagConstraints()

        gbc.gridx = sectionIndex % MAX_COLUMNS
        gbc.gridy = sectionIndex / MAX_COLUMNS

        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.BOTH
        gbc.insets = Insets(6, 6, 6, 6)

        propertyPanel.add(section, gbc)

        sectionIndex++
    }

    fun set(name: String, value: Any?)
    {
        val field = fieldMap[name] ?: return
        when (field) {
            is JTextField -> field.text = value?.toString() ?: ""
            is JCheckBox -> field.isSelected = value as? Boolean ?: false
        }

    }

}
