package ui.theme

import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.plaf.basic.BasicComboBoxRenderer
import javax.swing.plaf.basic.BasicComboBoxUI

class DepthComboBoxUI(
    private val arrowIcon: Icon,
    private val arrowSize: Int = 15,
) : BasicComboBoxUI() {
    override fun createArrowButton(): JButton =
        JButton(arrowIcon).apply {
            border = BorderFactory.createEmptyBorder(0, 0, 0, 2)
            isContentAreaFilled = false
            background = Color(150, 170, 170, 230)

            preferredSize = Dimension(arrowSize, 30)
            minimumSize = Dimension(arrowSize, 30)
            maximumSize = Dimension(arrowSize, 30)
        }

    override fun paintCurrentValueBackground(
        g: Graphics,
        bounds: Rectangle,
        hasFocus: Boolean,
    ) {
        g.color = comboBox.background
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
    }
}

class DepthComboBox<T>(
    items: Array<T>,
) : JComboBox<T>(items) {
    companion object {
        val arrowIcon: Icon = ImageIcon(IconLibrary.ARROW_KEY_DOWN)
    }

    init {
        isOpaque = false
        border = EmptyBorder(1, 1, 1, 1)
        setUI(DepthComboBoxUI(arrowIcon, 15))
        preferredSize = Dimension(60, 20)

        renderer =
            object : BasicComboBoxRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean,
                ): Component {
                    val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                    label.horizontalAlignment = JLabel.CENTER
                    label.isOpaque = true
                    label.text = value?.toString() ?: ""
                    label.background = if (isSelected) list?.selectionBackground ?: Color.LIGHT_GRAY else list?.background ?: Color.WHITE
                    label.foreground = if (isSelected) list?.selectionForeground ?: Color.BLACK else list?.foreground ?: Color.BLACK
                    return label
                }
            }
    }

    fun setValue(value: T) {
        selectedItem = value
    }
}
