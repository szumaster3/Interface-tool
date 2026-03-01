package ui

import fs.com.alex.Cache
import fs.com.alex.utils.Utils
import properties.PropertyValues
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath
import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import ui.draw.ComponentRender
import ui.draw.InterfaceDraw
import ui.draw.RenderPanel
import ui.menu.InterfaceUIButton
import ui.menu.InterfaceUIMenu
import ui.selection.SelectionModel
import ui.selection.SelectionSource
import util.InterfaceUtils
import util.InterfaceUtils.iconButton
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class InterfaceUI : JFrame()
{
    private var treeView            = true
    val interfacesListModel         = DefaultListModel<Int>()
    val interfacesList              = JList(interfacesListModel)
    private val treeTab             = JButton("Tree View")
    private val listTab             = JButton("List View")
    private val componentTreeRoot   = DefaultMutableTreeNode("Components")
    private val treeModel           = DefaultTreeModel(componentTreeRoot)
    private val componentTree       = JTree(treeModel)
    private val componentListModel  = DefaultListModel<ComponentDefinition>()
    private val componentList       = JList(componentListModel)
    private val componentScrollPane = JScrollPane(componentTree)
    var fieldMap                    = mutableMapOf<String, JComponent>()
    val renderPanel                 = RenderPanel()
    private val propertyPanel       = JPanel()
    private val scriptsList         = listOf("Load", "MouseHover", "MouseLeave", "OptionClick", "UseWith", "Use", "VarpTransmit", "InvTransmit", "StatTransmit", "Timer", "MouseRepeat", "ClickRepeat", "Drag", "Release", "Hold", "DragStart", "DragRelease", "Scroll", "VarcTransmit", "VarcStrTransmit", "VarpTriggers", "InventoryTriggers", "StatTriggers", "VarcTriggers", "VarcStrTriggers")
    private val scriptsPanel        = JPanel().apply{layout = BoxLayout(this, BoxLayout.Y_AXIS)}

    private var orderedComponents: List<ComponentDefinition> = emptyList()

    private val undoIcon            = ImageIcon(javaClass.getResource("/icons/undo.png"))
    private val undoButton          = iconButton(undoIcon, "Undo")
    {
        renderPanel.repaint()
    }

    private val redoIcon            = ImageIcon(javaClass.getResource("/icons/redo.png"))
    private val redoButton          = iconButton(redoIcon, "Redo")
    {
        renderPanel.repaint()
    }

    companion object
    {
        var selectedComponent: ComponentDefinition? = null
        @JvmStatic var instance: InterfaceUI? = null

        @JvmStatic
        fun main(args: Array<String>)
        {
            EventQueue.invokeLater {
                try {
                    UIManager.put("Button.gradient", listOf(0.3f, 0.0f, Color(248, 248, 248) , Color(230, 230, 230) , Color(200, 200, 200)))
                    UIManager.put("MenuBar.gradient", listOf(0.25f, 0.0f, Color(180, 180, 180), Color(145, 145, 145), Color(105, 105, 105)))
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
        setupSelection()
        setupChange()
    }

    private fun setupSelection()
    {

        SelectionModel.addListener { comp ->

            if (comp == null) {
                componentTree.clearSelection()
                componentList.clearSelection()
                return@addListener
            }

            val path = findTreePathForComponent(comp)
            if (path != null) {
                componentTree.selectionPath = path
                componentTree.scrollPathToVisible(path)
            }

            componentList.setSelectedValue(comp, false)
            componentList.ensureIndexIsVisible(componentList.selectedIndex)

            updatePropertyFields(comp)

            selectedComponent = comp
            renderPanel.repaint()
        }
    }

    private fun findTreePathForComponent(comp: ComponentDefinition): TreePath?
    {
        fun search(node: DefaultMutableTreeNode): TreePath?
        {
            if (node.userObject == comp)
            {
                return TreePath(node.path)
            }
            for (i in 0 until node.childCount)
            {
                val result = search(node.getChildAt(i) as DefaultMutableTreeNode)
                if (result != null) return result
            }

            return null
        }

        val root = treeModel.root as? DefaultMutableTreeNode ?: return null
        return search(root)
    }

    private fun setupInterfaceListListener()
    {
        interfacesList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        interfacesList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                interfacesList.selectedValue?.let { id ->
                    loadComponents(id)
                }
            }
        }
    }

    fun loadComponents(interfaceId: Int)
    {
        InterfaceDraw.prepareInterface(interfaceId)
        InterfaceDraw.viewMode = InterfaceDraw.ViewMode.NONE
        InterfaceDraw.selectedType = null
        refreshTreeAndList()
        renderPanel.interfaceId = interfaceId
        renderPanel.repaint()
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

        refreshTreeAndList()
    }

    private fun refreshTreeAndList()
    {
        val allComponents = InterfaceDraw.getFilteredComponents()

        val filtered = when (InterfaceDraw.viewMode) {
            InterfaceDraw.ViewMode.NONE -> allComponents
            InterfaceDraw.ViewMode.BY_TYPE -> allComponents.filter {
                InterfaceDraw.selectedType == null || it.type == InterfaceDraw.selectedType
            }
        }
        val rootNode = DefaultMutableTreeNode("root")
        filtered.forEach { rootNode.add(buildTreeNode(it)) }
        treeModel.setRoot(rootNode)
        treeModel.reload()

        val listModel = DefaultListModel<ComponentDefinition>()
        fun addAllComponents(comp: ComponentDefinition) {
            val include = when (InterfaceDraw.viewMode) {
                InterfaceDraw.ViewMode.NONE -> true
                InterfaceDraw.ViewMode.BY_TYPE -> InterfaceDraw.selectedType == null || comp.type == InterfaceDraw.selectedType
            }
            if (include) listModel.addElement(comp)
            InterfaceDraw.childrenCache[comp.componentHash]?.forEach { addAllComponents(it) }
        }

        filtered.forEach { addAllComponents(it) }
        componentList.model = listModel

        componentTree.repaint()
        componentList.repaint()
    }

    private fun buildTreeNode(comp: ComponentDefinition): DefaultMutableTreeNode
    {
        val node = DefaultMutableTreeNode(comp)
        InterfaceDraw.childrenCache[comp.componentHash]?.forEach { child ->
            val shouldInclude = when (InterfaceDraw.viewMode) {
                InterfaceDraw.ViewMode.NONE -> true
                InterfaceDraw.ViewMode.BY_TYPE -> InterfaceDraw.selectedType == null || child.type == InterfaceDraw.selectedType
            }
            if (shouldInclude) node.add(buildTreeNode(child))
        }
        return node
    }

    private fun loadPanels()
    {
        val mainPanel = JPanel(BorderLayout())
        val leftPanel = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(Consts.LEFT_PANEL_WIDTH, 0)
            border = BorderFactory.createLoweredBevelBorder()
        }

        val searchPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createRaisedSoftBevelBorder()
        }
        val findIcon = ImageIcon(javaClass.getResource("/icons/find.png"))
        val searchField = JTextField()
        val searchButton = JButton(findIcon).apply {
            preferredSize = Dimension(40, Consts.BUTTON_HEIGHT_SMALL - 5)
        }
        searchPanel.add(searchField, BorderLayout.CENTER)
        searchPanel.add(searchButton, BorderLayout.EAST)

        val scrollInterfaces = JScrollPane(interfacesList).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }

        val interfacesWrapper = JPanel(BorderLayout())
        interfacesWrapper.add(searchPanel, BorderLayout.NORTH)
        interfacesWrapper.add(scrollInterfaces, BorderLayout.CENTER)
        leftPanel.add(interfacesWrapper, BorderLayout.CENTER)
        setupInterfaceListListener()

        val centerWrapper = JPanel(GridBagLayout()).apply {
            background = Color.LIGHT_GRAY
            add(renderPanel)
        }

        val rightPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createLoweredBevelBorder()
            preferredSize = Dimension(Consts.RIGHT_PANEL_WIDTH, 0)
        }
        setupRightPanel(rightPanel)

        val topPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createLoweredBevelBorder()
            add(leftPanel, BorderLayout.WEST)
            add(centerWrapper, BorderLayout.CENTER)
            add(rightPanel, BorderLayout.EAST)
        }
        mainPanel.add(topPanel, BorderLayout.CENTER)

        val bottomPanel = JPanel(BorderLayout())

        val buttonPanel = JPanel(GridLayout(1, 0, 1, 1)).apply {
            border = BorderFactory.createEmptyBorder(1, 1, 1, 1)
        }
        listOf(
            InterfaceUIButton.newInterfaceButton(),
            InterfaceUIButton.cleanInterfaceButton(),
            InterfaceUIButton.printButton(),
            InterfaceUIButton.addLayerButton(),
            InterfaceUIButton.addFigureButton(),
            InterfaceUIButton.addSpriteButton(),
            InterfaceUIButton.addModelButton(),
            InterfaceUIButton.addTextButton(),
            InterfaceUIButton.addLineButton(),
            InterfaceUIButton.copyButton(),
            InterfaceUIButton.pasteButton(),
            InterfaceUIButton.spriteLibraryButton(),
            InterfaceUIButton.deleteButton(),
            InterfaceUIButton.saveButton()
        ).forEach {
            it.preferredSize = Dimension(it.preferredSize.width, Consts.BUTTON_HEIGHT_SMALL)
            buttonPanel.add(it)
        }
        bottomPanel.border = BorderFactory.createLoweredBevelBorder()
        bottomPanel.add(buttonPanel, BorderLayout.NORTH)

        propertyPanel.removeAll()
        propertyPanel.layout = BoxLayout(propertyPanel, BoxLayout.X_AXIS)
        propertyPanel.border = BorderFactory.createEmptyBorder(1, 1, 1, 1)
        initPropertyLayout()

        val sectionsScroll = JScrollPane(propertyPanel).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBar.unitIncrement = 7
            horizontalScrollBar.unitIncrement = 20
            border = BorderFactory.createEmptyBorder()
        }
        bottomPanel.add(sectionsScroll, BorderLayout.CENTER)

        initScriptsPanel()

        val scriptsScroll = JScrollPane(scriptsPanel).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = BorderFactory.createEmptyBorder()
        }

        bottomPanel.add(scriptsScroll, BorderLayout.SOUTH)

        mainPanel.add(bottomPanel, BorderLayout.SOUTH)
        add(mainPanel, BorderLayout.CENTER)
        loadTree()

        searchButton.addActionListener {
            val text = searchField.text
            val id = text.toIntOrNull()
            if (id != null) {
                val index = (0 until interfacesList.model.size)
                    .firstOrNull { interfacesList.model.getElementAt(it) == id }
                if (index != null) {
                    interfacesList.selectedIndex = index
                    interfacesList.ensureIndexIsVisible(index)
                } else {
                    JOptionPane.showMessageDialog(this, "Interface $id not found.")
                }
            }
        }
    }

    private fun setupRightPanel(rightPanel: JPanel)
    {
        val tabsPanel = JPanel(GridLayout(1, 2)).apply {
            preferredSize = Dimension(Consts.RIGHT_PANEL_WIDTH, Consts.BUTTON_HEIGHT_SMALL)
            border = BorderFactory.createLoweredBevelBorder()
        }
        treeTab.addActionListener { treeView = true; updateTabs() }
        listTab.addActionListener { treeView = false; updateTabs() }
        tabsPanel.add(treeTab)
        tabsPanel.add(listTab)
        val bgColor = Color(30,30,30)
        componentTree.background = bgColor
        componentTree.isOpaque = true
        componentList.background = bgColor
        componentList.isOpaque = true

        val optionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        val showPanel = JPanel(FlowLayout(FlowLayout.LEFT, 1, 0))
        val showLayersCB = JCheckBox("Layers", renderPanel.showContainers).apply {
            addActionListener { renderPanel.showContainers = isSelected; renderPanel.repaint() }
        }
        val showHiddenCB = JCheckBox("Show Hidden", renderPanel.showHidden).apply {
            addActionListener { renderPanel.showHidden = isSelected; renderPanel.repaint() }
        }
        val showModelsCB = JCheckBox("Models", renderPanel.showModels).apply {
            addActionListener { renderPanel.showModels = isSelected; renderPanel.repaint() }
        }

        showPanel.add(showHiddenCB)
        showPanel.add(showLayersCB)
        showPanel.add(showModelsCB)
        showPanel.add(Box.createHorizontalStrut(8))
        showPanel.add(undoButton)
        showPanel.add(Box.createHorizontalStrut(4))
        showPanel.add(redoButton)

        showPanel.border = BorderFactory.createLoweredBevelBorder()
        optionsPanel.add(showPanel)

        val viewModeComboBox = JComboBox(arrayOf("Default", "Type")).apply {
            selectedIndex = 0
        }
        val typeFilterComboBox = JComboBox(arrayOf("Layer", "Sprite", "Figure", "Model", "Text", "Line")).apply {
            selectedIndex = 0
            isEnabled = false
        }
        val renderComboBox = JComboBox(arrayOf("Preview", "Edit")).apply {
            selectedIndex = 0
        }

        val comboRow = JPanel(GridLayout(1, 3, 0, 0))

        val viewModeLabel = JLabel("View by").apply {
            border = BorderFactory.createRaisedBevelBorder()
        }

        val viewModePanel = JPanel(BorderLayout()).apply {
            add(viewModeLabel, BorderLayout.WEST)
            add(viewModeComboBox, BorderLayout.CENTER)
        }

        val typeFilterLabel = JLabel("Filter").apply {
            border = BorderFactory.createRaisedBevelBorder()
        }
        val typeFilterPanel = JPanel(BorderLayout()).apply {
            add(typeFilterLabel, BorderLayout.WEST)
            add(typeFilterComboBox, BorderLayout.CENTER)
        }

        val renderLabel = JLabel("Render").apply {
            border = BorderFactory.createRaisedBevelBorder()
        }
        val renderPanelPanel = JPanel(BorderLayout()).apply {
            add(renderLabel, BorderLayout.WEST)
            add(renderComboBox, BorderLayout.CENTER)
        }

        comboRow.add(renderPanelPanel)
        comboRow.add(viewModePanel)
        comboRow.add(typeFilterPanel)
        comboRow.border = BorderFactory.createLoweredBevelBorder()
        optionsPanel.add(comboRow)

        viewModeComboBox.addActionListener {
            InterfaceDraw.viewMode = if (viewModeComboBox.selectedIndex == 1)
                InterfaceDraw.ViewMode.BY_TYPE else InterfaceDraw.ViewMode.NONE
            typeFilterComboBox.isEnabled = (InterfaceDraw.viewMode == InterfaceDraw.ViewMode.BY_TYPE)
            InterfaceDraw.selectedType = null
            refreshTreeAndList()
            renderPanel.repaint()
        }

        typeFilterComboBox.addActionListener {
            if (!typeFilterComboBox.isEnabled) return@addActionListener
            InterfaceDraw.selectedType = when (typeFilterComboBox.selectedItem as String) {
                "Layer"  -> ComponentType.LAYER
                "Sprite" -> ComponentType.SPRITE
                "Figure" -> ComponentType.FIGURE
                "Model"  -> ComponentType.MODEL
                "Text"   -> ComponentType.TEXT
                "Line"   -> ComponentType.LINE
                else -> null
            }
            refreshTreeAndList()
            renderPanel.repaint()
        }

        renderComboBox.addActionListener {
            InterfaceDraw.editMode = (renderComboBox.selectedItem == "Edit")
            renderPanel.repaint()
        }


        val container = JPanel(BorderLayout()).apply {
            add(tabsPanel, BorderLayout.NORTH)
            add(componentScrollPane, BorderLayout.CENTER)
            add(optionsPanel, BorderLayout.SOUTH)
        }

        rightPanel.add(container, BorderLayout.CENTER)
        updateTabs()
    }

    private fun setupChange()
    {
        renderPanel.changeListeners += { updateChanges() }

        undoButton.addActionListener { renderPanel.undo() }
        redoButton.addActionListener { renderPanel.redo() }

        updateChanges()
    }

    private fun updateChanges()
    {
        undoButton.isEnabled = renderPanel.hasUndo()
        redoButton.isEnabled = renderPanel.hasRedo()
    }

    private fun updateTabs()
    {
        treeTab.background = if (treeView) Color.LIGHT_GRAY else null
        listTab.background = if (!treeView) Color.LIGHT_GRAY else null
        updateComponentView()
    }

    fun selectComponent(comp: ComponentDefinition)
    {
        selectedComponent = comp
        SelectionModel.setSelection(comp, SelectionSource.DEFAULT)
        buildPropertyPanel(comp)
        renderPanel.repaint()
    }

    private fun clearSelection()
    {
        InterfaceDraw.clearCache()
        SelectionModel.setSelection(null, SelectionSource.DEFAULT)
        selectedComponent = null
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
        clearSelection()
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

    private fun initPropertyLayout() {
        val standardSection = createSection(
            "Standard",
            listOf("ParentID", "Hash", "Hidden", "Type", "ContentType", "X", "Y", "Width", "Height", "WidthMode", "HeightMode", "XMode", "YMode"),
            width = 200,
            labelWidth = 90,
            fieldHeight = 23
        )
        addSection(standardSection)

        val individualSection = createSection(
            "Individual properties",
            listOf("SpriteID", "BorderThickness", "HFlip", "VFlip", "Tiling", "HasAlpha", "Transparency", "Color", "Angle2D", "ShadowColor", "Text",  "FontID", "XAlign", "YAlign", "TextLineHeight", "Shadow", "ModelID", "AnimID", "ModelOriginX", "ModelOriginY", "XAngle", "YAngle", "ZAngle", "ModelZoom", "Orthogonal", "ModelObjectWidth", "ModelObjectHeight", "UnknownProp3", "UnknownProp4", "UnknownProp5", "Fill", "ScrollWidth", "ScrollHeight", "NoClickThrough", "DragDeadzone", "DragDeadtime", "DragRenderBehavior", "LineWidth", "UnknownProp8"),
            width = 250,
            labelWidth = 130,
            fieldHeight = 23
        )
        addSection(individualSection)

        val optionSection = createSection(
            "Options",
                    (1..10).map { "Option $it" } +
                    listOf("OptionBase", "OptionCircumfix", "UseOnGround", "UseOnNpc", "UseOnObject", "UseOnPlayer", "UseOnSelf", "UseOnIComp"),
            width = 200,
            labelWidth = 90,
            fieldHeight = 23
        )
        addSection(optionSection)

        val settingsSection = createSection(
            "Active properties",
            (1..10).map { "Slot $it" } + listOf("RightClickAllowed", "HasUnlockedSlots", "IEventDepth", "CanBeUsedOn", "UnknownBit21", "UnknownBits28to30", "UnknownBit31"),
            labelWidth = 90,
            width = 200,
            fieldHeight = 23
        )
        addSection(settingsSection)
    }


    private fun initScriptsPanel()
    {
        scriptsPanel.removeAll()

        val scriptSection = createSection(null,
            scriptsList,
            fieldHeight = 20,
            labelWidth = 110,
            height = 96
        )

        scriptsPanel.add(scriptSection)
    }

    private fun updatePropertyFields(comp: ComponentDefinition)
    {
        (fieldMap["baseWidth"] as? JTextField)?.text = comp.baseWidth.toString()
        (fieldMap["baseHeight"] as? JTextField)?.text = comp.baseHeight.toString()
        (fieldMap["baseX"] as? JTextField)?.text = comp.baseX.toString()
        (fieldMap["baseY"] as? JTextField)?.text = comp.baseY.toString()
    }

    private val componentTypes = mapOf(
        "SpriteID"        to listOf(ComponentType.SPRITE),
        "BorderThickness" to listOf(ComponentType.SPRITE),
        "Angle2D"         to listOf(ComponentType.SPRITE),
        "HFlip"           to listOf(ComponentType.SPRITE),
        "VFlip"           to listOf(ComponentType.SPRITE),
        "Tiling"          to listOf(ComponentType.SPRITE),
        "HasAlpha"        to listOf(ComponentType.SPRITE),
        "ShadowColor"     to listOf(ComponentType.SPRITE),
        "Transparency"    to listOf(ComponentType.FIGURE, ComponentType.SPRITE),

        "Text"            to listOf(ComponentType.TEXT),
        "FontID"          to listOf(ComponentType.TEXT),
        "XAlign"          to listOf(ComponentType.TEXT),
        "YAlign"          to listOf(ComponentType.TEXT),
        "TextLineHeight"  to listOf(ComponentType.TEXT),
        "Shadow"          to listOf(ComponentType.TEXT),
        "Color"           to listOf(ComponentType.TEXT, ComponentType.FIGURE, ComponentType.LINE),

        "ModelID"               to listOf(ComponentType.MODEL),
        "AnimID"                to listOf(ComponentType.MODEL),
        "ModelOriginX"          to listOf(ComponentType.MODEL),
        "ModelOriginY"          to listOf(ComponentType.MODEL),
        "XAngle"                to listOf(ComponentType.MODEL),
        "YAngle"                to listOf(ComponentType.MODEL),
        "ZAngle"                to listOf(ComponentType.MODEL),
        "ModelZoom"             to listOf(ComponentType.MODEL),
        "Orthogonal"            to listOf(ComponentType.MODEL),
        "ModelObjectWidth"      to listOf(ComponentType.MODEL),
        "ModelObjectHeight"     to listOf(ComponentType.MODEL),
        "UnknownProp3"          to listOf(ComponentType.MODEL),
        "UnknownProp4"          to listOf(ComponentType.MODEL),
        "UnknownProp5"          to listOf(ComponentType.MODEL),

        "Fill"            to listOf(ComponentType.FIGURE),

        "ScrollWidth"     to listOf(ComponentType.LAYER),
        "ScrollHeight"    to listOf(ComponentType.LAYER),
        "NoClickThrough"  to listOf(ComponentType.LAYER),

        "LineWidth"       to listOf(ComponentType.LINE),
        "UnknownProp8"    to listOf(ComponentType.LINE)
    )

    fun buildPropertyPanel(comp: ComponentDefinition)
    {
        var c = ComponentDefinition.getInterfaceComponent(comp.interfaceId, comp.componentId)!!

        set("Hash",             c.componentHash)
        set("Type", Utils.getComponentFromInt(c.type))
        val typeInt = c.type
        setComponentType(typeInt, componentTypes)

        when(typeInt)
        {
            ComponentType.SPRITE -> set("Transparency", c.transparency)
            ComponentType.FIGURE -> {set("Color", c.color);set("Transparency", c.transparency)}
            ComponentType.TEXT   -> set("Color", c.color)
            ComponentType.LINE   -> set("Color", c.color)
        }
        set("ContentType",      c.contentType)
        set("ParentID",         c.parentId)
        set("X",                c.baseX)
        set("Y",                c.baseY)
        set("Width",            c.baseWidth)
        set("Height",           c.baseHeight)
        set("WidthMode",        c.widthMode)
        set("HeightMode",       c.heightMode)
        set("XMode",            c.xMode)
        set("YMode",            c.yMode)
        set("Hidden",           c.hidden)

        val optionRange = 1..10
        optionRange.forEach { set("Option $it", "") }
        comp.rightClickOptions?.forEachIndexed { index, value ->
            val slot = index + 1
            if (slot in optionRange) set("Option $slot", value)
        }

        set("OptionBase", c.optionBase)
        set("OptionCircumfix", c.optionCircumfix)

        val s = c.settings

        set("RightClickAllowed",        s.rightClickAllowed())
        set("IEventDepth",              s.interfaceEventDepth())
        set("CanBeUsedOn",              s.canBeUsedOn())
        set("UnknownBit21",             s.unknownBit21())
        set("UnknownBits28to30",        s.unknownBits28to30())
        set("UnknownBit31",             s.unknownBit31())
        set("HasUnlockedSlots",         s.hasUnlockedSlots())

        for (i in 1..10) set("Slot $i", s.isRightClickOptionAllowed(i))

        set("UseOnGround",              s.canUseOnGroundItems())
        set("UseOnNpc",                 s.canUseOnNpcs())
        set("UseOnObject",              s.canUseOnObjects())
        set("UseOnPlayer",              s.canUseOnOtherPlayers())
        set("UseOnSelf",                s.canUseOnSelf())
        set("UseOnIComp",           s.canUseOnInterfaceComponent())

        set("ScrollWidth",       c.scrollWidth)
        set("ScrollHeight",       c.scrollHeight)
        set("NoClickThrough",   c.noClickThrough)

        set("SpriteID",         c.spriteId)
        set("Angle2D",          c.angle2d)
        set("BorderThickness",  c.borderThickness)
        set("HFlip",            c.hFlip)
        set("VFlip",            c.vFlip)
        set("Tiling",           c.spriteTiling)
        set("HasAlpha",         c.hasAlpha)

        set("ShadowColor",      c.shadowColor)

        set("Text",             c.text)
        set("FontID",           c.fontId)
        set("XAlign",           c.textHorizontalAli)
        set("YAlign",           c.textVerticalAli)
        set("TextLineHeight",   c.textLineHeight)
        set("Shadow",           c.shadow)

        set("DragDeadzone",         c.dragDeadzone)
        set("DragDeadtime",         c.dragDeadtime)
        set("DragRenderBehavior",   c.dragRenderBehavior)

        set("ModelID",              c.modelId)
        set("AnimID",               c.modelAnimId)
        set("ModelOriginX",         c.modelOriginX)
        set("ModelOriginY",         c.modelOriginY)
        set("XAngle",               c.modelXAngle)
        set("YAngle",               c.modelYAngle)
        set("ZAngle",               c.modelZAngle)
        set("ModelZoom",            c.modelZoom)
        set("Orthogonal",           c.modelOrtho)
        set("ModelObjectWidth",     c.modelObjWidth)
        set("ModelObjectHeight",    c.modelObjHeight)
        set("UnknownProp3",         c.unknownModelProp3)
        set("UnknownProp4",         c.unknownModelProp4)
        set("UnknownProp5",         c.unknownModelProp5)

        set("LineWidth",        c.lineWidth)
        set("UnknownProp8",     c.unknownProp_8)

        set("Fill",             c.filled)

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

    fun save(c: ComponentDefinition) {

        val s = c.settings
        var newSettings = s.settings

        fun setBit(bit: Int, enabled: Boolean)
        {
            newSettings = if (enabled) newSettings or (1 shl bit) else newSettings and (1 shl bit).inv()
        }

        fun getText(name: String): String {
            val comp = fieldMap[name]
            return when (comp) {
                is JTextField -> comp.text
                is JCheckBox -> comp.isSelected.toString()
                is JComboBox<*> -> comp.selectedItem?.toString() ?: ""
                is JSpinner -> (comp.value as? Number)?.toInt()?.toString() ?: "0"
                else -> ""
            }
        }

        for ((name, component) in fieldMap)
        {

            val text = getText(name)

            // Ops
            if (name.startsWith("Option "))
            {
                name.removePrefix("Option ").toIntOrNull()?.let { option ->
                    if (option in 1..10)
                    {
                        InterfaceUtils.updateRightClickOption(c, option - 1, text)
                    }
                }
                continue
            }

            // Slots
            if (name.startsWith("Slot "))
            {
                name.removePrefix("Slot ").toIntOrNull()?.let { slot ->
                    if (slot in 1..10)
                    {
                        setBit(slot, text.toBoolean())
                    }
                }
                continue
            }

            when (name)
            {

                // Standard
                "Hidden"        -> c.hidden = text.toBoolean()
                "Hash"          -> c.componentHash = text.toIntOrNull() ?: 0
                "Type"          -> c.type = Utils.getComponentFromString(text)
                "ContentType"   -> c.contentType = text.toIntOrNull() ?: 0
                "ParentID"      -> c.parentId = text.toIntOrNull() ?: -1
                "Color"         -> c.color = text.toIntOrNull() ?: 0
                "Transparency"  -> c.transparency = text.toIntOrNull() ?: 0
                "X"             -> c.baseX = text.toIntOrNull() ?: 0
                "Y"             -> c.baseY = text.toIntOrNull() ?: 0
                "Width"         -> c.baseWidth = text.toIntOrNull() ?: 0
                "Height"        -> c.baseHeight = text.toIntOrNull() ?: 0
                "WidthMode"     -> c.widthMode = text.toByteOrNull() ?: 0
                "HeightMode"    -> c.heightMode = text.toByteOrNull() ?: 0
                "XMode"         -> c.xMode = text.toByteOrNull() ?: 0
                "YMode"         -> c.yMode = text.toByteOrNull() ?: 0

                // Layer
                "ScrollWidth"      -> c.scrollWidth = text.toIntOrNull() ?: 0
                "ScrollHeight"     -> c.scrollHeight = text.toIntOrNull() ?: 0
                "NoClickThrough"   -> c.noClickThrough = text.toBoolean()

                // Drag
                "DragDeadzone"       -> c.dragDeadzone = text.toIntOrNull() ?: 0
                "DragDeadtime"       -> c.dragDeadtime = text.toIntOrNull() ?: 0
                "DragRenderBehavior" -> c.dragRenderBehavior = text.toBoolean()

                // Op
                "OptionBase"      -> c.optionBase = text
                "OptionCircumfix" -> c.optionCircumfix = text

                // Sprite
                "SpriteID"          -> c.spriteId = text.toIntOrNull() ?: -1
                "Angle2D"           -> c.angle2d = text.toIntOrNull() ?: 0
                "BorderThickness"   -> c.borderThickness = text.toIntOrNull() ?: 0
                "HFlip"             -> c.hFlip = text.toBoolean()
                "VFlip"             -> c.vFlip = text.toBoolean()
                "Tiling"            -> c.spriteTiling = text.toBoolean()
                "HasAlpha"          -> c.hasAlpha = text.toBoolean()
                "ShadowColor"       -> c.shadowColor = text.toIntOrNull() ?: 0

                // Text
                "Text"              -> c.text = text
                "FontID"            -> c.fontId = text.toIntOrNull() ?: -1
                "XAlign"            -> c.textHorizontalAli = text.toIntOrNull() ?: 0
                "YAlign"            -> c.textVerticalAli = text.toIntOrNull() ?: 0
                "TextLineHeight"    -> c.textLineHeight = text.toIntOrNull() ?: 0
                "Shadow"            -> c.shadow = text.toBoolean()

                // Active properties
                "RightClickAllowed" -> setBit(0, text.toBoolean())
                "UseOnGround"       -> setBit(11, text.toBoolean())
                "UseOnNpc"          -> setBit(12, text.toBoolean())
                "UseOnObject"       -> setBit(13, text.toBoolean())
                "UseOnPlayer"       -> setBit(14, text.toBoolean())
                "UseOnSelf"         -> setBit(15, text.toBoolean())
                "UseOnIComp"        -> setBit(16, text.toBoolean())

                "UnknownBit21" -> setBit(21, text.toBoolean())
                "CanBeUsedOn"  -> setBit(22, text.toBoolean())
                "UnknownBit31" -> setBit(31, text.toBoolean())

                "IEventDepth" -> {
                    newSettings = newSettings and (0x7 shl 18).inv()
                    val depth = text.toIntOrNull()?.coerceIn(0, 7) ?: 0
                    newSettings = newSettings or (depth shl 18)
                }

                "UnknownBits28to30" -> {
                    newSettings = newSettings and (0x7 shl 28).inv()
                    val value = text.toIntOrNull()?.and(0x7) ?: 0
                    newSettings = newSettings or (value shl 28)
                }

                "HasUnlockedSlots" -> {
                    newSettings = if (text.toBoolean())
                        newSettings or 0x2EAA42
                    else
                        newSettings and 0x2EAA42.inv()
                }

                // Model
                "ModelID"           -> c.modelId = text.toIntOrNull() ?: -1
                "AnimID"            -> c.modelAnimId = text.toIntOrNull() ?: -1
                "ModelOriginX"      -> c.modelOriginX = text.toIntOrNull() ?: -1
                "ModelOriginY"      -> c.modelOriginY = text.toIntOrNull() ?: -1
                "XAngle"            -> c.modelXAngle = text.toIntOrNull() ?: 0
                "YAngle"            -> c.modelYAngle = text.toIntOrNull() ?: 0
                "ZAngle"            -> c.modelZAngle = text.toIntOrNull() ?: 0
                "ModelZoom"         -> c.modelZoom = text.toIntOrNull() ?: 100
                "Orthogonal"        -> c.modelOrtho = text.toBoolean()
                "ModelObjectWidth"  -> c.modelObjWidth = text.toIntOrNull() ?: 0
                "ModelObjectHeight" -> c.modelObjHeight = text.toIntOrNull() ?: 0
                "UnknownProp3"      -> c.unknownModelProp3 = text.toIntOrNull() ?: 0
                "UnknownProp4"      -> c.unknownModelProp4 = text.toIntOrNull() ?: 0
                "UnknownProp5"      -> c.unknownModelProp5 = text.toBoolean()

                // Line
                "LineWidth"    -> c.lineWidth = text.toIntOrNull()?.toByte() ?: 0
                "UnknownProp8" -> c.unknownProp_8 = text.toBoolean()

                // Figure
                "Fill"         -> c.filled = text.toBoolean()
            }
        }

        c.settings.settings = newSettings
        c.optionMask = newSettings

        // Scripts
        c.onLoadScript       = Utils.getScriptArray(getText("Load"))
        c.onMouseHoverScript = Utils.getScriptArray(getText("MouseHover"))
        c.onMouseLeaveScript = Utils.getScriptArray(getText("MouseLeave"))
        c.onOptionClick      = Utils.getScriptArray(getText("OptionClick"))
        c.onUseWith          = Utils.getScriptArray(getText("UseWith"))
        c.onUse              = Utils.getScriptArray(getText("Use"))
        c.onVarpTransmit     = Utils.getScriptArray(getText("VarpTransmit"))
        c.onInvTransmit      = Utils.getScriptArray(getText("InvTransmit"))
        c.onStatTransmit     = Utils.getScriptArray(getText("StatTransmit"))
        c.onTimer            = Utils.getScriptArray(getText("Timer"))
        c.onMouseRepeat      = Utils.getScriptArray(getText("MouseRepeat"))
        c.onClickRepeat      = Utils.getScriptArray(getText("ClickRepeat"))
        c.onDrag             = Utils.getScriptArray(getText("Drag"))
        c.onRelease          = Utils.getScriptArray(getText("Release"))
        c.onHold             = Utils.getScriptArray(getText("Hold"))
        c.onDragStart        = Utils.getScriptArray(getText("DragStart"))
        c.onDragRelease      = Utils.getScriptArray(getText("DragRelease"))
        c.onScroll           = Utils.getScriptArray(getText("Scroll"))
        c.onVarcTransmit     = Utils.getScriptArray(getText("VarcTransmit"))
        c.onVarcStrTransmit  = Utils.getScriptArray(getText("VarcStrTransmit"))

        // Triggers
        c.varpTriggers       = Utils.getConfigArray(getText("VarpTriggers"))
        c.inventoryTriggers  = Utils.getConfigArray(getText("InventoryTriggers"))
        c.statTriggers       = Utils.getConfigArray(getText("StatTriggers"))
        c.varcTriggers       = Utils.getConfigArray(getText("VarcTriggers"))
        c.varcstrTriggers    = Utils.getConfigArray(getText("VarcStrTriggers"))
    }


    private fun createSection(title: String?= null, fields: List<String>, componentTypes: Map<String, Int>? = null, width: Int? = null, labelWidth: Int? = null, height: Int? = null, fieldHeight: Int? = null): JPanel
    {
        val sectionWidth = width ?: Consts.SECTION_WIDTH
        val sectionHeight = height ?: Consts.SECTION_HEIGHT

        val labelWidth = labelWidth ?: 120
        val fieldHeight = fieldHeight?: Consts.BUTTON_HEIGHT_SMALL
        val showInfo = title == "Standard"
        val infoIcon   = InterfaceUtils.loadIcon("/icons/info.png")
        val unlockIcon = InterfaceUtils.loadIcon("/icons/unlock.png")
        val lockIcon   = InterfaceUtils.loadIcon("/icons/lock.png")
        val resetIcon  = InterfaceUtils.loadIcon("/icons/reset.png")
        val jSpinnerFont = Font("DotumChe", Font.PLAIN, 12)

        var expandedWidth = sectionWidth
        val minWidth = 100
        val maxWidth = 600

        val sectionPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createLoweredBevelBorder()
            alignmentX = Component.LEFT_ALIGNMENT
            background = Color.WHITE
        }

        if (title != null) {
            val titlePanel = JPanel(BorderLayout()).apply {
                border = BorderFactory.createLoweredBevelBorder()
                font.deriveFont(11f)
                background = Color.WHITE
                preferredSize = Dimension(sectionWidth, fieldHeight)
                maximumSize = Dimension(Int.MAX_VALUE, fieldHeight)

                val titleLabel = JLabel(title, JLabel.LEFT)
                titleLabel.border = BorderFactory.createEmptyBorder(0, 2, 0, 0)
                add(titleLabel, BorderLayout.CENTER)

                val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 1, 2)).apply {
                    isOpaque = false
                }

                if (showInfo) {
                    val infoButton = JButton().apply {
                        toolTipText = """<html>
                    <b>Buttons legend:</b><br>
                    <b>LPM</b> - increase width<br>
                    <b>RPM</b> - decrease width<br>
                    <b>Lock</b> - prevent resize<br>
                    <b>Reset</b> - reset width
                    </html>""".trimIndent()

                        icon = infoIcon
                        isFocusPainted = false
                        isContentAreaFilled = false
                        border = BorderFactory.createEmptyBorder()
                    }

                    buttonPanel.add(infoButton)
                }

                val resetButton = JButton().apply {
                    toolTipText = "Reset section width"
                    icon = resetIcon
                    isFocusPainted = false
                    isContentAreaFilled = false
                    border = BorderFactory.createEmptyBorder()
                    addActionListener {
                        expandedWidth = sectionWidth
                        sectionPanel.preferredSize =
                            Dimension(expandedWidth, sectionPanel.preferredSize.height)
                        sectionPanel.revalidate()
                        sectionPanel.repaint()
                    }
                }

                var locked = true
                val lockButton = JButton().apply {
                    toolTipText = "Lock section width"
                    icon = lockIcon
                    isFocusPainted = false
                    isContentAreaFilled = false
                    border = BorderFactory.createEmptyBorder()
                    addActionListener {
                        locked = !locked
                        icon = if (locked) lockIcon else unlockIcon
                    }
                }

                buttonPanel.add(resetButton)
                buttonPanel.add(lockButton)
                add(buttonPanel, BorderLayout.EAST)

                addMouseListener(object : MouseAdapter() {
                    override fun mouseClicked(e: MouseEvent) {
                        if (!locked) {
                            expandedWidth = when {
                                SwingUtilities.isLeftMouseButton(e) ->
                                    (expandedWidth + 50).coerceAtMost(maxWidth)
                                SwingUtilities.isRightMouseButton(e) ->
                                    (expandedWidth - 50).coerceAtLeast(minWidth)
                                else -> expandedWidth
                            }
                            sectionPanel.preferredSize =
                                Dimension(expandedWidth, sectionPanel.preferredSize.height)
                            sectionPanel.revalidate()
                            sectionPanel.repaint()
                        }
                    }
                })
            }

            sectionPanel.add(titlePanel, BorderLayout.NORTH)
        }

        val contentPanel = object : JPanel(GridBagLayout()), Scrollable {
            override fun getPreferredScrollableViewportSize(): Dimension = preferredSize
            override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int) = 16
            override fun getScrollableBlockIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int) = 64
            override fun getScrollableTracksViewportWidth() = true
            override fun getScrollableTracksViewportHeight() = false
        }

        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        }

        val booleanFields = mutableSetOf(
            "Hidden", "Fill", "HasAlpha", "Orthogonal", "Shadow", "HFlip", "VFlip", "Tiling", "NoClickThrough",
            "UnknownProp5", "UnknownProp8", "RightClickAllowed", "CanBeUsedOn", "DragRenderBehavior",
            "HasUnlockedSlots", "UseOnGround", "UseOnNpc", "UseOnObject", "UseOnPlayer", "UseOnSelf",
            "UseOnIComp", "UnknownBit21", "UnknownBit31"
        ).apply { addAll((1..10).map { "Slot $it" }) }

        val enumFields = mapOf(
            "Type" to arrayOf(
                Utils.getComponentFromInt(ComponentType.LAYER),
                Utils.getComponentFromInt(ComponentType.MODEL_LIST),
                Utils.getComponentFromInt(ComponentType.INVENTORY),
                Utils.getComponentFromInt(ComponentType.FIGURE),
                Utils.getComponentFromInt(ComponentType.TEXT),
                Utils.getComponentFromInt(ComponentType.SPRITE),
                Utils.getComponentFromInt(ComponentType.MODEL),
                Utils.getComponentFromInt(ComponentType.LINE)
            )
        )

        fields.forEachIndexed { row, name ->
            gbc.gridy = row
            gbc.insets = Insets(0, 0, 0, 0)
            gbc.gridx = 0
            gbc.weightx = 1.0

            val labelPanel: JComponent = (if (name == "Color") {
                InterfaceUIButton.colorPickButton()
            } else {
                JPanel(BorderLayout()).apply {
                    border = BorderFactory.createRaisedSoftBevelBorder()
                    add(JLabel(name), BorderLayout.CENTER)
                }
            }).apply {
                preferredSize = Dimension(labelWidth, fieldHeight)
                minimumSize = Dimension(labelWidth, fieldHeight)
                maximumSize = Dimension(labelWidth, fieldHeight)
            }

            val textFields = setOf(
                "ParentID", "Hash",
                "Text", "Color",
                "ModelID", "AnimID",
                "OptionBase", "OptionCircumfix",
            ) + scriptsList + (1..10).map { "Option $it" }

            val step1Fields = listOf(
                "XMode", "YMode", "XAlign", "YAlign", "WidthMode", "HeightMode", "TextLineHeight"
            )

            val fieldComponent: JComponent = when {
                name in booleanFields -> JComboBox(arrayOf("true", "false"))
                name in enumFields -> {
                    val combo = JComboBox(enumFields[name])
                    if (name == "Type" && componentTypes != null) {
                        combo.addActionListener {
                            val selectedTypeName = combo.selectedItem?.toString() ?: return@addActionListener
                            val selectedType = Utils.getComponentFromString(selectedTypeName)
                            val intComponentTypes = mutableMapOf<String, List<Int>>()
                            for ((key, value) in componentTypes) {
                                intComponentTypes[key] = listOf(value)
                            }
                            setComponentType(selectedType, intComponentTypes)
                        }
                    }
                    combo
                }
                name in textFields -> JTextField().apply {font=jSpinnerFont}
                step1Fields.any { name.contains(it) } -> InterfaceUtils.createSpinner(step = 1,font = jSpinnerFont)
                name == "ModelZoom"    -> InterfaceUtils.createSpinner(step = 100,font = jSpinnerFont)
                name == "Transparency" -> InterfaceUtils.createSpinner(max = 255, step = 5,font = jSpinnerFont)
                listOf("Width", "Height").any { name.contains(it) } -> InterfaceUtils.createSpinner(step = 4,font = jSpinnerFont)
                else -> InterfaceUtils.createSpinner(step = 1,font = jSpinnerFont)
            }

            fieldComponent.preferredSize = Dimension(fieldComponent.preferredSize.width, fieldHeight)

            val fieldPanel = JPanel(BorderLayout()).apply {
                preferredSize = Dimension(0, fieldHeight)
                minimumSize = Dimension(0, fieldHeight)
                maximumSize = Dimension(Int.MAX_VALUE, fieldHeight)
                add(fieldComponent, BorderLayout.CENTER)
            }

            val rowPanel = JPanel(GridBagLayout())
            val rowGbc = GridBagConstraints().apply {
                fill = GridBagConstraints.HORIZONTAL
                weightx = 0.0
                gridx = 0
            }
            rowPanel.add(labelPanel, rowGbc)
            rowGbc.gridx = 1
            rowGbc.weightx = 1.0
            rowPanel.add(fieldPanel, rowGbc)

            contentPanel.add(rowPanel, gbc)

            fieldMap[name] = fieldComponent
        }

        gbc.gridy = fields.size
        gbc.fill = GridBagConstraints.VERTICAL
        contentPanel.add(JPanel(), gbc)

        val scrollPane = JScrollPane(contentPanel).apply {
            verticalScrollBar.unitIncrement = 12
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = BorderFactory.createLoweredBevelBorder()
        }

        sectionPanel.add(scrollPane, BorderLayout.CENTER)
        sectionPanel.maximumSize = Dimension(Int.MAX_VALUE, sectionHeight)
        sectionPanel.preferredSize = Dimension(sectionWidth, sectionHeight)

        return sectionPanel
    }

    fun set(name: String, value: Any?)
    {
        val field = fieldMap[name] ?: return

        when (field) {
            is JTextField -> {
                field.text = value?.toString() ?: ""
            }
            is JComboBox<*> -> {
                val stringValue = value?.toString() ?: ""
                var found = false
                for (i in 0 until field.itemCount) {
                    if (field.getItemAt(i)?.toString() == stringValue) {
                        field.selectedIndex = i
                        found = true
                        break
                    }
                }

                if (!found && field.itemCount > 0) {
                    field.selectedIndex = 0
                }
            }
            is JSpinner -> {
                val model = field.model
                if (model is SpinnerNumberModel) {
                    val number = when (value) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull() ?: 0
                        else -> 0
                    }
                    model.value = number
                }
            }
        }
    }

    private fun setComponentType(type: Int, componentTypes: Map<String, List<Int>>) {
        fieldMap.forEach { (name, component) ->
            val allowedTypes = componentTypes[name] ?: listOf()
            val isVisible = allowedTypes.isEmpty() || allowedTypes.contains(type)

            val rowPanel = component.parent?.parent as? JPanel
            rowPanel?.isVisible = isVisible
            rowPanel?.revalidate()
            rowPanel?.repaint()
        }
    }

    private fun addSection(section: JPanel)
    {
        section.alignmentY = Component.TOP_ALIGNMENT
        propertyPanel.add(section)
    }

    fun addComponent(templateInterface: Int, templateComponentId: Int, setup: (ComponentDefinition) -> Unit = {})
    {
        val parentId = renderPanel.interfaceId ?: return
        val index = Cache.getStore()?.indexes?.get(3) ?: return
        val newId = index.getValidFilesCount(parentId)
        val templateComp = ComponentDefinition.getInterfaceComponent(templateInterface, templateComponentId) ?: return
        templateComp.parentId = -1
        setup(templateComp)
        templateComp.componentHash = newId + (parentId shl 16)
        buildPropertyPanel(templateComp)
        index.putFile(parentId, newId, templateComp.encode(true))
        ComponentDefinition.getInterface(parentId, true)
        loadComponents(parentId)
        selectComponent(templateComp)
    }
}