package ui.theme

import java.awt.*
import javax.swing.*
import javax.swing.BorderFactory

class RoundedButton(text: String, iconPath: String? = null, private val iconPosition: IconPosition = IconPosition.CENTER) : JButton(text) {

    private var pngIcon: Image? = null

    enum class IconPosition {
        CENTER,
        LEFT
    }

    init {
        isContentAreaFilled = false
        isFocusPainted = false
        isOpaque = false

        border = BorderFactory.createEmptyBorder(8, 16, 8, 16)
        foreground = Color.BLACK
        background = Color.WHITE

        font = Font("JetBrains Mono", Font.BOLD, 10)

        horizontalTextPosition = CENTER
        verticalTextPosition = CENTER
        iconTextGap = 4

        isRolloverEnabled = true

        iconPath?.let { setIconFromPNG(it) }
    }

    fun setIconFromPNG(path: String) {
        pngIcon = ImageIcon(path).image
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val arc = 3
        val pressed = model.isPressed
        val hover = model.isRollover

        if (pressed) g2.translate(0, 1)

        val topColor =
            when {
                pressed -> Color(230, 230, 230)
                hover -> Color(255, 255, 255)
                else -> Color(255, 255, 255)
            }
        val bottomColor =
            when {
                pressed -> Color(200, 200, 200)
                hover -> Color(220, 220, 220)
                else -> Color(192, 192, 192)
            }

        val gradient = GradientPaint(0f, 0f, topColor, 0f, height.toFloat(), bottomColor)
        g2.paint = gradient
        g2.fillRoundRect(0, 0, width, height, arc, arc)

        drawBevel(g2, arc, pressed)

        super.paintComponent(g)

        pngIcon?.let {
            val iconWidth = it.getWidth(null)
            val iconHeight = it.getHeight(null)
            val y = (height - iconHeight) / 2
            val x =
                when (iconPosition) {
                    IconPosition.CENTER -> (width - iconWidth) / 2
                    IconPosition.LEFT -> 8
                }
            g2.drawImage(it, x, y, null)

            if (iconPosition == IconPosition.LEFT) {
                horizontalTextPosition = SwingConstants.RIGHT
                iconTextGap = iconWidth + 6
            } else {
                horizontalTextPosition = CENTER
                iconTextGap = 4
            }
        }

        if (pressed) g2.translate(0, -1)
    }

    private fun drawBevel(g2: Graphics2D, arc: Int, pressed: Boolean) {
        val bright = Color(255, 255, 255)
        val dark = Color(130, 130, 130)

        if (!pressed) {

            g2.color = bright
            g2.drawLine(1, 1, width - 2, 1)
            g2.drawLine(1, 1, 1, height - 2)

            g2.color = dark
            g2.drawLine(1, height - 2, width - 2, height - 2)
            g2.drawLine(width - 2, 1, width - 2, height - 2)
        } else {

            g2.color = dark
            g2.drawLine(1, 1, width - 2, 1)
            g2.drawLine(1, 1, 1, height - 2)

            g2.color = bright
            g2.drawLine(1, height - 2, width - 2, height - 2)
            g2.drawLine(width - 2, 1, width - 2, height - 2)
        }
    }

    override fun paintBorder(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val arc = 6
        g2.color =
            when {
                model.isPressed -> Color(120, 120, 120)
                model.isRollover -> Color(150, 150, 150)
                else -> Color(180, 180, 180)
            }

        g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc)
    }
}
