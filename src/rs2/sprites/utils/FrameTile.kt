package rs2.sprites.utils

import rs2.sprites.SpriteArchive
import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

class FrameTile(
    val frameId: Int,
    image: Image,
) : JPanel(BorderLayout()) {
    val frameWidth: Int = image.getWidth(null)
    val frameHeight: Int = image.getHeight(null)

    var selected = false
        set(value) {
            field = value
            border =
                if (value) {
                    LineBorder(Color.WHITE, 2)
                } else {
                    LineBorder(Color.DARK_GRAY, 1)
                }
        }

    init {
        preferredSize = Dimension(64, 64)
        minimumSize = preferredSize
        maximumSize = preferredSize
        background = Color(55, 55, 55)
        border = LineBorder(Color.DARK_GRAY, 1)

        val imgLabel = JLabel(ImageIcon(image))
        imgLabel.horizontalAlignment = JLabel.CENTER

        val infoLabel = JLabel("$frameId ($frameWidth×$frameHeight)", JLabel.CENTER)
        infoLabel.foreground = Color.LIGHT_GRAY
        infoLabel.font = Font("JetBrains Mono", Font.PLAIN, 10)

        add(imgLabel, BorderLayout.CENTER)
        add(infoLabel, BorderLayout.SOUTH)
    }
}
