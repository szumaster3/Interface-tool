package rs2.sprites.utils

import java.awt.*
import javax.swing.*
import javax.swing.border.LineBorder

class SpriteTile(
    val spriteId: Int,
    image: Image
) : JPanel(BorderLayout()) {
    val spriteWidth: Int = image.getWidth(null)
    val spriteHeight: Int = image.getHeight(null)

    private val imgLabel = JLabel(ImageIcon(image))
    private val idLabel = JLabel(spriteId.toString(), JLabel.CENTER)
    private val sizeLabel = JLabel("${spriteWidth}×${spriteHeight}", JLabel.CENTER)

    var selected = false
        set(value) {
            field = value
            border = if (value)
                LineBorder(Color.WHITE, 1)
            else
                LineBorder(Color.DARK_GRAY, 1)
        }

    init {
        preferredSize = Dimension(64, 64)
        minimumSize = preferredSize
        maximumSize = preferredSize

        background = Color(55, 55, 55)
        border = LineBorder(Color.DARK_GRAY, 1)

        imgLabel.horizontalAlignment = JLabel.CENTER

        idLabel.foreground = Color.LIGHT_GRAY
        idLabel.font = Font("JetBrains Mono", Font.PLAIN, 11)

        sizeLabel.foreground = Color(160, 160, 160)
        sizeLabel.font = Font("JetBrains Mono", Font.PLAIN, 9)

        val infoPanel = JPanel(GridLayout(2, 1))
        infoPanel.isOpaque = false
        infoPanel.add(idLabel)
        infoPanel.add(sizeLabel)

        add(imgLabel, BorderLayout.CENTER)
        add(infoPanel, BorderLayout.SOUTH)
    }
}
