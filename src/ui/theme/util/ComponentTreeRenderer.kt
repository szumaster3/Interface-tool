package ui.theme.util

import ui.ComponentNode
import ui.theme.assets.IconLibrary
import java.awt.Color
import java.awt.Component
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

class ComponentTreeRenderer(
    private val iconMap: Map<String, Icon>,
) : DefaultTreeCellRenderer() {
    init {
        backgroundNonSelectionColor = Color.BLACK
        backgroundSelectionColor = Color.DARK_GRAY
        textNonSelectionColor = Color.GREEN
        textSelectionColor = Color.GREEN.brighter()
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)

        val node = value as? DefaultMutableTreeNode
        val data = node?.userObject as? ComponentNode
        if (node?.isRoot == true) {
            text = "Interface"
            icon = ImageIcon(IconLibrary.INTERFACE_ICON)
            return this
        }
        if (data != null) {
            text = "${data.componentId}: (${data.type})"

            icon =
                iconMap[data.type]
                    ?: when {
                        leaf -> defaultLeafIcon
                        expanded -> defaultOpenIcon
                        else -> defaultClosedIcon
                    }
        } else {
            icon = defaultOpenIcon
        }

        return this
    }
}
