package ui.theme.component

import java.awt.*
import javax.swing.*

class SpinnerTextField(
    columns: Int,
) : JPanel() {
    private val textField =
        JTextField(columns).apply {
            font = Font("JetBrains Mono", Font.TRUETYPE_FONT, 12)
        }
    private val upButton = JButton()
    private val downButton = JButton()

    init {
        layout = BorderLayout()
        isOpaque = false

        textField.horizontalAlignment = JTextField.CENTER
        textField.isOpaque = false
        textField.preferredSize = Dimension(columns * 10, 20)
        textField.maximumSize = Dimension(Int.MAX_VALUE, 20)
        textField.minimumSize = Dimension(columns * 10, 20)
        val arrowsPanel = JPanel(GridLayout(2, 1, 0, 0))
        arrowsPanel.isOpaque = false

        val arrowSize = Dimension(15, 13)

        upButton.ui = EditorArrowButton(SwingConstants.NORTH)
        downButton.ui = EditorArrowButton(SwingConstants.SOUTH)

        upButton.preferredSize = arrowSize
        upButton.maximumSize = arrowSize
        upButton.minimumSize = arrowSize

        downButton.preferredSize = arrowSize
        downButton.maximumSize = arrowSize
        downButton.minimumSize = arrowSize

        upButton.addActionListener { increment() }
        downButton.addActionListener { decrement() }

        arrowsPanel.add(upButton)
        arrowsPanel.add(downButton)

        add(textField, BorderLayout.CENTER)
        add(arrowsPanel, BorderLayout.EAST)
    }

    private fun increment() {
        val value = textField.text.toIntOrNull() ?: 0
        textField.text = (value + 1).toString()
    }

    private fun decrement() {
        val value = textField.text.toIntOrNull() ?: 0
        textField.text = (value - 1).toString()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)

        val w = width
        val h = height

        g2.color = Color.WHITE
        g2.fillRect(0, 0, w, h)

        g2.color = Color(200, 200, 200)
        g2.drawLine(0, 0, w - 1, 0)
        g2.drawLine(0, 0, 0, h - 1)

        g2.color = Color(0, 0, 0)
        g2.drawLine(0, h - 1, w - 1, h - 1)
        g2.drawLine(w - 1, 0, w - 1, h - 1)

        super.paintComponent(g)
    }

    fun getTextField(): JTextField = textField

    fun getUpButton(): JButton = upButton

    fun getDownButton(): JButton = downButton
}
