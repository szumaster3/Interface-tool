package rs2.sprites.utils

import java.awt.*
import javax.swing.*
import javax.swing.border.LineBorder

class SpriteTile(
    val spriteId: Int,
    image: Image
) : JPanel(BorderLayout()) {

    private val imgLabel = JLabel(ImageIcon(image))
    private val idLabel = JLabel(spriteId.toString(), JLabel.CENTER)

    var selected = false
        set(value) {
            field = value
            border = if (value)
                LineBorder(Color(255, 255, 255), 1)
            else
                LineBorder(Color.DARK_GRAY, 1)
        }

    init {
        preferredSize = Dimension(64, 64)
        background = Color(55, 55, 55)
        border = LineBorder(Color.DARK_GRAY, 1)

        imgLabel.horizontalAlignment = JLabel.CENTER
        idLabel.foreground = Color.LIGHT_GRAY
        idLabel.font = Font("JetBrains Mono", Font.PLAIN, 11)

        add(imgLabel, BorderLayout.CENTER)
        add(idLabel, BorderLayout.SOUTH)
    }
}