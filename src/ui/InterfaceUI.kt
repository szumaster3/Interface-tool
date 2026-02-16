package ui

import rs.cache.ComponentDefinition
import fs.com.alex.Cache
import fs.com.alex.utils.Utils
import properties.PropertyValues
import java.awt.*
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath
import util.GenericPrinter
import ui.draw.InterfaceDraw
import ui.draw.RenderPanel

class InterfaceUI : JFrame() {
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
    private var selectedComponent: ComponentDefinition?      = null
    private val propertyPanel       = JPanel()

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
        loadPanels()
        loadInterfaces()
    }

    private fun loadPanels()
    {
        val mainPanel = JPanel(BorderLayout())

        val topPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        }

        val leftPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
            preferredSize = Dimension(Consts.LEFT_PANEL_WIDTH, 0)
            add(JScrollPane(interfacesList), BorderLayout.CENTER)
        }

        interfacesList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        interfacesList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting)
            {
                interfacesList.selectedValue?.let { id ->
                    renderPanel.interfaceId = id
                    loadComponents(id)
                }
            }
        }

        val rightPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
            preferredSize = Dimension(Consts.RIGHT_PANEL_WIDTH, 0)
        }
        setupRightPanel(rightPanel)

        val centerWrapper = JPanel(GridBagLayout())
        centerWrapper.background = Color.DARK_GRAY
        centerWrapper.add(renderPanel)

        topPanel.add(leftPanel, BorderLayout.WEST)
        topPanel.add(centerWrapper, BorderLayout.CENTER)
        topPanel.add(rightPanel, BorderLayout.EAST)

        mainPanel.add(topPanel, BorderLayout.CENTER)

        val bottomPanel = JPanel(BorderLayout())

        val buttonPanel = JPanel(GridLayout(1, 0, 1, 1)).apply {
            border = BorderFactory.createEmptyBorder(1, 1, 1, 1)
        }

        listOf(
            createNewInterfaceButton(),
            createDeleteInterfaceButton(),
            createAddSpriteButton(),
            createAddLayerButton(),
            createAddFigureButton(),
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
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy   = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            border                    = BorderFactory.createEmptyBorder()
        }

        bottomPanel.add(sectionsScroll, BorderLayout.CENTER)

        mainPanel.add(bottomPanel, BorderLayout.SOUTH)

        add(mainPanel, BorderLayout.CENTER)
    }

    private fun setupRightPanel(rightPanel: JPanel)
    {
        val tabsPanel = JPanel(GridLayout(1, 2)).apply { preferredSize = Dimension(Consts.RIGHT_PANEL_WIDTH, Consts.BUTTON_HEIGHT) }
        val treeTab   = JButton("Tree")
        val listTab   = JButton("List")

        fun updateTabs()
        {
            treeTab.background = if (treeView)  Color.LIGHT_GRAY else null
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
        InterfaceDraw.prepareInterface(interfaceId)
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
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("New Interface")
        btn.addActionListener { println("Placeholder: New Interface") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createDeleteInterfaceButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Clean Interface")
        btn.addActionListener { println("Placeholder: Delete Interface") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddSpriteButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Add Sprite")
        btn.addActionListener { println("Placeholder: Add Sprite") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddLayerButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Add Layer")
        btn.addActionListener { println("Placeholder: Add Layer") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddFigureButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Add Figure")
        btn.addActionListener { println("Placeholder: Add Figure") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddTextButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Add Text")
        btn.addActionListener { println("Placeholder: Add Text") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createAddLineButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Add Line")
        btn.addActionListener { println("Placeholder: Add Line") }
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
        val btn = JButton("Print Component")
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
        val btn = JButton("Print Interface")
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
        btn.addActionListener { println("Placeholder: Copy") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createPasteButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Paste")
        btn.addActionListener { println("Placeholder: Paste") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createSpriteLibraryButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Sprite Library")
        btn.addActionListener { println("Placeholder: Sprite Library Button") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun createColorPickButton(): JPanel
    {
        val panel    = JPanel(BorderLayout())
        panel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        val btn = JButton("Colour Pallet")
        btn.addActionListener { println("Placeholder: PickButton") }
        panel.add(btn, BorderLayout.CENTER)
        return panel
    }

    private fun initPropertyLayout()
    {
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
        set("Ortho",            c.modelOrtho)
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
        val sectionPanel = JPanel(GridBagLayout())
        sectionPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createBevelBorder(BevelBorder.LOWERED),
            title
        )
        sectionPanel.alignmentX = Component.LEFT_ALIGNMENT

        val contentPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(1,1,1,1)
            weightx = 1.0
        }

        fields.forEachIndexed { index, name ->
            gbc.gridy = index
            gbc.gridx = 0
            gbc.weightx = 0.3
            contentPanel.add(JLabel(name), gbc)

            gbc.gridx = 1
            gbc.weightx = 0.7
            val field = JTextField(10)
            field.maximumSize = Dimension(Int.MAX_VALUE, Consts.BUTTON_HEIGHT)
            contentPanel.add(field, gbc)
            fieldMap[name] = field
        }

        val filler = JPanel()
        gbc.gridy = fields.size
        gbc.gridx = 0
        gbc.gridwidth = 2
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.VERTICAL
        contentPanel.add(filler, gbc)

        val scrollPane = JScrollPane(contentPanel).apply {
            verticalScrollBar.unitIncrement = 7
            verticalScrollBarPolicy         = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy       = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border                          = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        }

        val outerPanel = JPanel(BorderLayout())
        outerPanel.add(scrollPane, BorderLayout.CENTER)
        outerPanel.maximumSize   = Dimension(Int.MAX_VALUE, Consts.SECTION_HEIGHT)
        outerPanel.preferredSize = Dimension(Consts.SECTION_WIDTH, Consts.SECTION_HEIGHT)

        return outerPanel
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

}
