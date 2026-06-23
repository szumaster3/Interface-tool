package com.editor.util.sprites

import java.awt.*
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.LineBorder

class FrameTile(val frameId: Int, image: Image) : JPanel(BorderLayout()) {

    private val frameWidth: Int = image.getWidth(null)
    private val frameHeight: Int = image.getHeight(null)

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
            if (value) {
                border = LineBorder(Color.GREEN, 2)
                background = Color(75, 75, 75)
            } else {
                border = LineBorder(Color.DARK_GRAY, 1)
                background = Color(55, 55, 55)
            }
            repaint()
        }

    init {
        preferredSize = Dimension(80, 80)
        minimumSize = preferredSize
        maximumSize = preferredSize
        border = LineBorder(Color.DARK_GRAY, 1)
        isOpaque = true
        background = Color(45, 45, 45)

        val infoPanel = JPanel(GridLayout(2, 1)).apply {
            isOpaque = false
            add(idLabel)
            add(sizeLabel)
        }
        addMouseListener(object : java.awt.event.MouseAdapter() {

            override fun mouseEntered(e: java.awt.event.MouseEvent?) {
                if (!selected) {
                    border = LineBorder(Color.GRAY, 2)
                }
            }

            override fun mouseExited(e: java.awt.event.MouseEvent?) {
                if (!selected) {
                    border = LineBorder(Color.DARK_GRAY, 1)
                }
            }

        })
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
