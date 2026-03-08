package com.alex.loaders.sprites.util

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
            border = if (value) LineBorder(Color.WHITE, 1)
            else LineBorder(Color.DARK_GRAY, 1)
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
