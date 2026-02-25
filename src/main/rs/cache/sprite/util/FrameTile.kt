package rs.cache.sprite.util

import java.awt.*
import javax.swing.*
import javax.swing.border.LineBorder

class FrameTile(val frameId: Int, image: Image) : JPanel(BorderLayout()) {

    val frameWidth: Int = image.getWidth(null)
    val frameHeight: Int = image.getHeight(null)

    private val imgLabel = JLabel(ImageIcon(image)).apply {
        horizontalAlignment = JLabel.CENTER
    }

    private val idLabel = JLabel(frameId.toString(), JLabel.CENTER).apply {
        foreground = Color.LIGHT_GRAY
        font = font.deriveFont(Font.BOLD, 12f)
    }

    private val sizeLabel = JLabel("$frameWidth × $frameHeight", JLabel.CENTER).apply {
        foreground = Color.GRAY
        font = font.deriveFont(10f)
    }

    private var overlayLabel: JLabel? = null

    var selected = false
        set(value) {
            field = value
            border = if (value) LineBorder(Color.WHITE, 1) else LineBorder(Color.DARK_GRAY, 1)
        }

    init {
        preferredSize = Dimension(80, 80)
        minimumSize = preferredSize
        maximumSize = preferredSize
        border = LineBorder(Color.DARK_GRAY, 1)
        isOpaque = true
        background = Color(55, 55, 55)

        val infoPanel = JPanel(GridLayout(2, 1)).apply {
            isOpaque = false
            add(idLabel)
            add(sizeLabel)
        }

        add(imgLabel, BorderLayout.CENTER)
        add(infoPanel, BorderLayout.SOUTH)
    }

    fun addOverlay(text: String) {
        if (overlayLabel == null) {
            overlayLabel = JLabel(text, JLabel.CENTER).apply {
                foreground = Color.YELLOW
                font = font.deriveFont(Font.BOLD, 10f)
            }
            val overlayPanel = JPanel(BorderLayout()).apply {
                isOpaque = false
                add(overlayLabel!!, BorderLayout.NORTH)
            }
            add(overlayPanel, BorderLayout.NORTH)
            revalidate()
            repaint()
        } else {
            overlayLabel?.text = text
        }
    }
}
