package rs2.sprites

import java.awt.*
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.TitledBorder

class ColorPicker : JFrame() {
    private val colorPreview: JPanel
    private var selectedColor: Color = Color.WHITE
    private val statusLabel = JLabel()
    private var rs2hsb: Int = ImageUtils.rgbToRS2HSB(selectedColor.red, selectedColor.green, selectedColor.blue)

    init {
        title = "Color picker"
        setSize(410, 310)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        layout = BorderLayout(10, 10)
        isResizable = false
        setLocationRelativeTo(null)
        rootPane.border = EmptyBorder(4, 4, 4, 4)

        colorPreview =
            object : JPanel() {
                override fun paintComponent(g: Graphics) {
                    super.paintComponent(g)
                    g.color = selectedColor
                    g.fillRect(0, 0, width, height)

                    val luminance = 0.2126 * selectedColor.red + 0.7152 * selectedColor.green + 0.0722 * selectedColor.blue
                    val textColor = if (luminance < 128) Color.WHITE else Color.BLACK

                    (g as Graphics2D).apply {
                        color = textColor
                        font = font.deriveFont(Font.BOLD, 14f)
                        val text = "RS2: $rs2hsb"
                        val x = (width - fontMetrics.stringWidth(text)) / 2
                        val y = (height + fontMetrics.height) / 2 - 4
                        drawString(text, x, y)
                    }
                }
            }
        colorPreview.preferredSize = Dimension(350, 100)
        colorPreview.border = TitledBorder("Preview")
        add(colorPreview, BorderLayout.NORTH)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 5))
        buttonPanel.border = TitledBorder("Actions")

        val pickColorButton =
            JButton("Select color").apply {
                isFocusPainted = false
                preferredSize = Dimension(120, 30)
                addActionListener {
                    val colorChooser = JColorChooser(selectedColor)
                    val dialog = JColorChooser.createDialog(this@ColorPicker, "Select a Color", true, colorChooser, null, null)
                    colorChooser.selectionModel.addChangeListener {
                        selectedColor = colorChooser.color
                        rs2hsb = ImageUtils.rgbToRS2HSB(selectedColor.red, selectedColor.green, selectedColor.blue)
                        colorPreview.repaint()
                    }
                    dialog.isVisible = true
                }
            }

        val copyRS2Button =
            JButton("Copy RS2HSB").apply {
                isFocusPainted = false
                preferredSize = Dimension(120, 30)
                addActionListener {
                    copyToClipboard(rs2hsb.toString())
                    displayCopiedMessage()
                }
            }

        val copyRGBButton =
            JButton("Copy RGB").apply {
                isFocusPainted = false
                preferredSize = Dimension(120, 30)
                addActionListener {
                    val rgb = ImageUtils.rs2hsbToRGB(rs2hsb)
                    statusLabel.text = "RGB: $rgb"
                    copyToClipboard(rgb.toString())
                }
            }

        buttonPanel.add(pickColorButton)
        buttonPanel.add(copyRS2Button)
        buttonPanel.add(copyRGBButton)
        add(buttonPanel, BorderLayout.CENTER)

        statusLabel.apply {
            preferredSize = Dimension(350, 40)
            horizontalAlignment = SwingConstants.CENTER
            border = TitledBorder("Printed color:")
        }
        add(statusLabel, BorderLayout.SOUTH)
    }

    private fun copyToClipboard(text: String) {
        val selection = StringSelection(text)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    }

    private fun displayCopiedMessage() {
        statusLabel.text = "Copied to clipboard."
        Timer(2000) { statusLabel.text = "" }.start()
    }
}
