package rs2.sprites

import rs2.cache.loaders.ComponentDefinition
import ui.theme.SpinnerTextField
import java.awt.*
import java.awt.datatransfer.StringSelection
import javax.swing.*
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
            }
        colorPreview.preferredSize = Dimension(440, 80)
        colorPreview.border = LineBorder(Color.DARK_GRAY, 1)
        add(colorPreview, BorderLayout.NORTH)

        val infoPanel = JPanel(GridLayout(2, 1, 4, 4))
        val smallFont = Font("JetBrains Mono", Font.PLAIN, 10)
        rs2Label.font = smallFont
        rs2Label.horizontalAlignment = SwingConstants.CENTER
        rgbLabel.font = smallFont
        rgbLabel.horizontalAlignment = SwingConstants.CENTER
        infoPanel.add(rs2Label)
        infoPanel.add(rgbLabel)
        add(infoPanel, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 6, 4))
        val changeColorBtn = createButton("Change color") { openColorChooser() }
        val copyRS2Btn = createButton("Copy RS2HSB") { copyToClipboard(rs2hsb.toString()) }
        val copyRGBBtn =
            createButton("Copy RGB") {
                val rgb = ImageUtils.rs2hsbToRGB(rs2hsb)
                copyToClipboard(rgb.toString())
            }

        val moveToEditorBtn =
            createButton("Pick") {
                val rgb = (selectedColor.red shl 16) or (selectedColor.green shl 8) or (selectedColor.blue)
                comp.color = rgb
                colorField.getTextField().text = rgb.toString()
                colorField.getTextField().requestFocus()
                onColorApplied?.invoke()
            }

        buttonPanel.add(changeColorBtn)
        buttonPanel.add(copyRS2Btn)
        buttonPanel.add(copyRGBBtn)
        buttonPanel.add(moveToEditorBtn)
        add(buttonPanel, BorderLayout.SOUTH)

        if (comp.color != 0) selectedColor = Color(comp.color)
        updateDisplay()

        pack()
        setLocationRelativeTo(null)
    }

    private fun createButton(
        text: String,
        action: () -> Unit,
    ): JButton =
        JButton(text).apply {
            font = Font("JetBrains Mono", Font.PLAIN, 10)
            preferredSize = Dimension(110, 25)
            isFocusPainted = false
            addActionListener { action() }
        }

    private fun openColorChooser() {
        val chooser = JColorChooser(selectedColor)
        val dialog = JColorChooser.createDialog(this, "Select a Color", true, chooser, null, null)

        chooser.selectionModel.addChangeListener(
            ChangeListener {
                selectedColor = chooser.color
                updateDisplay()
                val rgb = (selectedColor.red shl 16) or (selectedColor.green shl 8) or (selectedColor.blue)
                comp.color = rgb
                colorField.getTextField().text = rgb.toString()
            },
        )

        dialog.isVisible = true
    }

    private fun updateDisplay() {
        rs2hsb = ImageUtils.rgbToRS2HSB(selectedColor.red, selectedColor.green, selectedColor.blue)
        colorPreview.repaint()
        rs2Label.text = "RS2HSB: $rs2hsb"
        val rgb = ImageUtils.rs2hsbToRGB(rs2hsb)
        rgbLabel.text = "RGB: $rgb"
    }

    private fun copyToClipboard(text: String) {
        val sel = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, null)
    }
}
