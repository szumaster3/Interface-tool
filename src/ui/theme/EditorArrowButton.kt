package ui.theme

import java.awt.Graphics
import javax.swing.*
import javax.swing.plaf.basic.BasicButtonUI
import ui.theme.IconLibrary.ARROW_KEY_DOWN
import ui.theme.IconLibrary.ARROW_KEY_LEFT
import ui.theme.IconLibrary.ARROW_KEY_RIGHT
import ui.theme.IconLibrary.ARROW_KEY_UP

class EditorArrowButton(private val dir: Int) : BasicButtonUI() {
    private val icon: Icon?

    init {
        var tempIcon: Icon? = null
        tempIcon =
            when (dir) {
                SwingConstants.NORTH -> ImageIcon(ARROW_KEY_UP)
                SwingConstants.SOUTH -> ImageIcon(ARROW_KEY_DOWN)
                SwingConstants.WEST -> ImageIcon(ARROW_KEY_LEFT)
                SwingConstants.EAST -> ImageIcon(ARROW_KEY_RIGHT)
                else -> throw IllegalArgumentException("" + dir)
            }
        this.icon = tempIcon
    }

    override fun paint(g: Graphics, c: JComponent) {
        val b = c as JButton
        if (icon != null) {
            val x = (b.width - icon.iconWidth) / 2
            val y = (b.height - icon.iconHeight) / 2
            icon.paintIcon(b, g, x, y)
        }
    }
}
