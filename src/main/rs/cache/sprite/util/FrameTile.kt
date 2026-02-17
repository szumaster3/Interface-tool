package rs.cache.sprite.util

import java.awt.*
import javax.swing.*
import javax.swing.border.LineBorder

class FrameTile(val frameId: Int, image: Image) : JPanel(BorderLayout())
{
    private val frameWidth: Int = image.getWidth(null)
    private val frameHeight: Int = image.getHeight(null)

    var selected = false
        set(value)
        {
            field = value
            border =
                if (value)
                {
                    LineBorder(Color.WHITE, 2)
                }
                else
                {
                    LineBorder(Color.DARK_GRAY, 1)
                }
        }

    init
    {
        preferredSize = Dimension(64, 64)
        minimumSize = preferredSize
        maximumSize = preferredSize
        background = Color(55, 55, 55)
        border = LineBorder(Color.DARK_GRAY, 1)

        val imgLabel = JLabel(ImageIcon(image))
        imgLabel.horizontalAlignment = JLabel.CENTER

        val infoLabel = JLabel("$frameId ($frameWidth×$frameHeight)", JLabel.CENTER)
        infoLabel.foreground = Color.RED
        infoLabel.font = Font("JetBrains Mono", Font.PLAIN, 12)

        add(imgLabel, BorderLayout.CENTER)
        add(infoLabel, BorderLayout.SOUTH)
    }
}
