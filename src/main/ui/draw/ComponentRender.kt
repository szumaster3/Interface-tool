package ui.draw

import fs.com.alex.utils.Utils
import rs.cache.ComponentDefinition
import rs.cache.ComponentType
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellRenderer

class ComponentRender(private val onHideToggle: (ComponentDefinition) -> Unit) :
    ListCellRenderer<ComponentDefinition>, TreeCellRenderer {

    private val uiFont = Font("Dialog", Font.PLAIN, 12)
    private val cellHeight = 19

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

    private val closeTree = ImageIcon(javaClass.getResource("/icons/tree_close.png"))
    private val openTree  = ImageIcon(javaClass.getResource("/icons/tree_open.png"))
    private val hideIcon  = ImageIcon(javaClass.getResource("/icons/hide_true.png"))
    private val showIcon  = ImageIcon(javaClass.getResource("/icons/hide.png"))
    private val defaultIcon = ImageIcon(javaClass.getResource("/icons/default.png"))

    private val componentIcons = mapOf(
        ComponentType.LAYER      to ImageIcon(javaClass.getResource("/icons/layer.png")),
        ComponentType.TEXT       to ImageIcon(javaClass.getResource("/icons/text.png")),
        ComponentType.SPRITE     to ImageIcon(javaClass.getResource("/icons/sprite.png")),
        ComponentType.FIGURE     to ImageIcon(javaClass.getResource("/icons/figure.png")),
        ComponentType.MODEL      to ImageIcon(javaClass.getResource("/icons/model.png")),
        ComponentType.LINE       to ImageIcon(javaClass.getResource("/icons/line.png")),
        ComponentType.MODEL_LIST to ImageIcon(javaClass.getResource("/icons/model_list.png")),
        ComponentType.INVENTORY  to ImageIcon(javaClass.getResource("/icons/inventory.png"))
    )

    private var currentComp: ComponentDefinition? = null

    init {
        hideLabel.font = uiFont
        typeLabel.font = uiFont
        textLabel.font = uiFont

        hideLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        hideLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                currentComp?.let {
                    it.hidden = !it.hidden
                    onHideToggle(it)
                }
            }
        })
    }

    private fun updatePanel(comp: ComponentDefinition) {
        currentComp = comp
        hideLabel.icon = if (comp.hidden) hideIcon else showIcon
        hideLabel.text = ""
        typeLabel.icon = componentIcons[comp.type] ?: defaultIcon
        typeLabel.text = ""
        val color = Color.GREEN
        textLabel.foreground = color
        typeLabel.foreground = color
        textLabel.text = "${comp.componentId}: ${Utils.getComponentType(comp.type)}"
    }

    override fun getListCellRendererComponent(
        list: JList<out ComponentDefinition>,
        value: ComponentDefinition,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        updatePanel(value)
        panel.background = if (isSelected) Color.DARK_GRAY else Color.BLACK
        textLabel.foreground = if (isSelected) Color.WHITE else Color.GREEN
        panel.preferredSize = Dimension(panel.preferredSize.width, cellHeight)
        return panel
    }

    private val treeRenderer = DefaultTreeCellRenderer().apply {
        backgroundNonSelectionColor = Color.BLACK
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
            if (comp != null) {
                updatePanel(comp)
                renderer.icon = typeLabel.icon
                renderer.text = "${comp.componentId}: ${Utils.getComponentType(comp.type)}"
                renderer.foreground = if (selected) Color.WHITE else Color.GREEN
                renderer.background = if (selected) Color.DARK_GRAY else Color.BLACK
                renderer.font = uiFont
                renderer.isOpaque = true
            } else if (value === tree.model.root) {
                renderer.text = value.userObject.toString()
                renderer.foreground = Color.GREEN
                renderer.background = if (selected) Color.DARK_GRAY else Color.BLACK
                renderer.font = uiFont.deriveFont(Font.BOLD)
                renderer.icon = openTree
                renderer.isOpaque = true
            }
        }

        return renderer
    }
}
