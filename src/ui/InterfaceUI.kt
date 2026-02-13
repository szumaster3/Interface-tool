package ui

import properties.Constants
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath
import rs2.cache.Cache
import rs2.cache.loaders.ComponentDefinition
import rs2.interfaces.ComponentConstants
import rs2.interfaces.draw.InterfaceRenderMode
import rs2.interfaces.InterfaceUtils
import rs2.interfaces.draw.InterfaceDraw
import rs2.interfaces.render.InterfaceMouseController
import rs2.logging.LogFactory
import rs2.sprites.ColorPicker
import rs2.sprites.SpriteDumper
import rs2.sprites.utils.SpriteBrowser
import tools.note.ScriptTable
import tools.print.GenericPrinter
import tools.print.GenericPrinter.printInterface
import ui.SwingUtils.getSlider
import ui.SwingUtils.getSliderLabel
import ui.theme.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.border.TitledBorder
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class InterfaceUI : JFrame() {
    var progressMonitor: ProgressMonitor? = null
    private var interface_list: JList<*>? = null
    private var viewportPanel: JPanel? = null
    private var interfaceViewportScrollPane: JScrollPane? = null
    private var comboBox: JComboBox<*>? = null
    private var chckbxShowContainers: DepthComboBox<Boolean> = DepthComboBox(arrayOf(true, false))
    private var chckbxShowModels: DepthComboBox<Boolean> = DepthComboBox(arrayOf(true, false))
    private var chckbxShowHiddenComps: DepthComboBox<Boolean> = DepthComboBox(arrayOf(true, false))
    private lateinit var componentTree: JTree
    private lateinit var componentList: JList<String>
    private lateinit var componentScrollpane: JScrollPane
    private var tabbedPane: JTabbedPane? = null
    private var chckbxRepeat = DepthComboBox(arrayOf(true, false))
    private var chckbxHorizontalFlip = DepthComboBox(arrayOf(true, false))
    private var chckbxVerticalFlip = DepthComboBox(arrayOf(true, false))
    private var chckbxHover = DepthComboBox(arrayOf(true, false))
    private var chckbxHasAlpha = DepthComboBox(arrayOf(true, false))
    private var chckbxShadow = DepthComboBox(arrayOf(true, false))
    private var chckbxFilled = DepthComboBox(arrayOf(true, false))
    private var chckbxHidden = DepthComboBox(arrayOf(true, false))
    private var chckbx_modelOrtho = DepthComboBox(arrayOf(true, false))
    private var chckbx_unknownModelProp5 = DepthComboBox(arrayOf(true, false))
    private var txt_modelId = SpinnerTextField(10)
    private var txt_modelAnimId = SpinnerTextField(10)
    private var txt_modelXAngle = SpinnerTextField(10)
    private var txt_modelYAngle = SpinnerTextField(10)
    private var txt_modelYOffset = SpinnerTextField(10)
    private var txt_modelZoom = SpinnerTextField(10)
    private var txt_unknownModelProp1 = SpinnerTextField(10)
    private var txt_unknownModelProp2 = SpinnerTextField(10)
    private var txt_unknownModelProp3 = SpinnerTextField(10)
    private var txt_unknownModelProp4 = SpinnerTextField(10)

    private var currentRenderer: InterfaceRenderMode = InterfaceRenderMode.CLASSIC
    private var txt_vpadding = SpinnerTextField(10)

    private var txt_type = DepthComboBox(arrayOf("Layer", "Text", "Sprite", "Figure", "Model"))

    private var txt_hash = SpinnerTextField(10)

    private var txt_x = SpinnerTextField(10)
    private var txt_y = SpinnerTextField(10)
    private var txt_height = SpinnerTextField(10)
    private var txt_width = SpinnerTextField(10)
    private var txt_text = SpinnerTextField(10)
    private var txt_shadowColor = SpinnerTextField(10)
    private var txt_leftclick = SpinnerTextField(10)
    private var txt_overlayer = SpinnerTextField(10)
    private var txt_option1 = SpinnerTextField(10)
    private var txt_option2 = SpinnerTextField(10)
    private var txt_option3 = SpinnerTextField(10)
    private var txt_option4 = SpinnerTextField(10)
    private var txt_option5 = SpinnerTextField(10)
    private var txt_modex = SpinnerTextField(10)
    private var txt_positionmodeY = SpinnerTextField(10)
    private var txt_optionMask = SpinnerTextField(10)
    private var txt_border = SpinnerTextField(10)
    private var txt_sprite = SpinnerTextField(10)
    private var txt_modeHeight = SpinnerTextField(10)
    private var txt_widthMode = SpinnerTextField(10)

    private var txt_lineWidth = SpinnerTextField(10)
    private var chckbx_unknownProp8 = DepthComboBox(arrayOf(true, false))

    private var txt_optionCircumfix = SpinnerTextField(10)
    private var txt_optionBase = SpinnerTextField(10)

    private var txt_font = SpinnerTextField(10)
    private var txt_color = SpinnerTextField(10)
    private var txt_trans = SpinnerTextField(10)
    private var txt_scrollX = SpinnerTextField(10)
    private var txt_scrollY = SpinnerTextField(10)
    private var txt_xali = SpinnerTextField(10)
    private var txt_yali = SpinnerTextField(10)
    private var txt_interId = SpinnerTextField(10)
    private var txt_dynamicWidth = SpinnerTextField(10)
    private var txt_dynamicHeight = SpinnerTextField(10)

    private var ifListScrollPane: JScrollPane? = null
    private var result: BufferedImage? = null

    private var selectedComp = -1
    private var copiedComp: ComponentDefinition? = null

    private var txt_onMouseRepeat = JTextField()
    private var txt_fullonhover = JTextField()
    private var txt_mouseLeave = JTextField()
    private var txt_onload = JTextField()
    private var txt_onUseWith = JTextField()
    private var txt_onUse = JTextField()
    private var txt_onVarpTransmit = JTextField()
    private var txt_onInvTransmit = JTextField()
    private var txt_onStatTransmit = JTextField()

    private var txt_onTimer = JTextField()
    private var txt_varpTriggers = JTextField()
    private var txt_onOptionClick = JTextField()
    private var txt_inventoryTriggers = JTextField()
    private var txt_statTriggers = JTextField()
    private var txt_varcTriggers = JTextField()
    private var txt_varcStrTriggers = JTextField()
    private var txt_onClickRepeat = JTextField()
    private var txt_onDrag = JTextField()
    private var txt_onRelease = JTextField()
    private var txt_onHold = JTextField()
    private var txt_onDragStart = JTextField()
    private var txt_onDragRelease = JTextField()
    private var txt_onScroll = JTextField()
    private var txt_onVarcTransmit = JTextField()
    private var txt_onVarcStrTransmit = JTextField()

    private var txt_buttonType = SpinnerTextField(10)
    private var txt_cs1Comparisons = SpinnerTextField(10)
    private var txt_unknownProp11 = SpinnerTextField(10)
    private var txt_objCounts = SpinnerTextField(10)
    private var txt_objTypes = SpinnerTextField(10)
    private var txt_invMarginX = SpinnerTextField(10)
    private var txt_invMarginY = SpinnerTextField(10)
    private var txt_activeText = SpinnerTextField(10)
    private var txt_activeColor  = SpinnerTextField(10)
    private var txt_overColor  = SpinnerTextField(10)
    private var txt_unknownColor = SpinnerTextField(10)

    var invOffsetXFields = Array(20) { JTextField() }
    var invOffsetYFields = Array(20) { JTextField() }
    var invSpriteFields = Array(20) { JTextField() }
    var invOptionsFields = Array(5) { JTextField() }
    var cs1ScriptsFields = Array(10) { JTextArea(3, 20) }

    val gridPanel = SwingUtils.labeledSliderButton("Grid", 1, 32, 8)
    val gridSlider = gridPanel.getSlider()!!
    val gridLabel = gridPanel.getSliderLabel()!!

    private var viewCombo = JComboBox(arrayOf("all", "type", "layer"))
    private var typeCombo = JComboBox(arrayOf("Sprite", "Figure", "Model", "Text", "Line"))
    private var updating = false

    private fun constructWestPanel() {
        val panel = JPanel(BorderLayout())
        panel.minimumSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.maximumSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)

        val searchPanel = JPanel(FlowLayout())
        txt_interId = SpinnerTextField(10)
        txt_interId.setBounds(Constants.CONTENT_PADDING, Constants.CONTENT_PADDING, Constants.SEARCHBOX_WIDTH, Constants.SEARCHBOX_HEIGHT)
        searchPanel.add(txt_interId)

        val btnFind = JButton("Find").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            addActionListener {
                val id = txt_interId.getTextField().text.toIntOrNull() ?: return@addActionListener
                currentInterface = id
                drawTree(id)
                viewportPanel?.repaint()
            }
        }
        searchPanel.add(btnFind)
        panel.add(searchPanel, BorderLayout.NORTH)

        interface_list = JList(populateList())
        interface_list!!.selectionMode = ListSelectionModel.SINGLE_SELECTION
        interface_list!!.addListSelectionListener { evt ->
            if (evt.valueIsAdjusting) return@addListSelectionListener
            val selected = interface_list!!.selectedValue ?: return@addListSelectionListener
            val id = selected.toString().replace("Interface: ", "").toIntOrNull() ?: return@addListSelectionListener
            currentInterface = id
            logger.info("Interface $id selected")
            viewportPanel?.repaint()
            drawTree(id)
            cleanValues()
        }

        val ifListScrollPane = JScrollPane(interface_list).apply {
            verticalScrollBar.unitIncrement = 20
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.VIEWPORT_HEIGHT)
        }

        val moreButtonsPanel = JPanel()
        moreButtonsPanel.layout = BoxLayout(moreButtonsPanel, BoxLayout.Y_AXIS)

        val interfaceOptionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        }

        val newInterfaceButton = JButton("New Interface").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                val newInterfaceId = ComponentDefinition.interfaceDefinitionsSize
                val defaultButton = ComponentDefinition.getInterfaceComponent(830, 0)
                defaultButton!!.parentId = -1
                Cache.store!!.indexes[3].putFile(newInterfaceId, 0, defaultButton.encode(true))
                ComponentDefinition.componentDefinition = ComponentDefinition.componentDefinition!!.copyOf(newInterfaceId + 1)
                currentInterface = newInterfaceId
                val newInterfaceListModel = populateList()
                (interface_list as JList<String>).model = newInterfaceListModel
                val indexToSelect = (0 until newInterfaceListModel.size).firstOrNull {
                    newInterfaceListModel.getElementAt(it) == "Interface: $newInterfaceId"
                } ?: 0
                (interface_list as JList<String>).selectedIndex = indexToSelect
                (interface_list as JList<String>).ensureIndexIsVisible(indexToSelect)

                val newCompListModel = createComponentList(currentInterface)
                componentList.model = newCompListModel
                selectedComp = 0
                if (newCompListModel.size > 0) {
                    componentList.selectedIndex = 0
                    componentList.ensureIndexIsVisible(0)
                    selectedComp = componentList.selectedValue.toString().substringBefore(":").trim().toInt()
                }

                val newTreeModel = createInterfaceTree(currentInterface)
                componentTree.model = newTreeModel
                drawTree(currentInterface)
            }
        }

        val deleteInterfaceButton = JButton("Delete Selected").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                if (currentInterface == -1) return@addActionListener
                val option = JOptionPane.showConfirmDialog(null,
                    "Are you sure that you want to clear components in interface $currentInterface?",
                    "Warning", JOptionPane.YES_NO_OPTION)
                if (option == JOptionPane.YES_OPTION) {
                    val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)
                    for (i in 0 until size) {
                        if (i == 0) addBaseComponent(currentInterface, true)
                        else Cache.store!!.indexes[3].removeFile(currentInterface, i)
                    }
                    Cache.init()
                    ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.interfaceDefinitionsSize)

                    val newCompListModel = createComponentList(currentInterface)
                    componentList.model = newCompListModel
                    selectedComp = if (newCompListModel.size > 0) {
                        componentList.selectedIndex = 0
                        componentList.ensureIndexIsVisible(0)
                        componentList.selectedValue.toString().substringBefore(":").trim().toInt()
                    } else -1

                    val newTreeModel = createInterfaceTree(currentInterface)
                    componentTree.model = newTreeModel
                    drawTree(currentInterface)
                    logger.info("Viewing: [$currentInterface]:[$selectedComp]")
                }
            }
        }


        val rendererLabel = JLabel("Mode:")
        val rendererCombo = JComboBox(arrayOf(*InterfaceRenderMode.values())).apply {
            selectedIndex = 0
            addPopupMenuListener(object : javax.swing.event.PopupMenuListener {
                override fun popupMenuWillBecomeVisible(e: javax.swing.event.PopupMenuEvent?) {}
                override fun popupMenuWillBecomeInvisible(e: javax.swing.event.PopupMenuEvent?) { viewportPanel?.repaint() }
                override fun popupMenuCanceled(e: javax.swing.event.PopupMenuEvent?) {}
            })
            addActionListener {
                val selected = selectedItem
                if (selected is InterfaceRenderMode) {
                    currentRenderer = selected
                    viewportPanel?.repaint()
                }
            }
        }

        val rendererPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            isOpaque = false
            add(rendererLabel)
            add(Box.createRigidArea(Dimension(6, 0)))
            add(rendererCombo)
        }

        viewCombo.addActionListener {
            val mode = when (viewCombo.selectedItem as String) {
                "type" -> InterfaceDraw.ViewMode.BY_TYPE
                "layer" -> InterfaceDraw.ViewMode.BY_LAYER
                "none" -> InterfaceDraw.ViewMode.NONE
                else -> InterfaceDraw.ViewMode.NONE
            }
            InterfaceUI.currentViewMode = mode

            typeCombo.isEnabled = mode == InterfaceDraw.ViewMode.BY_TYPE
            if (typeCombo.isEnabled && typeCombo.selectedItem == null){
                typeCombo.selectedIndex = 0
            }

            val selectedType = if (typeCombo.isEnabled) {
                when (typeCombo.selectedItem as String) {
                    "Sprite" -> ComponentConstants.SPRITE
                    "Figure" -> ComponentConstants.FIGURE
                    "Model"  -> ComponentConstants.MODEL
                    "Text"   -> ComponentConstants.TEXT
                    "Line"   -> ComponentConstants.LINE
                    else -> null
                }
            } else null

            val treeModel = createInterfaceTree(currentInterface, mode, selectedType)
            componentTree.model = treeModel
            (componentTree.model as DefaultTreeModel).reload()
            viewportPanel?.repaint()
        }
        val typeLabel = JLabel("ifType:")
        val typePanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            isOpaque = false
            add(typeLabel)
            add(Box.createRigidArea(Dimension(6, 0)))
            add(typeCombo)
        }

        typeCombo.addActionListener {
            if (!typeCombo.isEnabled) return@addActionListener

            val selectedType = when (typeCombo.selectedItem as String) {
                "Sprite" -> ComponentConstants.SPRITE
                "Figure" -> ComponentConstants.FIGURE
                "Model"  -> ComponentConstants.MODEL
                "Text"   -> ComponentConstants.TEXT
                "Line"   -> ComponentConstants.LINE
                else -> null
            }

            val treeModel = createInterfaceTree(currentInterface, InterfaceUI.currentViewMode, selectedType)
            componentTree.model = treeModel
            (componentTree.model as DefaultTreeModel).reload()
            viewportPanel?.repaint()
        }

        val viewLabel = JLabel("View:")
        val viewPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            maximumSize = Dimension(Int.MAX_VALUE , Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            isOpaque = false
            add(viewLabel)
            add(Box.createRigidArea(Dimension(6, 0)))
            add(viewCombo)
        }

        interfaceOptionsPanel.layout = BoxLayout(interfaceOptionsPanel, BoxLayout.Y_AXIS)
        interfaceOptionsPanel.border = TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Render", TitledBorder.LEADING, TitledBorder.TOP)
        interfaceOptionsPanel.add(rendererPanel)
        interfaceOptionsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        interfaceOptionsPanel.add(viewPanel)
        interfaceOptionsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        interfaceOptionsPanel.add(typePanel)
        interfaceOptionsPanel.add(Box.createRigidArea(Dimension(0, 5)))

        moreButtonsPanel.add(interfaceOptionsPanel)
        moreButtonsPanel.add(Box.createRigidArea(Dimension(0, 10)))
        val premadeComponentsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Quick Add", TitledBorder.LEADING, TitledBorder.TOP)
            maximumSize = Dimension(Int.MAX_VALUE, 80)
            alignmentX = Component.CENTER_ALIGNMENT
        }

        val premadeCombo = JComboBox(arrayOf(
            "Select component...",
            "Close button",
            "Normal button",
            "Start interface",
            "Basic custom hover",
            "Basic button with pop-up"
        )).apply {
            selectedIndex = 0
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
        }

        val btnAddPremadeComponent = JButton("Add Component").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                val selectedItem = premadeCombo.selectedItem?.toString() ?: return@addActionListener
                if (selectedItem == "Select component...") {
                    JOptionPane.showMessageDialog(
                        premadeComponentsPanel,
                        "Please select a component from the list.",
                        "No component selected",
                        JOptionPane.WARNING_MESSAGE
                    )
                    return@addActionListener
                }
                addDefaultComponent(currentInterface, selectedItem)
            }
        }

        premadeComponentsPanel.add(premadeCombo)
        premadeComponentsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        premadeComponentsPanel.add(btnAddPremadeComponent)
        moreButtonsPanel.add(premadeComponentsPanel)
        moreButtonsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        val actionsPanel = JPanel()
        actionsPanel.layout = BoxLayout(actionsPanel, BoxLayout.Y_AXIS)
        actionsPanel.border =
            TitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), "Operations", TitledBorder.LEADING, TitledBorder.TOP)

        val saveButton = JButton("Save").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                if (currentInterface != -1 && selectedComp != -1) {
                    saveInterface(currentInterface, selectedComp)
                    drawTree(currentInterface)
                    setValues(currentInterface, selectedComp)
                } else {
                    JOptionPane.showMessageDialog(interfaceViewportScrollPane, "Please select a component & interface before saving it.")
                }
            }
        }
        val printInterfaceButton = JButton("Print Interface").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                if (currentInterface != -1) {
                    printInterface(currentInterface)
                    JOptionPane.showMessageDialog(null, "Interface $currentInterface printed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE)
                }
            }
        }
        val printComponentButton = JButton("Print Component").apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            alignmentX = Component.CENTER_ALIGNMENT
            addActionListener {
                if (currentInterface != -1 && selectedComp != -1) {
                    GenericPrinter.printComponent(currentInterface, selectedComp)
                    JOptionPane.showMessageDialog(null, "Component $selectedComp from Interface $currentInterface printed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE)
                }
            }
        }

        actionsPanel.add(newInterfaceButton)
        actionsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        actionsPanel.add(deleteInterfaceButton)
        actionsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        actionsPanel.add(saveButton)
        actionsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        actionsPanel.add(printInterfaceButton)
        actionsPanel.add(Box.createRigidArea(Dimension(0, 5)))
        actionsPanel.add(printComponentButton)
        actionsPanel.add(Box.createRigidArea(Dimension(0, 5)))

        moreButtonsPanel.add(actionsPanel)

        val finalPanel = JPanel()
        finalPanel.layout = BoxLayout(finalPanel, BoxLayout.Y_AXIS)

        val interfaceListScroll = JScrollPane(interface_list).apply {
            verticalScrollBar.unitIncrement = 20
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, 350)
        }
        finalPanel.add(interfaceListScroll)
        finalPanel.add(Box.createRigidArea(Dimension(0, 5)))

        val buttonsScrollPane = JScrollPane(moreButtonsPanel).apply {
            verticalScrollBar.unitIncrement = 20
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, 400)
        }
        finalPanel.add(buttonsScrollPane)
        panel.add(finalPanel, BorderLayout.CENTER)
        contentPane.add(panel, BorderLayout.WEST)
    }

    private fun createTitledPanel(
        title: String,
        content: JPanel,
    ) = JPanel(BorderLayout()).apply {
        border = BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED), title)
        add(content, BorderLayout.CENTER)
    }

    private fun constructEastPanel() {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)

        val iconMap = mapOf(
            "Layer" to ImageIcon(IconLibrary.CONTAINER_ICON),
            "Text" to ImageIcon(IconLibrary.TEXT_ICON),
            "Sprite" to ImageIcon(IconLibrary.SPRITE_ICON),
            "Figure" to ImageIcon(IconLibrary.FIGURE_ICON),
            "Model" to ImageIcon(IconLibrary.MODEL_ICON),
            "Line" to ImageIcon(IconLibrary.LINE_ICON),
            "Model List" to ImageIcon(IconLibrary.MODEL_LIST_ICON),
            "Inventory" to ImageIcon(IconLibrary.INVENTORY_ICON),
            "Interface" to ImageIcon(IconLibrary.INTERFACE_ICON)
        )

        val tabbedPane = JTabbedPane()

        componentTree = JTree(createInterfaceTree(currentInterface)).apply {
            cellRenderer = ComponentTreeRenderer(iconMap)
            background = Color.BLACK
            isOpaque = true
            showsRootHandles = true
            isRootVisible = true
        }

        val treeScroll = JScrollPane(componentTree).apply {
            verticalScrollBar.unitIncrement = 20
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }
        tabbedPane.addTab("View Tree", treeScroll)

        fun updateComponentList(viewMode: InterfaceDraw.ViewMode, selectedType: Int?) {
            val allComponents = createComponentList(currentInterface)
            val filteredComponents = when (viewMode) {
                InterfaceDraw.ViewMode.BY_TYPE -> selectedType?.let { type ->
                    (0 until allComponents.size()).map { allComponents.getElementAt(it) }.filter {
                        it.substringAfter(":").trim().removePrefix("(").removeSuffix(")").equals(
                            when (type) {
                                ComponentConstants.SPRITE -> "Sprite"
                                ComponentConstants.FIGURE -> "Figure"
                                ComponentConstants.MODEL -> "Model"
                                ComponentConstants.TEXT -> "Text"
                                ComponentConstants.LINE -> "Line"
                                else -> ""
                            }, ignoreCase = true
                        )
                    }
                } ?: emptyList()
                InterfaceDraw.ViewMode.BY_LAYER -> (0 until allComponents.size()).map { allComponents.getElementAt(it) }
                    .filter { it.substringAfter(":").trim().removePrefix("(").removeSuffix(")") == "Layer" }
                InterfaceDraw.ViewMode.NONE -> (0 until allComponents.size()).map { allComponents.getElementAt(it) }
            }

            componentList.model = DefaultListModel<String>().apply { filteredComponents.forEach { addElement(it) } }
        }

        componentList = JList(createComponentList(currentInterface)).apply {
            background = Color.BLACK
            foreground = Color.GREEN
            isOpaque = true
            cellRenderer = ListCellRenderer { _, value, _, isSelected, _ ->
                val label = JLabel().apply {
                    isOpaque = true
                    font = Font("JetBrains Mono", Font.PLAIN, 12)
                    val text = value?.toString() ?: ""
                    this.text = text
                    val type = text.substringAfter(":").trim().removePrefix("(").removeSuffix(")")
                    icon = iconMap[type]
                    background = if (isSelected) Color.DARK_GRAY else Color.BLACK
                    foreground = if (isSelected) Color.GREEN.brighter() else Color.GREEN
                }
                label
            }
        }

        val listScroll = JScrollPane(componentList).apply {
            verticalScrollBar.unitIncrement = 20
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }
        tabbedPane.addTab("View List", listScroll)

        val selectionHandler: (Int) -> Unit = { selectedId ->
            selectedComp = selectedId
            setValues(currentInterface, selectedComp)

            for (i in 0 until componentList.model.size) {
                val item = componentList.model.getElementAt(i)
                val compId = item.substringBefore(":").trim().toIntOrNull() ?: continue
                if (compId == selectedId) {
                    componentList.selectedIndex = i
                    componentList.ensureIndexIsVisible(i)
                    break
                }
            }

            val treeNode = findNode(componentTree.model.root as DefaultMutableTreeNode)
            treeNode?.let {
                val treePath = javax.swing.tree.TreePath(it.path)
                componentTree.selectionPath = treePath
                componentTree.scrollPathToVisible(treePath)
            }

            repaint()
        }

        componentTree.addTreeSelectionListener {
            val node = componentTree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return@addTreeSelectionListener
            val data = node.userObject as? ComponentNode ?: return@addTreeSelectionListener
            if (data.componentId < 0) return@addTreeSelectionListener
            selectionHandler(data.componentId)
        }

        componentList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val value = componentList.selectedValue ?: return@addListSelectionListener
                val id = value.substringBefore(":").trim().toIntOrNull() ?: return@addListSelectionListener
                selectionHandler(id)
            }
        }

        typeCombo.apply {
            maximumSize = Dimension(Int.MAX_VALUE, Constants.BUTTON_HEIGHT)
            isEnabled = false
            addActionListener {
                val viewMode = when (viewCombo.selectedItem as String) {
                    "type" -> InterfaceDraw.ViewMode.BY_TYPE
                    "layer" -> InterfaceDraw.ViewMode.BY_LAYER
                    "none" -> InterfaceDraw.ViewMode.NONE
                    else -> InterfaceDraw.ViewMode.NONE
                }
                val selectedType = when (typeCombo.selectedItem as String) {
                    "Sprite" -> ComponentConstants.SPRITE
                    "Figure" -> ComponentConstants.FIGURE
                    "Model" -> ComponentConstants.MODEL
                    "Text" -> ComponentConstants.TEXT
                    "Line" -> ComponentConstants.LINE
                    else -> null
                }
                componentTree.model = createInterfaceTree(currentInterface, viewMode, selectedType)
                (componentTree.model as DefaultTreeModel).reload()
                updateComponentList(viewMode, selectedType)
                viewportPanel?.repaint()
            }
        }

        viewCombo.addActionListener {
            val viewMode = when (viewCombo.selectedItem as String) {
                "type" -> InterfaceDraw.ViewMode.BY_TYPE
                "layer" -> InterfaceDraw.ViewMode.BY_LAYER
                "none" -> InterfaceDraw.ViewMode.NONE
                else -> InterfaceDraw.ViewMode.NONE
            }
            typeCombo.isEnabled = (viewMode == InterfaceDraw.ViewMode.BY_TYPE)
            val selectedType = if (typeCombo.isEnabled) {
                when (typeCombo.selectedItem as String) {
                    "Sprite" -> ComponentConstants.SPRITE
                    "Figure" -> ComponentConstants.FIGURE
                    "Model" -> ComponentConstants.MODEL
                    "Text" -> ComponentConstants.TEXT
                    "Line" -> ComponentConstants.LINE
                    else -> null
                }
            } else null
            componentTree.model = createInterfaceTree(currentInterface, viewMode, selectedType)
            (componentTree.model as DefaultTreeModel).reload()
            updateComponentList(viewMode, selectedType)
            viewportPanel?.repaint()
        }
        val settingsPanel = JPanel(GridLayout(0, 1, 0, 1))
        val settingsFrame = createTitledPanel("Settings", settingsPanel)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 5))
        val btnSize = Dimension(Constants.BUTTON_WIDTH_SMALL, Constants.BUTTON_HEIGHT_SMALL)
        val btnCopy =
            JButton("Copy").apply {
                preferredSize = btnSize
                addActionListener { copiedComp = ComponentDefinition.getInterfaceComponent(currentInterface, selectedComp) }
            }
        val btnPaste =
            JButton("Paste").apply {
                preferredSize = btnSize
                addActionListener { pasteComponent() }
            }
        val btnDelete =
            JButton("Delete").apply {
                preferredSize = btnSize
                addActionListener { deleteSelectedComponent() }
            }
        val btnPlaceholder =
            JButton("Fonts").apply {
                preferredSize = btnSize
                isEnabled = false
            }
        buttonPanel.add(btnCopy)
        buttonPanel.add(btnPaste)
        buttonPanel.add(btnDelete)
        buttonPanel.add(btnPlaceholder)
        val buttonsFrame =
            JPanel(BorderLayout()).apply {
                border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
                add(buttonPanel, BorderLayout.CENTER)
            }
        val textTabData =
            listOf(
                "Left Click" to txt_leftclick,
                "Option 1" to txt_option1,
                "V Padding" to txt_vpadding,
                "Option 2" to txt_option2,
                "Has Shadow" to chckbxShadow,
                "Option 3" to txt_option3,
                "X Align" to txt_xali,
                "Option 4" to txt_option4,
                "Y Align" to txt_yali,
                "Option 5" to txt_option5,
                "Option Mask" to txt_optionMask,
            )

        txt_text.getTextField().horizontalAlignment = JTextField.LEFT

        val textPanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                add(SwingUtils.labeledField("Text", txt_text))
                add(Box.createRigidArea(Dimension(0, 5)))

                val twoColumn = JPanel(GridLayout(0, 2, 5, 5))
                textTabData.filter { it.first != "Text" }.forEach { (label, field) ->
                    if (field is JTextField) field.horizontalAlignment = JTextField.LEFT
                    twoColumn.add(SwingUtils.labeledField(label, field))
                }
                add(twoColumn)
            }

        val textFrame = createTitledPanel("Text", textPanel)
        val modelTabData =
            listOf(
                "Model ID" to txt_modelId,
                "Animation ID" to txt_modelAnimId,
                "X Angle" to txt_modelXAngle,
                "Prop 1" to txt_unknownModelProp1,
                "Y Angle" to txt_modelYAngle,
                "Prop 2" to txt_unknownModelProp2,
                "Y Offset" to txt_modelYOffset,
                "Prop 3" to txt_unknownModelProp3,
                "Model Zoom" to txt_modelZoom,
                "Prop 4" to txt_unknownModelProp4,
                "Ortho" to chckbx_modelOrtho,
                "DynWidth" to txt_dynamicWidth,
                "Prop 5" to chckbx_unknownModelProp5,
                "DynHeight" to txt_dynamicHeight,
            )

        val modelPanel = JPanel(GridLayout(0, 2, 0, 0))
        modelTabData.forEach { (label, field) -> modelPanel.add(SwingUtils.labeledField(label, field)) }
        val modelFrame = createTitledPanel("Model", modelPanel)

        val settingsData =
            listOf("Show Layer" to chckbxShowContainers, "Show Models" to chckbxShowModels, "Show Hidden" to chckbxShowHiddenComps)

        chckbxShowContainers.selectedItem = true
        chckbxShowModels.selectedItem = true
        chckbxShowHiddenComps.selectedItem = false

        settingsData.forEach { (label, combo) ->
            val comp = SwingUtils.labeledField(label, combo)

            combo.addItemListener { contentPane.repaint() }

            combo.addPopupMenuListener(
                object : PopupMenuListener {
                    override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
                        contentPane.repaint()
                    }

                    override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {
                        contentPane.repaint()
                    }

                    override fun popupMenuCanceled(e: PopupMenuEvent?) {
                        contentPane.repaint()
                    }
                },
            )

            settingsPanel.add(comp)
        }

        val bottomPanel =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(buttonsFrame)
                add(Box.createRigidArea(Dimension(0, 1)))
                add(textFrame)
                add(Box.createRigidArea(Dimension(0, 1)))
                add(modelFrame)
                add(Box.createRigidArea(Dimension(0, 1)))
                add(settingsFrame)
            }

        val editorScroll =
            JScrollPane(bottomPanel).apply {
                verticalScrollBar.unitIncrement = 6
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            }

        val splitPane =
            JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, editorScroll).apply {
                resizeWeight = 0.4
                isOneTouchExpandable = true
                dividerSize = 8
            }

        panel.add(splitPane, BorderLayout.CENTER)
        contentPane.add(panel, BorderLayout.EAST)
    }

    private fun findNode(node: DefaultMutableTreeNode): DefaultMutableTreeNode? {
        val data = node.userObject as? ComponentNode
        if (data?.componentId == selectedComp) return node
        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i) as DefaultMutableTreeNode
            val result = findNode(child)
            if (result != null) return result
        }
        return null
    }

    private fun deleteSelectedComponent() {
        if (selectedComp <= 0) {
            JOptionPane.showMessageDialog(interfaceViewportScrollPane, "Please select a component first.")
            return
        }

        val c = ComponentDefinition.getInterfaceComponent(currentInterface, selectedComp)
        var message: String? = null

        if (c != null) {
            message =
                if (c.type == ComponentConstants.LAYER) {
                    "Are you sure that you want to remove component $selectedComp from interface $currentInterface? NOTE: this component is a container, childs will be removed as well."
                } else {
                    "Are you sure that you want to remove component $selectedComp from interface $currentInterface?"
                }
        }

        val option = JOptionPane.showConfirmDialog(this, message, "Confirmation", JOptionPane.YES_NO_OPTION)
        if (option != JOptionPane.YES_OPTION) return

        try {
            Cache.store!!.indexes[3].removeFile(currentInterface, selectedComp)
            Cache.store!!.indexes[3].resetCachedFiles()
            Cache.store!!.indexes[3].rewriteTable()
            try {
                Cache.init()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } finally {
            drawTree(currentInterface)
        }
    }

    private fun constructCenterPanel() {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.maximumSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.minimumSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        val buttonSize = Dimension(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT_SMALL)
        val buttonPanel = JPanel(FlowLayout())

        val btnAddSprite = JButton("Sprite")
        btnAddSprite.preferredSize = buttonSize
        btnAddSprite.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(801, 4)
            comp?.parentId = -1
            comp?.componentHash = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!
                .indexes[3]
                .putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), comp!!.encode(true))
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddSprite)

        val btnAddText = JButton("Text")
        btnAddText.preferredSize = buttonSize
        btnAddText.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(752, 5)
            comp?.baseX = 0
            comp?.baseY = 0
            comp?.parentId = -1
            comp?.componentHash = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!
                .indexes[3]
                .putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), comp!!.encode(true))
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddText)

        val btnAddContainer = JButton("Layer")
        btnAddContainer.preferredSize = buttonSize
        btnAddContainer.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(778, 0)
            comp?.baseX = 0
            comp?.baseY = 0
            comp?.baseWidth = 50
            comp?.baseHeight = 50
            comp?.parentId = -1
            comp?.componentHash = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!
                .indexes[3]
                .putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), comp!!.encode(true))
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddContainer)

        val btnAddModel = JButton("Model")
        btnAddModel.preferredSize = buttonSize
        btnAddModel.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(205, 90)
            comp?.parentId = -1
            comp?.componentHash = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!
                .indexes[3]
                .putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), comp!!.encode(true))
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddModel)

        val btnAddRectangle = JButton("Figure")
        btnAddRectangle.preferredSize = buttonSize
        btnAddRectangle.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(625, 0)
            comp?.baseX = 0
            comp?.baseY = 0
            comp?.componentHash = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!
                .indexes[3]
                .putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), comp!!.encode(true))
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddRectangle)

        val scriptTableBtn = JButton("Scripts")
        scriptTableBtn.preferredSize = buttonSize
        scriptTableBtn.addActionListener { ScriptTable() }
        buttonPanel.add(scriptTableBtn)

        panel.add(buttonPanel, BorderLayout.NORTH)

        viewportPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)

                if (currentInterface == -1) return

                InterfaceDraw.editMode = (currentRenderer == InterfaceRenderMode.CLASSIC)
                val viewMode = when (viewCombo.selectedItem as String) {
                    "type" -> InterfaceDraw.ViewMode.BY_TYPE
                    "layer" -> InterfaceDraw.ViewMode.BY_LAYER
                    "none" -> InterfaceDraw.ViewMode.NONE
                    else -> InterfaceDraw.ViewMode.NONE
                }

                val selectedType: Int? = if (viewMode == InterfaceDraw.ViewMode.BY_TYPE && typeCombo.isEnabled) {
                    when ((typeCombo.selectedItem as String)) {
                        "Sprite" -> ComponentConstants.SPRITE
                        "Figure" -> ComponentConstants.FIGURE
                        "Model" -> ComponentConstants.MODEL
                        "Text" -> ComponentConstants.TEXT
                        "Line" -> ComponentConstants.LINE
                        else -> null
                    }
                } else null

                InterfaceDraw.drawInterfaceToGraphics(
                    g,
                    currentInterface,
                    chckbxShowContainers.selectedItem as Boolean,
                    chckbxShowHiddenComps.selectedItem as Boolean,
                    chckbxShowModels.selectedItem as Boolean,
                    viewMode,
                    selectedType
                )

                selectedComponent?.let { comp ->
                    if ((comp.componentHash shr 16) == currentInterface) {
                        InterfaceUtils.drawSelectedComponent(g, comp, fill = true)
                    }
                }
            }
        }

        (viewportPanel as JPanel).preferredSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
        (viewportPanel as JPanel).minimumSize   = Dimension(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
        (viewportPanel as JPanel).maximumSize   = Dimension(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)

        panel.add(viewportPanel!!, BorderLayout.CENTER)

        tabbedPane = JTabbedPane(JTabbedPane.TOP)
        tabbedPane!!.preferredSize =
            Dimension(
                Constants.DEFAULT_EDITOR_WIDTH - Constants.LEFT_SCROLLPANE_WIDTH - Constants.RIGHT_SCROLLPANE_WIDTH,
                Constants.DEFAULT_EDITOR_HEIGHT - Constants.VIEWPORT_HEIGHT - Constants.BUTTON_HEIGHT - 4,
            )
        tabbedPane!!.addTab("Standard", null, constructGeneralTab())
        tabbedPane!!.addTab("Configs", null, constructTriggersTab())
        tabbedPane!!.addTab("Legacy", null, constructLegacyTab())
        tabbedPane!!.setEnabledAt(tabbedPane!!.indexOfTab("Legacy"), false)

        panel.add(tabbedPane, BorderLayout.SOUTH)
        contentPane.add(panel, BorderLayout.CENTER)
    }

    private fun constructGeneralTab(): JPanel {
        val root = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            isOpaque = false
            background = Color.WHITE
        }

        val topPanel = JPanel(GridLayout(0, 3, 1, 1)).apply {
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        }

        listOf(
            "Hash" to txt_hash,
            "Type" to txt_type,
            "Hide" to chckbxHidden,
            "Parent ID" to txt_overlayer,
            "Hover" to chckbxHover,
            "Sprite ID" to txt_sprite,
            "X" to txt_x,
            "X Mode" to txt_modex,
            "Tiling" to chckbxRepeat,
            "Y" to txt_y,
            "Y Mode" to txt_positionmodeY,
            "H Flip" to chckbxHorizontalFlip,
            "Width" to txt_width,
            "Width Mode" to txt_widthMode,
            "V Flip" to chckbxVerticalFlip,
            "Height" to txt_height,
            "Height Mode" to txt_modeHeight,
            "Color" to txt_color,
            "Scroll X" to txt_scrollX,
            "Font ID" to txt_font,
            "Shadow color" to txt_shadowColor,
            "Scroll Y" to txt_scrollY,
            "B Thickness" to txt_border,
            "Alpha" to txt_trans,
            "Line Width" to txt_lineWidth,

        ).forEach { (label, field) ->
            topPanel.add(SwingUtils.labeledField(label, field))
        }

        gridSlider.addChangeListener {
            val value = gridSlider.value
            InterfaceMouseController.instance?.grid = value
            if (InterfaceMouseController.instance?.enabled == true) {
                gridLabel.text = "Grid: $value"
            }
        }
        topPanel.add(gridPanel)

        val pickerAddBtn = SwingUtils.labeledTwoButtons("Picker", "Sprites")
        val innerPanel = pickerAddBtn.getComponent(0) as? JPanel
        val btn1 = innerPanel?.getComponent(0) as? JButton
        val btn2 = innerPanel?.getComponent(1) as? JButton

        btn1?.addActionListener {
            val comp = selectedComponent
            if (comp == null) {
                ThemeUtils.showError(root, "You must first select a component.", "Error - Unable to launch")
                return@addActionListener
            }

            val picker = ColorPicker(comp, txt_color) {
                setValues(currentInterface, selectedComponent!!.componentId)
                repaint()
            }
            picker.pack()
            picker.setLocationRelativeTo(null)
            picker.isVisible = true
        }

        btn2?.addActionListener {
            SpriteBrowser(this) { spriteId, w, h ->
                if (currentInterface <= 0) return@SpriteBrowser
                selectedComponent?.spriteId = spriteId
                selectedComponent?.baseWidth = w
                selectedComponent?.baseHeight = h
                setValues(currentInterface, selectedComponent!!.componentId)
                repaint()
            }
        }

        topPanel.add(pickerAddBtn)
        val scriptsContent = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }

        listOf(
            "onLoad" to txt_onload,
            "onMouseOver" to txt_fullonhover,
            "onMouseLeave" to txt_mouseLeave,
            "onUseWith" to txt_onUseWith,
            "onUse" to txt_onUse,
            "onVarpTransmit" to txt_onVarpTransmit,
            "onInvTransmit" to txt_onInvTransmit,
            "onStatTransmit" to txt_onStatTransmit,
            "onTimer" to txt_onTimer,
            "onOptionClick" to txt_onOptionClick,
            "onMouseRepeat" to txt_onMouseRepeat,
            "onClickRepeat" to txt_onClickRepeat,
            "onDrag" to txt_onDrag,
            "onRelease" to txt_onRelease,
            "onHold" to txt_onHold,
            "onDragStart" to txt_onDragStart,
            "onDragRelease" to txt_onDragRelease,
            "onScroll" to txt_onScroll,
            "onVarcTransmit" to txt_onVarcTransmit,
            "onVarcstrTransmit" to txt_onVarcStrTransmit,
        ).forEach { (label, field) ->
            scriptsContent.add(SwingUtils.labeledField(label, field))
            scriptsContent.add(Box.createRigidArea(Dimension(0, 1)))
        }

        val scriptsScroll = JScrollPane(scriptsContent).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
            verticalScrollBar.unitIncrement = 6
        }

        root.add(topPanel)
        root.add(Box.createRigidArea(Dimension(0, 6)))
        root.add(scriptsScroll)

        val mouseEnabled = InterfaceMouseController.instance?.enabled ?: false
        gridSlider.isEnabled = mouseEnabled
        if (!mouseEnabled) {
            gridLabel.text = "Grid locked"
        }

        return root
    }

    private fun constructLegacyTab(): JPanel {
        val root = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
            isOpaque = false
            background = Color.WHITE
        }

        val mainFieldsPanel = JPanel(GridLayout(0, 2, 5, 5)).apply {
            listOf(
                "Button Type" to txt_buttonType,
                "CS1 Comparisons" to txt_cs1Comparisons,
                "Unknown Prop 11" to txt_unknownProp11,
                "Obj Counts" to txt_objCounts,
                "Obj Types" to txt_objTypes,
                "Inv Margin X" to txt_invMarginX,
                "Inv Margin Y" to txt_invMarginY,
                "Active Text" to txt_activeText,
                "Active Color" to txt_activeColor,
                "Over Color" to txt_overColor,
                "Unknown Color" to txt_unknownColor
            ).forEach { (label, field) ->
                add(SwingUtils.labeledField(label, field))
            }
        }

        val optionsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            for (i in 0 until 5) {
                add(SwingUtils.labeledField("Inv Option $i", invOptionsFields[i]))
                add(Box.createRigidArea(Dimension(0, 1)))
            }
        }

        val scriptsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            cs1ScriptsFields.forEachIndexed { i, field ->
                val fieldWrapper = SwingUtils.labeledField("CS1 Script $i", field)

                val height = txt_buttonType.preferredSize.height
                fieldWrapper.maximumSize = Dimension(Int.MAX_VALUE, height)
                fieldWrapper.preferredSize = Dimension(fieldWrapper.preferredSize.width, height)
                fieldWrapper.minimumSize = Dimension(fieldWrapper.minimumSize.width, height)

                add(fieldWrapper)
                add(Box.createRigidArea(Dimension(0, 1)))
            }
        }

        val slotsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            val offsetXPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                for (i in 0 until 20) {
                    add(SwingUtils.labeledField("Inv Offset X $i", invOffsetXFields[i]))
                    add(Box.createRigidArea(Dimension(0, 1)))
                }
            }
            add(SwingUtils.createBevelPanel(offsetXPanel))
            add(Box.createRigidArea(Dimension(0, 5)))

            val offsetYPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                for (i in 0 until 20) {
                    add(SwingUtils.labeledField("Inv Offset Y $i", invOffsetYFields[i]))
                    add(Box.createRigidArea(Dimension(0, 1)))
                }
            }
            add(SwingUtils.createBevelPanel(offsetYPanel))
            add(Box.createRigidArea(Dimension(0, 5)))

            val spritePanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                for (i in 0 until 20) {
                    add(SwingUtils.labeledField("Inv Sprite $i", invSpriteFields[i]))
                    add(Box.createRigidArea(Dimension(0, 1)))
                }
            }
            add(SwingUtils.createBevelPanel(spritePanel))
        }

        val scrollContent = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(SwingUtils.createBevelPanel(mainFieldsPanel))
            add(Box.createRigidArea(Dimension(0, 5)))
            add(SwingUtils.createBevelPanel(optionsPanel))
            add(Box.createRigidArea(Dimension(0, 5)))
            add(SwingUtils.createBevelPanel(scriptsPanel))
            add(Box.createRigidArea(Dimension(0, 5)))
            add(SwingUtils.createBevelPanel(slotsPanel))
        }

        val scrollPane = JScrollPane(scrollContent).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            border = null
            verticalScrollBar.unitIncrement = 12
        }

        root.add(scrollPane)
        return root
    }

    private fun constructTriggersTab(): JPanel {
        val root =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
                isOpaque = true
            }

        val triggersContent =
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false
            }

        listOf(
            "varpTriggers" to txt_varpTriggers,
            "inventoryTriggers" to txt_inventoryTriggers,
            "statTriggers" to txt_statTriggers,
            "varcTriggers" to txt_varcTriggers,
            "varcStrTriggers" to txt_varcStrTriggers,
        ).forEach { (label, field) ->
            val row = SwingUtils.labeledField(label, field)

            row.preferredSize = Dimension(0, 25)
            row.maximumSize = Dimension(Int.MAX_VALUE, 25)
            row.minimumSize = Dimension(0, 25)

            triggersContent.add(row)
            triggersContent.add(Box.createRigidArea(Dimension(0, 2)))
        }

        root.add(triggersContent)
        return root
    }

    init {
        instance = this
        title = "Interface Editor"
        contentPane.layout = BorderLayout()
        defaultCloseOperation = EXIT_ON_CLOSE
        setBounds(100, 100, Constants.DEFAULT_EDITOR_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        constructWestPanel()
        constructCenterPanel()
        constructEastPanel()

        chckbxShowHiddenComps.addItemListener { _: ItemEvent? -> viewportPanel!!.repaint() }

        val menuBar = JMenuBar()
        jMenuBar = menuBar
        val mnCredits = JMenu("Credits")
        mnCredits.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    val dialog = JDialog(this@InterfaceUI, "Credits", true)
                    dialog.layout = BorderLayout()
                    val message =
                        JLabel(
                            "<html><center>" +
                                "<h2>Interface Editor</h2>" +
                                "<p>Made by: Shnek<br>Discord: Cara Shnek#6969</p>" +
                                "<p>Major code refactoring & improvements by: Ceikry</p>" +
                                "<p>Recent changes - szumaster 05.02.2026</p></center></html>",
                        )
                    message.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    dialog.add(message, BorderLayout.CENTER)

                    val okButton = JButton("OK")
                    okButton.addActionListener { dialog.dispose() }

                    val panel = JPanel()
                    panel.add(okButton)
                    dialog.add(panel, BorderLayout.SOUTH)

                    dialog.pack()
                    dialog.setLocationRelativeTo(this@InterfaceUI)
                    dialog.isVisible = true
                }
            },
        )
        menuBar.add(mnCredits)

        val mnAbout = JMenu("Extra")
        menuBar.add(mnAbout)

        val mntmDumpSprites = JMenuItem("Export sprites")
        mntmDumpSprites.addActionListener { _: ActionEvent? ->

            val spriteIndex = Cache.store!!.indexes[Constants.SPRITES_INDEX]
            val max = spriteIndex.lastArchiveId + 1
            val progressMonitor = ProgressMonitor(menuBar, "Export sprites", "", 0, max)
            setLocationRelativeTo(null)
            Thread {
                for (i in 0 until max) {
                    if (progressMonitor.isCanceled) break

                    SpriteDumper.dump(i)

                    SwingUtilities.invokeLater {
                        progressMonitor.setProgress(i + 1)
                        progressMonitor.note = "Export sprite ${i + 1} of ${max - 1}"
                    }

                    try {
                        Thread.sleep(5)
                    } catch (ignored: InterruptedException) {
                    }
                }

                SwingUtilities.invokeLater { progressMonitor.close() }
            }.start()
        }
        mnAbout.add(mntmDumpSprites)
        val mntmExportInterface = JMenuItem("Export interface")
        mntmExportInterface.addActionListener { _: ActionEvent? ->
            val interfaceId = currentInterface
            if (interfaceId != -1) {
                try {
                    exportInterface(interfaceId)
                } catch (ex: Exception) {
                    JOptionPane.showMessageDialog(
                        interfaceViewportScrollPane,
                        "Error exporting interface $interfaceId:\n${ex.message}",
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            } else {
                JOptionPane.showMessageDialog(
                    interfaceViewportScrollPane,
                    "No interface selected to export.",
                    "Export Error",
                    JOptionPane.WARNING_MESSAGE
                )
            }
        }

        mnAbout.add(mntmExportInterface)
        val mntmPackInterface = JMenuItem("Pack interface")
        mntmPackInterface.isEnabled = false
        mntmPackInterface.addActionListener { _: ActionEvent? -> }

        val lockedIcon = ImageIcon(IconLibrary.MOUSE_LOCKED_BUTTON)
        val activeIcon = ImageIcon(IconLibrary.MOUSE_ACTIVE_BUTTON)

        val lockButton = JToggleButton(lockedIcon)
        lockButton.isFocusable = false
        lockButton.toolTipText = "Lock/Unlock Mouse"
        lockButton.isContentAreaFilled = false
        lockButton.isBorderPainted = false
        lockButton.isOpaque = false
        lockButton.preferredSize = Dimension(16, 20)
        lockButton.preferredSize = Dimension(16, 20)
        lockButton.preferredSize = Dimension(16, 20)
        lockButton.addActionListener {
            val status = lockButton.isSelected
            lockButton.icon = if (status) activeIcon else lockedIcon
            InterfaceMouseController(viewportPanel!!, enabled = status)
            gridSlider.isEnabled = status
            gridLabel.text = if (status) "Grid: ${gridSlider.value}" else "Grid: Locked"
        }

        mnAbout.add(mntmPackInterface)
        menuBar.add(mnCredits)
        menuBar.add(lockButton)
        pack()
        revalidate()
    }

    fun setValues(
        inter: Int,
        componentId: Int,
    ) {
        logger.info("Viewing: [$inter]:[$componentId]")
        cleanValues()

        val comp = ComponentDefinition.getInterfaceComponent(inter, componentId) ?: return
        selectedComponent = comp

        txt_hash.getTextField().text = comp.componentHash.toString()

        txt_type.selectedItem =
            when (comp.type) {
                ComponentConstants.LAYER -> "Layer"
                ComponentConstants.TEXT -> "Text"
                ComponentConstants.SPRITE -> "Sprite"
                ComponentConstants.FIGURE -> "Figure"
                ComponentConstants.MODEL -> "Model"
                ComponentConstants.LINE -> "Line"
                else -> comp.type.toString()
            }

        txt_x.getTextField().text = comp.baseX.toString()
        txt_y.getTextField().text = comp.baseY.toString()
        txt_width.getTextField().text = comp.baseWidth.toString()
        txt_height.getTextField().text = comp.baseHeight.toString()
        txt_widthMode.getTextField().text = comp.aspectWidthType.toString()
        txt_modeHeight.getTextField().text = comp.aspectHeightType.toString()

        txt_positionmodeY.getTextField().text = comp.yMode.toString()
        txt_modex.getTextField().text = comp.xMode.toString()

        txt_overlayer.getTextField().text = comp.parentId.toString()
        chckbxHidden.selectedItem = comp.hidden

        chckbxHover.selectedItem = comp.type == ComponentConstants.LAYER && comp.noClickThrough

        txt_sprite.getTextField().text = comp.spriteId.toString()
        txt_border.getTextField().text = comp.borderThickness.toString()
        txt_trans.getTextField().text = comp.transparency.toString()
        chckbxHorizontalFlip.selectedItem = comp.hFlip
        chckbxVerticalFlip.selectedItem = comp.vFlip
        chckbxRepeat.selectedItem = comp.spriteTiling
        chckbxHasAlpha.selectedItem = comp.hasAlpha

        txt_text.getTextField().text = comp.text
        txt_font.getTextField().text = comp.fontId.toString()
        txt_xali.getTextField().text = comp.textHorizontalAli.toString()
        txt_yali.getTextField().text = comp.textVerticalAli.toString()
        txt_vpadding.getTextField().text = comp.vPadding.toString()
        chckbxShadow.selectedItem = comp.shadow

        txt_color.getTextField().text = comp.color.toString()
        txt_shadowColor.getTextField().text = comp.shadowColor.toString()

        chckbxFilled.selectedItem = comp.filled
        txt_trans.getTextField().text = comp.transparency.toString()

        txt_scrollX.getTextField().text = comp.scrollMaxH.toString()
        txt_scrollY.getTextField().text = comp.scrollMaxV.toString()

        txt_modelId.getTextField().text = comp.modelId.toString()
        txt_modelAnimId.getTextField().text = comp.modelAnimId.toString()
        txt_modelXAngle.getTextField().text = comp.modelXAngle.toString()
        txt_modelYAngle.getTextField().text = comp.modelYAngle.toString()
        txt_modelYOffset.getTextField().text = comp.modelYOffset.toString()
        txt_modelZoom.getTextField().text = comp.modelZoom.toString()
        txt_unknownModelProp1.getTextField().text = comp.unknownModelProp1.toString()
        txt_unknownModelProp2.getTextField().text = comp.unknownModelProp2.toString()
        txt_unknownModelProp3.getTextField().text = comp.unknownModelProp3.toString()
        txt_unknownModelProp4.getTextField().text = comp.unknownModelProp4.toString()
        chckbx_modelOrtho.selectedItem = comp.modelOrtho
        chckbx_unknownModelProp5.selectedItem = comp.unknownModelProp5
        txt_dynamicWidth.getTextField().text = comp.dynamicWidth.toString()
        txt_dynamicHeight.getTextField().text = comp.dynamicHeight.toString()

        txt_lineWidth.getTextField().text = comp.lineWidth.toString()
        chckbx_unknownProp8.selectedItem = comp.unknownProp_8

        txt_optionMask.getTextField().text = comp.optionMask.toString()

        /*
        if (comp.version.toInt() == 1) {
            txt_buttonType.getTextField().text = comp.buttonType.toString()
            txt_unknownProp11.getTextField().text = comp.unknownProp_11.toString()
            txt_invMarginX.getTextField().text = comp.invMarginX.toString()
            txt_invMarginY.getTextField().text = comp.invMarginY.toString()
            txt_objCounts.getTextField().text = comp.objCounts?.joinToString(";") ?: ""
            txt_objTypes.getTextField().text = comp.objTypes?.joinToString(";") ?: ""
            txt_cs1Comparisons.getTextField().text = comp.cs1ComparisonOpcodes?.joinToString(";") ?: ""

            comp.invOffsetX?.let { arr ->
                val count = minOf(arr.size, invOffsetXFields.size)
                for (i in 0 until count) {
                    invOffsetXFields[i].text = arr[i].toString()
                }
            }

            comp.invOffsetY?.let { arr ->
                val count = minOf(arr.size, invOffsetYFields.size)
                for (i in 0 until count) {
                    invOffsetYFields[i].text = arr[i].toString()
                }
            }

            comp.invSprite?.let { arr ->
                val count = minOf(arr.size, invSpriteFields.size)
                for (i in 0 until count) {
                    invSpriteFields[i].text = arr[i].toString()
                }
            }

            comp.invOptions?.let { arr ->
                val count = minOf(arr.size, invOptionsFields.size)
                for (i in 0 until count) {
                    invOptionsFields[i].text = arr[i] ?: ""
                }
            }

            comp.cs1Scripts?.let { scripts ->
                val count = minOf(scripts.size, cs1ScriptsFields.size)
                for (i in 0 until count) {
                    cs1ScriptsFields[i].text = scripts[i].joinToString(";")
                }
            }
        }
        */

        val options = listOf(txt_leftclick, txt_option1, txt_option2, txt_option3, txt_option4, txt_option5)
        options.forEach { it.getTextField().text = "" }
        comp.rightclickOptions?.forEachIndexed { index, text ->
            if (index in 0..5) options[index].getTextField().text = text ?: ""
        }

        var values: String? = ""
        if (comp.onMouseHoverScript != null) {
            for (o in comp.onMouseHoverScript!!) {
                values += o.toString() + ";"
            }
        }
        txt_fullonhover.text = values
        values = ""
        if (comp.onMouseLeaveScript != null) {
            for (o in comp.onMouseLeaveScript!!) {
                values += o.toString() + ";"
            }
        }
        txt_mouseLeave.text = values
        values = ""
        if (comp.onLoadScript != null) {
            for (o in comp.onLoadScript!!) {
                values += o.toString() + ";"
            }
        }
        txt_onload.text = values

        values = ""
        if (comp.onOptionClick != null) {
            for (o in comp.onOptionClick!!) {
                values += o.toString() + ";"
            }
        }
        txt_onOptionClick.text = values
        values = ""
        if (comp.onUseWith != null) {
            for (o in comp.onUseWith!!) {
                values += o.toString() + ";"
            }
        }
        txt_onUseWith.text = values
        values = ""
        if (comp.onUse != null) {
            for (o in comp.onUse!!) {
                values += o.toString() + ";"
            }
        }
        txt_onUse.text = values
        values = ""
        if (comp.onVarpTransmit != null) {
            for (o in comp.onVarpTransmit!!) {
                values += o.toString() + ";"
            }
        }
        txt_onVarpTransmit.text = values
        values = ""
        if (comp.onMouseRepeat != null) {
            for (o in comp.onMouseRepeat!!) {
                values += o.toString() + ";"
            }
        }
        txt_onMouseRepeat.text = values
        values = ""
        if (comp.onInvTransmit != null) {
            for (o in comp.onInvTransmit!!) {
                values += o.toString() + ";"
            }
        }
        txt_onInvTransmit.text = values

        values = ""
        if (comp.onStatTransmit != null) {
            for (o in comp.onStatTransmit!!) {
                values += o.toString() + ";"
            }
        }
        txt_onStatTransmit.text = values

        values = ""
        if (comp.onTimer != null) {
            for (o in comp.onTimer!!) {
                values += o.toString() + ";"
            }
        }
        txt_onTimer.text = values

        values = ""
        if (comp.onClickRepeat != null) {
            for (o in comp.onClickRepeat!!) {
                values += o.toString() + ";"
            }
        }
        txt_onClickRepeat.text = values

        values = ""
        if (comp.onDrag != null) {
            for (o in comp.onDrag!!) {
                values += o.toString() + ";"
            }
        }
        txt_onDrag.text = values
        values = ""
        if (comp.onRelease != null) {
            for (o in comp.onRelease!!) {
                values += o.toString() + ";"
            }
        }
        txt_onRelease.text = values
        values = ""
        if (comp.onHold != null) {
            for (o in comp.onHold!!) {
                values += o.toString() + ";"
            }
        }
        txt_onHold.text = values
        values = ""
        if (comp.onDragStart != null) {
            for (o in comp.onDragStart!!) {
                values += o.toString() + ";"
            }
        }
        txt_onDragStart.text = values
        values = ""
        if (comp.onDragRelease != null) {
            for (o in comp.onDragRelease!!) {
                values += o.toString() + ";"
            }
        }
        txt_onDragRelease.text = values

        values = ""
        if (comp.onScroll != null) {
            for (o in comp.onScroll!!) {
                values += o.toString() + ";"
            }
        }
        txt_onScroll.text = values
        values = ""
        if (comp.onVarcTransmit != null) {
            for (o in comp.onVarcTransmit!!) {
                values += o.toString() + ";"
            }
        }
        txt_onVarcTransmit.text = values
        values = ""
        if (comp.onVarcStrTransmit != null) {
            for (o in comp.onVarcStrTransmit!!) {
                values += o.toString() + ";"
            }
        }
        txt_onVarcStrTransmit.text = values

        values = ""
        if (comp.varpTriggers != null) {
            for (i in comp.varpTriggers!!) {
                values += i
            }
        }
        txt_varpTriggers.text = values

        values = ""
        if (comp.inventoryTriggers != null) {
            for (i in comp.inventoryTriggers!!) {
                values += i
            }
        }
        txt_inventoryTriggers.text = values
        values = ""
        if (comp.statTriggers != null) {
            for (i in comp.statTriggers!!) {
                values += i
            }
        }
        txt_statTriggers.text = values
        values = ""
        if (comp.varcTriggers != null) {
            for (i in comp.varcTriggers!!) {
                values += i
            }
        }
        txt_varcTriggers.text = values
        values = ""
        if (comp.varcstrTriggers != null) {
            for (i in comp.varcstrTriggers!!) {
                values += i
            }
        }
        txt_varcStrTriggers.text = values

        selectedComponent = comp
    }

    private fun cleanTab(container: Container) {
        for (c in container.components) {
            if (c is JTextField && c.isEnabled()) {
                val ctrl = c
                if (ctrl.isEditable) ctrl.text = null
            }
        }
    }

    fun cleanValues() {
        for (i in 0 until tabbedPane!!.tabCount) {
            cleanTab(tabbedPane!!.getComponentAt(i) as Container)
        }
    }

    fun saveInterface(
        inter: Int,
        compId: Int,
    ) {
        var c = ComponentDefinition.getInterfaceComponent(inter, compId)

        c!!.type =
            when (txt_type.selectedItem?.toString()) {
                "Layer" -> ComponentConstants.LAYER
                "Text" -> ComponentConstants.TEXT
                "Sprite" -> ComponentConstants.SPRITE
                "Figure" -> ComponentConstants.FIGURE
                "Model" -> ComponentConstants.MODEL
                "Line" -> ComponentConstants.LINE
                else -> ComponentConstants.SPRITE
            }

        c.parentId = parseIntSafe(txt_overlayer.getTextField().text, -1)
        c.baseX = parseIntSafe(txt_x.getTextField().text, 0)
        c.baseY = parseIntSafe(txt_y.getTextField().text, 0)
        c.baseWidth = parseIntSafe(txt_width.getTextField().text, 0)
        c.baseHeight = parseIntSafe(txt_height.getTextField().text, 0)

        c.aspectWidthType = parseIntSafe(txt_widthMode.getTextField().text, 0).toByte()
        c.aspectHeightType = parseIntSafe(txt_modeHeight.getTextField().text, 0).toByte()
        c.xMode = parseIntSafe(txt_modex.getTextField().text, 0).toByte()
        c.yMode = parseIntSafe(txt_positionmodeY.getTextField().text, 0).toByte()

        c.hidden = (chckbxHidden.selectedItem as? Boolean) ?: false

        c.scrollMaxH = 0
        c.scrollMaxV = 0
        c.spriteId = -1
        c.modelId = -1
        c.text = ""
        c.rightclickOptions = null

        when (c.type) {
            ComponentConstants.LAYER -> {
                c.scrollMaxH = parseIntSafe(txt_scrollX.getTextField().text, 0)
                c.scrollMaxV = parseIntSafe(txt_scrollY.getTextField().text, 0)
                c.noClickThrough = (chckbxHover.selectedItem as? Boolean) ?: false
            }

            ComponentConstants.SPRITE -> {
                c.spriteId = parseIntSafe(txt_sprite.getTextField().text, -1)
                c.transparency = parseIntSafe(txt_trans.getTextField().text, 255)
                c.borderThickness = parseIntSafe(txt_border.getTextField().text, 0)
                c.color = parseIntSafe(txt_color.getTextField().text, 0)
                c.shadowColor= parseIntSafe(txt_shadowColor.getTextField().text, 0)

                c.spriteTiling = (chckbxRepeat.selectedItem as? Boolean) ?: false
                c.hasAlpha = (chckbxHasAlpha.selectedItem as? Boolean) ?: false
                c.hFlip = (chckbxHorizontalFlip.selectedItem as? Boolean) ?: false
                c.vFlip = (chckbxVerticalFlip.selectedItem as? Boolean) ?: false
            }

            ComponentConstants.TEXT -> {
                c.text = txt_text.getTextField().text
                c.fontId = parseIntSafe(txt_font.getTextField().text, -1)
                c.color = parseIntSafe(txt_color.getTextField().text, 0)
                c.transparency = parseIntSafe(txt_trans.getTextField().text, 255)
                c.textHorizontalAli = parseIntSafe(txt_xali.getTextField().text, 0)
                c.textVerticalAli = parseIntSafe(txt_yali.getTextField().text, 0)
                c.vPadding = parseIntSafe(txt_vpadding.getTextField().text, 0)
                c.shadow = (chckbxShadow.selectedItem as? Boolean) ?: false
            }

            ComponentConstants.MODEL -> {
                c.modelId = parseIntSafe(txt_modelId.getTextField().text, -1)
                c.modelAnimId = parseIntSafe(txt_modelAnimId.getTextField().text, -1)
                c.modelXAngle = parseIntSafe(txt_modelXAngle.getTextField().text, 0)
                c.modelYAngle = parseIntSafe(txt_modelYAngle.getTextField().text, 0)
                c.modelYOffset = parseIntSafe(txt_modelYOffset.getTextField().text, 0)
                c.modelZoom = parseIntSafe(txt_modelZoom.getTextField().text, 100)

                c.modelOrtho = (chckbx_modelOrtho.selectedItem as? Boolean) ?: false
                c.unknownModelProp5 = (chckbx_unknownModelProp5.selectedItem as? Boolean) ?: false

                if (c.aspectWidthType.toInt() != 0) {
                    c.dynamicWidth = parseIntSafe(txt_dynamicWidth.getTextField().text, 0)
                }

                if (c.aspectHeightType.toInt() != 0) {
                    c.dynamicHeight = parseIntSafe(txt_dynamicHeight.getTextField().text, 0)
                }
            }

            ComponentConstants.FIGURE -> {
                c.color = parseIntSafe(txt_color.getTextField().text, 0)
                c.transparency = parseIntSafe(txt_trans.getTextField().text, 255)
                c.filled = (chckbxFilled.selectedItem as? Boolean) ?: false
            }

            ComponentConstants.LINE -> {
                c.lineWidth = parseIntSafe(txt_lineWidth.getTextField().text, 0).toByte()
                c.color = parseIntSafe(txt_color.getTextField().text, 0)
                c.unknownProp_8 = (chckbx_unknownProp8.selectedItem as? Boolean) ?: false
            }
        }

        val opts = arrayOfNulls<String>(6)
        listOf(
            txt_leftclick,
            txt_option1,
            txt_option2,
            txt_option3,
            txt_option4,
            txt_option5,
        ).forEachIndexed { i, field ->
            val t = field.getTextField().text
            if (!t.isNullOrBlank()) opts[i] = t
        }
        c.rightclickOptions = if (opts.any { it != null }) opts else null

        c.optionMask = parseIntSafe(txt_optionMask.getTextField().text, 0)

        c.onLoadScript = InterfaceUtils.getScriptArray(txt_onload.text)
        c.onMouseHoverScript = InterfaceUtils.getScriptArray(txt_fullonhover.text)
        c.onMouseLeaveScript = InterfaceUtils.getScriptArray(txt_mouseLeave.text)
        c.onOptionClick = InterfaceUtils.getScriptArray(txt_onOptionClick.text)
        c.onUseWith = InterfaceUtils.getScriptArray(txt_onUseWith.text)
        c.onUse = InterfaceUtils.getScriptArray(txt_onUse.text)
        c.onVarpTransmit = InterfaceUtils.getScriptArray(txt_onVarpTransmit.text)
        c.onInvTransmit = InterfaceUtils.getScriptArray(txt_onInvTransmit.text)
        c.onStatTransmit = InterfaceUtils.getScriptArray(txt_onStatTransmit.text)
        c.onTimer = InterfaceUtils.getScriptArray(txt_onTimer.text)
        c.onMouseRepeat = InterfaceUtils.getScriptArray(txt_onMouseRepeat.text)
        c.onClickRepeat = InterfaceUtils.getScriptArray(txt_onClickRepeat.text)
        c.onDrag = InterfaceUtils.getScriptArray(txt_onDrag.text)
        c.onRelease = InterfaceUtils.getScriptArray(txt_onRelease.text)
        c.onHold = InterfaceUtils.getScriptArray(txt_onHold.text)
        c.onDragStart = InterfaceUtils.getScriptArray(txt_onDragStart.text)
        c.onDragRelease = InterfaceUtils.getScriptArray(txt_onDragRelease.text)
        c.onScroll = InterfaceUtils.getScriptArray(txt_onScroll.text)
        c.onVarcTransmit = InterfaceUtils.getScriptArray(txt_onVarcTransmit.text)
        c.onVarcStrTransmit = InterfaceUtils.getScriptArray(txt_onVarcStrTransmit.text)

        c.varpTriggers = InterfaceUtils.getConfigArray(txt_varpTriggers.text)
        c.inventoryTriggers = InterfaceUtils.getConfigArray(txt_inventoryTriggers.text)
        c.statTriggers = InterfaceUtils.getConfigArray(txt_statTriggers.text)
        c.varcTriggers = InterfaceUtils.getConfigArray(txt_varcTriggers.text)
        c.varcstrTriggers = InterfaceUtils.getConfigArray(txt_varcStrTriggers.text)

        val data = c.encodeIf3()
        Cache.store!!.indexes[3].putFile(inter, compId, data)

        drawTree(inter)
        logger.info("Saved [$inter:$compId]")
    }

    private fun parseIntSafe(
        value: String,
        defaultValue: Int,
    ): Int =
        try {
            value.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }

    private fun getAllComponents(interfaceId: Int): List<ComponentDefinition> {
        val list = ArrayList<ComponentDefinition>()
        val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)

        for (i in 0 until size) {
            val c = ComponentDefinition.getInterfaceComponent(interfaceId, i)
            if (c != null) list.add(c)
        }
        return list
    }

    private fun createComponentList(interfaceId: Int): DefaultListModel<String> {
        val model = DefaultListModel<String>()

        for (c in getAllComponents(interfaceId)) {
            model.addElement("${c.componentId} : ${InterfaceUtils.getComponentType(c.type)}")
        }
        return model
    }

    private fun buildTree(
        parentNode: DefaultMutableTreeNode,
        interfaceId: Int,
        parentHash: Int,
    ) {
        val childs = ComponentDefinition.getChildsByParent(interfaceId, parentHash)

        for (c in childs) {
            val node = DefaultMutableTreeNode(ComponentNode(c.componentId, InterfaceUtils.getComponentType(c.type)))

            parentNode.add(node)

            if (ComponentDefinition.hasChilds(interfaceId, c.componentHash)) {
                buildTree(node, interfaceId, c.componentHash)
            }
        }
    }

    private fun populateList(): DefaultListModel<String> {
        val listModel = DefaultListModel<String>()
        for (i in 0 until ComponentDefinition.interfaceDefinitionsSize) {
            try {
                val iface = ComponentDefinition.getInterface(i, false)
                if (iface != null && iface.toString().isNotBlank()) {
                    listModel.addElement("Interface: $i")
                }
            } catch (_: Exception) {
            }
        }
        return listModel
    }

    private fun createInterfaceTree(interfaceId: Int): DefaultTreeModel {
        val root = DefaultMutableTreeNode(ComponentNode(-1, "Interface $interfaceId"))

        val components = getAllComponents(interfaceId)

        for (c in components) {
            if (c.parentId == -1) {
                val node = DefaultMutableTreeNode(ComponentNode(c.componentId, InterfaceUtils.getComponentType(c.type)))
                root.add(node)

                if (ComponentDefinition.hasChilds(interfaceId, c.componentHash)) {
                    buildTree(node, interfaceId, c.componentHash)
                }
            }
        }

        return DefaultTreeModel(root)
    }

    fun createInterfaceTree(interfaceId: Int, viewMode: InterfaceDraw.ViewMode, selectedType: Int?): DefaultTreeModel {
        val comps = InterfaceDraw.getOrderedComps(interfaceId)

        val filtered = when (viewMode) {
            InterfaceDraw.ViewMode.NONE -> comps
            InterfaceDraw.ViewMode.BY_TYPE -> selectedType?.let { comps.filter { comp -> comp.type == it } } ?: emptyList()
            InterfaceDraw.ViewMode.BY_LAYER -> comps.filter { it.type == ComponentConstants.LAYER }
        }

        val root = DefaultMutableTreeNode("Interface $interfaceId")
        val nodesByParent = mutableMapOf<Int, DefaultMutableTreeNode>()
        nodesByParent[-1] = root

        for (comp in filtered) {
            val node = DefaultMutableTreeNode(ComponentNode(comp.componentId, InterfaceUtils.getComponentType(comp.type)))
            nodesByParent[comp.parentId]?.add(node)
            nodesByParent[comp.componentHash] = node
        }

        return DefaultTreeModel(root)
    }

    private fun addBaseComponent(
        interfaceId: Int,
        useScripts: Boolean,
    ) {
        val defaultButton = ComponentDefinition.getInterfaceComponent(7, 1)
        if (defaultButton != null) {
            defaultButton.parentId = -1
        }
        defaultButton!!.text = "New component"
        defaultButton.hasScripts = useScripts
        Cache.store!!.indexes[3].putFile(interfaceId, 0, defaultButton.encode(useScripts))
    }

    private fun pasteComponent() {
        if (copiedComp == null) {
            JOptionPane.showMessageDialog(interfaceViewportScrollPane, "No component was selected to paste.")
            return
        }
        if (copiedComp!!.type == ComponentConstants.LAYER) {
            val containerPlace = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)
            copiedComp!!.parentId = -1

            Cache.store!!.indexes[3].putFile(currentInterface, containerPlace, copiedComp!!.encode(copiedComp!!.hasScripts))
            val childs = ComponentDefinition.getChildsByParent(copiedComp!!.interfaceId, copiedComp!!.componentHash)
            for (c in childs) {
                if (c.type == ComponentConstants.LAYER) {
                } else {
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            currentInterface,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                            c.encode(copiedComp!!.hasScripts),
                        )
                }
            }
            ComponentDefinition.getInterface(currentInterface, true)
            val size = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)
            val parent = ComponentDefinition.getInterfaceComponent(currentInterface, containerPlace)
            for (i in size - childs.size - 1 until size) {
                val component = ComponentDefinition.getInterfaceComponent(currentInterface, i)
                if (component != null && component.type != 0) {
                    if (parent != null) {
                        component.parentId = parent.componentHash
                    }
                    Cache.store!!.indexes[3].putFile(currentInterface, i, component.encode(copiedComp!!.hasScripts))
                }
            }
        } else {
            copiedComp!!.parentId = -1
            Cache.store!!
                .indexes[3]
                .putFile(
                    currentInterface,
                    ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                    copiedComp!!.encode(copiedComp!!.hasScripts),
                )
        }
        ComponentDefinition.getInterface(currentInterface, true)
        drawTree(currentInterface)
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun exportInterface(interfaceId: Int) {
        val exportDir = File("data/export")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val file = File("data/export/$interfaceId.dat")
        val data =
            Cache.store!!
                .indexes[3]
                .getArchive(interfaceId)
                .data
        FileOutputStream(file).use { fos ->
            fos.write(data)
            fos.close()
            JOptionPane.showMessageDialog(interfaceViewportScrollPane, "$interfaceId dumped to data/export/$interfaceId.dat")
        }
    }

    private fun drawTree(id: Int) {
        logger.info("Drawing component tree and list for interface [$id]")

        val treeModel = createInterfaceTree(id)
        componentTree.model = treeModel
        componentTree.clearSelection()

        val listData = createComponentList(id)
        val model = componentList.model as DefaultListModel<String>
        model.clear()
        for (i in 0 until listData.size) {
            model.addElement(listData.getElementAt(i))
        }
    }

    private fun addDefaultComponent(
        interfaceId: Int,
        selectedItem: String,
    ) {
        if (selectedItem == "Select component...") {
            JOptionPane.showMessageDialog(
                null,
                "Please select a component from the list.",
                "No component selected",
                JOptionPane.WARNING_MESSAGE,
            )
            return
        }

        when (selectedItem) {
            "Close button" -> {
                val defaultCloseButton = ComponentDefinition.getInterfaceComponent(6, 36)
                if (defaultCloseButton != null) {
                    defaultCloseButton.parentId = -1
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            interfaceId,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId),
                            defaultCloseButton.encode(copiedComp?.hasScripts ?: false),
                        )
                }
                ComponentDefinition.getInterface(interfaceId, true)
                drawTree(interfaceId)
            }

            "Normal button" -> {
                val container = ComponentDefinition.getInterfaceComponent(506, 1)
                val text = ComponentDefinition.getInterfaceComponent(506, 2)
                if (container != null) {
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            interfaceId,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId),
                            container.encode(copiedComp?.hasScripts ?: false),
                        )
                }
                if (text != null) {
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            interfaceId,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId),
                            text.encode(copiedComp?.hasScripts ?: false),
                        )
                }

                ComponentDefinition.getInterface(interfaceId, true)

                val lastIndex = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId) - 1
                val parentIndex = lastIndex - 1
                val parentComp = ComponentDefinition.getInterfaceComponent(interfaceId, parentIndex)
                val textComp = ComponentDefinition.getInterfaceComponent(interfaceId, lastIndex)
                if (parentComp != null && textComp != null) {
                    textComp.parentId = parentComp.componentHash
                    Cache.store!!.indexes[3].putFile(interfaceId, lastIndex, textComp.encode(copiedComp?.hasScripts ?: false))
                }
                ComponentDefinition.getInterface(interfaceId, true)
                drawTree(interfaceId)
            }

            "Start interface" -> {
                val basic = ComponentDefinition.getInterfaceComponent(6, 0)
                val place = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)
                if (basic != null) {
                    Cache.store!!.indexes[3].putFile(interfaceId, place, basic.encode(copiedComp?.hasScripts ?: false))
                }

                val children = ComponentDefinition.getChildsByParent(6, ComponentDefinition.getInterfaceComponent(6, 0)?.componentHash ?: 0)
                for (comp in children) {
                    comp.text = "Example text"
                    comp.parentId++
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            interfaceId,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId),
                            comp.encode(copiedComp?.hasScripts ?: false),
                        )
                }
                ComponentDefinition.getInterface(interfaceId, true)
                drawTree(interfaceId)
            }

            "Basic custom hover" -> {
                val hover = ComponentDefinition.getInterfaceComponent(6, 36)
                if (hover != null) {
                    hover.parentId = -1
                    hover.onMouseRepeat = null
                    hover.onOptionClick = null
                    hover.spriteId = 0
                    hover.onMouseHoverScript?.set(2, 1)
                    hover.onMouseLeaveScript?.set(2, 0)
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            interfaceId,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId),
                            hover.encode(copiedComp?.hasScripts ?: false),
                        )
                }
                ComponentDefinition.getInterface(interfaceId, true)
                drawTree(interfaceId)
            }

            "Basic button with pop-up" -> {
                val popupButton = ComponentDefinition.getInterfaceComponent(762, 33)
                val cont = ComponentDefinition.getInterfaceComponent(762, 119)

                if (cont != null) cont.parentId = -1
                if (popupButton != null) popupButton.parentId = -1

                val place2 = ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)
                if (cont != null) {
                    Cache.store!!.indexes[3].putFile(interfaceId, place2, cont.encode(copiedComp?.hasScripts ?: false))
                }

                if (popupButton != null) {
                    popupButton.onMouseRepeat?.set(3, "Click here to change your preset settings.")
                    val contComp = ComponentDefinition.getInterfaceComponent(interfaceId, place2)
                    if (contComp != null) {
                        popupButton.onMouseLeaveScript?.set(2, contComp.componentHash)
                        popupButton.onMouseRepeat?.set(2, contComp.componentHash)
                    }
                    Cache.store!!
                        .indexes[3]
                        .putFile(
                            interfaceId,
                            ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId),
                            popupButton.encode(copiedComp?.hasScripts ?: false),
                        )
                }

                ComponentDefinition.getInterface(interfaceId, true)
                drawTree(interfaceId)
            }
        }
    }

    companion object {
        lateinit var instance: InterfaceUI
            private set
        private val logger: Logger = LogFactory.createLogger(InterfaceUI::class.java.name)
        var currentInterface = -1
        var currentViewMode: InterfaceDraw.ViewMode = InterfaceDraw.ViewMode.NONE

        @JvmStatic
        fun main(args: Array<String>) {
            EventQueue.invokeLater {
                try {
                    UIManager.put("Tree.collapsedIcon", ImageIcon(IconLibrary.TREE_OPEN))
                    UIManager.put("Tree.expandedIcon", ImageIcon(IconLibrary.TREE_CLOSE))

                    if (args.isEmpty()) {
                        try {
                            loadValues()
                        } catch (e: Exception) {
                            JOptionPane.showMessageDialog(null, "Properties can not be found, make sure you've a config.properties file.")
                            logger.log(Level.SEVERE, "Properties can not be found, make sure you've a config.properties file.\"")
                        }
                    } else {
                        val path = args[0]
                        logger.info("Using cache path: $path")
                        setCachePath(path)
                    }
                    logger.info("Application started...")
                    Cache.init()
                    val frame = InterfaceUI()
                    frame.setLocationRelativeTo(null)
                    frame.isVisible = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        @JvmStatic
        public fun getOrderedComps(interfaceId: Int): ArrayList<ComponentDefinition?>? {
            val comps: ArrayList<ComponentDefinition?> = ArrayList()
            val containers = ComponentDefinition.getInterfaceContainers(interfaceId)
            val allComps = ComponentDefinition.getInterface(interfaceId, false) ?: return null

            if (containers != null) {
                for (comp in containers) {
                    if (!comps.contains(comp)) comps.add(comp)

                    for (child in ComponentDefinition.getChildsByParent(interfaceId, comp.componentHash)) {
                        comps.add(child)
                    }
                }
            }

            for (i in allComps.indices) {
                if (allComps[i] == null) continue
                InterfaceDraw.setValues(allComps[i])
                var found = false
                for (c in comps) {
                    if (c?.componentId == allComps[i]?.componentId) found = true
                }
                if (!found) comps.add(allComps[i])
            }

            return comps
        }
    }

    fun updateComponentFields(comp: ComponentDefinition) {
        txt_x.getTextField().text = comp.baseX.toString()
        txt_y.getTextField().text = comp.baseY.toString()
        txt_width.getTextField().text = comp.baseWidth.toString()
        txt_height.getTextField().text = comp.baseHeight.toString()
    }

    private var _selectedComponent: ComponentDefinition? = null
    var selectedComponent: ComponentDefinition?
        get() = _selectedComponent
        set(value) {
            _selectedComponent = value
            value?.let { compDef ->
                InterfaceDraw.sizeCache.remove(compDef)
                InterfaceDraw.setValues(compDef)
                viewportPanel?.repaint()
            }
        }
}
