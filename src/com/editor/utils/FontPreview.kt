package com.editor.utils

import com.alex.loaders.fonts.CacheFonts
import com.alex.loaders.fonts.PixelImage
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField

/** Shows a text in every font of the cache, drawn the way the client draws it. */
object FontPreview {

    private const val DEFAULT_TEXT = "The quick brown fox <col=ff9040>jumps</col> over the lazy dog 0123456789"
    private const val WIDTH = 520
    private val BACKGROUND = Color(62, 53, 41)

    /** @return false when the cache has no fonts. */
    fun show(parent: Component?): Boolean {
        val ids = CacheFonts.ids()
        if (ids.isEmpty()) return false

        val rows = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = BACKGROUND
        }
        val textField = JTextField(DEFAULT_TEXT).apply {
            toolTipText = "Text with tags like <col=ff0000>, <shad=000000>, <u>, <str> and <br>. Enter to refresh."
        }

        fun refresh() {
            rows.removeAll()
            for (id in ids) rows.add(row(id, textField.text))
            rows.revalidate()
            rows.repaint()
        }
        textField.addActionListener { refresh() }
        refresh()

        val scroll = JScrollPane(rows).apply {
            preferredSize = Dimension(WIDTH + 130, 400)
            verticalScrollBar.unitIncrement = 16
        }
        val content = JPanel(BorderLayout(0, 4)).apply {
            add(textField, BorderLayout.NORTH)
            add(scroll, BorderLayout.CENTER)
        }
        JOptionPane.showMessageDialog(parent, content, "Font preview", JOptionPane.PLAIN_MESSAGE)
        return true
    }

    private fun row(id: Int, text: String): JPanel {
        val font = CacheFonts.get(id)!!
        val lines = font.splitParagraph(text, intArrayOf(WIDTH)).size
        val height = maxOf(20, lines * font.lineHeight + 8)

        val canvas = PixelImage(WIDTH, height)
        font.drawParagraph(canvas, text, 2, 2, WIDTH - 4, height - 4, 0xFF981F, 0, 0, 0, 0)
        val image = BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_ARGB)
        image.setRGB(0, 0, WIDTH, height, canvas.pixels, 0, WIDTH)

        return JPanel(BorderLayout(6, 0)).apply {
            background = BACKGROUND
            border = BorderFactory.createEmptyBorder(1, 4, 1, 4)
            add(JLabel("Font $id").apply {
                foreground = Color.WHITE
                preferredSize = Dimension(70, height)
            }, BorderLayout.WEST)
            add(JLabel(ImageIcon(image)), BorderLayout.CENTER)
            maximumSize = Dimension(Int.MAX_VALUE, height + 2)
        }
    }
}
