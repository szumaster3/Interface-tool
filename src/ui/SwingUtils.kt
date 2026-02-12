package ui

import java.awt.*
import javax.swing.*
import javax.swing.border.BevelBorder

object SwingUtils {
    fun labeledField(
        label: String,
        field: JComponent,
    ): JPanel {
        val panel = JPanel(BorderLayout(5, 0))
        panel.isOpaque = false
        val lblWrapper = JPanel(BorderLayout())
        lblWrapper.border =
            object : javax.swing.border.Border {
                override fun paintBorder(
                    c: Component?,
                    g: Graphics,
                    x: Int,
                    y: Int,
                    width: Int,
                    height: Int,
                ) {
                    g.color = Color(110, 110, 110)
                    g.drawLine(x, y + height - 1, x + width - 1, y + height - 1)
                    g.drawLine(x + width - 1, y, x + width - 1, y + height - 1)
                }

                override fun getBorderInsets(c: Component?) = Insets(0, 0, 1, 1)

                override fun isBorderOpaque() = true
            }

        lblWrapper.isOpaque = false
        val lbl = JLabel(label, SwingConstants.CENTER)
        lblWrapper.add(lbl, BorderLayout.CENTER)
        lblWrapper.preferredSize = Dimension(80, 25)
        panel.add(lblWrapper, BorderLayout.WEST)

        field.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        field.background = Color.WHITE
        field.preferredSize = Dimension(0, 25)
        field.maximumSize = Dimension(Int.MAX_VALUE, 25)
        field.minimumSize = Dimension(0, 25)
        panel.add(field, BorderLayout.CENTER)

        return panel
    }

    fun labeledButton(
        label: String,
        buttonText: String,
    ): JPanel {
        val panel = JPanel(BorderLayout(5, 0))
        panel.isOpaque = false

        val lblWrapper = JPanel(BorderLayout())
        lblWrapper.border =
            object : javax.swing.border.Border {
                override fun paintBorder(
                    c: Component?,
                    g: Graphics,
                    x: Int,
                    y: Int,
                    width: Int,
                    height: Int,
                ) {
                    g.color = Color(110, 110, 110)
                    g.drawLine(x, y + height - 1, x + width - 1, y + height - 1)
                    g.drawLine(x + width - 1, y, x + width - 1, y + height - 1)
                }

                override fun getBorderInsets(c: Component?) = Insets(0, 0, 1, 1)

                override fun isBorderOpaque() = true
            }
        lblWrapper.isOpaque = false
        val lbl = JLabel(label, SwingConstants.CENTER)
        lblWrapper.add(lbl, BorderLayout.CENTER)
        lblWrapper.preferredSize = Dimension(80, 25)
        panel.add(lblWrapper, BorderLayout.WEST)

        val button = JButton(buttonText)
        button.isOpaque = true
        button.background = Color.WHITE
        button.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        button.horizontalAlignment = SwingConstants.CENTER
        button.preferredSize = Dimension(0, 25)
        button.maximumSize = Dimension(Int.MAX_VALUE, 25)
        button.minimumSize = Dimension(0, 25)
        panel.add(button, BorderLayout.CENTER)

        return panel
    }

    fun labeledTwoButtons(
        buttonText1: String,
        buttonText2: String,
    ): JPanel {
        val panel = JPanel(BorderLayout(5, 0))
        panel.isOpaque = false

        val buttonPanel = JPanel(GridLayout(1, 2, 2, 0))
        buttonPanel.isOpaque = false

        fun createStyledButton(
            text: String,
            borderColor: Color,
        ): JButton {
            val btn = JButton(text)
            btn.isOpaque = false
            btn.background = Color.WHITE
            btn.horizontalAlignment = SwingConstants.CENTER
            btn.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
            btn.preferredSize = Dimension(0, 25)
            btn.maximumSize = Dimension(Int.MAX_VALUE, 25)
            btn.minimumSize = Dimension(0, 25)
            return btn
        }

        val btn1 = createStyledButton(buttonText1, Color(150, 0, 0))
        val btn2 = createStyledButton(buttonText2, Color(0, 120, 0))

        buttonPanel.add(btn1)
        buttonPanel.add(btn2)

        panel.add(buttonPanel, BorderLayout.CENTER)
        return panel
    }
}
