package ui.draw

import rs.cache.ComponentDefinition
import ui.Consts
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel

class RenderPanel : JPanel() {
    var interfaceId: Int = -1
    var showContainers = true
    var showHidden = true
    var showModels = true
    var selectedComponent: ComponentDefinition? = null
    private var backBuffer: BufferedImage? = null

    init {
        preferredSize = Dimension(Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
        minimumSize   = Dimension(Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
        maximumSize   = Dimension(Consts.CENTER_PANEL_WIDTH, Consts.CENTER_PANEL_HEIGHT)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (interfaceId == -1) return


        if (backBuffer == null || backBuffer!!.width != width || backBuffer!!.height != height) {
            backBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        }

        val g2 = backBuffer!!.createGraphics()
        g2.clearRect(0, 0, width, height)


        InterfaceDraw.preparedRoots.forEach { comp ->
            InterfaceDraw.layoutComponent(comp, width, height)
        }

        InterfaceDraw.drawPreparedInterface(
            g2,
            showContainers,
            showHidden,
            showModels,
            InterfaceDraw.viewMode,
            InterfaceDraw.selectedType,
            selectedComponent
        )

        g2.dispose()
        g.drawImage(backBuffer, 0, 0, width, height, null)
    }
}