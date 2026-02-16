package ui

import cache.ComponentDefinition
import fs.com.alex.Cache
import properties.PropertyValues.cache_path
import properties.PropertyValues.loadValues
import properties.PropertyValues.setCachePath
import java.awt.*
import javax.swing.*
import javax.swing.border.BevelBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeSelectionModel

class IfaceUI : JFrame() {

    private val interfacesListModel = DefaultListModel<Int>()
    private val interfacesList = JList(interfacesListModel)

    private var treeView = true
    private val componentTreeRoot = DefaultMutableTreeNode("Components")
    private val treeModel = DefaultTreeModel(componentTreeRoot)
    private val componentTree = JTree(treeModel)
    private val componentListModel = DefaultListModel<ComponentDefinition>()
    private val componentList = JList(componentListModel)
    private val componentScrollPane = JScrollPane(componentTree)

    private var orderedComponents: List<ComponentDefinition> = emptyList()
    private val renderPanel = RenderPanel()
    private var selectedComponent: ComponentDefinition? = null

    companion object {
        @JvmStatic var instance: IfaceUI? = null

        @JvmStatic
        fun main(args: Array<String>) {
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

    init {
        title = "Interface Editor"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(930, 400)
        layout = BorderLayout()
        setupPanels()
        loadInterfaces()
    }

    private fun setupPanels() {
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

        val centerPanel =
            JPanel(BorderLayout()).apply {
                border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
                preferredSize = Dimension(UIConsts.CENTER_PANEL_WIDTH, UIConsts.DEFAULT_HEIGHT)
                add(JScrollPane(renderPanel), BorderLayout.CENTER)
            }

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

    private fun setupRightPanel(rightPanel: JPanel) {
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

    private fun selectComponent(comp: ComponentDefinition) {
        selectedComponent = comp
        renderPanel.selectedComponent = comp
        renderPanel.repaint()
    }

    private fun clearSelection() {
        selectedComponent = null
        renderPanel.selectedComponent = null
        componentTree.clearSelection()
        componentList.clearSelection()
    }

    private fun updateComponentView() {
        componentScrollPane.setViewportView(if (treeView) componentTree else componentList)
        if (!treeView) rebuildListModel()
    }

    private fun rebuildListModel() {
        componentListModel.clear()
        orderedComponents.forEach { componentListModel.addElement(it) }
    }

    private fun loadInterfaces() {
        val size = ComponentDefinition.getInterfaceDefinitionsSize()
        interfacesListModel.clear()
        for (i in 0 until size) interfacesListModel.addElement(i)
    }

    private fun loadComponents(interfaceId: Int) {
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

    private fun buildOrderedComponents(interfaceId: Int): List<ComponentDefinition> {
        val comps = mutableListOf<ComponentDefinition>()
        val allComps = ComponentDefinition.getInterface(interfaceId, false) ?: return emptyList()

        ComponentDefinition.getInterfaceContainers(interfaceId)?.forEach { container ->
            comps.addIfAbsent(container)
            ComponentDefinition.getChildsByParent(interfaceId, container.componentHash).forEach { comps.addIfAbsent(it) }
        }
        allComps.filterNotNull().forEach { comp -> comps.addIfAbsent(comp) }
        return comps
    }

    private fun <T> MutableList<T>.addIfAbsent(item: T) {
        if (!contains(item)) add(item)
    }
}