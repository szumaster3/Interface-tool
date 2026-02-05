package ui

import com.alex.utils.Constants
import com.interfaces.ComponentConstants
import com.interfaces.ComponentPosition
import com.interfaces.ContainerHelper
import com.interfaces.InterfaceUtils
import com.logging.LogFactory
import com.rs.cache.Cache
import com.rs.cache.loaders.ComponentDefinition
import com.sprites.ImageUtils
import com.sprites.SpriteDumper
import com.sprites.SpriteLoader
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath
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
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ListSelectionEvent
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class InterfaceUI : JFrame() {

    var progressMonitor: ProgressMonitor? = null
    private var interface_list: JList<*>? = null
    private var viewportPanel: JPanel? = null
    private var interfaceViewportScrollPane: JScrollPane? = null
    private var componentScrollpane: JScrollPane? = null
    private var comboBox: JComboBox<*>? = null
    private var chckbxShowContainers: JCheckBox = JCheckBox()
    private var chckbxRepeat: JCheckBox = JCheckBox()
    private var chckbxHorizontalFlip: JCheckBox = JCheckBox()
    private var chckbxVerticalFlip: JCheckBox = JCheckBox()
    private var chckbxHover: JCheckBox = JCheckBox()
    private var chckbxHasAlpha: JCheckBox = JCheckBox()
    private var chckbxShadow: JCheckBox = JCheckBox()
    private var chckbxFilled: JCheckBox = JCheckBox()
    private var chckbxAnimated: JCheckBox = JCheckBox()
    private var chckbxCentre: JCheckBox = JCheckBox()
    private var chckbxIgnoreZ: JCheckBox = JCheckBox()
    private var chckbxMultiline: JCheckBox = JCheckBox()

    private val txt_viewportX = JTextField()
    private val txt_viewportY = JTextField()
    private val txt_viewportZ = JTextField()
    private val txt_pitch = JTextField()
    private val txt_roll = JTextField()
    private val txt_yaw = JTextField()
    private val txt_vpadding = JTextField()

    private val txt_type = JTextField()
    private val txt_hash = JTextField()
    private var tabbedPane: JTabbedPane? = null
    private val txt_x = JTextField()
    private val txt_y = JTextField()
    private val txt_height = JTextField()
    private val txt_width = JTextField()
    private val txt_text = JTextField()
    private val txt_leftclick = JTextField()
    private val txt_overlayer = JTextField()
    private val txt_option1 = JTextField()
    private val txt_option2 = JTextField()
    private val txt_option3 = JTextField()
    private val txt_option4 = JTextField()
    private val txt_option5 = JTextField()
    private val txt_modex = JTextField()
    private val txt_positionmodeY = JTextField()
    private var chckbxShowHiddenComps = JCheckBox()
    private var chckbxShowRectangles = JCheckBox()
    private val txt_border = JTextField()
    private val txt_sprite = JTextField()
    private val txt_modeHeight = JTextField()
    private val txt_widthMode = JTextField()
    private val txt_model = JTextField()
    private val chckbxHidden = JCheckBox()
    private val txt_onMouseRepeat = JTextField()
    private val txt_fullonhover = JTextField()
    private val txt_mouseLeave = JTextField()
    private val txt_onload = JTextField()
    private val txt_onUseWith = JTextField()
    private val txt_onUse = JTextField()
    private val txt_onVarpTransmit = JTextField()
    private val txt_onInvTransmit = JTextField()
    private val txt_onStatTransmit = JTextField()
    private val txt_onTimer = JTextField()
    private val txt_varpTriggers = JTextField()
    private val txt_onOptionClick = JTextField()
    private val txt_font = JTextField()
    private val txt_animationId = JTextField()
    private val txt_color = JTextField()
    private val txt_trans = JTextField()
    private val txt_multi = JTextField()
    private val txt_scrollX = JTextField()
    private val txt_scrollY = JTextField()
    private val txt_inventoryTriggers = JTextField()
    private val txt_statTriggers = JTextField()
    private val txt_varcTriggers = JTextField()
    private val txt_varcStrTriggers = JTextField()
    private val txt_onClickRepeat = JTextField()
    private val txt_onDrag = JTextField()
    private val txt_onRelease = JTextField()
    private val txt_onHold = JTextField()
    private val txt_onDragStart = JTextField()
    private val txt_onDragRelease = JTextField()
    private val txt_onScroll = JTextField()
    private val txt_onVarcTransmit = JTextField()
    private val txt_onVarcStrTransmit = JTextField()
    private val txt_xali = JTextField()
    private val txt_modelZoom = JTextField()
    private val txt_yali = JTextField()
    private var txt_interId = JTextField()
    private var chckbxRealFontTesting = JCheckBox()
    private var ifListScrollPane: JScrollPane? = null
    private var result: BufferedImage? = null

    private var currentInterface = -1
    private var selectedComp = -1
    private var copiedComp: ComponentDefinition? = null

    private fun constructWestPanel() {
        val panel = JPanel(BorderLayout())

        panel.minimumSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.maximumSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)

        val searchPanel = JPanel(FlowLayout())
        txt_interId = JTextField()
        txt_interId.setBounds(
            Constants.CONTENT_PADDING,
            Constants.CONTENT_PADDING,
            Constants.SEARCHBOX_WIDTH,
            Constants.SEARCHBOX_HEIGHT
        )
        txt_interId.columns = 10
        searchPanel.add(txt_interId)

        val btnFind = JButton("Find")
        btnFind.setBounds(
            Constants.CONTENT_PADDING * 2 + Constants.SEARCHBOX_WIDTH,
            Constants.CONTENT_PADDING,
            Constants.BUTTON_WIDTH,
            Constants.BUTTON_HEIGHT
        )
        btnFind.addActionListener {
            val id = txt_interId.text.toInt()
            currentInterface = id
            drawTree(id)
        }
        searchPanel.add(btnFind)
        panel.add(searchPanel, BorderLayout.NORTH)

        ifListScrollPane = JScrollPane()
        ifListScrollPane!!.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.VIEWPORT_HEIGHT)
        ifListScrollPane!!.setBounds(10, 54, 193, 331)
        interface_list = JList(populateList())
        ifListScrollPane!!.setViewportView(interface_list)
        interface_list!!.addListSelectionListener { evt: ListSelectionEvent ->
            if (evt.valueIsAdjusting) return@addListSelectionListener
            val id = interface_list!!.selectedValue.toString().replace("Interface: ".toRegex(), "").toInt()
            logger.info("Interface $id is selected.")
            currentInterface = id
            viewportPanel!!.repaint()
            drawTree(id)
            cleanValues()
        }
        ifListScrollPane?.let { panel.add(it, BorderLayout.CENTER) }

        val popupMenu = JPopupMenu()
        val export = JMenuItem("Export")
        popupMenu.add(export)
        interface_list!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(me: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(me) // if right mouse button clicked
                    && !interface_list!!.isSelectionEmpty // and list selection is not empty
                    && (interface_list!!.locationToIndex(me.point) // and clicked point is
                            == interface_list!!.selectedIndex)
                ) {       //   inside selected item bounds
                    popupMenu.show(interface_list, me.x, me.y)
                }
            }
        })
        export.addActionListener {
            try {
                exportInterface(currentInterface)
            } catch (e: IOException) {
                JOptionPane.showMessageDialog(
                    interfaceViewportScrollPane,
                    "File could not be dumped, error: " + e.message
                )
            }
        }

        val moreButtonsPanel = JPanel(FlowLayout())
        moreButtonsPanel.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT / 3)

        val addInterfaceButton = JButton("Add Interface")
        addInterfaceButton.addActionListener {
            val defaultComp = ComponentDefinition()
            defaultComp.componentId = ComponentDefinition.interfaceDefinitionsSize
            defaultComp.type = ComponentConstants.SPRITE
            defaultComp.optionBase = ""
            defaultComp.baseX = 0
            defaultComp.baseY = 0
            defaultComp.baseWidth = 18
            defaultComp.baseHeight = 18
            defaultComp.spriteId = 18
            defaultComp.parentId = -1
            Cache.store!!.indexes[3].putFile(defaultComp.componentId, 0, defaultComp.encode(true))
            ComponentDefinition.componentDefinition = arrayOfNulls(ComponentDefinition.interfaceDefinitionsSize)
            val listModel = DefaultListModel<String>()
            val tempModel = populateList()

            for (i in 0 until tempModel.size) {
                val element = tempModel.getElementAt(i) as String
                listModel.addElement(element)
            }
            val list = JList(listModel)
            list.selectionMode = ListSelectionModel.SINGLE_SELECTION
            list.addListSelectionListener { evt: ListSelectionEvent ->
                if (evt.valueIsAdjusting) return@addListSelectionListener
                val id = list.selectedValue.toString().replace("Interface: ".toRegex(), "").toInt()
                currentInterface = id
                drawTree(id)
            }
            ifListScrollPane!!.setViewportView(list)
            val lastIndex = listModel.size - 1
            if (lastIndex >= 0) {
                list.selectedIndex = lastIndex
                list.ensureIndexIsVisible(lastIndex)
            }
        }

        addInterfaceButton.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.BUTTON_HEIGHT)
        moreButtonsPanel.add(addInterfaceButton)

        val deleteInterfaceButton = JButton("Delete Selected").apply {
            preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.BUTTON_HEIGHT)

            addActionListener {
                val option = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure that you want to remove interface $currentInterface?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION
                )


                if (option == JOptionPane.YES_OPTION) {
                    val componentsSize = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)

                    for (i in 0 until componentsSize) {
                        if (i == 0) {
                            addBaseComponent(currentInterface, true)
                        } else {
                            Cache.store!!.indexes[3].removeFile(currentInterface, i)
                        }
                    }

                    try {
                        Cache.init()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    ComponentDefinition.componentDefinition =
                        arrayOfNulls(ComponentDefinition.interfaceDefinitionsSize)
                    drawTree(currentInterface)
                }
            }
        }

        deleteInterfaceButton.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.BUTTON_HEIGHT)
        moreButtonsPanel.add(deleteInterfaceButton)

        val premadeComponentsPanel = JPanel(FlowLayout())
        premadeComponentsPanel.border =
            TitledBorder(null, "Premade components", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        premadeComponentsPanel.preferredSize = Dimension(Constants.PREMADE_COMP_WIDTH, Constants.PREMADE_COMP_HEIGHT)

        val comboBox = JComboBox<String>()
        comboBox.model = DefaultComboBoxModel(
            arrayOf(
                "Close button",
                "Normal button",
                "Start interface",
                "Basic custom hover",
                "Basic button with pop-up"
            )
        )
        comboBox.preferredSize = Dimension(Constants.PREMADE_COMP_WIDTH - 10, Constants.BUTTON_HEIGHT)
        premadeComponentsPanel.add(comboBox)


        val btnAddPremadeComponent = JButton("Add Component")
        btnAddPremadeComponent.addActionListener { addDefaultComponent(currentInterface) }
        btnAddPremadeComponent.preferredSize = Dimension(Constants.PREMADE_COMP_WIDTH - 10, Constants.BUTTON_HEIGHT)
        premadeComponentsPanel.add(btnAddPremadeComponent)
        moreButtonsPanel.add(premadeComponentsPanel)

        val saveButton = JButton("Save")
        saveButton.preferredSize = Dimension(Constants.LEFT_SCROLLPANE_WIDTH, Constants.BUTTON_HEIGHT)
        saveButton.addActionListener { _: ActionEvent? ->
            if (currentInterface != -1 && selectedComp != -1) {
                saveInterface(currentInterface, selectedComp)
                drawTree(currentInterface)
                setValues(currentInterface, selectedComp)
            } else {
                JOptionPane.showMessageDialog(
                    interfaceViewportScrollPane,
                    "Please selected a component & interface before saving it."
                )
            }
        }
        moreButtonsPanel.add(saveButton)
        panel.add(moreButtonsPanel, BorderLayout.SOUTH)
        contentPane.add(panel, BorderLayout.WEST)
    }

    private fun constructEastPanel() {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.maximumSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.minimumSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)

        componentScrollpane = JScrollPane()
        componentScrollpane!!.preferredSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.VIEWPORT_HEIGHT)
        //jtree itself
        val componentTree = JTree(createInterfaceTree(1))
        componentScrollpane!!.setViewportView(componentTree)
        componentTree.addTreeSelectionListener { _: TreeSelectionEvent? ->
            val selectedNode = componentTree.lastSelectedPathComponent as DefaultMutableTreeNode
            val id = selectedNode.userObject.toString().replace("Component ".toRegex(), "").toInt()
            selectedComp = id
            setValues(currentInterface, id)
            repaint()
        }
        panel.add(componentScrollpane, BorderLayout.CENTER)

        val moreButtonsPanel = JPanel(FlowLayout())
        moreButtonsPanel.preferredSize =
            Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT / 3)

        val componentButtons = JPanel(FlowLayout())
        componentButtons.preferredSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.BUTTON_HEIGHT)

        val buttonSize = Dimension(Constants.BUTTON_WIDTH_SMALL, Constants.BUTTON_HEIGHT_SMALL)
        val btnCopy = JButton("Copy")
        btnCopy.toolTipText = "Copy selected interface"
        btnCopy.preferredSize = buttonSize
        btnCopy.addActionListener { _: ActionEvent? ->
            copiedComp = ComponentDefinition.getInterfaceComponent(currentInterface, selectedComp)
            if (copiedComp != null) {
                logger.info("Component " + copiedComp!!.componentId + " copied from interface " + copiedComp!!.interfaceId)
            }
        }
        componentButtons.add(btnCopy)

        val btnPaste = JButton("Paste")
        btnPaste.preferredSize = buttonSize
        btnPaste.toolTipText = "Past your copied component into the selected interface"
        btnPaste.addActionListener { _: ActionEvent? ->
            if (currentInterface == -1) {
                return@addActionListener
            }
            pasteComponent()
        }
        componentButtons.add(btnPaste)

        val btnDelete = JButton("Delete")
        btnDelete.preferredSize = buttonSize
        btnDelete.toolTipText = "Deletes the selected component"
        btnDelete.addActionListener { _: ActionEvent? ->
            if (selectedComp <= 0) {
                JOptionPane.showMessageDialog(interfaceViewportScrollPane, "Please select an component first.")
            } else {
                val c = ComponentDefinition.getInterfaceComponent(currentInterface, selectedComp)
                var message: String? = null
                if (c != null) {
                    message =
                        (if (c.type == ComponentConstants.CONTAINER) "Are you sure that you want to remove component $selectedComp from interface $currentInterface ? NOTE: this component is a container, childs will be removed aswell." else "Are you sure that you want to remove  component $selectedComp from interface $currentInterface ?")
                }
                val option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Inane warning",
                    JOptionPane.YES_NO_OPTION
                )
                if (option == 0) {
                    try {
                        Cache.store!!.indexes[3].removeFile(currentInterface, selectedComp)
                        Cache.store!!.indexes[3].resetCachedFiles()
                        Cache.store!!.indexes[3].rewriteTable()
                        try {
                            Cache.init()
                        } catch (e1: IOException) {
                            e1.printStackTrace()
                        }
                    } finally {
                        drawTree(currentInterface)
                    }
                }
            }
        }
        componentButtons.add(btnDelete)
        moreButtonsPanel.add(componentButtons)

        val settingsPanel = JPanel(FlowLayout())
        settingsPanel.border = TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null)
        settingsPanel.preferredSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT / 4)

        val checkBoxSize = Dimension(Constants.RIGHT_SCROLLPANE_WIDTH - 10, Constants.BUTTON_HEIGHT_SMALL)

        chckbxShowContainers = JCheckBox("Show Containers")
        chckbxShowContainers.isSelected = true
        chckbxShowContainers.preferredSize = checkBoxSize
        chckbxShowContainers.addItemListener { e: ItemEvent ->
            drawInterfaceComponents(
                currentInterface,
                e.stateChange == ItemEvent.SELECTED,
                chckbxShowHiddenComps.isSelected,
                chckbxShowRectangles.isSelected,
                chckbxRealFontTesting.isSelected
            )
        }
        settingsPanel.add(chckbxShowContainers)

        chckbxShowRectangles = JCheckBox("Show Models")
        chckbxShowRectangles.isSelected = true
        chckbxShowRectangles.preferredSize = checkBoxSize
        chckbxShowRectangles.addItemListener { e: ItemEvent ->
            drawInterfaceComponents(
                currentInterface,
                chckbxShowContainers.isSelected,
                chckbxShowHiddenComps.isSelected,
                e.stateChange == ItemEvent.SELECTED,
                chckbxRealFontTesting.isSelected
            )
        }
        settingsPanel.add(chckbxShowRectangles)

        chckbxShowHiddenComps = JCheckBox("Show Hidden")
        chckbxShowHiddenComps.preferredSize = checkBoxSize
        settingsPanel.add(chckbxShowHiddenComps)

        chckbxRealFontTesting = JCheckBox("Draw Real Font")
        chckbxRealFontTesting.preferredSize = checkBoxSize
        chckbxRealFontTesting.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(arg0: MouseEvent) {
                drawTree(currentInterface)
                drawInterfaceComponents(
                    currentInterface,
                    chckbxShowContainers.isSelected,
                    chckbxShowHiddenComps.isSelected,
                    chckbxShowRectangles.isSelected,
                    chckbxRealFontTesting.isSelected
                )
            }
        })
        chckbxRealFontTesting.setBounds(6, 107, 193, 25)
        settingsPanel.add(chckbxRealFontTesting)
        moreButtonsPanel.add(settingsPanel)
        panel.add(moreButtonsPanel, BorderLayout.SOUTH)
        contentPane.add(panel, BorderLayout.EAST)
    }

    private fun constructCenterPanel() {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.maximumSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        panel.minimumSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.DEFAULT_EDITOR_HEIGHT)
        val buttonSize = Dimension(Constants.BUTTON_WIDTH, Constants.BUTTON_HEIGHT_SMALL)
        val buttonPanel = JPanel(FlowLayout())

        val btnAddSprite = JButton("Add Sprite")
        btnAddSprite.preferredSize = buttonSize
        btnAddSprite.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(801, 4)
            comp?.parentId = -1
            comp?.componentHash =
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!.indexes[3].putFile(
                currentInterface,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                comp!!.encode(true)
            )
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddSprite)

        val btnAddText = JButton("Add Text")
        btnAddText.preferredSize = buttonSize
        btnAddText.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(7, 1)
            comp?.baseX = 0
            comp?.baseY = 0
            comp?.baseWidth = 1
            comp?.baseHeight = 1
            comp?.parentId = -1
            comp?.componentHash =
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!.indexes[3].putFile(
                currentInterface,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                comp!!.encode(true)
            )
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddText)

        val btnAddContainer = JButton("Add Container")
        btnAddContainer.preferredSize = buttonSize
        btnAddContainer.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(778, 0)
            comp?.baseX = 0
            comp?.baseY = 0
            comp?.baseWidth = 50
            comp?.baseHeight = 50
            comp?.parentId = -1
            comp?.componentHash =
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!.indexes[3].putFile(
                currentInterface,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                comp!!.encode(true)
            )
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddContainer)

        val btnAddModel = JButton("Add Model")
        btnAddModel.preferredSize = buttonSize
        btnAddModel.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(205, 90)
            comp?.parentId = -1
            comp?.componentHash =
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!.indexes[3].putFile(
                currentInterface,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                comp!!.encode(true)
            )
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddModel)

        val btnAddRectangle = JButton("Add Rectangle")
        btnAddRectangle.preferredSize = buttonSize
        btnAddRectangle.addActionListener { _: ActionEvent? ->
            if (currentInterface <= 0) return@addActionListener
            val comp = ComponentDefinition.getInterfaceComponent(625, 0)
            comp?.baseX = 0
            comp?.baseY = 0
            comp?.componentHash =
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) + (currentInterface shl 16)
            Cache.store!!.indexes[3].putFile(
                currentInterface,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                comp!!.encode(true)
            )
            ComponentDefinition.getInterface(currentInterface, true)
            drawTree(currentInterface)
        }
        buttonPanel.add(btnAddRectangle)

        val openSpriteBrowserBtn = JButton("Browse Sprites")
        openSpriteBrowserBtn.preferredSize = buttonSize
        openSpriteBrowserBtn.addActionListener {
            SpriteBrowser(this) { spriteId,w,h ->
                if (currentInterface <= 0) return@SpriteBrowser
                selectedComponent?.spriteId = spriteId
                selectedComponent?.baseWidth = w
                selectedComponent?.baseHeight =h
                setValues(currentInterface, selectedComponent!!.componentId)
                repaint()
            }
        }
        buttonPanel.add(openSpriteBrowserBtn)

        panel.add(buttonPanel, BorderLayout.NORTH)

        viewportPanel = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                if (currentInterface == -1) return
                drawInterfaceToGraphics(
                    g,
                    currentInterface,
                    chckbxShowContainers.isSelected,
                    chckbxShowHiddenComps.isSelected,
                    chckbxShowRectangles.isSelected,
                    chckbxRealFontTesting.isSelected
                )
                if (selectedComponent != null && (selectedComponent!!.componentHash shr 16) == currentInterface) {
                    g.color = Color.WHITE
                    ComponentPosition.setValues(selectedComponent)
                    g.drawRect(
                        ComponentDefinition.getX(selectedComponent!!, currentInterface),
                        ComponentDefinition.getY(selectedComponent!!, currentInterface),
                        selectedComponent!!.width,
                        selectedComponent!!.height
                    )
                }
            }
        }
        (viewportPanel as JPanel).preferredSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
        (viewportPanel as JPanel).minimumSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
        (viewportPanel as JPanel).maximumSize = Dimension(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)

        panel.add(viewportPanel!!, BorderLayout.CENTER)

        tabbedPane = JTabbedPane(JTabbedPane.TOP)
        tabbedPane!!.preferredSize = Dimension(
            Constants.DEFAULT_EDITOR_WIDTH - Constants.LEFT_SCROLLPANE_WIDTH - Constants.RIGHT_SCROLLPANE_WIDTH,
            Constants.DEFAULT_EDITOR_HEIGHT - Constants.VIEWPORT_HEIGHT - Constants.BUTTON_HEIGHT - 4
        )
        tabbedPane!!.addTab("General", null, constructGeneralTab())
        tabbedPane!!.addTab("Text", null, constructTextTab())
        tabbedPane!!.addTab("Model", null, constructModelTab())
        tabbedPane!!.addTab("Container", null, constructContainerTab())
        tabbedPane!!.addTab("Sprite", null, constructSpriteTab())
        tabbedPane!!.addTab("Scripts", null, constructScriptsTab())
        tabbedPane!!.addTab("Triggers", null, constructTriggersTab())
        panel.add(tabbedPane, BorderLayout.SOUTH)

        contentPane.add(panel, BorderLayout.CENTER)
    }

    private fun createInfoFieldPanel(label: String, backingField: JTextField): JPanel {
        val fieldPanel = JPanel(BorderLayout())
        fieldPanel.preferredSize = Dimension(Constants.INFO_FIELD_WIDTH, Constants.INFO_FIELD_HEIGHT)

        val labelField = JTextField()
        labelField.preferredSize =
            Dimension(Constants.INFO_FIELD_WIDTH / 3, Constants.INFO_FIELD_HEIGHT)
        labelField.text = label
        labelField.isEditable = false
        fieldPanel.add(labelField, BorderLayout.WEST)

        backingField.text = ""
        backingField.isEditable = true
        fieldPanel.add(backingField, BorderLayout.CENTER)

        return fieldPanel
    }

    private fun createInfoFieldPanel(label: String, backingField: JCheckBox): JPanel {
        val fieldPanel = JPanel(BorderLayout())
        fieldPanel.preferredSize = Dimension(Constants.INFO_FIELD_WIDTH, Constants.INFO_FIELD_HEIGHT)

        val labelField = JTextField()
        labelField.preferredSize =
            Dimension(Constants.INFO_FIELD_WIDTH / 3, Constants.INFO_FIELD_HEIGHT)
        labelField.text = label
        labelField.isEditable = false
        fieldPanel.add(labelField, BorderLayout.WEST)

        backingField.text = ""
        backingField.isEnabled = true
        fieldPanel.add(backingField, BorderLayout.CENTER)

        return fieldPanel
    }

    private fun constructGeneralTab(): JPanel {
        val generalTab = JPanel(GridLayout(10, 2))
        generalTab.preferredSize = Dimension(
            Constants.DEFAULT_EDITOR_WIDTH - Constants.LEFT_SCROLLPANE_WIDTH - Constants.RIGHT_SCROLLPANE_WIDTH - 10,
            Constants.DEFAULT_EDITOR_HEIGHT / 3 - 15
        )
        generalTab.add(createInfoFieldPanel("Overlayer", txt_overlayer))
        generalTab.add(createInfoFieldPanel("Hidden", chckbxHidden))
        generalTab.add(createInfoFieldPanel("Type", txt_type))
        generalTab.add(createInfoFieldPanel("Hash", txt_hash))
        generalTab.add(createInfoFieldPanel("X Pos", txt_x))
        generalTab.add(createInfoFieldPanel("X Mode", txt_modex))
        generalTab.add(createInfoFieldPanel("Y Pos", txt_y))
        generalTab.add(createInfoFieldPanel("Y Mode", txt_positionmodeY))
        generalTab.add(createInfoFieldPanel("Width", txt_width))
        generalTab.add(createInfoFieldPanel("Width Mode", txt_widthMode))
        generalTab.add(createInfoFieldPanel("Height", txt_height))
        generalTab.add(createInfoFieldPanel("Height Mode", txt_modeHeight))
        generalTab.add(createInfoFieldPanel("Color", txt_color))
        generalTab.add(createInfoFieldPanel("Transparency", txt_trans))
        return generalTab
    }

    private fun constructTextTab(): JPanel {
        val textTab = JPanel(GridLayout(10, 2))
        textTab.add(createInfoFieldPanel("Left Click", txt_leftclick))
        textTab.add(createInfoFieldPanel("Text", txt_text))
        textTab.add(createInfoFieldPanel("Option 1", txt_option1))
        textTab.add(createInfoFieldPanel("Font ID", txt_font))
        textTab.add(createInfoFieldPanel("Option 2", txt_option2))
        textTab.add(createInfoFieldPanel("Multi", txt_multi))
        textTab.add(createInfoFieldPanel("Option 3", txt_option3))
        textTab.add(createInfoFieldPanel("X Align", txt_xali))
        textTab.add(createInfoFieldPanel("Option 4", txt_option4))
        textTab.add(createInfoFieldPanel("Y Align", txt_yali))
        textTab.add(createInfoFieldPanel("Option 5", txt_option5))
        textTab.add(createInfoFieldPanel("Has shadow", chckbxShadow))
        return textTab
    }

    private fun constructModelTab(): JPanel {
        val modelTab = JPanel(GridLayout(10, 2))
        modelTab.add(createInfoFieldPanel("Model ID", txt_model))
        modelTab.add(createInfoFieldPanel("Animation", txt_animationId))
        modelTab.add(createInfoFieldPanel("Zoom", txt_modelZoom))
        modelTab.add(createInfoFieldPanel("Animated", chckbxAnimated))
        modelTab.add(createInfoFieldPanel("Centre", chckbxCentre))
        modelTab.add(createInfoFieldPanel("Ignore Z Buffer", chckbxIgnoreZ))
        modelTab.add(createInfoFieldPanel("Viewport X", txt_viewportX))
        modelTab.add(createInfoFieldPanel("Viewport Y", txt_viewportY))
        modelTab.add(createInfoFieldPanel("Viewport Z", txt_viewportZ))
        modelTab.add(createInfoFieldPanel("Pitch", txt_pitch))
        modelTab.add(createInfoFieldPanel("Roll", txt_roll))
        modelTab.add(createInfoFieldPanel("Yaw", txt_yaw))
        return modelTab
    }

    private fun constructContainerTab(): JPanel {
        val spriteTab = JPanel(GridLayout(10, 2))
        spriteTab.add(createInfoFieldPanel("Layer Width", txt_scrollX))
        spriteTab.add(createInfoFieldPanel("Layer Height", txt_scrollY))
        spriteTab.add(createInfoFieldPanel("Disable Hover", chckbxHover))
        return spriteTab
    }

    private fun constructSpriteTab(): JPanel {
        val spriteTab = JPanel(GridLayout(10, 2))
        spriteTab.add(createInfoFieldPanel("Sprite ID", txt_sprite))
        spriteTab.add(createInfoFieldPanel("Border Thickness", txt_border))
        spriteTab.add(createInfoFieldPanel("Repeat", chckbxRepeat))
        spriteTab.add(createInfoFieldPanel("H Flip", chckbxHorizontalFlip))
        spriteTab.add(createInfoFieldPanel("V Flip", chckbxVerticalFlip))
        spriteTab.add(createInfoFieldPanel("Has alpha", chckbxHasAlpha))
        return spriteTab
    }

    private fun constructScriptsTab(): JPanel {
        val scriptsTab = JPanel(GridLayout(10, 2))
        scriptsTab.add(createInfoFieldPanel("onLoad", txt_onload))
        scriptsTab.add(createInfoFieldPanel("onMouseOver", txt_fullonhover))
        scriptsTab.add(createInfoFieldPanel("onMouseLeave", txt_mouseLeave))
        scriptsTab.add(createInfoFieldPanel("onUseWith", txt_onUseWith))
        scriptsTab.add(createInfoFieldPanel("onUse", txt_onUse))
        scriptsTab.add(createInfoFieldPanel("onVarpTransmit", txt_onVarpTransmit))
        scriptsTab.add(createInfoFieldPanel("onInvTransmit", txt_onInvTransmit))
        scriptsTab.add(createInfoFieldPanel("onStatTransmit", txt_onStatTransmit))
        scriptsTab.add(createInfoFieldPanel("onTimer", txt_onTimer))
        scriptsTab.add(createInfoFieldPanel("onOptionClick", txt_onOptionClick))
        scriptsTab.add(createInfoFieldPanel("onMouseRepeat", txt_onMouseRepeat))
        scriptsTab.add(createInfoFieldPanel("onClickRepeat", txt_onClickRepeat))
        scriptsTab.add(createInfoFieldPanel("onDrag", txt_onDrag))
        scriptsTab.add(createInfoFieldPanel("onRelease", txt_onRelease))
        scriptsTab.add(createInfoFieldPanel("onHold", txt_onHold))
        scriptsTab.add(createInfoFieldPanel("onDragStart", txt_onDragStart))
        scriptsTab.add(createInfoFieldPanel("onDragRelease", txt_onDragRelease))
        scriptsTab.add(createInfoFieldPanel("onScroll", txt_onScroll))
        scriptsTab.add(createInfoFieldPanel("onVarcTransmit", txt_onVarcTransmit))
        scriptsTab.add(createInfoFieldPanel("onVarcstrTransmit", txt_onVarcStrTransmit))
        return scriptsTab
    }

    private fun constructTriggersTab(): JPanel {
        val triggersTab = JPanel(GridLayout(10, 2))
        triggersTab.add(createInfoFieldPanel("varpTriggers", txt_varpTriggers))
        triggersTab.add(createInfoFieldPanel("inventoryTriggers", txt_inventoryTriggers))
        triggersTab.add(createInfoFieldPanel("statTriggers", txt_statTriggers))
        triggersTab.add(createInfoFieldPanel("varcTriggers", txt_varcTriggers))
        triggersTab.add(createInfoFieldPanel("varcStrTriggers", txt_varcStrTriggers))
        return triggersTab
    }

    private var selectedComponent: ComponentDefinition? = null

    init {
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

        val mnNewMenu_1 = JMenu("Info")
        mnNewMenu_1.addActionListener { _: ActionEvent? ->
            JOptionPane.showMessageDialog(
                interfaceViewportScrollPane,
                "Interface editor made by Shnek, Discord : Cara Shnek#6969 "
            )
        }
        menuBar.add(mnNewMenu_1)

        val mnAbout = JMenu("Extra")
        menuBar.add(mnAbout)

        val mntmDumpSprites = JMenuItem("Dump sprites")
        mntmDumpSprites.addActionListener { _: ActionEvent? ->
            progressMonitor = ProgressMonitor(menuBar, "Dumping sprites", "", 0, Cache.store!!.indexes[8].lastArchiveId)
            progressMonitor!!.setProgress(1)
            SpriteDumper.dump()
        }
        mnAbout.add(mntmDumpSprites)

        val mntmPackInterface = JMenuItem("Pack interface")
        mntmPackInterface.addActionListener { _: ActionEvent? -> }
        mnAbout.add(mntmPackInterface)

        pack()
        revalidate()
    }

    fun setValues(inter: Int, componentId: Int) {
        logger.info("Viewing: [$inter]:[$componentId]")
        cleanValues()

        val comp = ComponentDefinition.getInterfaceComponent(inter, componentId) ?: return

        selectedComponent = comp

        txt_hash.text = comp.componentHash.toString()
        txt_type.text = when (comp.type) {
            ComponentConstants.CONTAINER -> "Container"
            ComponentConstants.TEXT -> "Text"
            ComponentConstants.SPRITE -> "Sprite"
            ComponentConstants.FIGURE -> "Figure"
            ComponentConstants.MODEL -> "Model"
            else -> comp.type.toString()
        }

        txt_x.text = comp.baseX.toString()
        txt_y.text = comp.baseY.toString()
        txt_width.text = comp.baseWidth.toString()
        txt_height.text = comp.baseHeight.toString()
        txt_widthMode.text = comp.aspectWidthType.toString()
        txt_modeHeight.text = comp.aspectHeightType.toString()
        txt_modex.text = comp.xMode.toString()
        txt_positionmodeY.text = comp.yMode.toString()
        txt_overlayer.text = comp.parentId.toString()

        txt_sprite.text = comp.spriteId.toString()
        txt_border.text = comp.borderThickness.toString()
        txt_color.text = comp.color.toString()
        txt_trans.text = comp.transparency.toString()
        chckbxHorizontalFlip.isSelected = comp.hFlip
        chckbxVerticalFlip.isSelected = comp.vFlip
        chckbxFilled.isSelected = comp.filled
        chckbxRepeat.isSelected = comp.spriteTiling
        chckbxHasAlpha.isSelected = comp.hasAlpha
        chckbxHover.isSelected = comp.noClickThrough
        chckbxHidden.isSelected = comp.hidden

        txt_text.text = comp.text
        txt_font.text = comp.fontId.toString()
        txt_multi.text = comp.multiline.toString()
        txt_xali.text = comp.textHorizontalAli.toString()
        txt_yali.text = comp.textVerticalAli.toString()
        chckbxShadow.isSelected = comp.shadow

        txt_scrollX.text = comp.scrollMaxH.toString()
        txt_scrollY.text = comp.scrollMaxV.toString()

        txt_model.text = comp.modelId.toString()
        txt_animationId.text = comp.modelAnimId.toString()
        txt_modelZoom.text = comp.spriteScale.toString()
        chckbxAnimated.isSelected = comp.animated
        chckbxCentre.isSelected = comp.centreType
        chckbxIgnoreZ.isSelected = comp.ignoreZBuffer
        txt_viewportX.text = comp.viewportX.toString()
        txt_viewportY.text = comp.viewportY.toString()
        txt_viewportZ.text = comp.viewportZ.toString()
        txt_pitch.text = comp.spritePitch.toString()
        txt_roll.text = comp.spriteRoll.toString()
        txt_yaw.text = comp.spriteYaw.toString()

        txt_leftclick.text = ""
        txt_option1.text = ""
        txt_option2.text = ""
        txt_option3.text = ""
        txt_option4.text = ""
        txt_option5.text = ""
        if (comp.rightclickOptions != null) {
            var i = 0
            while (i < comp.rightclickOptions!!.size && i < 6) {
                val text = comp.rightclickOptions!![i]
                if (text != null) {
                    when (i) {
                        0 -> txt_leftclick.text = text
                        1 -> txt_option1.text = text
                        2 -> txt_option2.text = text
                        3 -> txt_option3.text = text
                        4 -> txt_option4.text = text
                        5 -> txt_option5.text = text
                    }
                }
                i++
            }
        }

        txt_onload.text = joinScript(comp.onLoadScript)
        txt_fullonhover.text = joinScript(comp.onMouseHoverScript)
        txt_mouseLeave.text = joinScript(comp.onMouseLeaveScript)
        txt_onOptionClick.text = joinScript(comp.onOptionClick)
        txt_onUseWith.text = joinScript(comp.onUseWith)
        txt_onUse.text = joinScript(comp.onUse)
        txt_onVarpTransmit.text = joinScript(comp.onVarpTransmit)
        txt_onMouseRepeat.text = joinScript(comp.onMouseRepeat)
        txt_onClickRepeat.text = joinScript(comp.onClickRepeat)
        txt_onDrag.text = joinScript(comp.onDrag)
        txt_onRelease.text = joinScript(comp.onRelease)
        txt_onHold.text = joinScript(comp.onHold)
        txt_onDragStart.text = joinScript(comp.onDragStart)
        txt_onDragRelease.text = joinScript(comp.onDragRelease)
        txt_onScroll.text = joinScript(comp.onScroll)
        txt_onVarcTransmit.text = joinScript(comp.onVarcTransmit)
        txt_onVarcStrTransmit.text = joinScript(comp.onVarcStrTransmit)

        txt_varpTriggers.text = joinInts(comp.varpTriggers)
        txt_inventoryTriggers.text = joinInts(comp.inventoryTriggers)
        txt_statTriggers.text = joinInts(comp.statTriggers)
        txt_varcTriggers.text = joinInts(comp.varcTriggers)
        txt_varcStrTriggers.text = joinInts(comp.varcstrTriggers)
    }

    private fun joinScript(script: Array<Any?>?): String {
        if (script == null) return ""
        val sb = StringBuilder()
        for (o in script) sb.append(o).append(";")
        return sb.toString()
    }

    private fun joinInts(array: IntArray?): String {
        if (array == null) return ""
        val sb = StringBuilder()
        for (i in array) sb.append(i).append(";")
        return sb.toString()
    }

    private fun cleanTab(container: Container) {
        for (c in container.components) if (c is JTextField && c.isEnabled()) {
            val ctrl = c
            if (ctrl.isEditable) ctrl.text = null
        }
    }

    fun cleanValues() {
        for (i in 0 until tabbedPane!!.tabCount) {
            cleanTab(tabbedPane!!.getComponentAt(i) as Container)
        }
    }

    fun saveInterface(inter: Int, comp: Int) {
        var c = ComponentDefinition.getInterfaceComponent(inter, comp)

        if (c == null) {
            c = ComponentDefinition()
            c.componentId = comp
            c.type = ComponentConstants.SPRITE
            c.optionBase = ""
            c.contentType = 0
            if (ComponentDefinition.componentDefinition!![inter] == null) ComponentDefinition.componentDefinition!![inter] =
                arrayOfNulls(ComponentDefinition.getInterfaceDefinitionsComponentsSize(inter))
            ComponentDefinition.componentDefinition!![inter]!![comp] = c
        }

        c.parentId = parseIntSafe(txt_overlayer.text, -1)
        c.baseX = parseIntSafe(txt_x.text, 0)
        c.baseY = parseIntSafe(txt_y.text, 0)
        c.baseWidth = parseIntSafe(txt_width.text, 0)
        c.baseHeight = parseIntSafe(txt_height.text, 0)
        c.aspectWidthType = parseIntSafe(txt_widthMode.text, 0).toByte()
        c.aspectHeightType = parseIntSafe(txt_modeHeight.text, 0).toByte()
        c.xMode = parseIntSafe(txt_modex.text, 0).toByte()
        c.yMode = parseIntSafe(txt_positionmodeY.text, 0).toByte()
        c.hidden = chckbxHidden.isSelected

        when (c.type) {
            ComponentConstants.CONTAINER -> {
                c.scrollMaxH = parseIntSafe(txt_scrollX.text, 0)
                c.scrollMaxV = parseIntSafe(txt_scrollY.text, 0)
                c.noClickThrough = chckbxHover.isSelected
            }

            ComponentConstants.SPRITE -> {
                c.spriteId = parseIntSafe(txt_sprite.text, 0)
                c.angle2d = 0
                c.transparency = parseIntSafe(txt_trans.text, 255)
                c.borderThickness = parseIntSafe(txt_border.text, 0)
                c.shadowColor = 0

                c.spriteTiling = chckbxRepeat.isSelected
                c.hasAlpha = chckbxHasAlpha.isSelected
                c.vFlip = chckbxVerticalFlip.isSelected
                c.hFlip = chckbxHorizontalFlip.isSelected

                c.flag = 0
                if (c.spriteTiling) c.flag = c.flag or 1
                if (c.hasAlpha) c.flag = c.flag or 2
            }

            ComponentConstants.MODEL -> {
                c.modelId = parseIntSafe(txt_model.text, -1)
                c.modelAnimId = parseIntSafe(txt_animationId.text, -1)
                c.spriteScale = parseIntSafe(txt_modelZoom.text, 100)

                c.animated = chckbxAnimated.isSelected
                c.centreType = chckbxCentre.isSelected
                c.ignoreZBuffer = chckbxIgnoreZ.isSelected

                c.viewportX = parseIntSafe(txt_viewportX.text, 0)
                c.viewportY = parseIntSafe(txt_viewportY.text, 0)
                c.viewportZ = parseIntSafe(txt_viewportZ.text, 0)
                c.spritePitch = parseIntSafe(txt_pitch.text, 0)
                c.spriteRoll = parseIntSafe(txt_roll.text, 0)
                c.spriteYaw = parseIntSafe(txt_yaw.text, 0)
            }

            ComponentConstants.TEXT -> {
                c.text = txt_text.text
                c.textHorizontalAli = parseIntSafe(txt_xali.text, 0)
                c.textVerticalAli = parseIntSafe(txt_yali.text, 0)
                c.vPadding = parseIntSafe(txt_vpadding.text, 0)
                c.shadow = chckbxShadow.isSelected
                c.fontId = parseIntSafe(txt_font.text, -1)
                c.color = parseIntSafe(txt_color.text, 0)
                c.transparency = parseIntSafe(txt_trans.text, 255)
                c.multiline = chckbxMultiline.isSelected
            }

            ComponentConstants.FIGURE -> {
                c.color = parseIntSafe(txt_color.text, 0)
                c.filled = chckbxFilled.isSelected
                c.transparency = parseIntSafe(txt_trans.text, 255)
            }
        }
        c.optionMask = 0
        c.rightclickOptions = null

        if (!txt_leftclick.text.isEmpty()) {
            c.rightclickOptions = arrayOf(txt_leftclick.text)
            c.optionMask = ComponentConstants.CLICK_MASK
        }
        c.onLoadScript = InterfaceUtils.getScriptArray(txt_onload.text)
        c.onMouseHoverScript = InterfaceUtils.getScriptArray(txt_fullonhover.text)
        c.onMouseLeaveScript = InterfaceUtils.getScriptArray(txt_mouseLeave.text)
        c.onUseWith = InterfaceUtils.getScriptArray(txt_onUseWith.text)
        c.onUse = InterfaceUtils.getScriptArray(txt_onUse.text)
        c.onVarpTransmit = InterfaceUtils.getScriptArray(txt_onVarpTransmit.text)
        c.onInvTransmit = InterfaceUtils.getScriptArray(txt_onInvTransmit.text)
        c.onStatTransmit = InterfaceUtils.getScriptArray(txt_onStatTransmit.text)
        c.onTimer = InterfaceUtils.getScriptArray(txt_onTimer.text)
        c.onOptionClick = InterfaceUtils.getScriptArray(txt_onOptionClick.text)
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
        Cache.store!!.indexes[3].putFile(inter, comp, data)

        drawTree(inter)
        logger.info("Saved IF3 [$inter:$comp]")
    }


    private fun parseIntSafe(value: String, defaultValue: Int): Int {
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            defaultValue
        }
    }

    private fun populateList(): DefaultListModel<String> {
        logger.info("Populating interface list")
        val listModel = DefaultListModel<String>()

        for (i in 0 until ComponentDefinition.interfaceDefinitionsSize) {
            try {
                if (ComponentDefinition.getInterface(i, false) != null) {
                    listModel.addElement("Interface: $i")
                }
            } catch (_: Exception) {

            }
        }

        logger.info("Done Populating list")
        return listModel
    }

    private fun createInterfaceTree(interfaceId: Int): DefaultTreeModel {
        val inter = DefaultMutableTreeNode("Interface $interfaceId")
        //new stuff
        for (i in 0 until ComponentDefinition.getInterfaceDefinitionsComponentsSize(interfaceId)) {
            val c = ComponentDefinition.getInterfaceComponent(interfaceId, i)
            if (c == null) {
                println("is null$i")
                continue
            }
            //System.out.println("here");
            //check for the base containers
            if (c.parentId == -1 && ComponentDefinition.hasChilds(interfaceId, c.componentHash)) {
                val child = DefaultMutableTreeNode("Component " + c.componentId)
                val childs = ComponentDefinition.getChildsByParent(interfaceId, c.componentHash)
                //loop throu the childs
                for (c2 in childs) {
                    //check if the child is a container , if so loop throu his childs etc..
                    if (ComponentDefinition.hasChilds(interfaceId, c2.componentHash)) {
                        val childs2 = ComponentDefinition.getChildsByParent(interfaceId, c2.componentHash)
                        val container2 = DefaultMutableTreeNode("Component " + c2.componentId)
                        for (c3 in childs2) {
                            if (ComponentDefinition.hasChilds(interfaceId, c3.componentHash)) {
                                val childs3 = ComponentDefinition.getChildsByParent(interfaceId, c3.componentHash)
                                val container3 = DefaultMutableTreeNode("Component " + c3.componentId)
                                for (c4 in childs3) {
                                    if (ComponentDefinition.hasChilds(interfaceId, c4.componentHash)) {
                                        val child4 =
                                            ComponentDefinition.getChildsByParent(interfaceId, c4.componentHash)
                                        val container4 = DefaultMutableTreeNode("Component " + c4.componentId)
                                        for (c5 in child4) {
                                            val childs4 = DefaultMutableTreeNode("Component " + c5.componentId)
                                            container4.add(childs4)
                                        }

                                        container3.add(container4)
                                    } else {
                                        val child2 = DefaultMutableTreeNode("Component " + c4.componentId)
                                        container3.add(child2)
                                    }
                                }
                                container2.add(container3)
                            } else {
                                val child2 = DefaultMutableTreeNode("Component " + c3.componentId)
                                container2.add(child2)
                            }
                        }
                        child.add(container2)
                    } else {
                        val child2 = DefaultMutableTreeNode("Component " + c2.componentId)
                        child.add(child2)
                    }
                }

                inter.add(child)
            } else if (c.parentId == -1 && !ComponentDefinition.hasChilds(interfaceId, c.componentHash)) {
                val child2 = DefaultMutableTreeNode("Component " + c.componentId)
                inter.add(child2)
            }
        }

        return DefaultTreeModel(inter)
    }

    private fun addBaseComponent(interfaceId: Int, useScripts: Boolean) {
        val defaultButton = ComponentDefinition.getInterfaceComponent(6, 19)
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
        if (copiedComp!!.type == ComponentConstants.CONTAINER) {
            val containerPlace = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)
            copiedComp!!.parentId = -1

            Cache.store!!.indexes[3].putFile(
                currentInterface, containerPlace, copiedComp!!.encode(
                    copiedComp!!.hasScripts
                )
            )
            val childs = ComponentDefinition.getChildsByParent(copiedComp!!.interfaceId, copiedComp!!.componentHash)
            for (c in childs) {
                if (c.type == ComponentConstants.CONTAINER) { //TODO packing containers in containers
                } else Cache.store!!.indexes[3].putFile(
                    currentInterface,
                    ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                    c.encode(
                        copiedComp!!.hasScripts
                    )
                )
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
            Cache.store!!.indexes[3].putFile(
                currentInterface,
                ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                copiedComp!!.encode(copiedComp!!.hasScripts)
            )
        }
        ComponentDefinition.getInterface(currentInterface, true)
        drawTree(currentInterface)
    }

    @Throws(FileNotFoundException::class, IOException::class)
    fun exportInterface(interfaceId: Int) {
        val file = File("data/export/$interfaceId.dat")
        val data = Cache.store!!.indexes[3].getArchive(interfaceId).data
        FileOutputStream(file).use { fos ->
            fos.write(data)
            fos.close()
            JOptionPane.showMessageDialog(
                interfaceViewportScrollPane,
                "$interfaceId dumped to data/export/$interfaceId.dat"
            )
        }
    }


    private fun getOrderedComps(interfaceId: Int): ArrayList<ComponentDefinition?>? {
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
            ComponentPosition.setValues(allComps[i])
            var found = false
            for (c in comps) {
                if (c?.componentId == allComps[i]?.componentId) found = true
            }
            if (!found) comps.add(allComps[i])
        }

        return comps
    }

    fun drawTree(id: Int) {
        logger.info("Drawing componenttree ")
        val tree = JTree(createInterfaceTree(id))
        tree.addTreeSelectionListener { e: TreeSelectionEvent? ->
            val selectedNode = tree.lastSelectedPathComponent as DefaultMutableTreeNode
            try {
                if (selectedNode.userObject != null) {
                    val id1 = selectedNode.userObject.toString().replace("Component ".toRegex(), "").toInt()
                    selectedComp = id1
                    setValues(currentInterface, id1)
                    repaint()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                logger.log(Level.SEVERE, "Error selecting component, error->$ex")

                /* some roots aren't a root , better catch them instead of spamming console*/
            }
        }
        componentScrollpane!!.setViewportView(tree)
    }

    fun addDefaultComponent(id: Int) {
        when (comboBox!!.selectedIndex) {
            0 -> {
                val defaultcloseButton = ComponentDefinition.getInterfaceComponent(6, 36)
                if (defaultcloseButton != null) {
                    defaultcloseButton.parentId = -1
                }
                Cache.store!!.indexes[3].putFile(
                    currentInterface,
                    ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                    defaultcloseButton!!.encode(
                        copiedComp!!.hasScripts
                    )
                )
                ComponentDefinition.getInterface(currentInterface, true) //since we need to reload the array
                drawTree(currentInterface)
            }

            1 -> {
                val container = ComponentDefinition.getInterfaceComponent(506, 1)
                val text = ComponentDefinition.getInterfaceComponent(506, 2)
                if (container != null) {
                    Cache.store!!.indexes[3].putFile(
                        currentInterface,
                        ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                        container.encode(
                            copiedComp!!.hasScripts
                        )
                    )
                }
                if (text != null) {
                    Cache.store!!.indexes[3].putFile(
                        currentInterface,
                        ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                        text.encode(
                            copiedComp!!.hasScripts
                        )
                    )
                }
                ComponentDefinition.getInterface(currentInterface, true) //since we need to reload the array
                val n = ComponentDefinition.getInterfaceComponent(
                    currentInterface,
                    ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) - 2
                )
                val xd = ComponentDefinition.getInterfaceComponent(
                    currentInterface,
                    ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) - 1
                )
                if (n != null) {
                    xd!!.parentId = n.componentHash
                }
                Cache.store!!.indexes[3].putFile(
                    currentInterface,
                    ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface) - 1,
                    xd!!.encode(
                        copiedComp!!.hasScripts
                    )
                )
                ComponentDefinition.getInterface(currentInterface, true) //since we need to reload the array
                drawTree(currentInterface)
            }

            2 -> {
                val basic = ComponentDefinition.getInterfaceComponent(6, 0)
                val place = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)
                if (basic != null) {
                    Cache.store!!.indexes[3].putFile(
                        currentInterface, place, basic.encode(
                            copiedComp!!.hasScripts
                        )
                    )
                }
                for (comp in ComponentDefinition.getChildsByParent(
                    6,
                    ComponentDefinition.getInterfaceComponent(6, 0)!!.componentHash
                )) {
                    if (comp.text!!.lowercase(Locale.getDefault()).contains("brimhaven")) comp.text =
                        "My starter interface"
                    comp.parentId++
                    Cache.store!!.indexes[3].putFile(
                        currentInterface,
                        ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface),
                        comp.encode(
                            copiedComp!!.hasScripts
                        )
                    )
                }
                ComponentDefinition.getInterface(currentInterface, true) //since we need to reload the array
                drawTree(currentInterface)
            }

            3 -> {
                val hover = ComponentDefinition.getInterfaceComponent(6, 36)
                if (hover != null) {
                    hover.parentId = -1
                }
                hover!!.onMouseRepeat = null
                hover.onOptionClick = null
                hover.spriteId = 0
                hover.onMouseHoverScript?.set(2, 1)
                hover.onMouseLeaveScript?.set(2, 0)
                Cache.store!!.indexes[3].putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), hover.encode(copiedComp!!.hasScripts))
                ComponentDefinition.getInterface(currentInterface, true) //since we need to reload the array
                drawTree(currentInterface)
            }

            4 -> {
                val popupButton = ComponentDefinition.getInterfaceComponent(762, 33)
                val cont = ComponentDefinition.getInterfaceComponent(762, 119)
                if (cont != null) {
                    cont.parentId = -1
                }
                if (popupButton != null) {
                    popupButton.parentId = -1
                }
                val place2 = ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface)
                Cache.store!!.indexes[3].putFile(currentInterface, place2, cont!!.encode(copiedComp!!.hasScripts))
                popupButton!!.onMouseRepeat?.set(3, "Click here to change your preset settings.")
                popupButton.onMouseLeaveScript?.set(2, ComponentDefinition.getInterfaceComponent(currentInterface, place2)!!.componentHash)
                popupButton.onMouseRepeat?.set(2, ComponentDefinition.getInterfaceComponent(currentInterface, place2)!!.componentHash)
                Cache.store!!.indexes[3].putFile(currentInterface, ComponentDefinition.getInterfaceDefinitionsComponentsSize(currentInterface), popupButton.encode(copiedComp!!.hasScripts))
                ComponentDefinition.getInterface(currentInterface, true) //since we need to reload the array
                drawTree(currentInterface)
            }
        }
    }

    fun drawInterfaceComponents(
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        showRealFonts: Boolean
    ) {
        logger.info("Drawing preview interface")
        result = BufferedImage(viewportPanel!!.width, viewportPanel!!.height, BufferedImage.TYPE_INT_RGB)
        val g = result!!.graphics

        drawInterfaceToGraphics(g, interfaceId, showContainers, showHidden, showModels, showRealFonts)
    }

    private fun drawInterfaceToGraphics(
        g: Graphics,
        interfaceId: Int,
        showContainers: Boolean,
        showHidden: Boolean,
        showModels: Boolean,
        showRealFonts: Boolean
    ) {
        g.color = Constants.BG_FILL_COLOR
        g.fillRect(0, 0, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT)
        // drawBackground(g)
        val orderedComponents: List<ComponentDefinition?>? = this.getOrderedComps(interfaceId)
        if (orderedComponents == null) {
            println("is null")
            return
        }
        for (component in orderedComponents) {
            ComponentPosition.setValues(component)
            if (component == null || (InterfaceUtils.isHidden(component) && !showHidden)) {
                continue
            }
            /* vars */
            val width = component.width
            val height = component.height
            val x = ComponentDefinition.getX(component, interfaceId)
            val y = ComponentDefinition.getY(component, interfaceId)
            val parent =
                InterfaceUtils.getParent(component.parentId) //ComponentDefinition.getParent(component, interfaceId);
            /* setting correct values of the parent ofcourse*/
            if (parent != null) {
            }
            if (component.type == ComponentConstants.SPRITE && component.spriteId > -1) {
                var sprite: BufferedImage? = null
                val unscaled = SpriteLoader.getSprite(component.spriteId) ?: return

                val scaled = unscaled.getScaledInstance(component.width, component.height, Image.SCALE_SMOOTH)
                sprite = ImageUtils.imageToBufferedImage(scaled)

                /* horizontal flip*/
                if (component.hFlip) sprite = ImageUtils.horizontalFlip(sprite)
                /* vertical flip*/
                if (component.vFlip) sprite = ImageUtils.verticalFlip(sprite)
                g.drawImage(sprite, x, y, null)
            }

            if (component.type == ComponentConstants.FIGURE) {
                if (component.color == 0) {
                    g.color = Color.black
                } else {
                    val color = Color(component.color)
                    val red = color.red
                    val green = color.green
                    val blue = color.blue
                    g.color = Color(red, green, blue)
                }
                g.drawRect(
                    ComponentDefinition.getX(component, currentInterface),
                    ComponentDefinition.getY(component, currentInterface),
                    component.width,
                    component.height
                )
                if (component.filled) g.fillRect(
                    ComponentDefinition.getX(component, currentInterface),
                    ComponentDefinition.getY(component, currentInterface),
                    component.width,
                    component.height
                )
            }
            if (component.type == ComponentConstants.MODEL && showModels) {
                g.color = Color.BLUE
                g.drawRect(
                    ComponentDefinition.getX(component, interfaceId),
                    ComponentDefinition.getY(component, interfaceId),
                    component.width,
                    component.height
                )
            }
            if (component.type == ComponentConstants.CONTAINER) {
                var sprite: BufferedImage? = null
                if (ContainerHelper.isScrollBar(component)) {
                    try {
                        sprite =
                            ImageUtils.resize(ImageIO.read(File("data/scriptsprites/scrollbar.jpg")), width, height)
                    } catch (e: IOException) {
                        println("scrollbar.jpg not found")
                    }
                    g.drawImage(
                        sprite,
                        ComponentDefinition.getX(component, interfaceId),
                        ComponentDefinition.getY(component, interfaceId),
                        null
                    )
                } else if (ContainerHelper.isButton(component)) {
                    try {
                        sprite = ImageUtils.resize(ImageIO.read(File("data/scriptsprites/button.png")), width, height)
                    } catch (e: IOException) {
                        println("button.png not found")
                    }
                    g.drawImage(
                        sprite,
                        ComponentDefinition.getX(component, interfaceId),
                        ComponentDefinition.getY(component, interfaceId),
                        null
                    )
                } else if (showContainers) {
                    g.color = Color.RED
                    if (component.parentId > 0) {
                        g.drawRect(
                            ComponentDefinition.getX(component, interfaceId),
                            ComponentDefinition.getY(component, interfaceId),
                            component.width,
                            component.height
                        )
                    } else {
                        g.color = Color.green
                        g.drawRect(component.positionX, component.positionY, component.width, component.height)
                    }
                }
            }
            if (component.type == ComponentConstants.TEXT) {
                val fm = g.fontMetrics
                val rect = fm.getStringBounds(component.text, g)
                val color = Color(component.color)
                val red = color.red
                val green = color.green
                val blue = color.blue
                g.color = Color(red, green, blue)
                g.font = Font("Helvetica", 0, 11)
                if (component.parentId == -1) {
                    g.drawString(
                        component.text,
                        (component.positionX + component.width / 2 - rect.width / 2).toInt(),
                        (component.positionY + component.height / 2 + rect.height / 2).toInt()
                    )
                } else {
                    ComponentPosition.setValues(parent)

                    //text in buttons, to center it lol capypasta
                    if (component.baseWidth == 0 && component.baseHeight == 0) {
                        val metrics = g.getFontMetrics(Font("Helvetica", 0, 11))
                        // Determine the X coordinate for the text
                        var x2 = 0
                        if (parent != null) {
                            x2 = parent.positionX + (parent.width - metrics.stringWidth(component.text)) / 2
                        }
                        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
                        val y2 = parent!!.positionY + ((parent.height - metrics.height) / 2) + metrics.ascent
                        // Set the font
                        g.font = Font("Helvetica", 0, 11)
                        // Draw the String
                        g.drawString(component.text, x2, y2)
                    } else {
                        /*position*/
                        var positionX = ComponentDefinition.getX(component, interfaceId)
                        val positionY = ComponentDefinition.getY(component, interfaceId)
                        /*  not drAWING OUTSIDE THE CONTAINER*/
                        if (parent != null && positionX > parent.width + parent.positionX) positionX =
                            parent.width - component.width
                        g.drawString(
                            component.text,
                            (positionX + component.width / 2 - rect.width / 2).toInt(),
                            (positionY + component.height / 2 + rect.height / 2).toInt()
                        )
                    }
                }
                if (component.text?.contains("</u>") == true) {
                }
            }
        }
    }

    companion object {
        private val logger: Logger = LogFactory.createLogger(InterfaceUI::class.java.name)

        @JvmStatic
        fun main(args: Array<String>) {
            EventQueue.invokeLater {
                try {
                    try {
                        UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceRavenGraphiteLookAndFeel")
                        setDefaultLookAndFeelDecorated(true)
                        JDialog.setDefaultLookAndFeelDecorated(true)
                    } catch (e: Exception) {
                        JOptionPane.showMessageDialog(
                            null,
                            "" + e
                        )
                        logger.log(Level.SEVERE, e.message)
                    }

                    if (args.size < 1) {
                        try {
                            loadValues()
                        } catch (e: Exception) {
                            JOptionPane.showMessageDialog(
                                null,
                                "Properties can not be found, make sure you've a config.properties file."
                            )
                            logger.log(
                                Level.SEVERE,
                                "Properties can not be found, make sure you've a config.properties file.\""
                            )
                        }
                    } else {
                        val path = args[0]
                        logger.info("Using cache path: $path")
                        setCachePath(path)
                    }
                    logger.info("Application started...")
                    Cache.init()
                    val frame = InterfaceUI()
                    frame.isVisible = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /*private fun drawBackground(g: Graphics?) {
            val clip = if ((g != null)) g.clipBounds else null
            val w = if ((clip != null)) clip.width else Constants.VIEWPORT_WIDTH
            val h = if ((clip != null)) clip.height else Constants.VIEWPORT_HEIGHT

            if (g != null) {
                g.color = Color.BLACK
            }
            g!!.fillRect(0, 0, w, h)
            g.color = Color(80, 80, 80)
            val gridSize = 16
            run {
                var x = 0
                while (x <= w) {
                    g.drawLine(x, 0, x, h)
                    x += gridSize
                }
            }
            run {
                var y = 0
                while (y <= h) {
                    g.drawLine(0, y, w, y)
                    y += gridSize
                }
            }
         */
    }
}

