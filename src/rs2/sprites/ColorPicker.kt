package rs2.sprites

import rs2.cache.loaders.ComponentDefinition
import ui.theme.component.SpinnerTextField
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.Timer
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.event.ChangeListener

class ColorPicker(
    private val comp: ComponentDefinition,
    private val colorField: SpinnerTextField,
    private val onColorApplied: (() -> Unit)? = null,
) : JFrame() {
    private val colorPreview: JPanel
    private var selectedColor: Color = Color.WHITE
    private val rs2Label = JLabel()
    private val rgbLabel = JLabel()
    private var rs2hsb: Int = 0

    init {
        title = "Color Picker"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isResizable = false
        layout = BorderLayout(8, 8)
        rootPane.border = EmptyBorder(6, 6, 6, 6)

        val mainPanel = JPanel(BorderLayout(8, 8))
        mainPanel.border = BorderFactory.createBevelBorder(BevelBorder.LOWERED)
        add(mainPanel, BorderLayout.CENTER)

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.Y_AXIS)

        val changeColorBtn = createButton("Select new color") { openColorChooser() }
        val moveToEditorBtn =
            createButton("Transfer to editor") {
                val rs2Color = (selectedColor.red shl 16) or (selectedColor.green shl 8) or selectedColor.blue
                comp.color = rs2Color
                colorField.getTextField().text = rs2Color.toString()
                onColorApplied?.invoke()
            }

        val buttons = listOf(changeColorBtn, moveToEditorBtn)
        buttons.forEach { btn ->
            val wrapper = JPanel(BorderLayout())
            wrapper.isOpaque = false
            wrapper.border = EmptyBorder(0, 6, 0, 0)
            wrapper.add(btn, BorderLayout.CENTER)
            buttonPanel.add(wrapper)
            buttonPanel.add(Box.createRigidArea(Dimension(0, 6)))
        }

        listOf(changeColorBtn, moveToEditorBtn).forEach { btn ->
            buttonPanel.add(btn)
            buttonPanel.add(Box.createRigidArea(Dimension(0, 6)))
        }

        mainPanel.add(buttonPanel, BorderLayout.WEST)

        val previewPanel = JPanel()
        previewPanel.layout = BoxLayout(previewPanel, BoxLayout.Y_AXIS)

        colorPreview =
            object : JPanel() {
                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g.color = selectedColor
                    g.fillRect(0, 0, width, height)

                    val lum = 0.2126 * selectedColor.red + 0.7152 * selectedColor.green + 0.0722 * selectedColor.blue
                    g.color = if (lum < 128) Color.WHITE else Color.BLACK
                    val str = "Preview"
                    g.font = Font("JetBrains Mono", Font.BOLD, 12)
                    val fm = g.fontMetrics
                    g.drawString(str, (width - fm.stringWidth(str)) / 2, (height + fm.height) / 2 - 4)
                }

                override fun getPreferredSize(): Dimension = Dimension(150, 150)
            }

        colorPreview.border = LineBorder(Color.DARK_GRAY, 1)
        previewPanel.add(colorPreview)
        previewPanel.add(Box.createRigidArea(Dimension(0, 6)))

        val infoPanel = JPanel(GridLayout(2, 1, 4, 4))
        val smallFont = Font("JetBrains Mono", Font.PLAIN, 12)
        rs2Label.font = smallFont
        rs2Label.horizontalAlignment = SwingConstants.CENTER
        rgbLabel.font = smallFont
        rgbLabel.horizontalAlignment = SwingConstants.CENTER
        infoPanel.add(rs2Label)
        infoPanel.add(rgbLabel)
        previewPanel.add(infoPanel)

        mainPanel.add(previewPanel, BorderLayout.CENTER)

        if (comp.color != 0) selectedColor = Color(comp.color)
        updateDisplay()

        pack()
        setLocationRelativeTo(null)
    }

    private fun createButton(
        text: String,
        action: (ActionEvent) -> Unit,
    ): JButton =
        JButton(text).apply {
            font = Font("JetBrains Mono", Font.PLAIN, 12)
            preferredSize = Dimension(130, 24)
            maximumSize = preferredSize
            minimumSize = preferredSize
            isFocusPainted = false
            addActionListener { e -> action(e) }
        }

    private fun openColorChooser() {
        val chooser = JColorChooser(selectedColor)
        val dialog = JColorChooser.createDialog(this, "Select a Color", true, chooser, null, null)

        chooser.selectionModel.addChangeListener(
            ChangeListener {
                selectedColor = chooser.color
                updateDisplay()
            },
        )

        dialog.isVisible = true
    }

    private fun updateDisplay() {
        rs2hsb = (selectedColor.red shl 16) or (selectedColor.green shl 8) or selectedColor.blue
        colorPreview.repaint()
        rs2Label.text = "RS2: $rs2hsb"
        val hex = String.format("#%06X", rs2hsb and 0xFFFFFF)
        rgbLabel.text = "RGB: $hex"
    }
}
