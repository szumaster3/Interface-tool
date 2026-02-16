package ui

import cache.ComponentDefinition
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel

class RenderPanel : JPanel() {
    var interfaceId: Int = -1
    var showContainers = true
    var showHidden = true
    var showModels = true
    var viewMode = IfaceDraw.ViewMode.NONE
    var selectedType: Int? = null
    var selectedComponent: ComponentDefinition? = null
    private var backBuffer: BufferedImage? = null

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (interfaceId == -1) return
        if (backBuffer == null ||
            backBuffer!!.width != width ||
            backBuffer!!.height != height) {
            backBuffer = BufferedImage(UIConsts.CENTER_PANEL_WIDTH, UIConsts.DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        }
        val g2 = backBuffer!!.createGraphics()
        IfaceDraw.drawPreparedInterface(g2, showContainers, showHidden, showModels, viewMode, selectedType, selectedComponent)
        g2.dispose()
        g.drawImage(backBuffer, 0, 0, null)
    }
}
