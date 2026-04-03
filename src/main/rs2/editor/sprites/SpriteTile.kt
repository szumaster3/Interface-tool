package rs2.editor.sprites

import java.awt.*
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.LineBorder

class SpriteTile(val spriteId: Int, image: Image) : JPanel(BorderLayout()) {
    val spriteWidth: Int = image.getWidth(null)
    val spriteHeight: Int = image.getHeight(null)

    private val imgLabel = JLabel(ImageIcon(image)).apply {
        horizontalAlignment = JLabel.CENTER
    }

    private val idLabel = JLabel(spriteId.toString(), JLabel.CENTER).apply {
        foreground = Color.LIGHT_GRAY
        font = font.deriveFont(Font.BOLD, 12f)
    }

    private val sizeLabel = JLabel("W:$spriteWidth H:$spriteHeight", JLabel.CENTER).apply {
        foreground = Color.GRAY
        font = font.deriveFont(10f)
    }

    private var frameLabel: JLabel? = null

    var selected = false
        set(value) {
            field = value
            if (value) {
                border = LineBorder(Color.GREEN, 2)
                background = Color(70, 70, 70)
            } else {
                border = LineBorder(Color.DARK_GRAY, 1)
                background = Color.DARK_GRAY
            }
            repaint()
        }

    init {
        preferredSize = Dimension(80, 80)
        minimumSize = preferredSize
        maximumSize = preferredSize
        border = BorderFactory.createLoweredBevelBorder()
        isOpaque = true
        background = Color.DARK_GRAY

        val infoPanel = JPanel(GridLayout(2, 1)).apply {
            isOpaque = false
            add(idLabel)
            add(sizeLabel)
        }
        addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                if (!selected) border = LineBorder(Color.GRAY, 2)
            }

            override fun mouseExited(e: java.awt.event.MouseEvent?) {
                if (!selected) border = LineBorder(Color.DARK_GRAY, 1)
            }
        })
        add(imgLabel, BorderLayout.CENTER)
        add(infoPanel, BorderLayout.SOUTH)
    }

    fun addFrameIndicator(frameCount: Int) {
        if (frameLabel == null) {
            frameLabel = JLabel("Frames: $frameCount", JLabel.CENTER).apply {
                foreground = Color.YELLOW
                font = font.deriveFont(Font.BOLD, 10f)
            }

            val overlay = JPanel(BorderLayout()).apply {
                isOpaque = false
                add(frameLabel!!, BorderLayout.NORTH)
            }

            add(overlay, BorderLayout.NORTH)
            revalidate()
            repaint()
        } else {
            frameLabel?.text = "Frames: $frameCount"
        }
    }
}
