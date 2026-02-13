package rs2.sprites

import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.Timer
import javax.swing.border.BevelBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.event.ChangeListener
import rs2.cache.loaders.ComponentDefinition
import ui.theme.SpinnerTextField

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
        buttonPanel.layout =BoxLayout(buttonPanel, BoxLayout.Y_AXIS)
        val changeColorBtn = createButton("Change"){openColorChooser()}
        val copyRS2Btn =
            createButton("Copy RS2HSB"){ event ->
                val value = rs2hsb.toString()
                copyToClipboard(value)
                showCopiedButton(event.source as JButton, value)
            }

        val copyRGBBtn =
            createButton("Copy RGB"){ event ->
                val rgb = ImageUtils.rs2hsbToRGB(rs2hsb)
                val hex = String.format("#%06X", rgb and 0xFFFFFF)
                copyToClipboard(rgb.toString())
                showCopiedButton(event.source as JButton, hex)
            }
        val moveToEditorBtn =
            createButton("Pick"){
                val rgb = (selectedColor.red shl 16) or (selectedColor.green shl 8) or (selectedColor.blue)
                comp.color = rgb
                colorField.getTextField().text = rgb.toString()
                colorField.getTextField().requestFocus()
                onColorApplied?.invoke()
            }

        listOf(changeColorBtn, copyRS2Btn, copyRGBBtn, moveToEditorBtn).forEach { btn ->
            buttonPanel.add(btn)
            buttonPanel.add(Box.createRigidArea(Dimension(0, 6)))
        }

        mainPanel.add(buttonPanel, BorderLayout.WEST)
        val previewPanel = JPanel()
        previewPanel.layout = BoxLayout(previewPanel, BoxLayout.Y_AXIS)

        colorPreview =

            object : JPanel() {
                override fun paintComponent(g: Graphics)
                {
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
        val smallFont = Font("JetBrains Mono", Font.PLAIN, 11)
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

    private fun createButton(text: String, action: (ActionEvent) -> Unit): JButton =
        JButton(text).apply {
            font = Font("JetBrains Mono", Font.PLAIN, 11)
            preferredSize = Dimension(130, 24)
            maximumSize = preferredSize
            minimumSize = preferredSize
            isFocusPainted = false
            addActionListener { e -> action(e) }
        }

    private fun showCopiedButton(button: JButton, copiedValue: String) {
        val originalText = button.text
        button.text = "Copied=[$copiedValue]"

        Timer(59999) { button.text = originalText }.start()
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
