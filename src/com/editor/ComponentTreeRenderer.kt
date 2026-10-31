package com.editor

import com.alex.loaders.interfaces.ComponentDefinition
import com.alex.loaders.interfaces.ComponentType
import com.alex.utils.Utils
import com.editor.utils.Consts
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer

class ComponentTreeRenderer :
    ListCellRenderer<ComponentDefinition>, TreeCellRenderer {

    private val uiFont = Font("Dialog", Font.PLAIN, 12)
    private val cellHeight = 18

    private val hideLabel = JLabel()
    private val typeLabel = JLabel()
    private val textLabel = JLabel()

    private val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
        font = uiFont
        border = EmptyBorder(0, 0, 0, 0)
        add(hideLabel.apply { border = EmptyBorder(0, 0, 0, 2) })
        add(typeLabel)
        add(textLabel.apply { border = EmptyBorder(0, 2, 0, 0) })
        isOpaque = true
    }

    private val closeTree   = ImageIcon(javaClass.getResource("/icons/tree_close.png"))
    private val openTree    = ImageIcon(javaClass.getResource("/icons/tree_open.png"))
    private val hideIcon    = ImageIcon(javaClass.getResource("/icons/hide_true.png"))
    private val showIcon    = ImageIcon(javaClass.getResource("/icons/hide.png"))
    private val defaultIcon = ImageIcon(javaClass.getResource("/icons/default.png"))

    private fun icon(name: String) = ImageIcon(javaClass.getResource("/icons/$name.png"))

    private val componentIcons = mapOf(
        ComponentType.LAYER          to icon("layer"),
        ComponentType.TEXT           to icon("text"),
        ComponentType.SPRITE         to icon("sprite"),
        ComponentType.FIGURE         to icon("figure"),
        ComponentType.MODEL          to icon("model"),
        ComponentType.LINE           to icon("line"),
        ComponentType.MODEL_LIST     to icon("model_list"),
        ComponentType.INVENTORY      to icon("inventory"),
        ComponentType.INVENTORY_TEXT to icon("inventory"),
        ComponentType.TOOLTIP        to icon("text")
    )

    private var currentComp: ComponentDefinition? = null

    init {
        hideLabel.font = uiFont
        typeLabel.font = uiFont
        textLabel.font = uiFont
    }

    private fun getColorForComponent(comp: ComponentDefinition?): Color =
        comp?.let { Consts.colorOf(it.type) } ?: Color.GREEN

    private fun updatePanel(comp: ComponentDefinition) {
        currentComp = comp
        val typeName = Utils.getComponentFromInt(comp.type).replaceFirstChar { it.uppercaseChar() }

        hideLabel.icon = if (comp.hidden) hideIcon else showIcon
        hideLabel.text = ""
        typeLabel.icon = componentIcons[comp.type] ?: defaultIcon
        typeLabel.text = ""

        val color = getColorForComponent(comp)
        textLabel.foreground = color
        typeLabel.foreground = color
        val componentName = comp.name?.takeIf { it.isNotEmpty() }?.let { it } ?: ""
        textLabel.text = "${comp.componentId}: ($typeName) - $componentName"
    }

    override fun getListCellRendererComponent(
        list: JList<out ComponentDefinition>,
        value: ComponentDefinition,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        updatePanel(value)
        panel.background = if (isSelected) Color.DARK_GRAY else Color(30,30,30)
        textLabel.foreground = if (isSelected) Color.WHITE else getColorForComponent(value)
        panel.preferredSize = Dimension(panel.preferredSize.width, cellHeight)
        return panel
    }

    private val treeRenderer = DefaultTreeCellRenderer().apply {
        backgroundNonSelectionColor = Color(30,30,30)
        backgroundSelectionColor    = Color.DARK_GRAY
        font                        = uiFont
        closedIcon = closeTree
        openIcon   = openTree
        leafIcon   = defaultIcon
    }

    override fun getTreeCellRendererComponent(
        tree: JTree, value: Any?, selected: Boolean, expanded: Boolean,
        leaf: Boolean, row: Int, hasFocus: Boolean
    ): Component {
        val renderer = treeRenderer.getTreeCellRendererComponent(
            tree, value, selected, expanded, leaf, row, hasFocus
        ) as JLabel

        if (value is DefaultMutableTreeNode) {
            val comp = value.userObject as? ComponentDefinition
            val typeName = comp?.let { it -> Utils.getComponentFromInt(it.type).replaceFirstChar { it.uppercaseChar() } }

            if (comp != null) {
                updatePanel(comp)
                renderer.icon = typeLabel.icon
                val componentName = comp.name?.takeIf { it.isNotEmpty() }?.let { it } ?: ""
                renderer.text = "${comp.componentId}: ($typeName) - $componentName"
                renderer.foreground = if (selected) Color.WHITE else getColorForComponent(comp)
                renderer.background = if (selected) Color.DARK_GRAY else Color(30,30,30)
                renderer.font = uiFont
                renderer.isOpaque = true
            } else if (value === tree.model.root) {
                renderer.text = value.userObject.toString()
                renderer.foreground = Color.GREEN
                renderer.background = if (selected) Color.DARK_GRAY else Color(30,30,30)
                renderer.font = uiFont.deriveFont(Font.BOLD)
                renderer.icon = openTree
                renderer.isOpaque = true
            }
        }

        return renderer
    }
}